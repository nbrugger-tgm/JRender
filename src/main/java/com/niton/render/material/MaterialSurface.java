package com.niton.render.material;

import com.badlogic.gdx.math.Vector3;


/**
 * Describes the physical properties of a point on a surface
 */
public class MaterialSurface {
	public final Vector3 albedo = new Vector3(1, 0, 0);//albedo means base color
	public final Vector3 normal = new Vector3(0, 0, 1);
	public       float   depth  = 0;
	public       float   ao     = 0.5f;//ambient ocusion, not used yet
	public       float   refect = 0;
}
