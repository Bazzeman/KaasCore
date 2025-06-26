# KaasCore Changelog

---

## Update 1.2 — Accessibility Improvements and Unique Commands

### Update 1.2.4

#### Fixed
- Bug where `/hidenametag` command would throw a `NullPointerException` whenever the command sender was not in a clan.
- Bug where `/clan disband` command would throw a `null` error whenever the command sender did not own a clan.
- Bug where `/clan disband` command would not reset clan members and owner their colored name at times. Was being caused by changes made to scoreboard teams in update 1.2.3.

#### Changed

#### Added
- Added `messages.yml` configuration file for customization of messages being sent out by the KaasCore plugin.

### Update 1.2.3
**Release date:** 13-06-2025

#### Fixed
- Bug where `/hidenametag` command would hide the player nametag instead of toggling it between visible and invisible.

#### Changed
- Scoreboard teams are now managed by the utility class `ScoreboardTeamManager`.
- Scoreboard team switching is now improved by accepting `OfflinePlayer` objects and by removing unnecessary checks for already being present in a team.
- Adding, removing and toggling between scoreboard teams requested by `ClanCommands` class is now more robust with add and remove requests instead of update requests.
- Made retrieving the clan by player with an unknown role (ower/member) easier by adding (depreciated) methods `getClanByPlayer` and `isInClan` to class `ClanService`.
- Custom exceptions are now being returned by some `ClanService` methods in cases where the provided arguments were invalid or incorrect.

#### Added
- Added three new team types, one for each clan type with the hide player nametag property.


### Update 1.2.2
**Release date:** 08-06-2025

#### Fixed
- Bug where `/hidenametag` command did not update the rendering of the player's colored name.


### Update 1.2.1
**Release date:** 07-06-2025

#### Added
- `/goon` command to allow players to... goon.
- `/hidenametag` command to hide player nametags.

#### Changed
- Improved accessibility of the KaasCore reload command from /clan reload to /kaascore reload.

#### Fixed
- Bug where `/clan disband` command did not update the rendering of the clan owner's colored name.

---

## Update 1.1 — Clans and colors

#### Update 1.1.4
**Release date:** 01-06-2025

#### Fixed


#### Added


#### Changed

#### Update 1.1.3
**Release date:** 30-06-2025

#### Fixed


#### Added


#### Changed

### Update 1.1.2
**Release date:** 28-05-2025

#### Fixed


#### Added


#### Changed

### Update 1.1.1
**Release date:** 27-05-2025

#### Added
- `/clan` command to display all clan commands.
- `/clan create <name> [peaceful|normal|hostile]` command to create a new clan with a unique name and based on a clan type.
- `/clan disband` command to disband your clan.
- `/clan invite <player>` command to invite players to join your clan.
- `/clan type` command to change the clan type of your clan.
- `/clan kick <player>` command to kick a player out of your clan.
- `/clan leave` command to leave the current clan you are in.
- `/clan list` command to list all the existing clans.

---

## Update 1.0
**Release date:** 20-05-2025

#### Added
- **New feature: Custom World Spawn** — Players which join the server for their first time can be teleported to a location defined in the KaasCore configuration file.
- An enabled field for the **Custom World Spawn** feature in the KaasCore configuration file.