package bas.pennings.kaasCore.events;

import bas.pennings.kaasCore.clans.Clan;
import bas.pennings.kaasCore.clans.ClanService;
import bas.pennings.kaasCore.clans.ClanType;
import bas.pennings.kaasCore.config.MainConfigHandler;
import bas.pennings.kaasCore.utils.ScoreboardTeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventManager implements Listener {

    private final MainConfigHandler mainConfigHandler;
    private final ClanService clanService;

    public EventManager(MainConfigHandler mainConfigHandler, ClanService clanService) {
        this.mainConfigHandler = mainConfigHandler;
        this.clanService = clanService;
    }

    @EventHandler
    public void OnFirstJoin(PlayerJoinEvent event) {
        if (!mainConfigHandler.isSpawnSystemEnabled()) return;

        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.teleport(mainConfigHandler.getSpawnLocation());
        }
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            return;
        }

        Clan clan = clanService.getClanByPlayer(event.getPlayer().getUniqueId());
        ClanType clanType = clan != null ? clan.getType() : null;

        if (clanType != null) {
            ScoreboardTeamManager.addPlayerToClanTypeTeam(event.getPlayer(), clanType);
        } else {
            for (ClanType type : ClanType.values()) {
                ScoreboardTeamManager.removePlayerFromClanTypeTeam(event.getPlayer(), type);
            }
        }
    }
}
