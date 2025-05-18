# KaasCore
KaasCore is a custom Minecraft plugin developed specifically for the KaasLandMC server. It introduces a set of gameplay features designed to enhance security, immersion, and player experience.

## Features
### Lock & Key System
- Add craftable locks and keys to protect lootables (e.g., chests).
- Interact a lootable while holding a lock to:
  - Attach the lock to the block.
  - Automatically generate a unique key in return.
- Lootables with an active lock:
  - Cannot be opened, broken, or exploded.
  - Are only accessible with the correct key.
- Crouch + Interact with the correct key to remove the lock.
- Players with empty hands cannot interact with locked lootables.

### Lockpicking System
- Hold a lockpick item and interact a locked lootable to open a lockpicking GUI.
- Successfully picking the lock:
  - Removes the lock.
  - Makes the lootable publicly accessible.
- Great for PvP or high-risk environments.

### Custom Spawn System
- Players who join the server for the first time are automatically teleported to a predefined location in the world.
- Ensures a consistent and controlled onboarding experience.
