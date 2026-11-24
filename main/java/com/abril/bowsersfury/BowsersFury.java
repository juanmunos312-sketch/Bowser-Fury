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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BowsersFury.MOD_ID)
public final class BowsersFury {
    public static final String MOD_ID = "bowsersfury";

    public BowsersFury(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        ModItems.ITEMS.register(modBusGroup);
        ModEntities.ENTITY_TYPES.register(modBusGroup);
        ModCreativeTabs.TABS.register(modBusGroup);

        SpawnPlacementRegisterEvent.getBus(modBusGroup).addListener(ModCommonEvents::spawnPlacements);
        EntityAttributeCreationEvent.getBus(modBusGroup).addListener(ModCommonEvents::attributes);
        TickEvent.PlayerTickEvent.Post.BUS.addListener(FuryCombatEvents::gigaTimer);
        LivingHurtEvent.BUS.addListener(FuryCombatEvents::bossDamage);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModClientEvents.register(context);
        }
    }
}
