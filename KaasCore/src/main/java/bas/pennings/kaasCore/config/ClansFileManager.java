package bas.pennings.kaasCore.config;

import bas.pennings.kaasCore.KaasCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class ClansFileManager {

    private final KaasCore kaasCore;
    private FileConfiguration dataConfig;
    private File configFile;

    private static final String CLANS_CONFIG_FILE = "clans.yml";

    public ClansFileManager(KaasCore kaasCore) {
        this.kaasCore = kaasCore;
        saveDefaultClansConfig();
    }

    private void loadClansConfig() {
        if (configFile == null) {
            configFile = new File(kaasCore.getDataFolder(), CLANS_CONFIG_FILE);
        }

        dataConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultStream = kaasCore.getResource(CLANS_CONFIG_FILE);

        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getClansConfig(){
        if (dataConfig == null) {
            loadClansConfig();
        }
        return dataConfig;
    }

    public void saveClansConfig() {
        if (dataConfig != null && configFile != null) {
            try {
                getClansConfig().save(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveDefaultClansConfig(){
        if (configFile == null) {
            configFile = new File(kaasCore.getDataFolder(), CLANS_CONFIG_FILE);
        }
        if (!configFile.exists()) {
            kaasCore.saveResource(CLANS_CONFIG_FILE, false);
        }
    }
}
