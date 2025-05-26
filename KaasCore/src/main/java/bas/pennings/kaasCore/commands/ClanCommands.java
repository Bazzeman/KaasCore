package bas.pennings.kaasCore.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import bas.pennings.kaasCore.KaasCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public class ClanCommands implements CommandExecutor, TabCompleter {
    private final KaasCore claimPlugin;
    private final Logger logger;

    public ClanCommands(KaasCore claimPlugin, Logger logger) {
        this.claimPlugin = claimPlugin;
        this.logger = logger;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandHandler.class)) {
                CommandHandler annotation = Objects.requireNonNull(method.getAnnotation(CommandHandler.class));

                if (!annotation.name().equalsIgnoreCase(command.getName()))
                    return false;

                if (!sender.hasPermission(annotation.permission())) {
                    sendPlayerFeedback(sender, "You don't have permission to use this command.");
                    return true;
                }

                if (Arrays.stream(annotation.senderTypes())
                        .noneMatch(senderType -> senderType.isValidSender(sender))) {
                    sendPlayerFeedback(sender, "You can't use this command as sender type " + sender.getClass().getSimpleName());
                    return true;
                }

                try {
                    boolean success = (Boolean) method.invoke(this, new Object[]{sender, args});
                    if (!success) sendPlayerFeedback(sender, "Incorrect usage: " + annotation.usage());
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandCompleter.class)) {
                CommandCompleter annotation = Objects.requireNonNull(method.getAnnotation(CommandCompleter.class));

                if (!annotation.name().equalsIgnoreCase(command.getName()))
                    return completions;

                if (!sender.hasPermission(annotation.permission()))
                    return completions;

                try {
                    @SuppressWarnings("unchecked")
                    List<String> result = (List<String>) method.invoke(this, new Object[]{sender, args});
                    completions.addAll(result);
                } catch (Exception e) {
                    logger.severe("Error invoking tab completion: " + e.getMessage());
                }
            }
        }
        return completions;
    }

    public void registerCommands() {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandHandler.class)) {
                validateCommandHandler(method);
                setExecutor(method);
            } else if (method.isAnnotationPresent(CommandCompleter.class)) {
                validateCommandCompleter(method);
                setTabCompleter(method);
            }
        }
    }

    private void validateCommandHandler(@NotNull Method method) throws IllegalStateException {
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 2
            || !CommandSender.class.isAssignableFrom(parameters[0])
            || !parameters[1].isArray()
            || !parameters[1].getComponentType().equals(String.class)
            || !method.getReturnType().equals(boolean.class))
        {
            throw new IllegalStateException(
                "Invalid @CommandHandler method signature for method: "
                + method.getName()
                + ". Expected: boolean method(CommandSender, String[])");
        }
    }

    private void validateCommandCompleter(@NotNull Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 2
            || !CommandSender.class.isAssignableFrom(parameters[0])
            || !parameters[1].isArray() || !parameters[1].getComponentType().equals(String.class)
            || !List.class.isAssignableFrom(method.getReturnType()))
        {
            throw new IllegalStateException(
                "Invalid @CommandCompleter method signature for method: "
                + method.getName()
                + ". Expected: List<String> method(CommandSender, String[])");
        }
    }

    private void setExecutor(@NotNull Method method) {
        String commandName = Objects.requireNonNull(method.getAnnotation(CommandHandler.class)).name();
        PluginCommand command = this.claimPlugin.getCommand(commandName);
        if (command != null) {
            Objects.requireNonNull(command).setExecutor(this);
        } else {
            logger.severe("Error: Command '" + commandName + "' is not registered in plugin.yml!");
        }
    }

    private void setTabCompleter(@NotNull Method method) {
        String commandName = Objects.requireNonNull(method.getAnnotation(CommandCompleter.class)).name();
        PluginCommand command = this.claimPlugin.getCommand(commandName);
        if (command != null) {
            Objects.requireNonNull(command).setTabCompleter(this);
        } else {
            logger.severe("Error: Command '" + commandName + "' is not registered in plugin.yml!");
        }
    }

    private void sendPlayerFeedback(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    @CommandHandler(name = "clan", usage = "/clan", permission = "kaascore.clansystem.clan", senderTypes = SenderType.ANY)
    private boolean onClanCommand(CommandSender sender, String[] args) {
        sendPlayerFeedback(sender, "Clan info:");
        return true;
    }

    @CommandCompleter(name = "clan", permission = "kaascore.clansystem.clan")
    private List<String> onClanCompletion(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of("create", "type");
        }

        return List.of();
    }
}
