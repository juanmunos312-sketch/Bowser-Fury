package com.abril.bowsersfury.registry;

import com.abril.bowsersfury.BowsersFury;
import com.abril.bowsersfury.item.FuryLauncherItem;
import com.abril.bowsersfury.item.GigaBellItem;
import com.abril.bowsersfury.item.FurySummonItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BowsersFury.MOD_ID);
    public static final RegistryObject<Item> FURY_SHARD = ITEMS.register("fury_shard", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMBER_CORE = ITEMS.register("ember_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FURY_SCALE = ITEMS.register("fury_scale", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GIGA_BELL = ITEMS.register("giga_bell", GigaBellItem::new);
    public static final RegistryObject<Item> FURY_LAUNCHER = ITEMS.register("fury_launcher", () -> new FuryLauncherItem(1.8F, 7.0F, 1));
    public static final RegistryObject<Item> INFERNO_CANNON = ITEMS.register("inferno_cannon", () -> new FuryLauncherItem(2.8F, 11.0F, 2));
    public static final RegistryObject<Item> TIDAL_CANNON = ITEMS.register("tidal_cannon", () -> new FuryLauncherItem(3.5F, 15.0F, 3));
    public static final RegistryObject<Item> FURY_SPAWNER = ITEMS.register("fury_spawner", FurySummonItem::new);
    private ModItems() {}
}
