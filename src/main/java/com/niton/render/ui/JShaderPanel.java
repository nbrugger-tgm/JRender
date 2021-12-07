package com.niton.render.ui;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.RenderTarget;
import com.niton.render.api.Renderer;
import com.niton.render.api.Shader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Uses a jpanel to render a shader onto it
 * <p>
 * (still useable as a jpanel if you want)
 */
public class JShaderPanel<R> extends JPanel {
	private final Renderer<R>   renderer;
	private final Shader<R>     shader;
	private       BufferedImage img;

	private static class RasterTarget implements RenderTarget {
		private final WritableRaster raster;

		public RasterTarget(
				WritableRaster raster
		) {
			this.raster = raster;
		}

		@Override
		public void draw(int x, int y, Vector3 color) {
			final float[] colorBuffer = new float[3];
			colorBuffer[0] = color.x;
			colorBuffer[1] = color.y;
			colorBuffer[2] = color.z;
			raster.setPixel(x, y, colorBuffer);
		}

		@Override
		public int getWidth() {
			return raster.getWidth();
		}

		@Override
		public int getHeight() {
			return raster.getHeight();
		}
	}

	public JShaderPanel(Renderer<R> renderer, Shader<R> shader) {
		this.renderer = renderer;
		this.shader   = shader;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int        w   = (int) g.getClipBounds().getWidth();
		int        h   = (int) g.getClipBounds().getHeight();

		//buffer smaller than needed (or not existing)
		if (img == null || img.getWidth() != w || img.getHeight() != h)
			createPixelBuffer(w, h);

		renderer.render(shader);

		g2d.drawImage(img, 0, 0, null);
	}

	private void createPixelBuffer(int w, int h) {
		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		renderer.setTarget(new RasterTarget(img.getRaster()));
	}
}
