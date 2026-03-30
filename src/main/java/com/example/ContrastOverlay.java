package com.example;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
public class ContrastOverlay extends Overlay
{
	private final ContrastConfig config;

	private volatile BufferedImage lastFrame;
	private BufferedImage processedFrame;
	private LookupOp cachedOp;
	private int lastContrastValue = -1;

	@Inject
	ContrastOverlay(ContrastConfig config)
	{
		this.config = config;
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPosition(OverlayPosition.DYNAMIC);
		setMovable(false);
		setPreferredLocation(new Point(0, 0));
	}

	void onNewFrame(Image image)
	{
		if (image == null)
		{
			return;
		}

		int w = image.getWidth(null);
		int h = image.getHeight(null);
		if (w <= 0 || h <= 0)
		{
			return;
		}

		if (image instanceof BufferedImage && ((BufferedImage) image).getType() == BufferedImage.TYPE_INT_RGB)
		{
			lastFrame = (BufferedImage) image;
		}
		else
		{
			BufferedImage converted = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = converted.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			lastFrame = converted;
		}
	}

	void clearFrame()
	{
		lastFrame = null;
		processedFrame = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		BufferedImage frame = lastFrame;
		if (frame == null)
		{
			return null;
		}

		int contrastValue = config.contrast();
		if (contrastValue == 100)
		{
			return null;
		}

		if (contrastValue != lastContrastValue)
		{
			cachedOp = buildContrastOp(contrastValue);
			lastContrastValue = contrastValue;
		}

		int w = frame.getWidth();
		int h = frame.getHeight();

		if (processedFrame == null || processedFrame.getWidth() != w || processedFrame.getHeight() != h)
		{
			processedFrame = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}

		try
		{
			cachedOp.filter(frame, processedFrame);
		}
		catch (Exception e)
		{
			log.warn("contrast filter failed", e);
			return null;
		}

		Composite original = graphics.getComposite();
		graphics.setComposite(AlphaComposite.Src);
		graphics.drawImage(processedFrame, 0, 0, null);
		graphics.setComposite(original);

		return null;
	}

	private LookupOp buildContrastOp(int sliderValue)
	{
		double strength = (sliderValue - 100.0) / 50.0;

		short[] curve = new short[256];
		for (int i = 0; i < 256; i++)
		{
			double x = (i - 128.0) / 128.0;
			double y;
			if (Math.abs(strength) < 0.001)
			{
				y = x;
			}
			else
			{
				double k = strength * 1.5;
				y = Math.tanh(x * k) / Math.tanh(k);
			}
			int out = (int) Math.round(y * 128.0 + 128.0);
			curve[i] = (short) Math.max(0, Math.min(255, out));
		}

		short[][] table = {curve, curve, curve};
		return new LookupOp(new ShortLookupTable(0, table), null);
	}
}
