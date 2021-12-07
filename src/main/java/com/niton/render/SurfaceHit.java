package com.niton.render;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.shape.AbstractRaymarchShape;

/**
 * Data and metadata about a point on geometry
 */
public class SurfaceHit {
	/**
	 * the object that was hit (used for getting the material for example)
	 */
	public final AbstractRaymarchShape object;
	/**
	 * the point of impact in worldspace
	 */
	public       Vector3               hp;

	/**
	 * disdance from the surface, should allways be <MIN_DIST otherwhise i screwed up
	 */
	public float dst;

	/**
	 * Distance from the camera
	 */
	public float camDst;

	public SurfaceHit(AbstractRaymarchShape object, Vector3 hp, float dst, float camDst) {
		this.object = object;
		this.hp     = hp;
		this.dst    = dst;
		this.camDst = camDst;
	}
}
