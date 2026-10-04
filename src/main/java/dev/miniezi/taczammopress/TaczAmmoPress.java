package dev.miniezi.taczammopress;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TaczAmmoPress.MODID)
public final class TaczAmmoPress {
    public static final String MODID = "tacz_ammo_press";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredBlock<AmmoPressBlock> AMMO_PRESS = BLOCKS.register("ammo_press", () -> new AmmoPressBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.5f).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> AMMO_PRESS_ITEM = ITEMS.registerSimpleBlockItem("ammo_press", AMMO_PRESS);
    public static final java.util.function.Supplier<BlockEntityType<AmmoPressBlockEntity>> AMMO_PRESS_BE = BLOCK_ENTITIES.register("ammo_press", () -> BlockEntityType.Builder.of(AmmoPressBlockEntity::new, AMMO_PRESS.get()).build(null));
    public static final java.util.function.Supplier<MenuType<AmmoPressMenu>> AMMO_PRESS_MENU = MENUS.register("ammo_press", () -> IMenuTypeExtension.create((id, inv, data) -> new AmmoPressMenu(id, inv, data.readBlockPos())));

    public TaczAmmoPress(IEventBus modBus) {
        BLOCKS.register(modBus); ITEMS.register(modBus); BLOCK_ENTITIES.register(modBus); MENUS.register(modBus);
        modBus.addListener(this::registerCapabilities); modBus.addListener(this::addCreative);
    }
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AMMO_PRESS_BE.get(), (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, AMMO_PRESS_BE.get(), (be, side) -> be.getEnergyStorage());
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(AMMO_PRESS_ITEM);
    }
}
