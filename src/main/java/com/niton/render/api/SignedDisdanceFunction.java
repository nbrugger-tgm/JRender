package com.niton.render.api;

import com.badlogic.gdx.math.Vector3;

@FunctionalInterface
//https://en.wikipedia.org/wiki/Signed_distance_function <- i wouldn't understand this but there
// you are
public interface SignedDisdanceFunction {
	public float sdf(Vector3 point);
}
