package com.abril.bowsersfury;

import com.abril.bowsersfury.client.ModClientEvents;
import com.abril.bowsersfury.event.FuryCombatEvents;
import com.abril.bowsersfury.event.ModCommonEvents;
import com.abril.bowsersfury.registry.ModCreativeTabs;
import com.abril.bowsersfury.registry.ModEntities;
import com.abril.bowsersfury.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod(BowsersFury.MOD_ID)
public final class BowsersFury {
    public static final String MOD_ID = "bowsersfury";

    public BowsersFury(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        ModCreativeTabs.TABS.register(bus);

        SpawnPlacementRegisterEvent.BUS.addListener(ModCommonEvents::spawnPlacements);
        EntityAttributeCreationEvent.BUS.addListener(ModCommonEvents::attributes);
        TickEvent.PlayerTickEvent.Post.BUS.addListener(FuryCombatEvents::gigaTimer);
        LivingHurtEvent.BUS.addListener(FuryCombatEvents::bossDamage);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ModClientEvents::register);
    }
}
