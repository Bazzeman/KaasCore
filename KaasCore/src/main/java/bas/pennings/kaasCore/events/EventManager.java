package bas.pennings.kaasCore.events;

import bas.pennings.kaasCore.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventManager implements Listener {

    private final ConfigManager configManager;

    public EventManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void OnFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            player.teleport(configManager.getSpawnLocation());
        }
    }
}
