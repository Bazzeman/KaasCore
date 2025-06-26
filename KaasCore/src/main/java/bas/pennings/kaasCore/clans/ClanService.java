package bas.pennings.kaasCore.clans;

import bas.pennings.kaasCore.config.ClansConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

// ClanService can be optimized on the following subjects:
// - Buffering changes to clans and saving them all at once.
// - Thread safety by changing clansMap to a different variable type.

public class ClanService {

    private final Logger logger;
    private final ClansConfigHandler clansConfigHandler;
    private final Map<UUID, Clan> clansMap = new HashMap<>(); // <OwnerUUID, Clan>

    public ClanService(Logger logger, ClansConfigHandler clansConfigHandler) {
        this.logger = logger;
        this.clansConfigHandler = clansConfigHandler;
    }

    public void loadClans() {
        clansMap.clear();
        clansConfigHandler.getClansData().forEach(clanData -> {
            try {
                Clan clan = Clan.fromClanData(clanData);
                clansMap.put(clan.getOwnerUUID(), clan);
            } catch (InvalidClanTypeException | EmptyClanNameException e) {
                logger.warning("Failed to load clan for owner with UUID " + clanData.ownerUUID() + ": " + e.getMessage());

                // Remove invalid clan from config
                clansConfigHandler.removeClanData(clanData.ownerUUID());

                // Notify online clan owner and members of the removal of their clan.
                notifyIfOnline(UUID.fromString(clanData.ownerUUID()), Component.text(
                        "Your clan data was invalid and the clan has been removed",
                        NamedTextColor.YELLOW));
                clanData.memberUUIDs().stream()
                        .map(UUID::fromString)
                        .forEach(p -> notifyIfOnline(p, Component.text(
                                "A clan you were part of was invalid and has been removed.",
                                NamedTextColor.YELLOW)));
            }
        });
    }

    public void saveClans() {
        clansMap.forEach((owner, clan) -> saveClan(clan));
    }

    private void saveClan(@NotNull Clan clan) {
        ClanData clanData = clan.toClanData();
        clansConfigHandler.storeClanData(clanData);
    }

    private void notifyIfOnline(@NotNull UUID playerUUID, Component message) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    public void createClan(@NotNull String clanName, ClanType clanType, @NotNull UUID clanOwnerUUID) throws EmptyClanNameException, IllegalArgumentException {
        if (clanName.isBlank()) {
            throw new EmptyClanNameException("Clan name cannot be null or blank");
        }
        if (clansMap.containsKey(clanOwnerUUID)) {
            throw new IllegalArgumentException("Clan owner with UUID " + clanOwnerUUID + " already owns a clan");
        }

        Clan clan = new Clan(clanName, clanType, clanOwnerUUID);
        clansMap.put(clanOwnerUUID, clan);
        saveClan(clan);
    }

    public void removeClan(@NotNull UUID clanOwnerUUID) throws ClanNotFoundException {
        if (!clansMap.containsKey(clanOwnerUUID)) {
            throw new ClanNotFoundException("Clan not found for owner with UUID: " + clanOwnerUUID);
        }

        String ownerUUID = clanOwnerUUID.toString();
        clansConfigHandler.removeClanData(ownerUUID);
        clansMap.remove(clanOwnerUUID);
    }

    public void addClanMember(@NotNull UUID clanOwnerUUID, @NotNull UUID memberUUID) throws ClanNotFoundException, DuplicateClanMemberException {
        Clan clan = lookupClanByOwner(clanOwnerUUID);
        if (clan == null) {
            throw new ClanNotFoundException("Clan not found for owner with UUID: " + clanOwnerUUID);
        }

        if (clan.getMemberUUIDs().contains(memberUUID)) {
            throw new DuplicateClanMemberException("Clan already has member with UUID:" + memberUUID);
        }

        clan.addMember(memberUUID);
        saveClan(clan);
    }

    public void removeClanMember(@NotNull UUID clanOwnerUUID, @NotNull UUID memberUUID) throws IllegalArgumentException {
        Clan clan = lookupClanByOwner(clanOwnerUUID);
        if (clan == null) {
            throw new IllegalArgumentException("Clan not found for owner with UUID: " + clanOwnerUUID);
        }

        if (!clan.getMemberUUIDs().contains(memberUUID)) {
            throw new IllegalArgumentException("Clan does not have member with UUID " + memberUUID);
        }

        clan.removeMember(memberUUID);
        saveClan(clan);
    }

    private @Nullable Clan lookupClanByOwner(@NotNull UUID clanOwnerUUID) {
        return clansMap.get(clanOwnerUUID);
    }

    private @Nullable Clan lookupClanByMember(@NotNull UUID memberUUID) {
        return clansMap.values().stream()
                .filter(clan -> clan.getMemberUUIDs().contains(memberUUID))
                .findFirst()
                .orElse(null);
    }

    public @Nullable Clan getClanByOwner(@NotNull UUID clanOwnerUUID) {
        Clan clan = lookupClanByOwner(clanOwnerUUID);
        return clan != null ? clan.copy() : null;
    }

    public @Nullable Clan getClanByMember(@NotNull UUID memberUUID) {
        Clan clan = lookupClanByMember(memberUUID);
        return clan != null ? clan.copy() : null;
    }

    /**
     * @deprecated Use {@link #getClanByOwner(UUID)} or {@link #getClanByMember(UUID)} directly when you know the player's role.
     * This method should only be used when the player's role (owner/member) is unknown.
     * Using the specific methods is more efficient as they avoid multiple lookups.
     */
    @Deprecated(since = "1.2.3", forRemoval = false)
    public @Nullable Clan getClanByPlayer(@NotNull UUID playerUUID) {
        Clan clan = lookupClanByOwner(playerUUID);
        if (clan != null) return clan.copy();

        clan = lookupClanByMember(playerUUID);
        return clan != null ? clan.copy() : null;
    }

    public @NotNull Collection<Clan> getAllClans() {
        return Collections.unmodifiableCollection(clansMap.values());
    }

    public boolean isOwner(@NotNull UUID OwnerUUID) {
        return lookupClanByOwner(OwnerUUID) != null;
    }

    public boolean isMember(@NotNull UUID memberUUID) {
        return lookupClanByMember(memberUUID) != null;
    }

    /**
     * @deprecated Use {@link #isOwner(UUID)} or {@link #isMember(UUID)} directly when you know the player's role.
     * This method should only be used when the player's role (owner/member) is unknown.
     * Using the specific methods is more efficient as they avoid multiple lookups.
     */
    @Deprecated(since = "1.2.3", forRemoval = false)
    public boolean isInClan(@NotNull UUID playerUUID) {
        return lookupClanByOwner(playerUUID) != null || lookupClanByMember(playerUUID) != null;
    }
}