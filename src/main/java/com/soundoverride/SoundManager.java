package com.soundoverride;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

@Slf4j
@Singleton
public class SoundManager
{
	private final AudioPlayer audioPlayer;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "sound-override-player");
		t.setDaemon(true);
		return t;
	});

	@Inject
	SoundManager(AudioPlayer audioPlayer)
	{
		this.audioPlayer = audioPlayer;
	}

	/**
	 * Plays a wav file asynchronously at the given volume (0-100),
	 * converted to a decibel gain for RuneLite's AudioPlayer.
	 */
	public void play(File file, int volume)
	{
		executor.submit(() -> {
			try
			{
				audioPlayer.play(file, volumeToGainDb(volume));
			}
			catch (Exception e)
			{
				log.warn("Failed to play {}: {}", file.getName(), e.getMessage());
			}
		});
	}

	/**
	 * Plays a wav bundled inside the plugin jar, e.g. "sounds/level_up.wav".
	 */
	public void playBundled(String resourcePath, int volume)
	{
		executor.submit(() -> {
			try
			{
				audioPlayer.play(SoundOverridePlugin.class, resourcePath, volumeToGainDb(volume));
			}
			catch (Exception e)
			{
				log.warn("Failed to play bundled {}: {}", resourcePath, e.getMessage());
			}
		});
	}

	private static float volumeToGainDb(int volume)
	{
		// Linear 0-100 -> dB; clamp so 0 becomes effectively silent rather than -Infinity
		float linear = Math.max(0.001f, Math.min(100, volume) / 100f);
		return (float) (20.0 * Math.log10(linear));
	}

	public void close()
	{
		executor.shutdownNow();
	}
}
