package dev.miniezi.taczammopress;

import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TaczAmmoPress.MODID, value = Dist.CLIENT)
public final class TaczAmmoPressClient {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TaczAmmoPress.AMMO_PRESS_MENU.get(), AmmoPressScreen::new);
    }
}
