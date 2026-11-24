package com.abril.bowsersfury.event;

import com.abril.bowsersfury.BowsersFury;
import com.abril.bowsersfury.entity.FuryBowserEntity;
import com.abril.bowsersfury.item.FuryLauncherItem;
import com.abril.bowsersfury.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FuryCombatEvents {
    private static final Identifier SIZE_ID = Identifier.fromNamespaceAndPath(BowsersFury.MOD_ID, "giga_size");
    private static final Identifier POWER_ID = Identifier.fromNamespaceAndPath(BowsersFury.MOD_ID, "giga_power");
    private static final Map<UUID, Integer> GIGA_TICKS = new ConcurrentHashMap<>();

    public static void startGiga(Player player, int durationTicks) {
        GIGA_TICKS.put(player.getUUID(), durationTicks);
    }

    public static void bossDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof FuryBowserEntity)) return;
        if (event.getSource().getEntity() instanceof Player player) {
            var main = player.getMainHandItem().getItem();
            var off = player.getOffhandItem().getItem();
            var held = main instanceof FuryLauncherItem ? main : off;
            if (event.getSource().getDirectEntity() instanceof SmallFireball && held instanceof FuryLauncherItem launcher) {
                event.setAmount(Math.max(event.getAmount(), launcher.getBossDamage()));
            }
            if (main == ModItems.GIGA_BELL.get() || off == ModItems.GIGA_BELL.get()) event.setAmount(event.getAmount() * 3.0F);
        }
    }

    public static void gigaTimer(TickEvent.PlayerTickEvent.Post event) {
        Player player = event.player();
        if (player.level().isClientSide()) return;
        Integer remaining = GIGA_TICKS.get(player.getUUID());
        if (remaining != null) {
            int ticks = remaining - 1;
            if (ticks <= 0) {
                GIGA_TICKS.remove(player.getUUID());
                var scale = player.getAttribute(Attributes.SCALE);
                var attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
                if (scale != null) scale.removeModifier(SIZE_ID);
                if (attack != null) attack.removeModifier(POWER_ID);
            } else {
                GIGA_TICKS.put(player.getUUID(), ticks);
            }
        }
    }
}
