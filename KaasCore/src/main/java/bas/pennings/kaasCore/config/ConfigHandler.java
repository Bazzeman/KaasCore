package bas.pennings.kaasCore.config;

/**
 * Represents a handler for managing a single configuration file.
 * Implementing classes should manage their own specific config file.
 */
public interface ConfigHandler {

    /**
     * Creates and loads the configuration file.
     * <p>
     * If the configuration file does not exist, it will be created from the default resource
     * bundled with the plugin (if available), and then loaded into memory.
     */
    void setupConfig();

    /**
     * Saves the current in-memory configuration to disk.
     * <p>
     * This writes any changes made to the configuration back to the file.
     * If the file cannot be saved (e.g., due to I/O errors), the method should handle it appropriately.
     */
    void saveConfig();

    /**
     * Reloads the configuration from disk into memory.
     * <p>
     * This discards any unsaved changes and replaces the in-memory configuration
     * with the version on disk.
     */
    void reloadConfig();
}
