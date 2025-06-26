package bas.pennings.kaasCore.config;

import bas.pennings.kaasCore.KaasCore;
import bas.pennings.kaasCore.clans.ClanData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClansConfigHandler implements ConfigHandler {

    private final KaasCore kaasCore;
    private final Logger logger;
    private YamlConfiguration clanConfig;
    private File clanFile;

    private static final String CLANS_CONFIG_FILE = "clans.yml";
    private static final Boolean overwriteConfigFile = false;

    private static final String CLAN_DATA_PREFIX = "clans.data";
    private static final String CLAN_NAME_KEY = "clanName";
    private static final String CLAN_TYPE_KEY = "clanType";
    private static final String CLAN_MEMBERS_KEY = "clanMembers";

    public ClansConfigHandler(KaasCore kaasCore, Logger logger) {
        this.kaasCore = kaasCore;
        this.logger = logger;
    }

    @Override
    public void setupConfig() {
        clanFile = new File(kaasCore.getDataFolder(), CLANS_CONFIG_FILE);

        if (!clanFile.exists()) {
            kaasCore.saveResource(CLANS_CONFIG_FILE, overwriteConfigFile);
            logger.info("Created new clans config file: " + CLANS_CONFIG_FILE);
        }

        clanConfig = YamlConfiguration.loadConfiguration(clanFile);
    }

    @Override
    public void saveConfig() {
        try {
            clanConfig.save(clanFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save clan config!", e);
        }
    }

    @Override
    public void reloadConfig() {
        clanConfig = YamlConfiguration.loadConfiguration(clanFile);
    }

    public void storeClanData(@NotNull ClanData clanData) {
        clanConfig.set(CLAN_DATA_PREFIX + "." + clanData.ownerUUID() + "." + CLAN_NAME_KEY, clanData.name());
        clanConfig.set(CLAN_DATA_PREFIX + "." + clanData.ownerUUID() + "." + CLAN_TYPE_KEY, clanData.type());
        clanConfig.set(CLAN_DATA_PREFIX + "." + clanData.ownerUUID() + "." + CLAN_MEMBERS_KEY, clanData.memberUUIDs());
        saveConfig();
    }

    public void removeClanData(String clanOwnerUUID) {
        clanConfig.set(CLAN_DATA_PREFIX + "." + clanOwnerUUID, null);
        saveConfig();
    }

    public @NotNull List<ClanData> getClansData() {
        List<ClanData> clansData = new ArrayList<>();
        @Nullable ConfigurationSection clanDataSection =  clanConfig.getConfigurationSection(CLAN_DATA_PREFIX);

        if (clanDataSection == null) {
            return clansData;
        }

        boolean deepSearch = false;
        clanDataSection.getKeys(deepSearch).forEach(key -> {
            @Nullable String clanName = clanDataSection.getString(key + "." + CLAN_NAME_KEY);
            @Nullable String clanType = clanDataSection.getString(key + "." + CLAN_TYPE_KEY);
            List<String> clanMemberUUIDs = clanDataSection.getStringList(key + "." + CLAN_MEMBERS_KEY);

            if (clanName == null) {
                logger.warning("Could not find clan name for " + key);
            }
            if (clanType == null) {
                logger.warning("Could not find clan type for " + key);
            }

            clansData.add(new ClanData(clanName, clanType, key, clanMemberUUIDs));
        });

        return clansData;
    }
}
