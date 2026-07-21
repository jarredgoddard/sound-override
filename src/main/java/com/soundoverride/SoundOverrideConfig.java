package com.soundoverride;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("soundoverride")
public interface SoundOverrideConfig extends Config
{
	@ConfigSection(name = "Skilling & Achievements", description = "Sounds for levelling, quests, and achievements", position = 1)
	String achievementsSection = "achievements";

	@ConfigSection(name = "Drops & Pets", description = "Sounds for drops, keys, and pets", position = 2)
	String dropsSection = "drops";

	@ConfigSection(name = "Combat & PvP", description = "Sounds for combat, spec weapons, and PvP", position = 3)
	String combatSection = "combat";

	@ConfigSection(name = "Raids", description = "Sounds for raid events", position = 4)
	String raidsSection = "raids";

	@ConfigSection(name = "Interactions & Misc", description = "Sounds for trades, teleports, and other interactions", position = 5)
	String miscSection = "misc";

	@Range(min = 0, max = 100)
	@ConfigItem(keyName = "volume", name = "Volume", description = "Playback volume for custom sounds (0-100)", position = 0)
	default int volume()
	{
		return 80;
	}

	// ---- Skilling & Achievements ----

	@ConfigItem(keyName = "levelUpEnabled", name = "Level up", description = "Play sound when you gain a level in any skill. File: level_up.wav", position = 1, section = achievementsSection)
	default boolean levelUpEnabled() { return true; }

	@ConfigItem(keyName = "questCompleteEnabled", name = "Quest complete", description = "Play sound when the quest completed scroll appears. File: quest_complete.wav", position = 2, section = achievementsSection)
	default boolean questCompleteEnabled() { return true; }

	@ConfigItem(keyName = "diaryCompleteEnabled", name = "Diary complete", description = "Play sound when you complete an achievement diary tier. File: diary_complete.wav", position = 3, section = achievementsSection)
	default boolean diaryCompleteEnabled() { return true; }

	@ConfigItem(keyName = "collectionLogEnabled", name = "Collection log", description = "Play sound on a new collection log slot. Requires the in-game 'Collection log - New addition notification' setting. File: collection_log.wav", position = 4, section = achievementsSection)
	default boolean collectionLogEnabled() { return true; }

	@ConfigItem(keyName = "combatTaskEnabled", name = "Combat task", description = "Play sound when you complete a combat achievement task. File: combat_task.wav", position = 5, section = achievementsSection)
	default boolean combatTaskEnabled() { return true; }

	@ConfigItem(keyName = "slayerTaskEnabled", name = "Slayer task", description = "Play sound when you complete a slayer task. File: slayer_task.wav", position = 6, section = achievementsSection)
	default boolean slayerTaskEnabled() { return true; }

	@ConfigItem(keyName = "farmingContractEnabled", name = "Farming contract", description = "Play sound when you complete a Farming Guild contract. File: farming_contract.wav", position = 7, section = achievementsSection)
	default boolean farmingContractEnabled() { return true; }

	@ConfigItem(keyName = "hunterRumourEnabled", name = "Hunter rumour", description = "Play sound when you find the rare creature piece for a hunter rumour. File: hunter_rumour.wav", position = 8, section = achievementsSection)
	default boolean hunterRumourEnabled() { return true; }

	// ---- Drops & Pets ----

	@ConfigItem(keyName = "rareDropEnabled", name = "Rare drop", description = "Play sound on valuable/untradeable drop messages. Requires the in-game loot drop notification settings. File: rare_drop.wav", position = 1, section = dropsSection)
	default boolean rareDropEnabled() { return true; }

	@ConfigItem(keyName = "petEnabled", name = "Pet drop", description = "Play sound on the 'funny feeling' pet drop messages, including the backpack variant. File: pet.wav", position = 2, section = dropsSection)
	default boolean petEnabled() { return true; }

	@ConfigItem(keyName = "grubbyKeyEnabled", name = "Grubby key", description = "Play sound when a grubby key drops. File: grubby_key.wav", position = 3, section = dropsSection)
	default boolean grubbyKeyEnabled() { return true; }

	@ConfigItem(keyName = "larransKeyEnabled", name = "Larran's key", description = "Play sound when a Larran's key drops. File: larrans_key.wav", position = 4, section = dropsSection)
	default boolean larransKeyEnabled() { return true; }

	@ConfigItem(keyName = "brimstoneKeyEnabled", name = "Brimstone key", description = "Play sound when a brimstone key drops. File: brimstone_key.wav", position = 5, section = dropsSection)
	default boolean brimstoneKeyEnabled() { return true; }

	@ConfigItem(keyName = "gemCrabEnabled", name = "Gemstone crab", description = "Play sound when the gemstone crab you're fighting burrows and moves. File: gem_crab.wav", position = 6, section = dropsSection)
	default boolean gemCrabEnabled() { return true; }

	// ---- Combat & PvP ----

	@ConfigItem(keyName = "deathEnabled", name = "Death", description = "Play sound when you die. File: death.wav", position = 1, section = combatSection)
	default boolean deathEnabled() { return true; }

