package bas.pennings.kaasCore.config;

import bas.pennings.kaasCore.KaasCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MessagesConfigHandler implements ConfigHandler {

    private final KaasCore kaasCore;
    private final Logger logger;
    private YamlConfiguration messagesConfig;
    private File messagesFile;

    private static final String MESSAGES_CONFIG_FILE = "messages.yml";
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessagesConfigHandler(KaasCore kaasCore, Logger logger) {
        this.kaasCore = kaasCore;
        this.logger = logger;
    }

    @Override
    public void setupConfig() {
        messagesFile = new File(kaasCore.getDataFolder(), MESSAGES_CONFIG_FILE);

        if (!messagesFile.exists()) {
            kaasCore.saveResource(MESSAGES_CONFIG_FILE, false);
            logger.info("Created new messages config file: " + MESSAGES_CONFIG_FILE);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public @Nullable Map<String, Map<String, String>> getAllMessages() {
        Map<String, Map<String, String>> messages = new HashMap<>();

        for (String section : messagesConfig.getKeys(false)) {
            @Nullable ConfigurationSection clanDataSection =  messagesConfig.getConfigurationSection(section);
            if (clanDataSection == null) {
                continue;
            }

            Map<String, String> sectionMessages = new HashMap<>();
            for (String key : clanDataSection.getKeys(false)) {
                String message = messagesConfig.getString(key);
                sectionMessages.put(key, message);
            }

            messages.put(section, sectionMessages);
        }
        return messages;
    };

    @Override
    public void saveConfig() {}

    @Override
    public void reloadConfig() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
