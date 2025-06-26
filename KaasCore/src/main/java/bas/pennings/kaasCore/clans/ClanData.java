package bas.pennings.kaasCore.clans;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ClanData(
        @Nullable String name,
        @Nullable String type,
        @NotNull String ownerUUID,
        @NotNull List<String> memberUUIDs) { }