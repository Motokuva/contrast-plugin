package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Contrast",
	description = "Adjusts the contrast of the game client",
	tags = {"contrast", "visual", "graphics"}
)
public class ContrastPlugin extends Plugin
{
	@Inject private DrawManager drawManager;
	@Inject private OverlayManager overlayManager;
	@Inject private ContrastOverlay overlay;

	private Runnable frameListener;

	@Override
	protected void startUp()
	{
		frameListener = () -> drawManager.requestNextFrameListener(overlay::onNewFrame);
		drawManager.registerEveryFrameListener(frameListener);
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		drawManager.unregisterEveryFrameListener(frameListener);
		overlay.clearFrame();
	}

	@Provides
	ContrastConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ContrastConfig.class);
	}
}
