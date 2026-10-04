package dev.miniezi.taczammopress;

public enum SideMode {
    NONE(0xFF7A3434),
    INPUT(0xFF39B85A),
    OUTPUT(0xFFD38A22),
    ENERGY(0xFF299DD1);

    public final int color;
    SideMode(int color){this.color=color;}
    public SideMode next(){return values()[(ordinal()+1)%values().length];}
    public static SideMode byId(int id){return values()[Math.floorMod(id, values().length)];}
}
