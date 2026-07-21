Bundled sound pack directory.

Drop your .wav files here named by preset (level_up.wav, quest_complete.wav,
gem_crab.wav, ...) before building. Files placed here are packed into the
plugin jar and play by default for all users.

Users can still override any bundled sound by putting a wav with the same
name in ~/.runelite/sound-overrides/.

Keep clips small: mono, 22050 Hz, 16-bit PCM (the Sound Override Studio
"Plugin resources" target exports exactly this).
