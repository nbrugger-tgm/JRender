package com.niton.render;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.awt.*;

public interface SwingShader {
	/**
	 * Calculates the color for a pixel on the screen based on a virtual 3D world
	 * @param screenUV the poition of the pixel to color (on the screen) [0..1]
	 * @return a vector3 [0..1] expressing the color of the pixel on said cord. x,y,z -> r,g,b (chads dont need alpha)
	 */
	Vector3 render(Vector2 screenUV);

	/**
	 * Tells the shader the size of the viewport before the rendering process
	 */
	void setDimension(int w, int h);

	/**
	 * called before every frame, used to keep time (enables animations for example)
	 */
	void frame();
}
