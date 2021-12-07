package com.niton.render;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.world.Surface;

public class HeightMappedSurface {
	public Surface surf;
	public Vector3 normal;
	public Vector3     albedo = new Vector3();
	public boolean passthru;
}
