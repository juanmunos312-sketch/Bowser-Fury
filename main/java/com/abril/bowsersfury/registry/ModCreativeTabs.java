package com.abril.bowsersfury.registry;

import com.abril.bowsersfury.BowsersFury;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BowsersFury.MOD_ID);
    public static final RegistryObject<CreativeModeTab> BOWSERS_FURY = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bowsersfury"))
            .icon(() -> new ItemStack(ModItems.GIGA_BELL.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.FURY_SHARD.get()); output.accept(ModItems.EMBER_CORE.get());
                output.accept(ModItems.FURY_SCALE.get()); output.accept(ModItems.GIGA_BELL.get());
                output.accept(ModItems.FURY_LAUNCHER.get()); output.accept(ModItems.INFERNO_CANNON.get());
                output.accept(ModItems.TIDAL_CANNON.get()); output.accept(ModItems.FURY_SPAWNER.get());
            }).build());
    private ModCreativeTabs() {}
}
