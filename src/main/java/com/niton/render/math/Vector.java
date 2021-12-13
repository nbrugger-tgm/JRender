package com.niton.render.math;

import com.badlogic.gdx.math.Vector3;

import java.util.function.UnaryOperator;

public class Vector {
	public static void apply(Vector3 vec, UnaryOperator<Float> math) {
		vec.x = math.apply(vec.x);
		vec.y = math.apply(vec.y);
		vec.z = math.apply(vec.z);
	}
}
