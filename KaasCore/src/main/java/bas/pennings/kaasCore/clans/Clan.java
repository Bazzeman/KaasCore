package bas.pennings.kaasCore.clans;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Clan {

    @Getter @NotNull private final String name;
    @Getter @NotNull private final ClanType type;
    @Getter @NotNull private final UUID ownerUUID;
    @Getter @NotNull private final Set<UUID> memberUUIDs;

    public Clan(@NotNull String clanName, @NotNull ClanType clanType, @NotNull UUID clanOwnerUUID) {
        name = clanName;
        type = clanType;
        ownerUUID = clanOwnerUUID;
        memberUUIDs = new HashSet<>();
    }

    public Clan(@NotNull String clanName, @NotNull ClanType clanType, @NotNull UUID clanOwnerUUID, @NotNull Set<UUID> clanMemberUUIDs) {
        name = clanName;
        type = clanType;
        ownerUUID = clanOwnerUUID;
        memberUUIDs = new HashSet<>(clanMemberUUIDs);
    }

    public void addMember(@NotNull UUID playerUUID)  {
        memberUUIDs.add(playerUUID);
    }

    public void removeMember(@NotNull UUID playerUUID) {
        memberUUIDs.remove(playerUUID);
    }

    public @NotNull ClanData toClanData() {
        String type = this.type.name();
        String ownerUUID = this.ownerUUID.toString();
        List<String> memberUUIDs = this.memberUUIDs.stream()
                .map(UUID::toString)
                .toList();

        return new ClanData(name, type, ownerUUID, memberUUIDs);
    }

    public static @NotNull Clan fromClanData(@NotNull ClanData data) throws InvalidClanTypeException, EmptyClanNameException {
        ClanType type = ClanType.fromString(data.type());
        String name = data.name();

        if (type == null) {
            throw new InvalidClanTypeException("Invalid clan type '" + data.type() + "' encountered while converting ClanData to Clan.");
        }
        if (name == null || name.isBlank()) {
            throw new EmptyClanNameException("Empty clan name encountered while converting ClanData to Clan.");
        }

        UUID ownerUUID = UUID.fromString(data.ownerUUID());
        Set<UUID> memberUUIDs = data.memberUUIDs().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        return new Clan(name, type, ownerUUID, memberUUIDs);
    }

    public @NotNull Clan copy() {
        return new Clan(name, type, ownerUUID, new HashSet<>(memberUUIDs));
    }
}