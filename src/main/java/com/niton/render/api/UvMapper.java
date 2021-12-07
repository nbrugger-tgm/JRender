package com.niton.render.api;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.SurfaceHit;

@FunctionalInterface
public interface UvMapper
{
	//[0..1]
	Vector3 getUVCord(SurfaceHit hp);
}
