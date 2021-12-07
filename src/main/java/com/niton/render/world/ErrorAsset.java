package com.niton.render.world;

import com.niton.render.MapAsset;

import java.awt.image.BufferedImage;

public class ErrorAsset extends MapAsset {
	public static final BufferedImage img;

	static {
		img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
		img.createGraphics().drawString("ERROR", 0, 256);
	}

	public ErrorAsset() {
		super(img, 3);
	}
}
