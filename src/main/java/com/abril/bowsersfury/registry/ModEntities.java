package com.abril.bowsersfury.registry;

import com.abril.bowsersfury.BowsersFury;
import com.abril.bowsersfury.entity.FuryBowserEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BowsersFury.MOD_ID);

    public static final RegistryObject<EntityType<FuryBowserEntity>> FURY_BOWSER = ENTITY_TYPES.register("fury_bowser",
            () -> EntityType.Builder.of(FuryBowserEntity::new, MobCategory.MONSTER)
                    .sized(29.0F, 40.0F).clientTrackingRange(12).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BowsersFury.MOD_ID, "fury_bowser"))));

    private ModEntities() {}
}
