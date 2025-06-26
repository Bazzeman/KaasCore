package bas.pennings.kaasCore.utils;

import bas.pennings.kaasCore.clans.ClanType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScoreboardTeamManager {

    private static final String TEAM_PREFIX = "KaasCore_";
    private static final String TEAM_HIDDEN_NAMETAGS_NAMESPACE = "hiddenNametags_";
    private static final String DEFAULT_TEAM_NAMESPACE = "default";
    private static final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    private static @NotNull Team getOrCreateClanTypeTeam(@NotNull ClanType clanType) {
        String teamName = TEAM_PREFIX + clanType.name;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.color(clanType.color);
        }
        return team;
    }

    private static @NotNull Team getOrCreateHiddenNametagClanTeam(@Nullable ClanType clanType) {
        String clanTypeName = clanType == null ? DEFAULT_TEAM_NAMESPACE : clanType.name;
        String teamName = TEAM_PREFIX + TEAM_HIDDEN_NAMETAGS_NAMESPACE + clanTypeName;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.color(clanType.color);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        return team;
    }

    public static boolean togglePlayerHiddenNametagTeam(@NotNull OfflinePlayer player, @Nullable ClanType clanType) throws PlayerNameResolutionException {
        @Nullable String playerName = player.getName();
        if (playerName == null) {
            throw new PlayerNameResolutionException("Failed to resolve player name for OfflinePlayer: " + player.getUniqueId());
        }

        Team hiddenNametagTeam = getOrCreateHiddenNametagClanTeam(clanType);
        if (hiddenNametagTeam.hasEntry(player.getName())) {
            hiddenNametagTeam.removeEntry(player.getName());
            addPlayerToClanTypeTeam(player, clanType);
        } else {
            hiddenNametagTeam.addEntry(player.getName());
        }
        return hiddenNametagTeam.hasEntry(player.getName());
    }

    public static void addPlayerToClanTypeTeam(@NotNull OfflinePlayer player, @NotNull ClanType clanType) throws PlayerNameResolutionException {
        @Nullable String playerName = player.getName();
        if (playerName == null) {
            throw new PlayerNameResolutionException("Failed to resolve player name for OfflinePlayer: " + player.getUniqueId());
        }

        Team clanTypeTeam = getOrCreateClanTypeTeam(clanType);
        clanTypeTeam.addEntry(player.getName());
    }

    public static void removePlayerFromClanTypeTeam(@NotNull OfflinePlayer player, @NotNull ClanType clanType) throws PlayerNameResolutionException {
        @Nullable String playerName = player.getName();
        if (playerName == null) {
            throw new PlayerNameResolutionException("Failed to resolve player name for OfflinePlayer: " + player.getUniqueId());
        }

        Team clanTypeTeam = getOrCreateClanTypeTeam(clanType);
        clanTypeTeam.removeEntry(player.getName());
    }
}