package com.niton.render.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.SwingShader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Uses a jpanel to render a shader onto it
 *
 * (still useable as a jpanel if you want)
 */
public class SwingRender extends JPanel {
	//shader to render
	private SwingShader shader;
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if(shader == null) {
			g.drawChars("NO SHADER".toCharArray(), 0, 9, 20, 10);
			return;
		}
		int w = (int) g.getClipBounds().getWidth(), h = (int) g.getClipBounds().getHeight();

		//i render to an buffered image. Why?
		//* Its easy
		//* you could straigt up save the image as JPEG/PNG
		BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		WritableRaster view = img.getRaster();
		shader.setDimension(w,h);
		shader.frame();
		for (int i = 0; i < w; i++) {
			for (int y = 0; y < h; y++) {
				//calculating UVs and interpreting the result of the shader
				float ux = (float) i/w;
				float uy = (float) y/h;
				Vector3 col = shader.render(new Vector2(ux,uy));
				col.scl(255);
				view.setPixel(i,y,new float[]{
						Math.min(255,col.x),
						Math.min(255,col.y),
						Math.min(255,col.z)
				});
			}
		}
		//paint image to UI
		g2d.drawImage(img,0,0,null);
	}

	public void setShader(SwingShader shader) {
		this.shader = shader;
	}
}
