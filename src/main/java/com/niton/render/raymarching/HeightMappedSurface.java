package com.niton.render.raymarching;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.material.MaterialSurface;

public class HeightMappedSurface {
	public MaterialSurface surf;
	public Vector3         normal;
	public Vector3     albedo = new Vector3();
	public boolean passthru;
}
