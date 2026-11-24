package com.abril.bowsersfury.entity;

import com.abril.bowsersfury.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.levelgen.Heightmap;

public class FuryBowserEntity extends Monster {
    private int fireCooldown = 100;
    private int spikeCooldown = 180;
    private int roarCooldown = 260;
    private int rageTicks = 0;
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.bowsersfury.fury_bowser"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public FuryBowserEntity(EntityType<? extends Monster> type, Level level) { super(type, level); this.xpReward = 180; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1250.0)
                .add(Attributes.ATTACK_DAMAGE, 32.0)
                .add(Attributes.ARMOR, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 72.0);
    }

    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bossBar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossBar.removePlayer(player); }

    @Override protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this, level); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.08D, false));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public void tick() {
        super.tick();
        if (isInWater()) setAirSupply(getMaxAirSupply());
        if (!level().isClientSide()) bossBar.setProgress(Math.max(0.0F, getHealth() / getMaxHealth()));
        if (level().isClientSide()) {
            if (tickCount % 3 == 0) {
                for (int i = 0; i < 7; i++) {
                    double angle = (Math.PI * 2.0 * i / 7.0) + tickCount * 0.018;
                    double px = getX() + Math.cos(angle) * 3.4;
                    double pz = getZ() + Math.sin(angle) * 3.4;
                    int surface = level().getHeight(Heightmap.Types.MOTION_BLOCKING, net.minecraft.util.Mth.floor(px), net.minecraft.util.Mth.floor(pz));
                    level().addParticle(ParticleTypes.SQUID_INK, px, surface + 0.12, pz, 0, 0.025, 0);
                    if (i % 2 == 0) level().addParticle(ParticleTypes.DRAGON_BREATH, px, surface + 0.25, pz, 0, 0.018, 0);
                }
            }
            return;
        }
        if (fireCooldown > 0) fireCooldown--;
        if (spikeCooldown > 0) spikeCooldown--;
        if (roarCooldown > 0) roarCooldown--;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        double distance = distanceToSqr(target);
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (fireCooldown <= 0 && distance > 64.0) breatheFury(target);
        if (spikeCooldown <= 0 && distance > 16.0) summonSpikes(target);
        if (roarCooldown <= 0) {
            level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 5.0F, 0.65F);
            roarCooldown = 260 + random.nextInt(100);
            rageTicks = 35;
        }
        if (rageTicks > 0) rageTicks--;
    }

    private void breatheFury(LivingEntity target) {
        Vec3 direction = new Vec3(target.getX() - getX(), target.getEyeY() - (getY() + getBbHeight() * 0.68), target.getZ() - getZ()).normalize().scale(1.35);
        SmallFireball fireball = new SmallFireball(level(), this, direction);
        fireball.setPos(getX() + direction.x * 2.2, getY() + getBbHeight() * 0.68, getZ() + direction.z * 2.2);
        level().addFreshEntity(fireball);
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 4.0F, 0.55F);
        fireCooldown = rageTicks > 0 ? 45 : 78;
    }

    private void summonSpikes(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) return;
        RandomSource rng = getRandom();
        for (int i = 0; i < 7; i++) {
            int dx = rng.nextInt(13) - 6;
            int dz = rng.nextInt(13) - 6;
            BlockPos origin = BlockPos.containing(target.getX() + dx, target.getY() + 15 + rng.nextInt(5), target.getZ() + dz);
            if (server.isEmptyBlock(origin) && server.isEmptyBlock(origin.below())) {
                FallingBlockEntity spike = FallingBlockEntity.fall(server, origin, Blocks.POINTED_DRIPSTONE.defaultBlockState());
                spike.setHurtsEntities(3.0F, 3);
            }
        }
        level().playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 4.5F, 0.7F);
        spikeCooldown = rageTicks > 0 ? 95 : 145;
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && getHealth() < getMaxHealth() * 0.5F) rageTicks = 100;
        return hurt;
    }

    @Override public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.knockback(1.8F, getX() - living.getX(), getZ() - living.getZ());
            level.explode(this, living.getX(), living.getY(), living.getZ(), 1.5F, Level.ExplosionInteraction.MOB);
        }
        return hit;
    }

    @Override protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(level, new ItemStack(ModItems.FURY_SHARD.get(), 8 + random.nextInt(9)));
        spawnAtLocation(level, new ItemStack(ModItems.FURY_SCALE.get(), 3 + random.nextInt(4)));
        if (random.nextFloat() < 0.35F) spawnAtLocation(level, new ItemStack(ModItems.EMBER_CORE.get(), 1));
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return SoundEvents.ENDER_DRAGON_AMBIENT; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENDER_DRAGON_HURT; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.ENDER_DRAGON_DEATH; }
}
