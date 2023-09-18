package com.niton.render.example;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.material.Material;
import com.niton.render.material.MaterialFeature;
import com.niton.render.material.MaterialSurface;

public class Metal extends Material
{
	public final Vector3 color = new Vector3(1,1,1);
	public final float metallic;

	public Metal(float metallic) {
		this.metallic = metallic;
		enableFeature(MaterialFeature.METALLIC, true, "native");
	}
	public MaterialSurface getPoint(Vector2 surface2UV) {
		MaterialSurface sur = new MaterialSurface();
		sur.albedo.set(color);
		sur.refect = metallic;
		return sur;
	}
}
