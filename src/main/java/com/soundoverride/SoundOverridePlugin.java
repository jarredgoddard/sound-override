package com.soundoverride;

import com.google.inject.Provides;
import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Slyestcat Sound Pack",
	description = "Replace game sound effects and play custom audio on game events",
	tags = {"sound", "audio", "custom", "override", "level", "quest", "collection log", "pet", "death", "spec"}
)
public class SoundOverridePlugin extends Plugin
{
	public static final File SOUND_DIR = new File(RuneLite.RUNELITE_DIR, "sound-overrides");

	// Quest completed scroll interface
	private static final int QUEST_COMPLETED_GROUP_ID = 153;
	// Vengeance active varbit (from odablock-sounds Vengeance)
	private static final int VENGEANCE_VARBIT = 2450;
	// Run toggle varp (from odablock-sounds OdablockVarbits.RUNNING)
	private static final int RUNNING_VARP = 173;
	// Trade screen widget groups (from odablock-sounds DeclineTrade)
	private static final List<Integer> TRADE_SCREEN_GROUP_IDS = Arrays.asList(334, 335);
	// Widget ids where 'Dismiss' is NOT a random event (from odablock-sounds DismissRandomEvent)
	private static final int RUNE_POUCH_WIDGET_ID = 983062;
	private static final int LOOTING_BAG_WIDGET_ID = 983048;
	// Pet dog menu id (from odablock-sounds PetDog)
	private static final int PET_DOG_MENU_ID = 23766;
	// Loot key chest widget group (from odablock-sounds PkChest)
	private static final int PK_CHEST_GROUP_ID = 742;
	// TOA sarcophagus object, spawns as the chest is looted (from odablock-sounds ToaChestOpens)
	private static final int TOA_SARCOPHAGUS_ID = 44934;
	// Gemstone crab burrow detection (from c-engineer-completed QualityOfLifeTriggers)
	private static final String GEMSTONE_CRAB_ACTOR_NAME = "Gemstone Crab";

	// Chat patterns (regexes/messages ported from c-engineer-completed and odablock-sounds)
	private static final Pattern COLLECTION_LOG_PATTERN =
		Pattern.compile("New item added to your collection log:.*");
	private static final Pattern DIARY_PATTERN =
		Pattern.compile("Congratulations! You have completed (?:all of the .+ tasks|the .+ (?:achievement )?diary).*", Pattern.CASE_INSENSITIVE);
	private static final Pattern RARE_DROP_PATTERN =
		Pattern.compile("(Valuable drop|Untradeable drop): .*");
	private static final Pattern COMBAT_TASK_PATTERN =
		Pattern.compile("(?:CA_ID:\\d+\\|)?Congratulations, you've completed an? \\w+ combat task:.*");
	private static final Pattern SLAYER_TASK_PATTERN =
		Pattern.compile("You have completed your task! You killed .*\\. You gained .* xp\\..*");
	private static final Pattern COX_SPECIAL_DROP_PATTERN = Pattern.compile("(.+) - (.+)");
	private static final String FARMING_CONTRACT_MESSAGE =
		"You've completed a Farming Guild Contract. You should return to Guildmaster Jane.";
	private static final String HUNTER_RUMOUR_MESSAGE =
		"You find a rare piece of the creature! You should take it back to the Hunter Guild.";
	private static final String HUNTER_RUMOUR_FULL_INV_MESSAGE =
		"You find a rare piece of the creature! Though without space in your inventory, it drops to the ground.";
	private static final String PET_FOLLOWING_PREFIX = "You have a funny feeling";
	private static final String PET_BACKPACK_MESSAGE = "You feel something weird sneaking into your backpack.";

	@Inject
	private Client client;

	@Inject
	private SoundOverrideConfig config;

	@Inject
	private SoundManager soundManager;

