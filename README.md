# Slyestcat Sound Pack

Replace RuneScape's sound effects and announce game events with **your own audio**.

A bundled pack of custom sounds plays on game events and replaces a curated set of
in-game sound effects (teleports, spec weapons, UI sounds). Every sound can be
overridden with your own WAV files — record your own voice lines, use memes, use
your friend's voice, whatever.

## How it works

Drop 16-bit PCM `.wav` files into `~/.runelite/sound-overrides/` (the folder is created
on first plugin start — on Windows: `C:\Users\<you>\.runelite\sound-overrides\`).

Files are recognized by preset name (see table below). Example: `level_up.wav`
plays whenever you gain a level, overriding the bundled default if one ships.

Every preset has its own on/off toggle in the plugin config. Toggling any config
setting hot-reloads the sounds folder — no client restart needed.

## Presets

| File | Trigger |
|---|---|
| `level_up.wav` | Gaining a level in any skill |
| `quest_complete.wav` | Quest completed scroll |
| `diary_complete.wav` | Achievement diary tier completed |
| `collection_log.wav` | New collection log slot * |
| `rare_drop.wav` | Valuable / untradeable drop message * |
| `combat_task.wav` | Combat achievement task completed |
| `slayer_task.wav` | Slayer task completed |
| `farming_contract.wav` | Farming Guild contract completed |
| `hunter_rumour.wav` | Hunter rumour rare piece found |
| `death.wav` | You die |
| `grubby_key.wav` / `larrans_key.wav` / `brimstone_key.wav` | Key drops |
| `pet.wav` | Pet drop ("funny feeling" messages, incl. backpack) |
| `gem_crab.wav` | The gemstone crab you're fighting burrows and moves |
| `vengeance.wav` | Casting Vengeance |
| `smited.wav` | Prayer points hit 0 (off by default) |
| `accept_trade.wav` / `decline_trade.wav` | Trade accepted / declined |
| `dismiss_random.wav` | Dismissing a random event |
| `pet_dog.wav` | Petting the stray dog |
| `turn_on_run.wav` | Toggling run on |
| `pk_chest.wav` | Opening the wilderness loot key chest |
| `toa_chest.wav` | TOA sarcophagus opens |
| `cox_purple.wav` / `cox_white.wav` | CoX raid ends with / without a unique |
| `redemption.wav` | Redemption proc (replaces sound 2681) |
| `dds_spec.wav` / `ags_spec.wav` / `acb_spec.wav` | Spec sounds (replaces 2537 / 3869 / 3892) |
| `ruby_bolt.wav` | Ruby bolt proc (replaces sound 2911) |
| `bank_pin.wav` | Bank PIN buttons (replaces sound 1041) |
| `zebak_roar.wav` | Zebak's roar (replaces sound 5829) |
| `teleport.wav` | Standard teleport sound (replaces sound 200) |

\* Requires the corresponding in-game notification setting to be enabled.

Note: level-up, quest, and diary fanfares are jingles (music system), so those presets
play *over* the vanilla jingle rather than replacing it — lower the in-game Music
volume if you want only your custom audio. Sound-ID presets are true replacements.

## Creating sound files

Files must be real 16-bit PCM WAV — renaming an mp3 does not work. A companion desktop
app, **Sound Override Studio**, records/imports audio, trims and levels it, and saves
correctly-named WAVs straight into the overrides folder:
https://github.com/jarredgoddard/sound-override-studio

## Credits

Event detection logic in this plugin is ported from two excellent plugins, both
BSD 2-Clause licensed — huge thanks to their authors:

- [c-engineer-completed](https://github.com/m0bilebtw/c-engineer-completed) by m0bile btw
  (combat/slayer task, farming contract, hunter rumour, key drop, death, and gemstone
  crab detection)
- [odablock-sounds](https://github.com/DapperMickie/odablock-sounds) by DapperMickie
  (pet, vengeance, redemption, prayer-down, spec weapon, trade, random event, run,
  loot chest, TOA and CoX detection; CoX logic originally from
  [cox-light-colors](https://github.com/AnkouOSRS/cox-light-colors))

This plugin differs from both by letting users replace every sound in the pack
with their own audio files.

## Development

Requires JDK 11. Open in IntelliJ as a Gradle project and run
`SoundOverridePluginTest` to launch a RuneLite client with the plugin loaded.
