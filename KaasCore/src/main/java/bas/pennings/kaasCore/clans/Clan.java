package bas.pennings.kaasCore.clans;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Clan {

    @Getter private final String clanOwnerUuidString;
    @Getter private final String clanName;
    @Getter @Setter private ArrayList<String> clanMemberUuidStrings;
    @Getter @Setter private ClanType clanType;

    public Clan(String clanOwnerUuidString, String clanName, ClanType clanType) {
        this.clanOwnerUuidString = clanOwnerUuidString;
        this.clanName = clanName;
        clanMemberUuidStrings = new ArrayList<>();
        this.clanType = clanType;
    }

    public void addClanMember(String clanMember) {
        clanMemberUuidStrings.add(clanMember);
    }

    public boolean removeClanMember(String clanMember) {
        return clanMemberUuidStrings.remove(clanMember);
    }
}