	// preset name -> custom wav (event-triggered, plays over any vanilla jingle)
	private final Map<Preset, File> presetSounds = new EnumMap<>(Preset.class);
	// presets with a default sound packed into the plugin jar
	private final java.util.Set<Preset> bundledPresets = java.util.EnumSet.noneOf(Preset.class);
	// fixed sound effect id -> preset (true replacements, from odablock-sounds SoundIds)
	private static final Map<Integer, Preset> FIXED_SOUND_PRESETS = new HashMap<>();
	static
	{
		FIXED_SOUND_PRESETS.put(200, Preset.TELEPORT);
		FIXED_SOUND_PRESETS.put(2681, Preset.REDEMPTION);
		FIXED_SOUND_PRESETS.put(2537, Preset.DDS_SPEC);
		FIXED_SOUND_PRESETS.put(3869, Preset.AGS_SPEC);
		FIXED_SOUND_PRESETS.put(3892, Preset.ACB_SPEC);
		FIXED_SOUND_PRESETS.put(2911, Preset.RUBY_BOLT);
		FIXED_SOUND_PRESETS.put(1041, Preset.BANK_PIN);
		FIXED_SOUND_PRESETS.put(5829, Preset.ZEBAK_ROAR);
	}

	// Track real skill levels so we can detect actual level-ups vs login stat flood
	private final Map<Skill, Integer> previousLevels = new EnumMap<>(Skill.class);
	private boolean statsPopulated = false;
	private int lastPrayer = -1;
	private boolean isRunning = false;
	// CoX end-of-raid chest light tracking (from odablock-sounds CoxSounds)
	private int coxEndedRaidTick = -1;
	private boolean coxWhiteLight = true;

	enum Preset
	{
		LEVEL_UP("level_up", "Level up", "levelUpEnabled"),
		QUEST_COMPLETE("quest_complete", "Quest complete", "questCompleteEnabled"),
		DIARY_COMPLETE("diary_complete", "Diary complete", "diaryCompleteEnabled"),
		COLLECTION_LOG("collection_log", "Collection log", "collectionLogEnabled"),
		RARE_DROP("rare_drop", "Rare drop", "rareDropEnabled"),
		TELEPORT("teleport", "Teleport", "teleportEnabled"),
		// Ported from C Engineer: Completed
		COMBAT_TASK("combat_task", "Combat task", "combatTaskEnabled"),
		SLAYER_TASK("slayer_task", "Slayer task", "slayerTaskEnabled"),
		FARMING_CONTRACT("farming_contract", "Farming contract", "farmingContractEnabled"),
		HUNTER_RUMOUR("hunter_rumour", "Hunter rumour", "hunterRumourEnabled"),
		DEATH("death", "Death", "deathEnabled"),
		GRUBBY_KEY("grubby_key", "Grubby key", "grubbyKeyEnabled"),
		LARRANS_KEY("larrans_key", "Larran's key", "larransKeyEnabled"),
		BRIMSTONE_KEY("brimstone_key", "Brimstone key", "brimstoneKeyEnabled"),
		// Ported from Odablock Sounds
		PET("pet", "Pet drop", "petEnabled"),
		VENGEANCE("vengeance", "Vengeance", "vengeanceEnabled"),
		REDEMPTION("redemption", "Redemption proc", "redemptionEnabled"),
		SMITED("smited", "Prayer hits 0", "smitedEnabled"),
		DDS_SPEC("dds_spec", "DDS spec", "ddsSpecEnabled"),
		AGS_SPEC("ags_spec", "AGS spec", "agsSpecEnabled"),
		ACB_SPEC("acb_spec", "ACB spec", "acbSpecEnabled"),
		RUBY_BOLT("ruby_bolt", "Ruby bolt proc", "rubyBoltEnabled"),
		BANK_PIN("bank_pin", "Bank PIN", "bankPinEnabled"),
		ZEBAK_ROAR("zebak_roar", "Zebak roar", "zebakRoarEnabled"),
		ACCEPT_TRADE("accept_trade", "Accept trade", "acceptTradeEnabled"),
		DECLINE_TRADE("decline_trade", "Decline trade", "declineTradeEnabled"),
		DISMISS_RANDOM("dismiss_random", "Dismiss random event", "dismissRandomEnabled"),
		PET_DOG("pet_dog", "Pet the dog", "petDogEnabled"),
		TURN_ON_RUN("turn_on_run", "Turn on run", "turnOnRunEnabled"),
		PK_CHEST("pk_chest", "Loot key chest", "pkChestEnabled"),
		TOA_CHEST("toa_chest", "TOA chest", "toaChestEnabled"),
		COX_PURPLE("cox_purple", "CoX purple", "coxPurpleEnabled"),
		COX_WHITE("cox_white", "CoX no purple", "coxWhiteEnabled"),
		GEM_CRAB("gem_crab", "Gemstone crab", "gemCrabEnabled");

