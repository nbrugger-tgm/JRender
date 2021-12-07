package com.niton.render.world;

import com.badlogic.gdx.math.Vector3;

public class Light {
	public Vector3 position;

	public Light(Vector3 position, Vector3 color, float strenght) {
		this.position = position;
		this.color    = color;
		this.strenght = strenght;
	}

	public Vector3 color;
	public float   strenght;
}
