package dev.miniezi.taczammopress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AmmoPressBlockEntity extends BlockEntity implements Container, net.minecraft.world.MenuProvider {
    private static final int TEMPLATE=0, INPUT_FIRST=1, INPUT_LAST=6, OUTPUT_FIRST=7, OUTPUT_LAST=9;
    private static final int PROCESS_TICKS=100, ENERGY_PER_TICK=50;
    private int progress=0;
    private final ItemStackHandler items=new ItemStackHandler(10) {
        @Override protected void onContentsChanged(int slot){setChanged();}
        @Override public boolean isItemValid(int slot,ItemStack stack){if(slot==TEMPLATE)return isTaczAmmo(stack);return slot>=INPUT_FIRST&&slot<=INPUT_LAST;}
        @Override public int getSlotLimit(int slot){return slot==TEMPLATE?1:super.getSlotLimit(slot);}
    };
    private final EnergyStorage energy=new EnergyStorage(100000,10000,10000){
        @Override public int receiveEnergy(int maxReceive,boolean simulate){int r=super.receiveEnergy(maxReceive,simulate);if(r>0&&!simulate)setChanged();return r;}
    };
    private final SideMode[] sideModes = {SideMode.INPUT, SideMode.OUTPUT, SideMode.NONE, SideMode.ENERGY, SideMode.NONE, SideMode.NONE};
    private final IItemHandler inputAutomation=new IItemHandler(){
        private int real(int slot){return slot+INPUT_FIRST;}
        @Override public int getSlots(){return 6;}
        @Override public ItemStack getStackInSlot(int slot){return items.getStackInSlot(real(slot));}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return items.insertItem(real(slot),stack,simulate);}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return ItemStack.EMPTY;}
        @Override public int getSlotLimit(int slot){return items.getSlotLimit(real(slot));}
        @Override public boolean isItemValid(int slot,ItemStack stack){return items.isItemValid(real(slot),stack);}
    };
    private final IItemHandler outputAutomation=new IItemHandler(){
        private int real(int slot){return slot+OUTPUT_FIRST;}
        @Override public int getSlots(){return 3;}
        @Override public ItemStack getStackInSlot(int slot){return items.getStackInSlot(real(slot));}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return items.extractItem(real(slot),amount,simulate);}
        @Override public int getSlotLimit(int slot){return items.getSlotLimit(real(slot));}
        @Override public boolean isItemValid(int slot,ItemStack stack){return false;}
    };
    private final IEnergyStorage energyInput=new IEnergyStorage(){
        @Override public int receiveEnergy(int maxReceive,boolean simulate){return energy.receiveEnergy(maxReceive,simulate);}
        @Override public int extractEnergy(int maxExtract,boolean simulate){return 0;}
        @Override public int getEnergyStored(){return energy.getEnergyStored();}
        @Override public int getMaxEnergyStored(){return energy.getMaxEnergyStored();}
        @Override public boolean canExtract(){return false;}
        @Override public boolean canReceive(){return true;}
    };
    private record Material(String tag,int count){}
    private record AmmoRecipe(int output,List<Material> materials){}
    private static Material m(String tag,int count){return new Material(tag,count);}
    private static AmmoRecipe r(int output,Material... materials){return new AmmoRecipe(output,List.of(materials));}
    private static final Map<String,AmmoRecipe> RECIPES=new LinkedHashMap<>();
    static{
        RECIPES.put("tacz:12g",r(18,m("c:ingots/copper",15),m("c:gunpowders",6),m("c:nuggets/iron",18)));
        RECIPES.put("tacz:22wmr",r(100,m("c:ingots/copper",10),m("c:gunpowders",2)));
        RECIPES.put("tacz:308",r(60,m("c:ingots/copper",30),m("c:gunpowders",10),m("c:gems/lapis",1)));
        RECIPES.put("tacz:30_06",r(32,m("c:ingots/copper",20),m("c:gunpowders",6)));
        RECIPES.put("tacz:338",r(18,m("c:ingots/copper",25),m("c:gunpowders",8),m("c:gems/lapis",4)));
        RECIPES.put("tacz:357mag",r(48,m("c:ingots/copper",25),m("c:gunpowders",6)));
        RECIPES.put("tacz:40mm",r(6,m("c:ingots/iron",3),m("c:ingots/copper",9),m("c:gunpowders",9)));
        RECIPES.put("tacz:45acp",r(30,m("c:ingots/copper",10),m("c:gunpowders",2)));
        RECIPES.put("tacz:45_70",r(36,m("c:ingots/copper",30),m("c:gunpowders",7),m("c:gems/lapis",5)));
        RECIPES.put("tacz:46x30",r(48,m("c:ingots/copper",12),m("c:gunpowders",2)));
        RECIPES.put("tacz:500mag",r(32,m("c:ingots/copper",40),m("c:gunpowders",10),m("c:gems/lapis",5)));
        RECIPES.put("tacz:50ae",r(36,m("c:ingots/copper",30),m("c:gunpowders",7),m("c:gems/lapis",5)));
        RECIPES.put("tacz:50bmg",r(24,m("c:ingots/copper",110),m("c:gunpowders",20),m("c:gems/lapis",12),m("c:rods/blaze",1)));
        RECIPES.put("tacz:545x39",r(45,m("c:ingots/copper",13),m("c:gunpowders",3)));
        RECIPES.put("tacz:556x45",r(45,m("c:ingots/copper",15),m("c:gunpowders",3)));
        RECIPES.put("tacz:57x28",r(48,m("c:ingots/copper",15),m("c:gems/lapis",5),m("c:gunpowders",2)));
        RECIPES.put("tacz:58x42",r(40,m("c:ingots/copper",15),m("c:gunpowders",3)));
        RECIPES.put("tacz:68x51fury",r(40,m("c:ingots/copper",15),m("c:gunpowders",5)));
        RECIPES.put("tacz:762x25",r(45,m("c:ingots/copper",10),m("c:gunpowders",2)));
        RECIPES.put("tacz:762x39",r(35,m("c:ingots/copper",15),m("c:gunpowders",3)));
        RECIPES.put("tacz:762x54",r(60,m("c:ingots/copper",25),m("c:gunpowders",8)));
        RECIPES.put("tacz:792x57",r(48,m("c:ingots/copper",20),m("c:gunpowders",6)));
        RECIPES.put("tacz:9mm",r(50,m("c:ingots/copper",10),m("c:gunpowders",2)));
        RECIPES.put("tacz:rpg_rocket",r(3,m("c:ingots/iron",3),m("c:ingots/copper",30),m("c:gunpowders",12)));
    }
    public AmmoPressBlockEntity(BlockPos pos,BlockState state){super(TaczAmmoPress.AMMO_PRESS_BE.get(),pos,state);}
    private int relativeIndex(Direction side){
        if(side==Direction.UP)return 0;
        if(side==Direction.DOWN)return 1;
        Direction front=getBlockState().getValue(AmmoPressBlock.FACING);
        if(side==front)return 2;
        if(side==front.getOpposite())return 3;
        if(side==front.getCounterClockWise())return 4;
        return 5;
    }
    public SideMode getSideMode(int relative){return sideModes[Math.floorMod(relative,6)];}
    public void cycleSideMode(int relative){int i=Math.floorMod(relative,6);sideModes[i]=sideModes[i].next();setChanged();}
    public @Nullable IItemHandler getAutomationHandler(@Nullable Direction side){
        if(side==null)return inputAutomation;
        return switch(sideModes[relativeIndex(side)]){case INPUT->inputAutomation;case OUTPUT->outputAutomation;default->null;};
    }
    public @Nullable IEnergyStorage getEnergyStorage(@Nullable Direction side){
        if(side==null)return energyInput;
        return sideModes[relativeIndex(side)]==SideMode.ENERGY?energyInput:null;
    }
    public static void tick(Level level,BlockPos pos,BlockState state,AmmoPressBlockEntity be){
        if(level.isClientSide)return; AmmoRecipe recipe=be.currentRecipe();
        if(recipe==null||!be.hasMaterials(recipe)||!be.canFitOutput(recipe.output)||be.energy.getEnergyStored()<ENERGY_PER_TICK){if(be.progress!=0){be.progress=0;be.setChanged();}return;}
        be.energy.extractEnergy(ENERGY_PER_TICK,false);be.progress++;
        if(be.progress>=PROCESS_TICKS){if(be.consumeMaterials(recipe))be.insertOutput(recipe.output);be.progress=0;be.setChanged();}
    }
    private @Nullable AmmoRecipe currentRecipe(){String id=ammoId(items.getStackInSlot(TEMPLATE));return id==null?null:RECIPES.get(id);}
    private static boolean isTaczAmmo(ItemStack stack){return !stack.isEmpty()&&ResourceLocation.parse("tacz:ammo").equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))&&ammoId(stack)!=null;}
    private static @Nullable String ammoId(ItemStack stack){if(stack.isEmpty())return null;CustomData data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY);CompoundTag tag=data.copyTag();String id=tag.getString("AmmoId");return id.isBlank()?null:id;}
    private static boolean matches(ItemStack stack,Material material){if(stack.isEmpty())return false;TagKey<Item>tag=TagKey.create(Registries.ITEM,ResourceLocation.parse(material.tag));return stack.is(tag);}
    private int count(Material material){int total=0;for(int i=INPUT_FIRST;i<=INPUT_LAST;i++)if(matches(items.getStackInSlot(i),material))total+=items.getStackInSlot(i).getCount();return total;}
    private boolean hasMaterials(AmmoRecipe recipe){for(Material material:recipe.materials)if(count(material)<material.count)return false;return true;}
    private boolean consumeMaterials(AmmoRecipe recipe){if(!hasMaterials(recipe))return false;for(Material material:recipe.materials){int left=material.count;for(int i=INPUT_FIRST;i<=INPUT_LAST&&left>0;i++){ItemStack stack=items.getStackInSlot(i);if(!matches(stack,material))continue;int take=Math.min(left,stack.getCount());items.extractItem(i,take,false);left-=take;}}return true;}
    private boolean canFitOutput(int amount){ItemStack template=items.getStackInSlot(TEMPLATE);if(template.isEmpty())return false;int free=0;for(int i=OUTPUT_FIRST;i<=OUTPUT_LAST;i++){ItemStack out=items.getStackInSlot(i);if(out.isEmpty())free+=template.getMaxStackSize();else if(ItemStack.isSameItemSameComponents(out,template))free+=Math.max(0,out.getMaxStackSize()-out.getCount());}return free>=amount;}
    private void insertOutput(int amount){ItemStack template=items.getStackInSlot(TEMPLATE);int left=amount;for(int i=OUTPUT_FIRST;i<=OUTPUT_LAST&&left>0;i++){ItemStack out=items.getStackInSlot(i);if(!out.isEmpty()&&ItemStack.isSameItemSameComponents(out,template)){int add=Math.min(left,out.getMaxStackSize()-out.getCount());out.grow(add);left-=add;}}for(int i=OUTPUT_FIRST;i<=OUTPUT_LAST&&left>0;i++){if(!items.getStackInSlot(i).isEmpty())continue;int add=Math.min(left,template.getMaxStackSize());items.setStackInSlot(i,template.copyWithCount(add));left-=add;}}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.put("items",items.serializeNBT(registries));tag.put("energy",energy.serializeNBT(registries));tag.putInt("progress",progress);int[] modes=new int[6];for(int i=0;i<6;i++)modes[i]=sideModes[i].ordinal();tag.putIntArray("sideModes",modes);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);if(tag.contains("items")){CompoundTag savedItems=tag.getCompound("items").copy();savedItems.putInt("Size",10);items.deserializeNBT(registries,savedItems);}if(tag.contains("energy"))energy.deserializeNBT(registries,tag.get("energy"));progress=tag.getInt("progress");int[] modes=tag.getIntArray("sideModes");if(modes.length==6)for(int i=0;i<6;i++)sideModes[i]=SideMode.byId(modes[i]);}
    @Override public int getContainerSize(){return 10;} @Override public boolean isEmpty(){for(int i=0;i<10;i++)if(!items.getStackInSlot(i).isEmpty())return false;return true;}
    @Override public ItemStack getItem(int slot){return items.getStackInSlot(slot);} @Override public ItemStack removeItem(int slot,int amount){return items.extractItem(slot,amount,false);}
    @Override public ItemStack removeItemNoUpdate(int slot){ItemStack s=items.getStackInSlot(slot);items.setStackInSlot(slot,ItemStack.EMPTY);return s;} @Override public void setItem(int slot,ItemStack stack){items.setStackInSlot(slot,stack);}
    @Override public boolean stillValid(Player player){return !isRemoved()&&player.distanceToSqr(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5)<=64.0;}
    @Override public void clearContent(){for(int i=0;i<10;i++)items.setStackInSlot(i,ItemStack.EMPTY);} @Override public boolean canPlaceItem(int slot,ItemStack stack){return items.isItemValid(slot,stack);}
    @Override public Component getDisplayName(){return Component.translatable("container.tacz_ammo_press.ammo_press");}
    public ContainerData dataAccess(){return new ContainerData(){
        @Override public int get(int index){
            if(index==0)return progress;
            if(index==1)return energy.getEnergyStored();
            if(index==2)return energy.getMaxEnergyStored();
            if(index>=3&&index<9)return sideModes[index-3].ordinal();
            return 0;
        }
        @Override public void set(int index,int value){if(index==0)progress=value;else if(index>=3&&index<9)sideModes[index-3]=SideMode.byId(value);}
        @Override public int getCount(){return 9;}
    };}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new AmmoPressMenu(id,inventory,this,dataAccess());}
}