		final String fileName;
		final String displayName;
		final String configKey;

		Preset(String fileName, String displayName, String configKey)
		{
			this.fileName = fileName;
			this.displayName = displayName;
			this.configKey = configKey;
		}
	}

	@Provides
	SoundOverrideConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SoundOverrideConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (!SOUND_DIR.exists())
		{
			SOUND_DIR.mkdirs();
		}

		// Detect which presets ship with a bundled default in the jar
		bundledPresets.clear();
		for (Preset preset : Preset.values())
		{
			if (SoundOverridePlugin.class.getResource("sounds/" + preset.fileName + ".wav") != null)
			{
				bundledPresets.add(preset);
			}
		}
		log.debug("{} bundled preset sounds found", bundledPresets.size());

		loadOverrides();
	}

	@Override
	protected void shutDown()
	{
		presetSounds.clear();
		previousLevels.clear();
		statsPopulated = false;
		lastPrayer = -1;
		coxEndedRaidTick = -1;
		soundManager.close();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("soundoverride".equals(event.getGroup()))
		{
			loadOverrides();
		}
	}

	/**
	 * Debug commands:
	 *   ::sotest level_up   — play a preset by name through the plugin's sound path
	 *   ::sotest 2739       — play an ID override
	 *   ::solist            — list loaded presets and ID overrides in chat
	 */
	@Subscribe
	public void onCommandExecuted(net.runelite.api.events.CommandExecuted event)
	{
		if ("solist".equalsIgnoreCase(event.getCommand()))
		{
			StringBuilder presets = new StringBuilder("Presets loaded: ");
			presetSounds.keySet().forEach(p -> presets.append(p.fileName).append(" "));
			chat(presetSounds.isEmpty() ? "No presets loaded." : presets.toString());
			return;
		}

		if (!"sotest".equalsIgnoreCase(event.getCommand()))
		{
			return;
		}

		String[] args = event.getArguments();
		if (args.length == 0)
		{
			chat("Usage: ::sotest <preset name> — e.g. ::sotest level_up. ::solist to see what's loaded.");
			return;
		}

		String arg = args[0].toLowerCase();

		for (Preset preset : Preset.values())
		{
			if (preset.fileName.equals(arg))
			{
				File f = presetSounds.get(preset);
				if (f != null)
				{
					chat("Playing preset " + arg + " (folder override)");
					soundManager.play(f, config.volume());
				}
				else if (bundledPresets.contains(preset))
				{
					chat("Playing preset " + arg + " (bundled)");
					soundManager.playBundled("sounds/" + preset.fileName + ".wav", config.volume());
				}
				else
				{
					chat("No sound for preset '" + arg + "' — no bundled default and no " + arg + ".wav in the sound-overrides folder.");
				}
				return;
			}
		}

		chat("Unknown preset '" + arg + "'. ::solist shows loaded sounds.");
	}

	private void chat(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[SoundOverride] " + message, null);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Reset trackers on login so the initial stat flood doesn't fire level_up / smited
		if (event.getGameState() == GameState.LOGGING_IN
			|| event.getGameState() == GameState.HOPPING)
		{
			previousLevels.clear();
			statsPopulated = false;
			lastPrayer = -1;
			coxEndedRaidTick = -1;
		}
	}

	// ------------------------------------------------------------------
	// Raw sound effect replacement (consume vanilla, play custom)
	// ------------------------------------------------------------------

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		handleSoundEffect(event.getSoundId(), () -> event.consume());
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		handleSoundEffect(event.getSoundId(), () -> event.consume());
	}

	private void handleSoundEffect(int soundId, Runnable consume)
	{
		// Fixed-ID preset replacements (redemption, spec weapons, ruby bolt, bank pin, Zebak)
		Preset fixed = FIXED_SOUND_PRESETS.get(soundId);
		if (fixed != null && presetEnabled(fixed) && hasSound(fixed))
		{
			consume.run();
			playPreset(fixed);
			return;
		}

	}

	boolean presetEnabled(Preset preset)
	{
		switch (preset)
		{
			case LEVEL_UP: return config.levelUpEnabled();
			case QUEST_COMPLETE: return config.questCompleteEnabled();
			case DIARY_COMPLETE: return config.diaryCompleteEnabled();
			case COLLECTION_LOG: return config.collectionLogEnabled();
			case RARE_DROP: return config.rareDropEnabled();
			case TELEPORT: return config.teleportEnabled();
			case COMBAT_TASK: return config.combatTaskEnabled();
			case SLAYER_TASK: return config.slayerTaskEnabled();
			case FARMING_CONTRACT: return config.farmingContractEnabled();
			case HUNTER_RUMOUR: return config.hunterRumourEnabled();
			case DEATH: return config.deathEnabled();
			case GRUBBY_KEY: return config.grubbyKeyEnabled();
			case LARRANS_KEY: return config.larransKeyEnabled();
			case BRIMSTONE_KEY: return config.brimstoneKeyEnabled();
			case PET: return config.petEnabled();
			case VENGEANCE: return config.vengeanceEnabled();
			case REDEMPTION: return config.redemptionEnabled();
			case SMITED: return config.smitedEnabled();
			case DDS_SPEC: return config.ddsSpecEnabled();
			case AGS_SPEC: return config.agsSpecEnabled();
			case ACB_SPEC: return config.acbSpecEnabled();
			case RUBY_BOLT: return config.rubyBoltEnabled();
			case BANK_PIN: return config.bankPinEnabled();
			case ZEBAK_ROAR: return config.zebakRoarEnabled();
			case ACCEPT_TRADE: return config.acceptTradeEnabled();
			case DECLINE_TRADE: return config.declineTradeEnabled();
			case DISMISS_RANDOM: return config.dismissRandomEnabled();
			case PET_DOG: return config.petDogEnabled();
			case TURN_ON_RUN: return config.turnOnRunEnabled();
			case PK_CHEST: return config.pkChestEnabled();
			case TOA_CHEST: return config.toaChestEnabled();
			case COX_PURPLE: return config.coxPurpleEnabled();
			case COX_WHITE: return config.coxWhiteEnabled();
			case GEM_CRAB: return config.gemCrabEnabled();
			default: return true;
		}
	}

	// ------------------------------------------------------------------
	// Event-based presets
	// ------------------------------------------------------------------

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!config.levelUpEnabled())
		{
			return;
		}

		Skill skill = event.getSkill();
		int newLevel = event.getLevel(); // real (unboosted) level
		Integer previous = previousLevels.put(skill, newLevel);

		if (!statsPopulated)
		{
			if (previousLevels.size() >= Skill.values().length)
			{
				statsPopulated = true;
			}
			return;
		}

		if (previous != null && newLevel > previous)
		{
			playPreset(Preset.LEVEL_UP);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (config.questCompleteEnabled() && event.getGroupId() == QUEST_COMPLETED_GROUP_ID)
		{
			playPreset(Preset.QUEST_COMPLETE);
		}
		else if (config.pkChestEnabled() && event.getGroupId() == PK_CHEST_GROUP_ID)
		{
			playPreset(Preset.PK_CHEST);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		// Accept trade: game-sent message with no sender (from odablock-sounds AcceptTrade)
		if (config.acceptTradeEnabled()
			&& "accepted trade.".equals(Text.standardize(event.getMessage()))
			&& (event.getName() == null || event.getName().isEmpty()))
		{
			playPreset(Preset.ACCEPT_TRADE);
			return;
		}

		// CoX end-of-raid light color (from odablock-sounds CoxSounds)
		if (event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION
			&& (config.coxPurpleEnabled() || config.coxWhiteEnabled()))
		{
			String coxMessage = Text.removeTags(event.getMessage());
			if (coxMessage.contains("your raid is complete!"))
			{
				coxWhiteLight = true;
				coxEndedRaidTick = client.getTickCount();
				return;
			}
			Matcher matcher = COX_SPECIAL_DROP_PATTERN.matcher(coxMessage);
			if (matcher.find())
			{
				coxWhiteLight = false;
				return;
			}
		}

		if (event.getType() != ChatMessageType.GAMEMESSAGE
			&& event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (config.collectionLogEnabled() && COLLECTION_LOG_PATTERN.matcher(message).matches())
		{
			playPreset(Preset.COLLECTION_LOG);
		}
		else if (config.diaryCompleteEnabled() && DIARY_PATTERN.matcher(message).matches())
		{
			playPreset(Preset.DIARY_COMPLETE);
		}
		else if (config.rareDropEnabled() && RARE_DROP_PATTERN.matcher(message).matches())
		{
			playPreset(Preset.RARE_DROP);
		}
		else if (config.combatTaskEnabled() && COMBAT_TASK_PATTERN.matcher(message).matches())
		{
			playPreset(Preset.COMBAT_TASK);
		}
		else if (config.slayerTaskEnabled() && SLAYER_TASK_PATTERN.matcher(message).matches())
		{
			playPreset(Preset.SLAYER_TASK);
		}
		else if (config.farmingContractEnabled() && FARMING_CONTRACT_MESSAGE.equals(message))
		{
			playPreset(Preset.FARMING_CONTRACT);
		}
		else if (config.hunterRumourEnabled()
			&& (HUNTER_RUMOUR_MESSAGE.equals(message) || HUNTER_RUMOUR_FULL_INV_MESSAGE.equals(message)))
		{
			playPreset(Preset.HUNTER_RUMOUR);
		}
		else if (config.petEnabled()
			&& (message.startsWith(PET_FOLLOWING_PREFIX) || PET_BACKPACK_MESSAGE.equals(message)))
		{
			playPreset(Preset.PET);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String option = event.getMenuOption();
		if (option == null)
		{
			return;
		}

		switch (option)
		{
			case "Decline":
				// Only within trade screens (widget group of param1)
				int groupId = event.getParam1() >> 16;
				if (config.declineTradeEnabled() && TRADE_SCREEN_GROUP_IDS.contains(groupId))
				{
					playPreset(Preset.DECLINE_TRADE);
				}
				break;
			case "Dismiss":
				// Random event dismissal; rune pouch and looting bag also have a 'Dismiss' option
				if (config.dismissRandomEnabled()
					&& event.getParam1() != RUNE_POUCH_WIDGET_ID
					&& event.getParam1() != LOOTING_BAG_WIDGET_ID)
				{
					playPreset(Preset.DISMISS_RANDOM);
				}
				break;
			case "Pet":
				if (config.petDogEnabled() && event.getId() == PET_DOG_MENU_ID)
				{
					playPreset(Preset.PET_DOG);
				}
				break;
			case "Toggle Run":
				if (config.turnOnRunEnabled() && !isRunning)
				{
					playPreset(Preset.TURN_ON_RUN);
				}
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		// The TOA sarcophagus spawns as the player starts looting the chest
		if (config.toaChestEnabled() && event.getGameObject().getId() == TOA_SARCOPHAGUS_ID)
		{
			playPreset(Preset.TOA_CHEST);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (config.deathEnabled() && event.getActor() == client.getLocalPlayer())
		{
			playPreset(Preset.DEATH);
			return;
		}

		// Gemstone crab burrows/moves when it dies (from c-engineer-completed QualityOfLifeTriggers)
		if (config.gemCrabEnabled()
			&& client.getLocalPlayer() != null
			&& client.getLocalPlayer().getInteracting() == event.getActor()
			&& GEMSTONE_CRAB_ACTOR_NAME.equals(event.getActor().getName()))
		{
			playPreset(Preset.GEM_CRAB);
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event)
	{
		for (ItemStack itemStack : event.getItems())
		{
			int itemId = itemStack.getId();
			if (config.grubbyKeyEnabled() && itemId == ItemID.HOSDUN_GRUBBY_KEY)
			{
				playPreset(Preset.GRUBBY_KEY);
			}
			else if (config.larransKeyEnabled() && itemId == ItemID.SLAYER_WILDERNESS_KEY)
			{
				playPreset(Preset.LARRANS_KEY);
			}
			else if (config.brimstoneKeyEnabled() && itemId == ItemID.KONAR_KEY)
			{
				playPreset(Preset.BRIMSTONE_KEY);
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (config.vengeanceEnabled()
			&& event.getVarbitId() == VENGEANCE_VARBIT
			&& event.getValue() == 1)
		{
			playPreset(Preset.VENGEANCE);
		}

		// Track run state for the 'Toggle Run' preset
		if (event.getVarpId() == RUNNING_VARP)
		{
			isRunning = event.getValue() == 1;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Prayer hits 0 (from odablock-sounds PrayerDown)
		if (config.smitedEnabled() && statsPopulated)
		{
			int prayer = client.getBoostedSkillLevel(Skill.PRAYER);
			if (lastPrayer > 0 && prayer == 0)
			{
				playPreset(Preset.SMITED);
			}
			lastPrayer = prayer;
		}

		// CoX chest light fires 2 ticks after raid completion (from odablock-sounds CoxSounds)
		if (coxEndedRaidTick != -1 && client.getTickCount() - coxEndedRaidTick == 2)
		{
			coxEndedRaidTick = -1;
			if (coxWhiteLight)
			{
				if (config.coxWhiteEnabled())
				{
					playPreset(Preset.COX_WHITE);
				}
			}
			else if (config.coxPurpleEnabled())
			{
				playPreset(Preset.COX_PURPLE);
			}
		}
	}

	private void playPreset(Preset preset)
	{
		File sound = presetSounds.get(preset);
		if (sound != null)
		{
			soundManager.play(sound, config.volume());
			return;
		}
		if (bundledPresets.contains(preset))
		{
			soundManager.playBundled("sounds/" + preset.fileName + ".wav", config.volume());
		}
	}

	private boolean hasSound(Preset preset)
	{
		return presetSounds.containsKey(preset) || bundledPresets.contains(preset);
	}

	// ------------------------------------------------------------------
	// Loading
	// ------------------------------------------------------------------

	/**
	 * Scans ~/.runelite/sound-overrides/ for:
	 *  - Preset wavs by name (level_up.wav, pet.wav, dds_spec.wav, ...)
	 *  - Numeric wavs by sound ID: e.g. 2739.wav overrides sound effect 2739
	 */
	private void loadOverrides()
	{
		presetSounds.clear();

		File[] files = SOUND_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
		if (files == null)
		{
			return;
		}

		outer:
		for (File f : files)
		{
			String base = f.getName().substring(0, f.getName().length() - 4).trim().toLowerCase();

			for (Preset preset : Preset.values())
			{
				if (preset.fileName.equals(base))
				{
					presetSounds.put(preset, f);
					continue outer;
				}
			}

			log.warn("Ignoring {} — name must match a known preset", f.getName());
		}

		log.debug("Loaded {} presets", presetSounds.size());
	}
}
