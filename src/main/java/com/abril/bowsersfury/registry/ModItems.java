package com.abril.bowsersfury.registry;

import com.abril.bowsersfury.BowsersFury;
import com.abril.bowsersfury.item.FuryLauncherItem;
import com.abril.bowsersfury.item.GigaBellItem;
import com.abril.bowsersfury.item.FurySummonItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BowsersFury.MOD_ID);

    private static Item.Properties props(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BowsersFury.MOD_ID, name)));
    }

    public static final RegistryObject<Item> FURY_SHARD = ITEMS.register("fury_shard", () -> new Item(props("fury_shard")));
    public static final RegistryObject<Item> EMBER_CORE = ITEMS.register("ember_core", () -> new Item(props("ember_core")));
    public static final RegistryObject<Item> FURY_SCALE = ITEMS.register("fury_scale", () -> new Item(props("fury_scale")));
    public static final RegistryObject<Item> GIGA_BELL = ITEMS.register("giga_bell", () -> new GigaBellItem(props("giga_bell")));
    public static final RegistryObject<Item> FURY_LAUNCHER = ITEMS.register("fury_launcher", () -> new FuryLauncherItem(props("fury_launcher"), 1.8F, 7.0F, 1));
    public static final RegistryObject<Item> INFERNO_CANNON = ITEMS.register("inferno_cannon", () -> new FuryLauncherItem(props("inferno_cannon"), 2.8F, 11.0F, 2));
    public static final RegistryObject<Item> TIDAL_CANNON = ITEMS.register("tidal_cannon", () -> new FuryLauncherItem(props("tidal_cannon"), 3.5F, 15.0F, 3));
    public static final RegistryObject<Item> FURY_SPAWNER = ITEMS.register("fury_spawner", () -> new FurySummonItem(props("fury_spawner")));
    private ModItems() {}
}
