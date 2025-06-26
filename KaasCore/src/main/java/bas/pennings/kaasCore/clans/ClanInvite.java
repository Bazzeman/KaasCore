package bas.pennings.kaasCore.clans;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class ClanInvite {

    @Getter @Setter private String inviter;
    @Getter @Setter private String invitee;
    @Getter final Date inviteTime;

    public ClanInvite(String inviter, String invitee) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.inviteTime = new Date();
    }
}
