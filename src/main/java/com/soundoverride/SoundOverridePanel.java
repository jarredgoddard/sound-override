package com.soundoverride;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Sound pack panel: one checkbox per sound that actually exists
 * (bundled in the jar or present in the sound-overrides folder).
 * Checkboxes toggle the same enable settings as the plugin config.
 */
@Slf4j
public class SoundOverridePanel extends PluginPanel
{
	private static final String CONFIG_GROUP = "soundoverride";

	private final SoundOverridePlugin plugin;
	private final SoundManager soundManager;
	private final SoundOverrideConfig config;
	private final ConfigManager configManager;
	private final JPanel listPanel = new JPanel();

	SoundOverridePanel(SoundOverridePlugin plugin, SoundManager soundManager,
		SoundOverrideConfig config, ConfigManager configManager)
	{
		this.plugin = plugin;
		this.soundManager = soundManager;
		this.config = config;
		this.configManager = configManager;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(8, 8, 8, 8));

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
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
		Set<SoundOverridePlugin.Preset> bundled = plugin.getBundledPresets();
		Map<Integer, File> ids = plugin.getIdOverrides();

		boolean anyPreset = false;
		listPanel.add(sectionLabel("Sounds"));
		for (SoundOverridePlugin.Preset preset : SoundOverridePlugin.Preset.values())
		{
			File f = presets.get(preset);
			boolean hasSound = f != null || bundled.contains(preset);
			if (!hasSound)
			{
				continue; // only show sounds that actually exist
			}
			anyPreset = true;

			JCheckBox box = new JCheckBox(preset.displayName, plugin.presetEnabled(preset));
			box.setBackground(ColorScheme.DARK_GRAY_COLOR);
			box.setForeground(java.awt.Color.WHITE);
			box.setToolTipText(f != null
				? f.getAbsolutePath()
				: "Bundled default (override with " + preset.fileName + ".wav)");
			box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
			box.addActionListener(e ->
				configManager.setConfiguration(CONFIG_GROUP, preset.configKey, box.isSelected()));
			listPanel.add(box);
		}
		if (!anyPreset)
		{
			listPanel.add(mutedLabel("No sounds loaded yet."));
		}

		if (!ids.isEmpty())
		{
			listPanel.add(Box.createVerticalStrut(10));
			listPanel.add(sectionLabel("Sound replacements"));
			Set<Integer> disabled = plugin.getDisabledIds();

			ids.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> {
					int soundId = entry.getKey();
					JCheckBox box = new JCheckBox("Sound " + soundId, !disabled.contains(soundId));
					box.setBackground(ColorScheme.DARK_GRAY_COLOR);
					box.setForeground(java.awt.Color.WHITE);
					box.setToolTipText(entry.getValue().getAbsolutePath());
					box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
					box.addActionListener(e -> toggleId(soundId, box.isSelected()));
					listPanel.add(box);
				});
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private void toggleId(int soundId, boolean enabled)
	{
		Set<Integer> disabled = new HashSet<>(plugin.getDisabledIds());
		if (enabled)
		{
			disabled.remove(soundId);
		}
		else
		{
			disabled.add(soundId);
		}
		String joined = disabled.stream().sorted()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
		configManager.setConfiguration(CONFIG_GROUP, "disabledSoundIds", joined);
	}

	private JLabel sectionLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.BRAND_ORANGE);
		label.setBorder(new EmptyBorder(6, 0, 4, 0));
		return label;
	}

	private JLabel mutedLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}
}
