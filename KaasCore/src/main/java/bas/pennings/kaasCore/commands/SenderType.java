package bas.pennings.kaasCore.commands;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum SenderType {

    PLAYER(Player.class),
    CONSOLE(ConsoleCommandSender.class),
    REMOTE_CONSOLE(RemoteConsoleCommandSender.class),
    BLOCK(BlockCommandSender.class),
    ANY(CommandSender.class);

    private final Class<? extends CommandSender> senderClass;

    SenderType(Class<? extends CommandSender> senderClass) {
        this.senderClass = senderClass;
    }

    public boolean isValidSender(@NotNull CommandSender sender) {
        return senderClass.isInstance(sender);
    }
}
