package bas.pennings.kaasCore;

import bas.pennings.kaasCore.commands.ClanCommands;
import bas.pennings.kaasCore.config.ConfigManager;
import bas.pennings.kaasCore.events.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KaasCore extends JavaPlugin {

    private ConfigManager configManager;
    private EventManager eventManager;
    private ClanCommands clanCommands;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        eventManager = new EventManager(configManager);
        clanCommands = new ClanCommands(this, getLogger());

        configManager.loadConfig();

        saveDefaultConfig();
        clanCommands.registerCommands();
        getServer().getPluginManager().registerEvents(eventManager, this);
    }

    @Override
    public void onDisable() {

    }
}
