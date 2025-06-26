package bas.pennings.kaasCore;

import bas.pennings.kaasCore.clans.ClanService;
import bas.pennings.kaasCore.commands.ClanCommands;
import bas.pennings.kaasCore.config.ClansConfigHandler;
import bas.pennings.kaasCore.config.ConfigHandler;
import bas.pennings.kaasCore.config.MainConfigHandler;
import bas.pennings.kaasCore.config.MessagesConfigHandler;
import bas.pennings.kaasCore.events.EventManager;
import bas.pennings.kaasCore.utils.MessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class KaasCore extends JavaPlugin {

    private ConfigHandler[] configHandlers;
    private ClanService clanService;

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        MainConfigHandler mainConfigHandler = new MainConfigHandler(this);
        ClansConfigHandler clansConfigHandler = new ClansConfigHandler(this, logger);
        MessagesConfigHandler messagesConfigHandler = new MessagesConfigHandler(this, logger);
        MessageFormatter messageFormatter = new MessageFormatter(logger, messagesConfigHandler);
        clanService = new ClanService(logger, clansConfigHandler);
        EventManager eventManager = new EventManager(mainConfigHandler, clanService);
        ClanCommands clanCommands = new ClanCommands(this, logger, clanService, messageFormatter);

        // Setup configuration files
        configHandlers = new ConfigHandler[] { mainConfigHandler, clansConfigHandler, messagesConfigHandler };
        for (ConfigHandler configHandler : configHandlers) {
            configHandler.setupConfig();
        }

        // Registers
        clanCommands.registerCommands();
        getServer().getPluginManager().registerEvents(eventManager, this);

        // Services
        clanService.loadClans();
        messageFormatter.loadMessages();
    }

    @Override
    public void onDisable() {
        clanService.saveClans();

        // Save configuration files
        for (ConfigHandler configHandler : configHandlers) {
            configHandler.saveConfig();
        }

        logger.info("KaasCore has been shutdown successfully");
    }

    public void reloadConfigHandlers() {
        for (ConfigHandler configHandler : configHandlers) {
            configHandler.reloadConfig();
        }
    }

}
