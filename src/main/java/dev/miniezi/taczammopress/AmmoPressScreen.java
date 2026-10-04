package dev.miniezi.taczammopress;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AmmoPressScreen extends AbstractContainerScreen<AmmoPressMenu> {
    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 208;
        imageHeight = 195;
        inventoryLabelY = 101;
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x=leftPos, y=topPos;
        g.fill(x, y, x+imageWidth, y+imageHeight, 0xFF20252B);
        g.fill(x+5, y+18, x+203, y+96, 0xFF2B323A);
        g.fill(x+7, y+103, x+201, y+191, 0xFF171B20);

        panel(g,x+11,y+27,38,47,0xFF34404A);
        panel(g,x+59,y+27,66,47,0xFF34404A);
        panel(g,x+151,y+18,38,65,0xFF34404A);

        slot(g,x+19,y+41,0xFF57B7D9);
        for(int row=0;row<2;row++)for(int col=0;col<3;col++)slot(g,x+67+col*18,y+32+row*18,0xFFE0A84B);
        for(int row=0;row<3;row++)slot(g,x+159,y+23+row*18,0xFF64C97B);

        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+22+col*18,y+112+row*18,0xFF59616A);
        for(int col=0;col<9;col++)slot(g,x+22+col*18,y+170,0xFF59616A);

        g.fill(x+12,y+82,x+145,y+90,0xFF111418);
        int energy=menu.energyScaled(131);
        if(energy>0)g.fill(x+13,y+83,x+13+energy,y+89,0xFF35BDEB);

        g.fill(x+151,y+86,x+190,y+92,0xFF111418);
        int progress=menu.progressScaled(37);
        if(progress>0)g.fill(x+152,y+87,x+152+progress,y+91,0xFF72D987);
    }

    private static void panel(GuiGraphics g,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+h,0xFF111418);g.fill(x+1,y+1,x+w-1,y+h-1,color);}
    private static void slot(GuiGraphics g,int x,int y,int accent){g.fill(x,y,x+18,y+18,accent);g.fill(x+1,y+1,x+17,y+17,0xFF101419);g.fill(x+2,y+2,x+16,y+16,0xFF2A3036);}

    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY) {
        g.drawString(font,title,8,6,0xFFE8EEF2,false);
        g.drawString(font,Component.translatable("gui.tacz_ammo_press.template"),10,19,0xFF8EDCF4,false);
        g.drawString(font,Component.translatable("gui.tacz_ammo_press.resources"),59,19,0xFFF0C36B,false);
        g.drawString(font,Component.translatable("gui.tacz_ammo_press.output"),151,10,0xFF82E49A,false);
        g.drawString(font,Component.translatable("gui.tacz_ammo_press.energy",menu.energy(),menu.capacity()),12,72,0xFFB8C4CC,false);
        g.drawString(font,playerInventoryTitle,23,101,0xFFB8C4CC,false);
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick){renderBackground(g,mouseX,mouseY,partialTick);super.render(g,mouseX,mouseY,partialTick);renderTooltip(g,mouseX,mouseY);}
}
