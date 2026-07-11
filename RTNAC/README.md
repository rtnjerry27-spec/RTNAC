# RTNAC — Real-Time Network Anti-Cheat

A lightweight, dependency-free anti-cheat plugin for Paper (and Paper forks
like Purpur/Leaf) servers, targeting the 1.21.x API line.

## ⚠️ Important — read before you build

This project was written in a sandboxed environment **with no internet
access**, so it could not be compiled or packaged into a `.jar` here. What
you have is complete, real Java source — not stubs — that you build
yourself with one command once you have Maven + internet access. This also
means there may be a small compile-time typo I couldn't catch without a
real compiler; if `mvn package` reports an error, paste it back to me and
I'll fix the exact line immediately.

## What's included (real logic, not placeholders)

| Category | Check | What it detects |
|---|---|---|
| Movement | Speed | Horizontal movement exceeding sprint/potion/ice-adjusted caps |
| Movement | Flight | Sustained airtime with no fall, no elytra/levitation/creative |
| Movement | NoFall | Fall damage suppression after significant fall distance |
| Movement | Jesus | Standing/walking on water surface without a legitimate cause |
| Movement | NoSlowdown | Ignoring vanilla slowdown while eating/blocking/using items |
| Movement | Timer | Movement-packet rate exceeding 20/s (client tick manipulation) |
| Combat | KillAura | Impossible view-angle snaps between rapid attacks, swing-less hits |
| Combat | Reach | Melee hits beyond vanilla's ~3 block interaction range |
| Combat | AutoClicker | CPS ceiling + click-interval consistency (statistical) analysis |
| Blocks | Scaffold | Sprint-bridging without looking down (bot-like placement angle) |
| Blocks | FastPlace | Block placement rate exceeding client-side cooldown |
| Blocks | FastBreak | Breaking blocks faster than tool/hardness/haste allow |

Plus:
- Per-check, per-player **violation level (VL)** system with automatic decay
- Configurable **staff alerts** (`rtnac.alerts` permission) broadcast live
- Configurable **punishment thresholds** (kick/ban commands per VL tier), fully
  editable in `config.yml` — hook in your own ban plugin's commands too
- `/rtnac reload|checks|status <player>|history` admin command
- `rtnac.bypass` permission for staff/testers

## Build

Requires JDK 21 and Maven, with internet access (to pull the Paper API from
`repo.papermc.io`).

```bash
cd RTNAC
mvn clean package
```

The built jar will be at `target/RTNAC-1.0.0.jar`.

If you're targeting a specific Paper build number, edit the
`<paper.api.version>` property in `pom.xml` — any recent 1.21.x Paper API
works since this plugin uses no version-specific NMS/internals.

## Install

1. Drop `RTNAC-1.0.0.jar` into your server's `plugins/` folder.
2. Restart (or `/reload confirm`, though a full restart is safer for a
   listener-heavy plugin like this).
3. Edit `plugins/RTNAC/config.yml` to tune thresholds and punishments, then
   `/rtnac reload`.

## Known limitations / honest notes

- **Timer check** is a coarse approximation based on `PlayerMoveEvent`
  frequency. True packet-timestamp timer detection needs a packet library
  like ProtocolLib or PacketEvents — wiring that in is a natural next step
  if you want tighter detection.
- **FastBreak** uses a simplified hardness table covering common blocks
  rather than the complete vanilla dataset — extend `baseHardness()` in
  `FastBreakCheck.java` for full coverage.
- This is a solid **foundation**, comparable in structure to early-stage
  Grim/Vulcan-style anticheats, but it is not a drop-in replacement for
  mature, actively-maintained anticheats that have years of tuning against
  real cheat clients. Expect to tune thresholds against your own player base
  to balance false positives vs. false negatives.
- No anti-cheat can fully replace packet-level verification (NCP/Grim-style
  prediction) for the highest-stakes servers; this plugin uses event-level
  Bukkit API data, which is easier to maintain across versions but slightly
  less airtight than raw packet interception.

## Permissions

- `rtnac.admin` — use `/rtnac` (default: op)
- `rtnac.alerts` — receive live cheat alerts (default: op)
- `rtnac.bypass` — exempt from all checks (default: false)
