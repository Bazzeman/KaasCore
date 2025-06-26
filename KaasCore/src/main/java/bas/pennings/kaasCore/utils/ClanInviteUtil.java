package bas.pennings.kaasCore.utils;

import bas.pennings.kaasCore.KaasCore;
import bas.pennings.kaasCore.clans.ClanInvite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public final class ClanInviteUtil {

    private static final Logger logger = KaasCore.getPlugin(KaasCore.class).getLogger();

    private static Map<UUID, ClanInvite> invitesList = new HashMap<>();
    
    public static ClanInvite createInvite(String inviterUUID, String inviteeUUID) {
        UUID uuid = UUID.fromString(inviterUUID);
        clearExpiredInvites();
        if (!invitesList.containsKey(uuid)) {
            invitesList.put(uuid, new ClanInvite(inviterUUID, inviteeUUID));
            return invitesList.get(uuid);
        }
        return null;
    }

    public static boolean searchInvitee(String inviteeUUID){
        for (ClanInvite invite : invitesList.values()){
            if (invite.getInvitee().equals(inviteeUUID)){
                return true;
            }
        }
        return false;
    }

    public static void clearExpiredInvites(){
        int expiryTime = 25 * 1000;
        Date currentTime = new Date();
        for (ClanInvite clanInvite : invitesList.values()){
            if (currentTime.getTime() - clanInvite.getInviteTime().getTime() > expiryTime){
                invitesList.remove(clanInvite);
            }
        }
    }

    public static void clearInvite(String inviteeUUID) {
        invitesList.entrySet().removeIf(entry -> entry.getValue().getInvitee().equals(inviteeUUID));
    }

    public static Set<Map.Entry<UUID, ClanInvite>> getInvites(){
        return invitesList.entrySet();
    }

    public static UUID getInviteOwner(String inviteeUUID) {
        if (inviteeUUID.length() < 37) {
            for (Map.Entry<UUID, ClanInvite> entry : invitesList.entrySet()) {
                ClanInvite invite = entry.getValue();
                if (invite.getInvitee().equals(inviteeUUID)) {
                    return entry.getKey();
                }
            }
        } else {
            logger.warning("An error occurred whilst getting an Invite Owner.");
            logger.warning("The provided UUID is too long.");
        }
        return null;
    }


}
