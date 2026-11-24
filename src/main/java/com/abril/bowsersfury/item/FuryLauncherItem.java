package com.abril.bowsersfury.item;

import com.abril.bowsersfury.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FuryLauncherItem extends Item {
    private final float speed;
    private final float damage;
    private final int shardCost;

    public FuryLauncherItem(float speed, float damage, int shardCost) {
        super(new Item.Properties().stacksTo(1).durability(420));
        this.speed = speed;
        this.damage = damage;
        this.shardCost = shardCost;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild && countShards(player) < shardCost) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle().normalize().scale(speed);
            SmallFireball shot = new SmallFireball(level, player, look);
            shot.setPos(player.getX() + look.x * 1.5, player.getEyeY() - 0.15 + look.y * 1.5, player.getZ() + look.z * 1.5);
            shot.igniteForSeconds(2.0F);
            level.addFreshEntity(shot);
            if (!player.getAbilities().instabuild) {
                int remaining = shardCost;
                for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                    ItemStack ammo = player.getInventory().getItem(i);
                    if (ammo.is(ModItems.FURY_SHARD.get())) { int used = Math.min(remaining, ammo.getCount()); ammo.shrink(used); remaining -= used; }
                }
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
            player.getCooldowns().addCooldown(this, 12);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private int countShards(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item.is(ModItems.FURY_SHARD.get())) count += item.getCount();
        }
        return count;
    }

    public float getBossDamage() { return damage; }
    public int getShardCost() { return shardCost; }
}
