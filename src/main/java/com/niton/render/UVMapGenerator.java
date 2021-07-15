package com.niton.render;

import com.badlogic.gdx.math.Vector3;

@FunctionalInterface
public interface UVMapGenerator {
	//[0..1]
	public Vector3 getUVCord(SurfaceHit hp);
}
