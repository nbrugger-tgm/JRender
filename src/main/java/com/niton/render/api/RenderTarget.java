package com.niton.render.api;

import com.badlogic.gdx.math.Vector3;

public interface RenderTarget {
	void draw(int x, int y, Vector3 color);

	int getWidth();

	int getHeight();
}
