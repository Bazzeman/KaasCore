package bas.pennings.kaasCore.config;

import bas.pennings.kaasCore.KaasCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final KaasCore kaasCore;
    @Getter private Location spawnLocation;

    public ConfigManager(KaasCore kaasCore) {
        this.kaasCore = kaasCore;
    }

    public void loadConfig() {
        kaasCore.saveDefaultConfig(); // Creates config.yml if it doesn't exist
        FileConfiguration config = kaasCore.getConfig();

        String worldName = config.getString("spawn-location.world", "world");
        double x = config.getDouble("spawn-location.x", 0);
        double y = config.getDouble("spawn-location.y", 999);
        double z = config.getDouble("spawn-location.z", 0);
        float yaw = (float) config.getDouble("spawn-location.yaw", 0.0);
        float pitch = (float) config.getDouble("spawn-location.pitch", 0.0);

        spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
