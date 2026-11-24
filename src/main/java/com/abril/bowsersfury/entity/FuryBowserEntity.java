package com.abril.bowsersfury.entity;

import com.abril.bowsersfury.registry.ModItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fury Bowser's server-authoritative boss AI and attack controller.
 * Fireballs and falling hazards are boss-managed effects rather than vanilla
 * fireball entities, so their damage and visuals continue to work underwater.
 */
public class FuryBowserEntity extends Monster {
    private int fireballCooldown = 90;
    private int spikeCooldown = 150;
    private int breathCooldown = 115;
    private int tailCooldown = 95;
    private int roarCooldown = 240;
    private boolean phaseTwo = false;
    private boolean finalMeteorStarted = false;

    private final List<ImpactHazard> impactHazards = new ArrayList<>();
    private final List<TravelingFireball> travelingFireballs = new ArrayList<>();
    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.translatable("entity.bowsersfury.fury_bowser"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public FuryBowserEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 180;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1250.0)
                .add(Attributes.ATTACK_DAMAGE, 32.0)
                .add(Attributes.ARMOR, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 72.0);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12D, false));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 56.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (isInWater()) {
            setAirSupply(getMaxAirSupply());
        }

        if (level().isClientSide()) {
            spawnClientAura();
            return;
        }

        if (!(level() instanceof ServerLevel server)) {
            return;
        }

        bossBar.setProgress(Math.max(0.0F, getHealth() / getMaxHealth()));
        tickCooldowns();
        tickImpactHazards(server);
        tickTravelingFireballs(server);

        if (!finalMeteorStarted && getHealth() <= getMaxHealth() * 0.20F) {
            finalMeteorStarted = true;
            startMeteorStorm(server, getTarget());
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        getLookControl().setLookAt(target, 35.0F, 35.0F);
        double distanceSqr = distanceToSqr(target);

        // Long-range attacks. In phase two the boss chains attacks much faster.
        if (fireballCooldown <= 0 && distanceSqr > 49.0D) {
            launchFuryFireball(server, target);
        }
        if (spikeCooldown <= 0 && distanceSqr > 16.0D) {
            summonFallingSpikes(server, target);
        }
        if (breathCooldown <= 0 && distanceSqr > 9.0D && distanceSqr < 625.0D) {
            breatheArea(server, target);
        }
        if (tailCooldown <= 0 && distanceSqr < 196.0D) {
            tailSwipe(server);
        }

        if (roarCooldown <= 0) {
            roar(server);
        }
    }

    private void tickCooldowns() {
        if (fireballCooldown > 0) fireballCooldown--;
        if (spikeCooldown > 0) spikeCooldown--;
        if (breathCooldown > 0) breathCooldown--;
        if (tailCooldown > 0) tailCooldown--;
        if (roarCooldown > 0) roarCooldown--;
    }

    private void spawnClientAura() {
        if (tickCount % 3 != 0) return;
        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2.0D * i / 12.0D) + tickCount * 0.018D;
            double radius = 5.2D;
            double px = getX() + Math.cos(angle) * radius;
            double pz = getZ() + Math.sin(angle) * radius;
            double py = getY() + 0.4D + (i % 3) * 0.55D;
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz,
                    Math.cos(angle) * 0.015D, 0.035D, Math.sin(angle) * 0.015D);
            if (i % 2 == 0) {
                level().addParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), px, py + 0.35D, pz,
                        0.0D, 0.012D, 0.0D);
            }
        }
    }

    /** Large, persistent fireball effect. It is not a vanilla projectile and cannot be extinguished by water. */
    private void launchFuryFireball(ServerLevel server, LivingEntity target) {
        Vec3 origin = new Vec3(getX(), getY() + getBbHeight() * 0.62D, getZ());
        Vec3 aim = target.position().add(0.0D, target.getBbHeight() * 0.48D, 0.0D);
        Vec3 direction = aim.subtract(origin).normalize();
        if (direction.lengthSqr() < 1.0E-5D) return;

        travelingFireballs.add(new TravelingFireball(
                origin.add(direction.scale(3.0D)), direction.scale(1.35D),
                phaseTwo ? 38 : 32, phaseTwo ? 42.0F : 30.0F
        ));

        server.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 28,
                0.8D, 0.8D, 0.8D, 0.12D);
        server.sendParticles(ParticleTypes.LAVA, origin.x, origin.y, origin.z, 8,
                0.45D, 0.45D, 0.45D, 0.04D);
        server.playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT,
                SoundSource.HOSTILE, 6.0F, 0.48F);
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT,
                SoundSource.HOSTILE, 4.0F, 0.65F);
        fireballCooldown = phaseTwo ? 38 : 68;
    }

    private void tickTravelingFireballs(ServerLevel server) {
        Iterator<TravelingFireball> iterator = travelingFireballs.iterator();
        while (iterator.hasNext()) {
            TravelingFireball fireball = iterator.next();
            fireball.position = fireball.position.add(fireball.velocity);
            fireball.ticksLeft--;

            double x = fireball.position.x;
            double y = fireball.position.y;
            double z = fireball.position.z;
            server.sendParticles(ParticleTypes.FLAME, x, y, z, 16,
                    0.9D, 0.9D, 0.9D, 0.10D);
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 7,
                    0.55D, 0.55D, 0.55D, 0.06D);
            server.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), x, y, z, 5,
                    0.45D, 0.45D, 0.45D, 0.025D);

            AABB blastBox = new AABB(x - 2.6D, y - 2.6D, z - 2.6D,
                    x + 2.6D, y + 2.6D, z + 2.6D);
            List<LivingEntity> hits = server.getEntitiesOfClass(LivingEntity.class, blastBox,
                    entity -> entity != this && entity.isAlive());
            if (!hits.isEmpty()) {
                for (LivingEntity hit : hits) {
                    hit.hurtServer(server, server.damageSources().mobAttack(this), fireball.damage);
                    Vec3 push = hit.position().subtract(fireball.position).normalize().scale(1.05D);
                    hit.setDeltaMovement(hit.getDeltaMovement().add(push.x, 0.48D, push.z));
                }
                fireballImpact(server, fireball.position, 2.8D);
                iterator.remove();
                continue;
            }

            if (fireball.ticksLeft <= 0) {
                fireballImpact(server, fireball.position, 2.0D);
                iterator.remove();
            }
        }
    }

    private void fireballImpact(ServerLevel server, Vec3 position, double radius) {
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                position.x, position.y, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.LAVA, position.x, position.y, position.z, 24,
                radius * 0.45D, radius * 0.35D, radius * 0.45D, 0.08D);
        server.sendParticles(ParticleTypes.FLAME, position.x, position.y, position.z, 36,
                radius, radius * 0.65D, radius, 0.12D);
        server.playSound(null, position.x, position.y, position.z,
                SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 5.5F, 0.58F);
    }

    /** Schedules spike-like falling strikes with a visible warning ring at the impact point. */
    private void summonFallingSpikes(ServerLevel server, LivingEntity target) {
        int count = phaseTwo ? 10 : 7;
        for (int i = 0; i < count; i++) {
            double offsetX = getRandom().nextInt(19) - 9.0D;
            double offsetZ = getRandom().nextInt(19) - 9.0D;
            double impactX = target.getX() + offsetX;
            double impactZ = target.getZ() + offsetZ;
            double impactY = target.isInWater()
                    ? target.getY() + 0.15D
                    : Math.max(target.getY(), server.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                            net.minecraft.core.BlockPos.containing(impactX, target.getY(), impactZ)).getY());
            impactHazards.add(new ImpactHazard(impactX, impactY, impactZ,
                    22 + getRandom().nextInt(7), false,
                    phaseTwo ? 30.0F : 22.0F, phaseTwo ? 3.8D : 3.1D));
        }

        server.playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT,
                SoundSource.HOSTILE, 5.0F, 0.62F);
        server.playSound(null, blockPosition(), SoundEvents.BREEZE_CHARGE,
                SoundSource.HOSTILE, 3.0F, 0.72F);
        spikeCooldown = phaseTwo ? 72 : 128;
    }

    private void tickImpactHazards(ServerLevel server) {
        Iterator<ImpactHazard> iterator = impactHazards.iterator();
        while (iterator.hasNext()) {
            ImpactHazard hazard = iterator.next();
            hazard.ticksLeft--;
            double fallStart = hazard.impactY + (hazard.meteor ? 34.0D : 25.0D);
            double fallProgress = 1.0D - Math.max(0, hazard.ticksLeft) / (double) hazard.totalTicks;
            double fallingY = fallStart - fallProgress * (hazard.meteor ? 34.0D : 25.0D);

            if (hazard.ticksLeft % 2 == 0) {
                server.sendParticles(hazard.meteor ? ParticleTypes.FLAME : ParticleTypes.END_ROD,
                        hazard.x, fallingY, hazard.z, hazard.meteor ? 12 : 9,
                        0.35D, 0.75D, 0.35D, hazard.meteor ? 0.08D : 0.025D);
                server.sendParticles(hazard.meteor ? ParticleTypes.LAVA : ParticleTypes.CRIT,
                        hazard.x, fallingY, hazard.z, hazard.meteor ? 5 : 4,
                        0.22D, 0.45D, 0.22D, 0.04D);
                if (!hazard.meteor) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
                                    Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                            hazard.x, fallingY, hazard.z, 5, 0.18D, 0.55D, 0.18D, 0.025D);
                }
            }

            // The ring is deliberately drawn at the victim's feet / water level, not in an air-only block.
            if (hazard.ticksLeft > 0 && hazard.ticksLeft % 4 == 0) {
                spawnWarningRing(server, hazard);
            }

            if (hazard.ticksLeft <= 0) {
                impactHazard(server, hazard);
                iterator.remove();
            }
        }
    }

    private void spawnWarningRing(ServerLevel server, ImpactHazard hazard) {
        int points = hazard.meteor ? 18 : 14;
        double radius = hazard.radius;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            double x = hazard.x + Math.cos(angle) * radius;
            double z = hazard.z + Math.sin(angle) * radius;
            server.sendParticles(hazard.meteor ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    x, hazard.impactY + 0.12D, z, 1, 0.015D, 0.035D, 0.015D, 0.0D);
        }
    }

    private void impactHazard(ServerLevel server, ImpactHazard hazard) {
        Vec3 center = new Vec3(hazard.x, hazard.impactY + 0.3D, hazard.z);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(hazard.meteor ? ParticleTypes.LAVA : ParticleTypes.CRIT,
                center.x, center.y, center.z, hazard.meteor ? 34 : 24,
                hazard.radius * 0.55D, 0.8D, hazard.radius * 0.55D, 0.08D);
        server.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 28,
                hazard.radius * 0.65D, 0.5D, hazard.radius * 0.65D, 0.10D);

        AABB damageArea = new AABB(hazard.x - hazard.radius, hazard.impactY - 2.8D, hazard.z - hazard.radius,
                hazard.x + hazard.radius, hazard.impactY + 3.2D, hazard.z + hazard.radius);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, damageArea,
                entity -> entity != this && entity.isAlive())) {
            DamageSource source = hazard.meteor
                    ? server.damageSources().mobAttack(this)
                    : server.damageSources().fallingStalactite(this);
            victim.hurtServer(server, source, hazard.damage);
            Vec3 knockback = victim.position().subtract(center).normalize().scale(hazard.meteor ? 1.25D : 0.8D);
            victim.setDeltaMovement(victim.getDeltaMovement().add(knockback.x, hazard.meteor ? 0.72D : 0.45D, knockback.z));
        }

        server.playSound(null, center.x, center.y, center.z,
                hazard.meteor ? SoundEvents.DRAGON_FIREBALL_EXPLODE : SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.HOSTILE, hazard.meteor ? 6.0F : 4.5F, hazard.meteor ? 0.5F : 0.72F);
    }

    /** A short cone of fire that damages entities in front of the boss, including underwater targets. */
    private void breatheArea(ServerLevel server, LivingEntity target) {
        Vec3 origin = new Vec3(getX(), getY() + getBbHeight() * 0.60D, getZ());
        Vec3 direction = target.position().add(0.0D, target.getBbHeight() * 0.45D, 0.0D)
                .subtract(origin).normalize();
        if (direction.lengthSqr() < 1.0E-5D) return;

        for (int step = 1; step <= 14; step++) {
            Vec3 point = origin.add(direction.scale(step * 1.15D));
            double spread = 0.65D + step * 0.24D;
            server.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 12,
                    spread, spread * 0.72D, spread, 0.06D);
            if (step % 2 == 0) {
                server.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), point.x, point.y, point.z, 5,
                        spread * 0.55D, spread * 0.45D, spread * 0.55D, 0.025D);
            }
        }

        AABB search = getBoundingBox().inflate(19.0D, 10.0D, 19.0D);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, search,
                entity -> entity != this && entity.isAlive())) {
            Vec3 toVictim = victim.position().add(0.0D, victim.getBbHeight() * 0.45D, 0.0D).subtract(origin);
            double along = toVictim.dot(direction);
            if (along < 1.0D || along > 17.0D) continue;
            double sideDistance = toVictim.subtract(direction.scale(along)).length();
            if (sideDistance <= 2.5D + along * 0.22D) {
                victim.hurtServer(server, server.damageSources().mobAttack(this), phaseTwo ? 38.0F : 27.0F);
                Vec3 push = direction.scale(0.75D);
                victim.setDeltaMovement(victim.getDeltaMovement().add(push.x, 0.35D, push.z));
            }
        }

        server.playSound(null, blockPosition(), SoundEvents.BLAZE_BURN,
                SoundSource.HOSTILE, 5.5F, 0.55F);
        server.playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE,
                SoundSource.HOSTILE, 4.0F, 0.62F);
        breathCooldown = phaseTwo ? 52 : 92;
    }

    /** A broad rearward sweep, matching the boss's oversized armored tail. */
    private void tailSwipe(ServerLevel server) {
        Vec3 backward = getViewVector(1.0F).scale(-1.0D).normalize();
        Vec3 center = position().add(backward.scale(5.5D)).add(0.0D, getBbHeight() * 0.38D, 0.0D);
        AABB sweep = new AABB(center.x - 8.0D, center.y - 4.5D, center.z - 8.0D,
                center.x + 8.0D, center.y + 4.5D, center.z + 8.0D);

        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, sweep,
                entity -> entity != this && entity.isAlive())) {
            Vec3 offset = victim.position().subtract(position());
            double behind = offset.dot(backward);
            if (behind < 0.5D || offset.lengthSqr() > 225.0D) continue;
            victim.hurtServer(server, server.damageSources().mobAttack(this), phaseTwo ? 48.0F : 36.0F);
            Vec3 push = backward.scale(1.55D);
            victim.setDeltaMovement(victim.getDeltaMovement().add(push.x, 0.85D, push.z));
        }

        server.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z,
                18, 5.5D, 2.0D, 5.5D, 0.0D);
        server.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z,
                30, 6.0D, 2.0D, 6.0D, 0.12D);
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_FLAP,
                SoundSource.HOSTILE, 5.5F, 0.55F);
        server.playSound(null, blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.HOSTILE, 3.5F, 0.8F);
        tailCooldown = phaseTwo ? 48 : 78;
    }

    private void roar(ServerLevel server) {
        server.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + getBbHeight() * 0.55D,
                getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + getBbHeight() * 0.7D,
                getZ(), 45, 4.0D, 4.0D, 4.0D, 0.12D);
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 7.0F, phaseTwo ? 0.48F : 0.62F);
        server.playSound(null, blockPosition(), SoundEvents.WITHER_AMBIENT,
                SoundSource.HOSTILE, 4.0F, 0.65F);
        roarCooldown = phaseTwo ? 150 : 235 + getRandom().nextInt(65);
    }

    private void startMeteorStorm(ServerLevel server, LivingEntity target) {
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 8.0F, 0.42F);
        server.playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.HOSTILE, 6.0F, 0.65F);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + getBbHeight(), getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);

        double centerX = target != null ? target.getX() : getX();
        double centerZ = target != null ? target.getZ() : getZ();
        double centerY = target != null ? target.getY() : getY();
        for (int i = 0; i < 13; i++) {
            double angle = Math.PI * 2.0D * i / 13.0D;
            double radius = 2.0D + getRandom().nextDouble() * 9.0D;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            double y = target != null && target.isInWater()
                    ? centerY + 0.15D
                    : Math.max(centerY, server.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                            net.minecraft.core.BlockPos.containing(x, centerY, z)).getY());
            impactHazards.add(new ImpactHazard(x, y, z, 24 + getRandom().nextInt(15), true,
                    52.0F, 5.0D));
        }
    }

    private void enterPhaseTwo(ServerLevel server) {
        if (phaseTwo) return;
        phaseTwo = true;
        fireballCooldown = Math.min(fireballCooldown, 18);
        spikeCooldown = Math.min(spikeCooldown, 28);
        breathCooldown = Math.min(breathCooldown, 24);
        tailCooldown = Math.min(tailCooldown, 24);
        roarCooldown = 35;

        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                getX(), getY() + getBbHeight() * 0.5D, getZ(), 2,
                1.5D, 1.5D, 1.5D, 0.0D);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                getX(), getY() + getBbHeight() * 0.5D, getZ(), 100,
                6.0D, 7.0D, 6.0D, 0.16D);
        server.sendParticles(ParticleTypes.LAVA,
                getX(), getY() + getBbHeight() * 0.5D, getZ(), 35,
                4.0D, 4.0D, 4.0D, 0.08D);
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 8.0F, 0.45F);
        server.playSound(null, blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.HOSTILE, 6.0F, 0.55F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !phaseTwo && getHealth() <= getMaxHealth() * 0.5F) {
            enterPhaseTwo(level);
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            Vec3 away = living.position().subtract(position()).normalize().scale(0.9D);
            living.setDeltaMovement(living.getDeltaMovement().add(away.x, 0.45D, away.z));
            level.sendParticles(ParticleTypes.CRIT, living.getX(), living.getY() + 1.0D,
                    living.getZ(), 12, 0.55D, 0.65D, 0.55D, 0.08D);
        }
        return hit;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(level, new ItemStack(ModItems.FURY_SHARD.get(), 8 + random.nextInt(9)));
        spawnAtLocation(level, new ItemStack(ModItems.FURY_SCALE.get(), 3 + random.nextInt(4)));
        if (random.nextFloat() < 0.35F) {
            spawnAtLocation(level, new ItemStack(ModItems.EMBER_CORE.get(), 1));
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    private static final class ImpactHazard {
        private final double x;
        private final double impactY;
        private final double z;
        private final int totalTicks;
        private final boolean meteor;
        private final float damage;
        private final double radius;
        private int ticksLeft;

        private ImpactHazard(double x, double impactY, double z, int ticks,
                             boolean meteor, float damage, double radius) {
            this.x = x;
            this.impactY = impactY;
            this.z = z;
            this.totalTicks = ticks;
            this.ticksLeft = ticks;
            this.meteor = meteor;
            this.damage = damage;
            this.radius = radius;
        }
    }

    private static final class TravelingFireball {
        private Vec3 position;
        private final Vec3 velocity;
        private int ticksLeft;
        private final float damage;

        private TravelingFireball(Vec3 position, Vec3 velocity, int ticksLeft, float damage) {
            this.position = position;
            this.velocity = velocity;
            this.ticksLeft = ticksLeft;
            this.damage = damage;
        }
    }
}
