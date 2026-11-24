package com.abril.bowsersfury.item;

import net.minecraft.resources.Identifier;
import com.abril.bowsersfury.event.FuryCombatEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GigaBellItem extends Item {
    private static final Identifier SIZE_ID = Identifier.fromNamespaceAndPath("bowsersfury", "giga_size");
    private static final Identifier POWER_ID = Identifier.fromNamespaceAndPath("bowsersfury", "giga_power");

    public GigaBellItem() { super(new Item.Properties().stacksTo(1).durability(3)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            var scale = player.getAttribute(Attributes.SCALE);
            var attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (scale != null && attack != null) {
                scale.removeModifier(SIZE_ID);
                attack.removeModifier(POWER_ID);
                scale.addTransientModifier(new AttributeModifier(SIZE_ID, 1.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                attack.addTransientModifier(new AttributeModifier(POWER_ID, 2.75, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                FuryCombatEvents.startGiga(player, 20 * 45);
                player.getFoodData().eat(20, 1.0F);
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 12.0F));
                player.getCooldowns().addCooldown(this, 20 * 60);
                if (!player.getAbilities().instabuild) stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
