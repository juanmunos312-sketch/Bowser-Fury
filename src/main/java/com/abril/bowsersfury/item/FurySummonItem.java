package com.abril.bowsersfury.item;

import com.abril.bowsersfury.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.EntitySpawnReason;

public class FurySummonItem extends Item {
    public FurySummonItem() { super(new Item.Properties().stacksTo(16)); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        if (!level.isClientSide()) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            var boss = ModEntities.FURY_BOWSER.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (boss != null) {
                boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getPlayer() == null ? 0 : context.getPlayer().getYRot(), 0);
                level.addFreshEntity(boss);
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
