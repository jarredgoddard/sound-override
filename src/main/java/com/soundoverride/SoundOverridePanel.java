package com.soundoverride;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Debug panel: shows every preset and ID override, whether a wav is loaded
 * for it, and a play button that routes through the exact same SoundManager
 * path the plugin uses in-game. Lets you verify sounds saved from
 * Sound Override Studio without triggering the real game events.
 */
@Slf4j
public class SoundOverridePanel extends PluginPanel
{
	private final SoundOverridePlugin plugin;
	private final SoundManager soundManager;
	private final SoundOverrideConfig config;
	private final JPanel listPanel = new JPanel();

	SoundOverridePanel(SoundOverridePlugin plugin, SoundManager soundManager, SoundOverrideConfig config)
	{
		this.plugin = plugin;
		this.soundManager = soundManager;
		this.config = config;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton reload = new JButton("Reload folder");
		reload.addActionListener(e -> plugin.reloadOverrides());
		header.add(reload);

		JButton openFolder = new JButton("Open folder");
		openFolder.addActionListener(e -> {
			try
			{
				Desktop.getDesktop().open(SoundOverridePlugin.SOUND_DIR);
			}
			catch (Exception ex)
			{
				log.warn("Couldn't open sound folder", ex);
			}
		});
		header.add(openFolder);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(header, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		refresh();
	}

	void refresh()
	{
		SwingUtilities.invokeLater(this::rebuild);
	}

	private void rebuild()
	{
		listPanel.removeAll();

		Map<SoundOverridePlugin.Preset, File> presets = plugin.getPresetSounds();
		Map<Integer, File> ids = plugin.getIdOverrides();

		listPanel.add(sectionLabel("Presets (" + presets.size() + "/"
			+ SoundOverridePlugin.Preset.values().length + " loaded)"));

		for (SoundOverridePlugin.Preset preset : SoundOverridePlugin.Preset.values())
		{
			File f = presets.get(preset);
			boolean bundled = plugin.getBundledPresets().contains(preset);
			listPanel.add(row(preset.fileName + ".wav", f,
				bundled ? "sounds/" + preset.fileName + ".wav" : null));
		}

		listPanel.add(Box.createVerticalStrut(10));
		listPanel.add(sectionLabel("ID overrides (" + ids.size() + ")"));

		if (ids.isEmpty())
		{
			JLabel none = new JLabel("none — add e.g. 2739.wav");
			none.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			listPanel.add(none);
		}
		else
		{
			ids.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(e -> listPanel.add(row(e.getKey() + ".wav", e.getValue(), null)));
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private JLabel sectionLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.BRAND_ORANGE);
		label.setBorder(new EmptyBorder(6, 0, 4, 0));
		return label;
	}

	private JPanel row(String name, File file, String bundledResource)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

		JLabel label = new JLabel(name + (file == null && bundledResource != null ? "  (bundled)" : ""));
		if (file != null)
		{
			label.setForeground(Color.WHITE);
			label.setToolTipText(file.getAbsolutePath());
		}
		else if (bundledResource != null)
		{
			label.setForeground(ColorScheme.BRAND_ORANGE);
			label.setToolTipText("Bundled default — add " + name + " to the sound-overrides folder to override");
		}
		else
		{
			label.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			label.setToolTipText("No bundled default and no file in sound-overrides folder");
		}
		row.add(label, BorderLayout.CENTER);

		if (file != null || bundledResource != null)
		{
			JButton play = new JButton("▶");
			play.setMargin(new java.awt.Insets(0, 6, 0, 6));
			play.setToolTipText("Play through the plugin's sound path");
			if (file != null)
			{
				play.addActionListener(e -> soundManager.play(file, config.volume()));
			}
			else
			{
				play.addActionListener(e -> soundManager.playBundled(bundledResource, config.volume()));
			}
			row.add(play, BorderLayout.EAST);
		}

		return row;
	}
}
