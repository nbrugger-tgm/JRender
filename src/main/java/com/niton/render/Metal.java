package com.niton.render;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.world.Material;
import com.niton.render.world.Surface;

public class Metal extends Material
{
	public final Vector3 color = new Vector3(1,1,1);
	public final float metallic;

	public Metal(float metallic) {
		this.metallic = metallic;
	}
	public Surface getPoint(Vector2 surface2UV) {
		Surface sur = new Surface();
		sur.albedo = color.cpy();
		sur.refect = metallic;
		return sur;
	}
}
