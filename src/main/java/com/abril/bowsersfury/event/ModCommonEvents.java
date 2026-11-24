package com.abril.bowsersfury.event;

import com.abril.bowsersfury.registry.ModEntities;
import com.abril.bowsersfury.entity.FuryBowserEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

public final class ModCommonEvents {
    public static void spawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.FURY_BOWSER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getFluidState(pos).is(Fluids.WATER) && pos.getY() > 35,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FURY_BOWSER.get(), FuryBowserEntity.createAttributes().build());
    }

    private ModCommonEvents() {}
}
