package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("contrast")
public interface ContrastConfig extends Config
{
	@Range(min = 1, max = 200)
	@ConfigItem(
		keyName = "contrast",
		name = "Contrast",
		description = "100 = default. Above 100: darker darks + brighter brights. Below 100: flatter image."
	)
	default int contrast()
	{
		return 100;
	}
}
