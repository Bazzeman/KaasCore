package bas.pennings.kaasCore.commands;

import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import bas.pennings.kaasCore.KaasCore;
import bas.pennings.kaasCore.clans.*;
import bas.pennings.kaasCore.utils.ClanInviteUtil;
import bas.pennings.kaasCore.utils.MessageFormatter;
import bas.pennings.kaasCore.utils.ScoreboardTeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanCommands implements CommandExecutor, TabCompleter {

    private final KaasCore kaasCore;
    private final Logger logger;
    private final ClanService clanService;
    private final MessageFormatter msg;

    private final static String GENERAL_MESSAGE_SECTION = "general";
    private final static String CLAN_MESSAGE_SECTION = "clans";

    public ClanCommands(KaasCore kaasCore, Logger logger, ClanService clanService, MessageFormatter messageFormatter) {
        this.kaasCore = kaasCore;
        this.logger = logger;
        this.clanService = clanService;
        this.msg = messageFormatter;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandHandler.class)) {
                CommandHandler annotation = Objects.requireNonNull(method.getAnnotation(CommandHandler.class));

                if (!annotation.name().equalsIgnoreCase(command.getName()))
                    continue;

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

    private void validateCommandCompleter(@NotNull Method method) throws IllegalStateException {
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
        PluginCommand command = kaasCore.getCommand(commandName);
        if (command != null) {
            command.setExecutor(this);
        } else {
            logger.severe("Error: Command '" + commandName + "' is not registered in plugin.yml!");
        }
    }

    private void setTabCompleter(@NotNull Method method) {
        String commandName = Objects.requireNonNull(method.getAnnotation(CommandCompleter.class)).name();
        PluginCommand command = kaasCore.getCommand(commandName);
        if (command != null) {
            command.setTabCompleter(this);
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
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(msg.getMessage("clan", "clan-commands"));
            return true;
        }

        return switch (args[0]) {
            case "create" -> requestClanCreation(player, args);
            case "invite" -> requestPlayerInvitation(player, args);
            case "join" -> requestJoinClan(player);
            case "disband" -> { requestClanDisband(player); yield true; }
            case "kick" -> requestKickingPlayer(player, args);
            case "leave" -> { requestLeavingClan(player); yield true; }
            case "list" -> { requestListClans(player); yield true; }
            default -> false;
        };
    }

    @CommandCompleter(name = "clan", permission = "kaascore.clansystem.clan")
    private List<String> onClanCompletion(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("create", "invite", "join", "disband", "kick", "leave", "list");
        }
        else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return List.of(ClanType.HOSTILE.name, ClanType.NEUTRAL.name, ClanType.PEACEFUL.name);
        }

        return List.of();
    }

    @CommandHandler(
            name = "goon",
            usage = "/goon",
            permission = "kaascore.goon",
            senderTypes = SenderType.PLAYER)
    private boolean onGoonCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        float pitch = ThreadLocalRandom.current().nextFloat(0.8f, 1.2f);
        Sound sound = ThreadLocalRandom.current().nextBoolean() ? Sound.BLOCK_SLIME_BLOCK_BREAK : Sound.BLOCK_HONEY_BLOCK_BREAK;

        broadcastToPlayersInRadius(player,
                Component.text(player.getName(), NamedTextColor.WHITE)
                        .append(Component.text(" is gooning!", NamedTextColor.YELLOW)),
                20);

        player.playSound(player.getLocation(), sound, 1, pitch);
        spawnGoonParticlesWithVelocity(player);
        return true;
    }

    @CommandHandler(
            name = "kaascore",
            usage = "/kaascore reload",
            permission = "kaascore.reload",
            senderTypes = SenderType.ANY)
    private boolean onKaasCoreCommand(CommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            return false;
        }

        requestReload(sender);
        return true;
    }

    @CommandCompleter(name = "clan", permission = "kaascore.reload")
    private List<String> onKaasCoreCompletion(CommandSender sender, String[] args) {
        return args.length == 1 ? List.of("reload") : List.of();
    }

    @CommandHandler(
            name = "hidenametag",
            usage = "/hidenametag",
            permission = "kaascore.hidenametag",
            senderTypes = SenderType.PLAYER)
    private boolean onHideNametagCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        @SuppressWarnings("deprecation")
        Clan clan = clanService.getClanByPlayer(player.getUniqueId());
        ClanType clanType = clan != null ? clan.getType() : null;

        boolean hidden = ScoreboardTeamManager.togglePlayerHiddenNametagTeam(player, clanType);
        player.sendMessage(Component.text("Nametag is now " + (hidden ? "hidden" : "visible"), NamedTextColor.YELLOW));
        return true;
    }

    private void requestReload(CommandSender sender) {
        if (sender instanceof Player player  && !sender.hasPermission("kaascore.reload")) {
            sendPlayerFeedback(player, "You don't have permission to use this command.");
        }

        clanService.loadClans();
        kaasCore.reloadConfigHandlers();

        Bukkit.getOnlinePlayers().forEach(p -> {
            Clan clan = clanService.getClanByPlayer(p.getUniqueId());
            ClanType clanType = clan != null ? clan.getType() : null;
            ScoreboardTeamManager.addPlayerToClanTypeTeam(p, clanType);
        });

        if (sender instanceof Player player) {
            player.sendMessage(Component.text("Successfully updated clans and player colors!", NamedTextColor.YELLOW));
        }
        else {
            sender.sendMessage(Component.text("Successfully updated clans and player colors!", NamedTextColor.YELLOW));
        }
    }

    private boolean requestClanCreation(Player player, String[] args) {
        String clanName = args[1];

        if (args.length < 3) {
            return false;
        }

        if (clanName.length() < 3 || clanName.length() > 16) {
            player.sendMessage(msg.getMessage(CLAN_MESSAGE_SECTION, "invalid-clan-name-length"));
            return true;
        }

        ClanType clanType = ClanType.fromString(args[2]);
        if (clanType == null) {
            player.sendMessage(msg.getMessage(CLAN_MESSAGE_SECTION, "invalid-clan-type"));
            return true;
        }

        try {
            clanService.createClan(clanName, clanType, player.getUniqueId());
            player.sendMessage(msg.getFormattedMessage(CLAN_MESSAGE_SECTION, "created-clan", clanType.name, clanName));
            ScoreboardTeamManager.addPlayerToClanTypeTeam(player, clanType);
        } catch (IllegalArgumentException e) {
            player.sendMessage(msg.getMessage(CLAN_MESSAGE_SECTION, "already-in-clan"));
        }

        return true;
    }

    private boolean requestPlayerInvitation(Player sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        String inviteeName = args[1];

        if (clanService.isMember(sender.getUniqueId())) {
            sender.sendMessage(msg.getMessage(CLAN_MESSAGE_SECTION, "inviting-not-allowed"));
            return true;
        }

        Clan clan = clanService.getClanByOwner(sender.getUniqueId());
        if (clan == null) {
            sender.sendMessage(msg.getMessage(CLAN_MESSAGE_SECTION, "not-in-clan"));
            return true;
        }

        if (sender.getName().equals(args[1])) {
            sender.sendMessage(msg.getMessage(CLAN_MESSAGE_SECTION, "cannot-invite-self"));
            return true;
        }

        Player invitedPlayer = Bukkit.getPlayer(inviteeName);
        if (invitedPlayer == null) {
            sender.sendMessage(msg.getFormattedMessage(CLAN_MESSAGE_SECTION, "invalid-invitee", inviteeName));
            return true;
        }

        if (clanService.isOwner(invitedPlayer.getUniqueId()) || clanService.isMember(invitedPlayer.getUniqueId())) {
            sender.sendMessage(msg.getFormattedMessage(CLAN_MESSAGE_SECTION, "invitee-already-in-clan", inviteeName));
            return true;
        }

        if (ClanInviteUtil.createInvite(sender.getUniqueId().toString(), invitedPlayer.getUniqueId().toString()) != null) {
            // TODO: Finish these two messages.
            sender.sendMessage(Component.text("Successfully invited player " + invitedPlayer.getName(), NamedTextColor.YELLOW));
            invitedPlayer.sendMessage(Component.text("You have been invited to join clan " + clan.getName() + ". Join it using /clan join", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(msg.getFormattedMessage(CLAN_MESSAGE_SECTION, "invitee-already-invited", inviteeName));
        }
        return true;
    }

    private boolean requestJoinClan(Player sender) {
        StringBuilder inviterUUIDString = new StringBuilder();
        Set<Map.Entry<UUID, ClanInvite>> clanInvitesList = ClanInviteUtil.getInvites();

        if (ClanInviteUtil.searchInvitee(sender.getUniqueId().toString())) {
            clanInvitesList.forEach((invites) ->
                    inviterUUIDString.append(invites.getValue().getInviter()));
            Clan clan = clanService.getClanByOwner(ClanInviteUtil.getInviteOwner(sender.getUniqueId().toString()));
            if (clan != null) {
                clanService.addClanMember(clan.getOwnerUUID(), sender.getUniqueId());
                ClanInviteUtil.clearInvite(sender.getUniqueId().toString());
                sender.sendMessage(Component.text("Successfully joined clan " + clan.getName(), NamedTextColor.YELLOW));
                ScoreboardTeamManager.addPlayerToClanTypeTeam(sender, clan.getType());
            } else {
                sender.sendMessage(Component.text("Invalid clan invite! Did not join clan.", NamedTextColor.YELLOW));
            }
        } else {
            sender.sendMessage(Component.text("You have not received any clan invite", NamedTextColor.YELLOW));
        }

        return true;
    }

    private void requestClanDisband(Player sender) {
        Clan clan = clanService.getClanByOwner(sender.getUniqueId());

        try {
            clanService.removeClan(sender.getUniqueId());

            clan.getMemberUUIDs().forEach(uuid -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    ScoreboardTeamManager.removePlayerFromClanTypeTeam(player, clan.getType());
            });

            OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwnerUUID());
            if (owner != null) {
                ScoreboardTeamManager.removePlayerFromClanTypeTeam(owner, clan.getType());
            }

            sender.sendMessage(Component.text("Successfully disbanded clan!", NamedTextColor.YELLOW));
        } catch (ClanNotFoundException e) {
            sender.sendMessage(Component.text("You must be a clan owner to do this!", NamedTextColor.RED));
        }
    }

    private boolean requestKickingPlayer(Player sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        if (!clanService.isOwner(sender.getUniqueId())) {
            sender.sendMessage(Component.text("You must be a clan owner to do this!", NamedTextColor.RED));
            return true;
        }

        Player playerToKick = Bukkit.getPlayer(args[1]);
        if (playerToKick == null) {
            sender.sendMessage(Component.text(
                    "Player "
                            + args[1]
                            + " cannot be found! Make sure this player is online.",
                    NamedTextColor.RED));
            return true;
        }

        if (sender.getName().equals(args[1])) {
            sender.sendMessage(Component.text("You can't kick yourself!", NamedTextColor.RED));
            return true;
        }

        try {
            clanService.removeClanMember(sender.getUniqueId(), playerToKick.getUniqueId());
            sender.sendMessage(Component.text(
                    "Successfully kicked player "
                            + playerToKick.getName(),
                    NamedTextColor.YELLOW));
            if (playerToKick.isOnline()) {
                playerToKick.sendMessage(Component.text(
                        "You have been kicked from your clan.",
                        NamedTextColor.YELLOW));
            }

            Clan clan = clanService.getClanByOwner(sender.getUniqueId());
            ScoreboardTeamManager.removePlayerFromClanTypeTeam(playerToKick, clan.getType());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Targeted player is not in your clan!", NamedTextColor.RED));
        }

        return true;
    }

    private void requestLeavingClan(Player sender) {
        if (clanService.isOwner(sender.getUniqueId())) {
            sender.sendMessage(Component.text("You can't leave your own clan. Use /clan disband", NamedTextColor.RED));
            return;
        }

        Clan targetClan = clanService.getClanByMember(sender.getUniqueId());
        if (targetClan == null) {
            sender.sendMessage(Component.text("You are not in a clan!", NamedTextColor.RED));
            return;
        }

        try {
            clanService.removeClanMember(targetClan.getOwnerUUID(), sender.getUniqueId());
            sender.sendMessage(Component.text("Successfully left clan!", NamedTextColor.YELLOW));
            ScoreboardTeamManager.removePlayerFromClanTypeTeam(sender, targetClan.getType());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("You are not in a clan!", NamedTextColor.RED));
        }
    }

    private void requestListClans(Player sender) {
        Collection<Clan> clans = clanService.getAllClans();
        if (clans.isEmpty()) {
            sender.sendMessage(Component.text("No clans to list", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("--- List of all clans ---", NamedTextColor.YELLOW));

            for (Clan clan : clans) {
                ClanType type = clan.getType();

                sender.sendMessage(Component.text(clan.getName() + " (" + type.name + ")", type.color));

                UUID ownerUUID = clan.getOwnerUUID();
                Player ownerPlayer = Bukkit.getPlayer(ownerUUID);
                OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(ownerUUID);

                String ownerName = ownerPlayer != null ? ownerPlayer.getName()
                        : offlineOwner.hasPlayedBefore() ? offlineOwner.getName() : "Unknown player";

                sender.sendMessage(Component.text("- " + ownerName + " (owner)", type.color));

                for (UUID memberUUID : clan.getMemberUUIDs()) {
                    Player memberPlayer = Bukkit.getPlayer(memberUUID);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);

                    String memberName = memberPlayer != null ? memberPlayer.getName()
                            : offlinePlayer.hasPlayedBefore() ? offlinePlayer.getName() : "Unknown player";

                    sender.sendMessage(Component.text("- " + memberName, type.color));
                }
            }
            sender.sendMessage(Component.text("-------------------------", NamedTextColor.YELLOW));
        }
    }

    private void spawnGoonParticlesWithVelocity(@NotNull Player player) {
        Location legLocation = player.getLocation().add(0, 0.6, 0);
        int particleCount = ThreadLocalRandom.current().nextInt(25, 75);

        for (int i = 0; i < particleCount; i++) {
            double offsetX = ThreadLocalRandom.current().nextDouble(-0.2, 0.3);
            double offsetY = ThreadLocalRandom.current().nextDouble(0, 0.2);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-0.2, 0.3);
            double velocityX = ThreadLocalRandom.current().nextDouble(-0.1, 0.2);
            double velocityY = ThreadLocalRandom.current().nextDouble(0.1, 0.25);
            double velocityZ = ThreadLocalRandom.current().nextDouble(-0.1, 0.2);
            double speed = ThreadLocalRandom.current().nextDouble(0.1, 0.5);

            player.getWorld().spawnParticle(
                    Particle.WHITE_SMOKE,
                    legLocation.clone().add(offsetX, offsetY, offsetZ),
                    1,
                    velocityX, velocityY, velocityZ,
                    speed
            );
        }
    }

    private void broadcastToPlayersInRadius(@NotNull Player source, TextComponent message, double radius) {
        Location sourceLocation = source.getLocation();

        source.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(sourceLocation) <= radius)
                .forEach(player -> player.sendMessage(message));
    }
}
