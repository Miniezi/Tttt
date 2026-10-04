package dev.miniezi.taczammopress;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AmmoPressMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public AmmoPressMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, getContainer(inventory, pos), new SimpleContainerData(3));
    }

    public AmmoPressMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(TaczAmmoPress.AMMO_PRESS_MENU.get(), id);
        checkContainerSize(container, 10);
        this.container = container;
        this.data = data;
        container.startOpen(inventory.player);

        addSlot(new Slot(container, 0, 20, 42));
        for (int row=0; row<2; row++) for (int col=0; col<3; col++) addSlot(new Slot(container, 1 + row*3 + col, 68 + col*18, 33 + row*18));
        for (int row=0; row<3; row++) addSlot(new Slot(container, 7 + row, 160, 24 + row*18) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        for (int row=0; row<3; row++) for (int col=0; col<9; col++) addSlot(new Slot(inventory, col + row*9 + 9, 23 + col*18, 113 + row*18));
        for (int col=0; col<9; col++) addSlot(new Slot(inventory, col, 23 + col*18, 171));
        addDataSlots(data);
    }

    private static Container getContainer(Inventory inv, BlockPos pos) {
        if (inv.player.level().getBlockEntity(pos) instanceof AmmoPressBlockEntity press) return press;
        throw new IllegalStateException("Ammo Press block entity missing at " + pos);
    }

    public int progressScaled(int pixels) { return data.get(0) * pixels / 100; }
    public int energyScaled(int pixels) { return data.get(1) * pixels / Math.max(1, data.get(2)); }
    public int energy() { return data.get(1); }
    public int capacity() { return data.get(2); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index < 10) {
            if (!moveItemStackTo(original, 10, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (container.canPlaceItem(0, original)) {
                if (!moveItemStackTo(original, 0, 1, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(original, 1, 7, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
