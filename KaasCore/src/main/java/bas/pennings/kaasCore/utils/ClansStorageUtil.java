package bas.pennings.kaasCore.utils;

import bas.pennings.kaasCore.KaasCore;
import bas.pennings.kaasCore.clans.Clan;
import bas.pennings.kaasCore.clans.ClanType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.util.*;

public class ClansStorageUtil {

    private static Map<UUID, Clan> clansList = new HashMap<>();
    private static final FileConfiguration clansStorage = KaasCore.getClansFileManager().getClansConfig();
    private static final String CLAN_OWNER_KEY = ".clanOwner";
    private static final String CLAN_NAME_KEY = ".clanName";
    private static final String CLAN_MEMBERS_KEY = ".clanMembers";
    private static final String CLAN_TYPE_KEY = ".clanType";

    public static void saveClans() throws IOException {
        for (Map.Entry<UUID, Clan> entry : clansList.entrySet()) {
            clansStorage.set("clans.data." + entry.getKey() + CLAN_OWNER_KEY, entry.getValue().getClanOwnerUuidString());
            clansStorage.set("clans.data." + entry.getKey() + CLAN_NAME_KEY, entry.getValue().getClanName());
            clansStorage.set("clans.data." + entry.getKey() + CLAN_MEMBERS_KEY, entry.getValue().getClanMemberUuidStrings());
            clansStorage.set("clans.data." + entry.getKey() + CLAN_TYPE_KEY, entry.getValue().getClanType().name);
        }
        KaasCore.getClansFileManager().saveClansConfig();
    }

    public static void restoreClans() throws IOException {
        clansStorage.getConfigurationSection("clans.data").getKeys(false).forEach(key -> {
            UUID uuid = UUID.fromString(key);
            String clanName = clansStorage.getString("clans.data." + key + CLAN_NAME_KEY);
            List<String> clanMembersConfigSection = clansStorage.getStringList("clans.data." + key + CLAN_MEMBERS_KEY);
            ArrayList<String> clanMembers = new ArrayList<>(clanMembersConfigSection);
            ClanType clanType = ClanType.getClanType(clansStorage.getString("clans.data." + key + CLAN_TYPE_KEY));
            Clan clan = new Clan(key, clanName, clanType);
            clan.setClanMemberUuidStrings(clanMembers);
            clansList.put(uuid, clan);
        });
    }

    public static void createClan(Player player, String clanName, ClanType clanType) {
        UUID ownerUUID = player.getUniqueId();
        String ownerUuidString = player.getUniqueId().toString();
        clansList.put(ownerUUID, new Clan(ownerUuidString, clanName, clanType));
        updatePlayerClanTypeTeam(player);
    }

    public static boolean isClanExisting(Player player) {
        UUID uuid = player.getUniqueId();
        return clansList.containsKey(uuid);
    }

    public static boolean deleteClan(Player player) throws IOException {
        UUID uuid = player.getUniqueId();
        String key = uuid.toString();
        Clan clan = findClanByOwner(player);

        if (clan == null) {
            return false;
        }

        clansList.remove(uuid);
        clansStorage.set("clans.data." + key, null);
        KaasCore.getClansFileManager().saveClansConfig();
        updateClanMembersClanTypeTeam(clan);
        return true;
    }

    public static boolean isClanOwner(Player player) {
        UUID uuid = player.getUniqueId();
        String ownerUUID = uuid.toString();
        Clan clan = clansList.get(uuid);

        return clan != null && clan.getClanOwnerUuidString().equals(ownerUUID);
    }

    public static Clan findClanByOwner(Player player) {
        UUID uuid = player.getUniqueId();
        return clansList.get(uuid);
    }

    public static Clan findClanByOfflineOwner(OfflinePlayer offlinePlayer){
        UUID uuid = offlinePlayer.getUniqueId();
        return clansList.get(uuid);
    }

    public static Clan findClanByPlayer(Player player) {
        Clan clanByOwner = findClanByOwner(player);
        if (clanByOwner != null) {
            return clanByOwner;
        }

        for (Clan clan : clansList.values()) {
            if (clan.getClanMemberUuidStrings() != null) {
                for (String member : clan.getClanMemberUuidStrings()) {
                    if (member.equals(player.getUniqueId().toString())) {
                        return clan;
                    }
                }
            }
        }
        return null;
    }

    public static Clan findClanByOfflinePlayer(OfflinePlayer player) {
        Clan clanByOfflineOwner = findClanByOfflineOwner(player);
        if (clanByOfflineOwner != null) {
            return clanByOfflineOwner;
        }

        for (Clan clan : clansList.values()) {
            if (clan.getClanMemberUuidStrings() != null) {
                for (String member : clan.getClanMemberUuidStrings()) {
                    if (member.equals(player.getUniqueId().toString())) {
                        return clan;
                    }
                }
            }
        }
        return null;
    }

    public static boolean addClanMember(Clan clan, Player player) {
        UUID uuid = player.getUniqueId();
        String memberUUID = uuid.toString();
        clan.addClanMember(memberUUID);
        updatePlayerClanTypeTeam(player);
        return true;
    }

    private static Team getEnsuredTeam(ClanType clanType) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "ClansLite_" + clanType.name;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.color(clanType.color);
        }
        return team;
    }

    public static void updatePlayerClanTypeTeam(Player player) {
        Clan playerClan = findClanByPlayer(player);
        Team[] clanTypeTeams = {
                getEnsuredTeam(ClanType.HOSTILE),
                getEnsuredTeam(ClanType.NEUTRAL),
                getEnsuredTeam(ClanType.PEACEFUL)
        };

        // Remove the player from any clan type team if it is not in a clan.
        if (playerClan == null) {
            for (Team clanTypeTeam : clanTypeTeams)
            {
                clanTypeTeam.removeEntry(player.getName());
            }
        } else {
            Team clanTeam = getEnsuredTeam(playerClan.getClanType());

            // Remove the player from any clan type team it should not be in if the player is in one.
            for (Team clanTypeTeam : clanTypeTeams) {
                if (clanTypeTeam.hasEntry(player.getName()) && clanTypeTeam != clanTeam) {
                    clanTypeTeam.removeEntry(player.getName());
                }
            }

            // Add the player to the right team if it is not already in.
            if (!clanTeam.hasEntry(player.getName())) {
                clanTeam.addEntry(player.getName());
            }
        }
    };

    private static void updateClanMembersClanTypeTeam(Clan clan) {
        ArrayList<String> clanMemberUUIDStrings = clan.getClanMemberUuidStrings();
        String clanOwnerUUIDString = clan.getClanOwnerUuidString();
        clanMemberUUIDStrings.add(clanOwnerUUIDString);

        for (String playerUUIDString : clanMemberUUIDStrings) {
            UUID uuid = UUID.fromString(playerUUIDString);
            Player onlineplayer = Bukkit.getPlayer(uuid);
            if (onlineplayer != null) {
                updatePlayerClanTypeTeam(onlineplayer);
            }
        }
    }

    public static Set<Map.Entry<UUID, Clan>> getClans(){
        return clansList.entrySet();
    }

    public static Set<UUID> getRawClansList(){
        return clansList.keySet();
    }
}
