package bas.pennings.kaasCore.utils;

import bas.pennings.kaasCore.config.MessagesConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MessageFormatter {

    private final Logger logger;
    private final MessagesConfigHandler messagesConfigHandler;
    private final Map<String, Map<String, String>> messageCache = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageFormatter(Logger logger, MessagesConfigHandler messagesConfigHandler) {
        this.logger = logger;
        this.messagesConfigHandler = messagesConfigHandler;
    }

    /**
     * Loads and processes messages from the config.
     * Messages with invalid content are skipped and logged as warnings.
     */
    public void loadMessages() {
        Map<String, Map<String, String>> messages = messagesConfigHandler.getAllMessages();
        if (messages == null) {
            logger.warning("No messages were loaded from the config");
            return;
        }

        messageCache.clear();

        for (Map.Entry<String, Map<String, String>> sectionEntry : messages.entrySet()) {
            String section = sectionEntry.getKey();
            Map<String, String> processedMessages = new HashMap<>();

            for (Map.Entry<String, String> messageEntry : sectionEntry.getValue().entrySet()) {
                String key = messageEntry.getKey();
                String rawMessage = messageEntry.getValue();

                if (rawMessage == null || rawMessage.isBlank()) {
                    logger.warning("Message for key " + section + "." + key + "from messages.yml is blank.");
                    continue;
                }

                String processedMessage = rawMessage.replace("\\n", "\n");
                if (processedMessage.isBlank()) {
                    logger.warning("Message for key " + section + "." + key + " from messages.yml only contains a new line character.");
                    continue;
                }

                processedMessages.put(key, processedMessage);
            }

            messageCache.put(section, processedMessages);
        }
    }

    /**
     * Gets a message component with numbered placeholders ({0}, {1}, etc.).
     * @param section The section name
     * @param key The message key
     * @param args The values to replace the numbered placeholders with
     * @return The formatted component
     */
    public @NotNull Component getFormattedMessage(@NotNull String section, @NotNull String key, @NotNull String ... args) {
        String message = getRawMessage(section, key);

        // Replace {0}, {1}, etc. with the provided arguments
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }

        return miniMessage.deserialize(message);
    }

    /**
     * Gets a message component for the specified section and key.
     * @param section The section name (e.g., "clans")
     * @param key The message key within the section
     * @return The formatted component
     * @throws MessageNotFoundException if the message is not found
     */
    public @NotNull Component getMessage(@NotNull String section, @NotNull String key) {
        String message = getRawMessage(section, key);
        return miniMessage.deserialize(message);
    }

    private @NotNull String getRawMessage(@NotNull String section, @NotNull String key) throws MessageNotFoundException {
        Map<String, String> sectionMessages = messageCache.get(section);
        if (sectionMessages == null) {
            throw new MessageNotFoundException(section, key);
        }

        String message = sectionMessages.get(key);
        if (message == null) {
            throw new MessageNotFoundException(section, key);
        }

        return message;
    }

}