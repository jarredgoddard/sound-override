package com.soundoverride;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class SoundManager
{
	private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "sound-override-player");
		t.setDaemon(true);
		return t;
	});

	/**
	 * Plays a wav file asynchronously at the given volume (0-100).
	 */
	public void play(File file, int volume)
	{
		executor.submit(() -> doPlay(file, volume));
	}

	private void doPlay(File file, int volume)
	{
		try (AudioInputStream raw = AudioSystem.getAudioInputStream(
			new BufferedInputStream(new FileInputStream(file))))
		{
			// Normalize to PCM in case the wav is a compressed/odd format
			AudioFormat baseFormat = raw.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(),
				16,
				baseFormat.getChannels(),
				baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(),
				false);

			try (AudioInputStream decoded = AudioSystem.getAudioInputStream(decodedFormat, raw))
			{
				Clip clip = AudioSystem.getClip();
				clip.open(decoded);
				setVolume(clip, volume);

				clip.addLineListener(event -> {
					if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP)
					{
						clip.close();
					}
				});

				clip.start();
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to play {}: {}", file.getName(), e.getMessage());
		}
	}

	private void setVolume(Clip clip, int volume)
	{
		if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float clamped = Math.max(0, Math.min(100, volume)) / 100f;
		// Convert linear 0-1 to decibels
		float dB = clamped == 0
			? gain.getMinimum()
			: (float) (20.0 * Math.log10(clamped));
		gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
	}

	public void close()
	{
		executor.shutdownNow();
	}
}
