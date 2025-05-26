package bas.pennings.kaasCore.commands;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import bas.pennings.kaasCore.KaasCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                    boolean correctUsage = (Boolean) method.invoke(this, new Object[]{sender, args});
                    if (!correctUsage) sendPlayerFeedback(sender, "Incorrect usage: " + annotation.usage());
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
                    List<String> commandCompletions = (List<String>) method.invoke(this, new Object[]{sender, args});
                    completions.addAll(commandCompletions);
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

    @CommandHandler(
            name = "clan",
            usage = "/clan [create|invite|disband|type|kick|leave|list] or /clan",
            permission = "kaascore.clansystem.clan",
            senderTypes = SenderType.PLAYER)
    private boolean onClanCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text(
        "KaasCore Clans usage:"
                    + "\n/clan create <name> <type>"
                    + "\n/clan invite <username>"
                    + "\n/clan disband"
                    + "\n/clan kick <username>"
                    + "\n/clan leave"
                    + "\n/clan list",
                    NamedTextColor.DARK_AQUA));
            return true;
        }

        return switch (args[0]) {
            case "reload" -> { requestReload(sender); yield true; }
            case "create" -> requestClanCreation(sender, args);
            case "invite" -> requestPlayerInvitation(sender, args);
            case "disband" -> { requestClanDisband(sender); yield true; }
            case "kick" -> requestKickingPlayer(sender, args);
            case "leave" -> { requestLeavingClan(sender); yield true; }
            case "list" -> { requestListClans(sender); yield true; }
            default -> false;
        };
    }

    @CommandCompleter(name = "clan", permission = "kaascore.clansystem.clan")
    private List<String> onClanCompletion(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> List.of("reload", "create", "invite", "disband", "kick", "leave", "list");
            default -> List.of();
        };
    }

    private void requestReload(CommandSender sender) {
        if (!sender.hasPermission("kaascore.clansystem.admin")) {
            sendPlayerFeedback(sender, "You don't have permission to use this command.");
        }

        // TODO: Go over every clan owner and clan member and update their scoreboard
        // TODO: Read config files again

        sender.sendMessage(Component.text("Successfully updated clans and player colors!", NamedTextColor.DARK_AQUA));
    };

    private boolean requestClanCreation(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return false;
        }

        if (args[1].length() < 3 || args[1].length() > 16) {
            sender.sendMessage(Component.text("Clan name should be between 3 and 16 characters long!", NamedTextColor.RED));
            return true;
        }

        // TODO: Detect for correct clan type

    sender.sendMessage(Component.text("creating clan...", NamedTextColor.DARK_AQUA));
        // TODO: Call clan creation method
        // TODO: Call method to give clan owner color
        return true;
    }

    private boolean requestPlayerInvitation(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        // TODO: Detect if the sender is in a clan and is the owner

        @Nullable Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            sender.sendMessage(Component.text(
            "Player "
                    + args[1]
                    + " can't be found! Make sure this player is online.",
                NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text(
            "Successfully invited player "
                    + targetPlayer.getName()
                    + " to join clan "
                    + "{clan name}", // TODO: Replace with clan name
                NamedTextColor.DARK_AQUA
        ));
        // TODO: Call invite player method
        return true;
    }

    private void requestClanDisband(CommandSender sender) {
        // TODO: Detect if the sender is a clan owner
//        if () {
//            sender.sendMessage(Component.text("You must be a clan owner to do this!", NamedTextColor.RED));
//            return;
//        }

        // TODO: Call clan disband method
        // TODO: Call method to give clear clan owner's and clan members their color

        sender.sendMessage(Component.text(
            "Successfully disbanded clan "
                    + "{clan name}", // TODO: Replace with disbanded clan name
            NamedTextColor.DARK_AQUA));
    }

    private boolean requestKickingPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        // TODO: Detect if the sender is a clan owner
        // TODO: Detect if the target player is in the clan

//        if () {
//            sender.sendMessage(Component.text(
//                    "Player "
//                            + args[1]
//                            + " is not part of this clan!",
//                    NamedTextColor.RED));
//            return true;
//        }

        @NotNull Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text(
            "Lijkt er op dat je een onverwachte error hebt veroorzaakt. Tijd om weer iets te gaan fixen, je wordt bedankt",
                    NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text(
                "Successfully kicked player "
                        + targetPlayer.getName()
                        + " from clan "
                        + "{clan name}", // TODO: Replace with clan name
                NamedTextColor.DARK_AQUA
        ));
        // TODO: Call kick player method
        // TODO: Clear color of kicked player
        return true;
    }

    private void requestLeavingClan(CommandSender sender) {
        // TODO: Detect if the sender is in a clan
//        if () {
//            sender.sendMessage(Component.text("You must be in a clan to do this!", NamedTextColor.RED));
//            return false;
//        }

        // TODO: Call leave clan method
        // TODO: Call method to give clear sender's color

        sender.sendMessage(Component.text(
                "Successfully left clan "
                        + "{clan name}", // TODO: Replace with left clan name
                NamedTextColor.DARK_AQUA));
    }

    private void requestListClans(CommandSender sender) {
        // TODO: Get list of all clans
        // TODO: Go trough every clan and message the player the clan name and type.
    }
}
