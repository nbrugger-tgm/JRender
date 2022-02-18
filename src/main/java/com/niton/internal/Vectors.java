package com.niton.internal;

import com.badlogic.gdx.math.Vector3;

import java.util.function.UnaryOperator;

public final class Vectors {
	private Vectors() {}

	public static void apply(Vector3 vec, UnaryOperator<Float> math) {
		vec.x = math.apply(vec.x);
		vec.y = math.apply(vec.y);
		vec.z = math.apply(vec.z);
	}
}
