package bas.pennings.kaasCore.clans;

import net.kyori.adventure.text.format.NamedTextColor;

public enum ClanType {

    HOSTILE("hostile", NamedTextColor.RED),
    NEUTRAL("neutral", NamedTextColor.YELLOW),
    PEACEFUL("peaceful", NamedTextColor.GREEN);

    public final String name;
    public final NamedTextColor color;

    ClanType(String name, NamedTextColor color) {
        this.name = name;
        this.color = color;
    }

    public static ClanType getClanType(String clanTypeName) {
        for (ClanType clanType : ClanType.values()) {
            if (clanType.name.equalsIgnoreCase(clanTypeName)) {
                return clanType;
            }
        }
        return null;
    }
}
