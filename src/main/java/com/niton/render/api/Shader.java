package com.niton.render.api;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * A shader to calculate single pixels (ray-based)
 * @param <R> type of the runtime
 *
 *           the runtime is used to store runtime variables this is needed for multi threading
 */
public interface Shader<R> {
	/**
	 * Calculates the color for a pixel on the screen based on a virtual 3D world
	 * @param screenUV the poition of the pixel to color (on the screen) [0..1]
	 * @param runtime
	 * @return a vector3 [0..1] expressing the color of the pixel on said cord. x,y,z -> r,g,b (chads dont need alpha)
	 */
	default Vector3 render(Vector2 screenUV, R runtime){
		Vector3 col = new Vector3();
		render(screenUV, col, runtime);
		return col;
	}
	void render(Vector2 screenUV,Vector3 result,R runtume);
	/**
	 * Tells the shader the size of the viewport before the rendering process
	 */
	void setDimension(int w, int h);

	/**
	 * called before every frame, used to keep time (enables animations for example)
	 */
	void frame();
}
