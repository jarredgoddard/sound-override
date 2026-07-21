package com.soundoverride;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("soundoverride")
public interface SoundOverrideConfig extends Config
{
	@ConfigSection(
		name = "General",
		description = "General settings",
		position = 0
	)
	String generalSection = "general";

	@ConfigSection(
		name = "Presets",
		description = "Toggle individual preset triggers. Drop matching wav files in .runelite/sound-overrides/",
		position = 1
	)
	String presetSection = "presets";

	// ---------------- General ----------------

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Playback volume for custom sounds (0-100)",
		position = 1,
		section = generalSection
	)
	default int volume()
	{
		return 80;
	}

	@ConfigItem(
		keyName = "overrideAreaSounds",
		name = "Override area sounds",
		description = "Also intercept ambient/area sound effects",
		position = 2,
		section = generalSection
	)
	default boolean overrideAreaSounds()
	{
		return true;
	}

	@ConfigItem(
		keyName = "logSoundIds",
		name = "Log sound IDs",
		description = "Log every unmatched sound effect ID to the client log — useful for discovering IDs to override",
		position = 3,
		section = generalSection
	)
	default boolean logSoundIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "idOverridesEnabled",
		name = "Enable ID overrides",
		description = "Master toggle for numeric sound ID overrides (e.g. 2739.wav)",
		position = 4,
		section = generalSection
	)
	default boolean idOverridesEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "disabledSoundIds",
		name = "Disabled sound IDs",
		description = "Comma-separated sound IDs to temporarily disable without deleting their wav files (e.g. 2739, 200)",
		position = 5,
		section = generalSection
	)
	default String disabledSoundIds()
	{
		return "";
	}

	// ---------------- Presets ----------------

	@ConfigItem(
		keyName = "levelUpEnabled",
		name = "Level up (level_up.wav)",
		description = "Play custom sound when you gain a level in any skill",
		position = 10,
		section = presetSection
	)
	default boolean levelUpEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "questCompleteEnabled",
		name = "Quest complete (quest_complete.wav)",
		description = "Play custom sound when the quest completed scroll appears",
		position = 11,
		section = presetSection
	)
	default boolean questCompleteEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "diaryCompleteEnabled",
		name = "Diary complete (diary_complete.wav)",
		description = "Play custom sound when you complete an achievement diary tier",
		position = 12,
		section = presetSection
	)
	default boolean diaryCompleteEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "collectionLogEnabled",
		name = "Collection log (collection_log.wav)",
		description = "Play custom sound on new collection log slot. Requires the in-game 'Collection log - New addition notification' setting to be enabled",
		position = 13,
		section = presetSection
	)
	default boolean collectionLogEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rareDropEnabled",
		name = "Rare drop (rare_drop.wav)",
		description = "Play custom sound on valuable/untradeable drop messages. Requires the in-game loot drop notification settings to be enabled",
		position = 14,
		section = presetSection
	)
	default boolean rareDropEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ectophialEnabled",
		name = "Ectophial (ectophial.wav)",
		description = "Replace the ectophial teleport sound effect",
		position = 15,
		section = presetSection
	)
	default boolean ectophialEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ectophialSoundId",
		name = "Ectophial sound ID",
		description = "Sound effect ID to treat as the ectophial teleport. Default 200 is the shared teleport sound; enable 'Log sound IDs' and teleport once to find the exact ID if it differs",
		position = 16,
		section = presetSection
	)
	default int ectophialSoundId()
	{
		return 200;
	}

	// ---- Ported from C Engineer: Completed ----

	@ConfigItem(
		keyName = "combatTaskEnabled",
		name = "Combat task (combat_task.wav)",
		description = "Play custom sound when you complete a combat achievement task",
		position = 20,
		section = presetSection
	)
	default boolean combatTaskEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "slayerTaskEnabled",
		name = "Slayer task (slayer_task.wav)",
		description = "Play custom sound when you complete a slayer task",
		position = 21,
		section = presetSection
	)
	default boolean slayerTaskEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "farmingContractEnabled",
		name = "Farming contract (farming_contract.wav)",
		description = "Play custom sound when you complete a Farming Guild contract",
		position = 22,
		section = presetSection
	)
	default boolean farmingContractEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hunterRumourEnabled",
		name = "Hunter rumour (hunter_rumour.wav)",
		description = "Play custom sound when you find the rare creature piece for a hunter rumour",
		position = 23,
		section = presetSection
	)
	default boolean hunterRumourEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "deathEnabled",
		name = "Death (death.wav)",
		description = "Play custom sound when you die",
		position = 24,
		section = presetSection
	)
	default boolean deathEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "grubbyKeyEnabled",
		name = "Grubby key (grubby_key.wav)",
		description = "Play custom sound when a grubby key drops",
		position = 25,
		section = presetSection
	)
	default boolean grubbyKeyEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "larransKeyEnabled",
		name = "Larran's key (larrans_key.wav)",
		description = "Play custom sound when a Larran's key drops",
		position = 26,
		section = presetSection
	)
	default boolean larransKeyEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "brimstoneKeyEnabled",
		name = "Brimstone key (brimstone_key.wav)",
		description = "Play custom sound when a brimstone key drops",
		position = 27,
		section = presetSection
	)
	default boolean brimstoneKeyEnabled()
	{
		return true;
	}

	// ---- Ported from Odablock Sounds ----

	@ConfigItem(
		keyName = "petEnabled",
		name = "Pet (pet.wav)",
		description = "Play custom sound on the 'funny feeling' pet drop messages, including the backpack variant",
		position = 28,
		section = presetSection
	)
	default boolean petEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "vengeanceEnabled",
		name = "Vengeance (vengeance.wav)",
		description = "Play custom sound when you cast Vengeance",
		position = 29,
		section = presetSection
	)
	default boolean vengeanceEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "redemptionEnabled",
		name = "Redemption (redemption.wav)",
		description = "Replace the redemption proc sound effect (true replacement — consumes sound ID 2681)",
		position = 30,
		section = presetSection
	)
	default boolean redemptionEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "smitedEnabled",
		name = "Prayer hits 0 (smited.wav)",
		description = "Play custom sound when your prayer points drop to 0. Fires on any drain reaching zero, not only smite — off by default",
		position = 31,
		section = presetSection
	)
	default boolean smitedEnabled()
	{
		return false;
	}

	// ---- Spec weapons & procs (sound ID replacements) ----

	@ConfigItem(
		keyName = "ddsSpecEnabled",
		name = "DDS spec (dds_spec.wav)",
		description = "Replace the dragon dagger special attack sound (consumes sound ID 2537)",
		position = 40,
		section = presetSection
	)
	default boolean ddsSpecEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "agsSpecEnabled",
		name = "AGS spec (ags_spec.wav)",
		description = "Replace the Armadyl godsword special attack sound (consumes sound ID 3869)",
		position = 41,
		section = presetSection
	)
	default boolean agsSpecEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "acbSpecEnabled",
		name = "ACB spec (acb_spec.wav)",
		description = "Replace the Armadyl crossbow special attack sound (consumes sound ID 3892)",
		position = 42,
		section = presetSection
	)
	default boolean acbSpecEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rubyBoltEnabled",
		name = "Ruby bolt proc (ruby_bolt.wav)",
		description = "Replace the ruby bolt special effect sound (consumes sound ID 2911)",
		position = 43,
		section = presetSection
	)
	default boolean rubyBoltEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "bankPinEnabled",
		name = "Bank PIN (bank_pin.wav)",
		description = "Replace the bank PIN button sound (consumes sound ID 1041)",
		position = 44,
		section = presetSection
	)
	default boolean bankPinEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "zebakRoarEnabled",
		name = "Zebak roar (zebak_roar.wav)",
		description = "Replace Zebak's roar sound (consumes sound ID 5829)",
		position = 45,
		section = presetSection
	)
	default boolean zebakRoarEnabled()
	{
		return true;
	}

	// ---- Interactions ----

	@ConfigItem(
		keyName = "acceptTradeEnabled",
		name = "Accept trade (accept_trade.wav)",
		description = "Play custom sound when a trade is accepted",
		position = 50,
		section = presetSection
	)
	default boolean acceptTradeEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "declineTradeEnabled",
		name = "Decline trade (decline_trade.wav)",
		description = "Play custom sound when you decline a trade",
		position = 51,
		section = presetSection
	)
	default boolean declineTradeEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dismissRandomEnabled",
		name = "Dismiss random event (dismiss_random.wav)",
		description = "Play custom sound when you dismiss a random event",
		position = 52,
		section = presetSection
	)
	default boolean dismissRandomEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "petDogEnabled",
		name = "Pet the dog (pet_dog.wav)",
		description = "Play custom sound when you pet the stray dog",
		position = 53,
		section = presetSection
	)
	default boolean petDogEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "turnOnRunEnabled",
		name = "Turn on run (turn_on_run.wav)",
		description = "Play custom sound when you toggle run on",
		position = 54,
		section = presetSection
	)
	default boolean turnOnRunEnabled()
	{
		return true;
	}

	// ---- Raids & PvP ----

	@ConfigItem(
		keyName = "pkChestEnabled",
		name = "Loot key chest (pk_chest.wav)",
		description = "Play custom sound when you open the wilderness loot key chest",
		position = 60,
		section = presetSection
	)
	default boolean pkChestEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "toaChestEnabled",
		name = "TOA chest (toa_chest.wav)",
		description = "Play custom sound when the TOA sarcophagus opens",
		position = 61,
		section = presetSection
	)
	default boolean toaChestEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "coxPurpleEnabled",
		name = "CoX purple (cox_purple.wav)",
		description = "Play custom sound when your CoX raid ends with a unique drop",
		position = 62,
		section = presetSection
	)
	default boolean coxPurpleEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "coxWhiteEnabled",
		name = "CoX no purple (cox_white.wav)",
		description = "Play custom sound when your CoX raid ends without a unique drop",
		position = 63,
		section = presetSection
	)
	default boolean coxWhiteEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "gemCrabEnabled",
		name = "Gemstone crab (gem_crab.wav)",
		description = "Play custom sound when the gemstone crab you're fighting burrows and moves",
		position = 64,
		section = presetSection
	)
	default boolean gemCrabEnabled()
	{
		return true;
	}
}
