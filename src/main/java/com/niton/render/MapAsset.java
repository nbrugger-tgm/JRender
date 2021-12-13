package com.niton.render;

import com.badlogic.gdx.math.Vector2;

import java.awt.image.BufferedImage;

public class MapAsset {
	private static final float   NORMAL = 1 / 255f;
	public final         float[] data;
	private final        int     components;
	private final        int     w, h;
	private final float[] outputBuffer;

	public MapAsset(BufferedImage img, int components) {
		this(components, img.getWidth(), img.getHeight());
		readFrom(img);
	}

	public MapAsset(int components, int w, int h) {
		this.components = components;
		this.w          = w;
		this.h          = h;
		if (components < 1 || components > 3)
			throw new IllegalArgumentException("Map assets only support 1,2 or 3 components");
		data         = new float[w * h * components];
		outputBuffer = new float[components];
	}

	/**
	 * Reads 3 component vectors from an image into a normalized array accesible with index = {@code
	 * [3*(y*width+x)+componentIndex]}
	 *
	 * @param image the image to read the indexes from
	 *
	 * @return the normalized array [0..1]
	 */
	public void readFrom(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		if (w != this.w || h != this.h)
			throw new IllegalArgumentException("The images dimensions are not matching the asset");
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int pxl      = image.getRGB(x, y);
				int pxlIndex = components * (y * w + x);
				for (int i = 0; i < components; i++) {
					data[pxlIndex + components - (i + 1)] = ((pxl >> i * 8) & 0xff) * NORMAL;
				}
			}
		}
	}

	public float[] get(Vector2 surfaceUV) {
		return get(surfaceUV, outputBuffer);
	}

	public float[] get(Vector2 surfaceUV, float[] out) {
		int x       = (int) (w * surfaceUV.x);
		int y       = (int) (h * surfaceUV.y);
		int pxlCord = components * (y * w + x);
		System.arraycopy(data, pxlCord, out, 0, components);
		return out;
	}
}