	@ConfigItem(keyName = "vengeanceEnabled", name = "Vengeance", description = "Play sound when you cast Vengeance. File: vengeance.wav", position = 2, section = combatSection)
	default boolean vengeanceEnabled() { return true; }

	@ConfigItem(keyName = "redemptionEnabled", name = "Redemption proc", description = "Replace the redemption proc sound effect. File: redemption.wav", position = 3, section = combatSection)
	default boolean redemptionEnabled() { return true; }

	@ConfigItem(keyName = "smitedEnabled", name = "Prayer hits 0", description = "Play sound when your prayer points drop to 0. Fires on any drain reaching zero, not only smite. File: smited.wav", position = 4, section = combatSection)
	default boolean smitedEnabled() { return false; }

	@ConfigItem(keyName = "ddsSpecEnabled", name = "DDS spec", description = "Replace the dragon dagger special attack sound. File: dds_spec.wav", position = 5, section = combatSection)
	default boolean ddsSpecEnabled() { return true; }

	@ConfigItem(keyName = "agsSpecEnabled", name = "AGS spec", description = "Replace the Armadyl godsword special attack sound. File: ags_spec.wav", position = 6, section = combatSection)
	default boolean agsSpecEnabled() { return true; }

	@ConfigItem(keyName = "acbSpecEnabled", name = "ACB spec", description = "Replace the Armadyl crossbow special attack sound. File: acb_spec.wav", position = 7, section = combatSection)
	default boolean acbSpecEnabled() { return true; }

	@ConfigItem(keyName = "rubyBoltEnabled", name = "Ruby bolt proc", description = "Replace the ruby bolt special effect sound. File: ruby_bolt.wav", position = 8, section = combatSection)
	default boolean rubyBoltEnabled() { return true; }

	@ConfigItem(keyName = "pkChestEnabled", name = "Loot key chest", description = "Play sound when you open the wilderness loot key chest. File: pk_chest.wav", position = 9, section = combatSection)
	default boolean pkChestEnabled() { return true; }

	// ---- Raids ----

	@ConfigItem(keyName = "toaChestEnabled", name = "TOA chest", description = "Play sound when the TOA sarcophagus opens. File: toa_chest.wav", position = 1, section = raidsSection)
	default boolean toaChestEnabled() { return true; }

	@ConfigItem(keyName = "coxPurpleEnabled", name = "CoX purple", description = "Play sound when your CoX raid ends with a unique drop. File: cox_purple.wav", position = 2, section = raidsSection)
	default boolean coxPurpleEnabled() { return true; }

	@ConfigItem(keyName = "coxWhiteEnabled", name = "CoX no purple", description = "Play sound when your CoX raid ends without a unique drop. File: cox_white.wav", position = 3, section = raidsSection)
	default boolean coxWhiteEnabled() { return true; }

	@ConfigItem(keyName = "zebakRoarEnabled", name = "Zebak roar", description = "Replace Zebak's roar sound. File: zebak_roar.wav", position = 4, section = raidsSection)
	default boolean zebakRoarEnabled() { return true; }

	// ---- Interactions & Misc ----

	@ConfigItem(keyName = "ectophialEnabled", name = "Ectophial teleport", description = "Replace the ectophial teleport sound effect. File: ectophial.wav", position = 1, section = miscSection)
	default boolean ectophialEnabled() { return true; }

	@ConfigItem(keyName = "ectophialSoundId", name = "Ectophial sound ID", description = "Sound effect ID to treat as the ectophial teleport. Default 200 is the shared teleport sound", position = 2, section = miscSection)
	default int ectophialSoundId() { return 200; }

	@ConfigItem(keyName = "acceptTradeEnabled", name = "Accept trade", description = "Play sound when a trade is accepted. File: accept_trade.wav", position = 3, section = miscSection)
	default boolean acceptTradeEnabled() { return true; }

	@ConfigItem(keyName = "declineTradeEnabled", name = "Decline trade", description = "Play sound when you decline a trade. File: decline_trade.wav", position = 4, section = miscSection)
	default boolean declineTradeEnabled() { return true; }

	@ConfigItem(keyName = "dismissRandomEnabled", name = "Dismiss random event", description = "Play sound when you dismiss a random event. File: dismiss_random.wav", position = 5, section = miscSection)
	default boolean dismissRandomEnabled() { return true; }

	@ConfigItem(keyName = "petDogEnabled", name = "Pet the dog", description = "Play sound when you pet the stray dog. File: pet_dog.wav", position = 6, section = miscSection)
	default boolean petDogEnabled() { return true; }

	@ConfigItem(keyName = "turnOnRunEnabled", name = "Turn on run", description = "Play sound when you toggle run on. File: turn_on_run.wav", position = 7, section = miscSection)
	default boolean turnOnRunEnabled() { return true; }

	@ConfigItem(keyName = "bankPinEnabled", name = "Bank PIN", description = "Replace the bank PIN button sound. File: bank_pin.wav", position = 8, section = miscSection)
	default boolean bankPinEnabled() { return true; }
}
