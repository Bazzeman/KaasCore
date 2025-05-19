package bas.pennings.kaasCore;

import bas.pennings.kaasCore.config.ConfigManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class KaasCore extends JavaPlugin {
    @Getter private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
    }

    @Override
    public void onDisable() {

    }
}
