package com.niton.render.raymarching;

import com.badlogic.gdx.math.Vector3;

/**
 * Data and metadata about a point on geometry
 */
public class SurfaceHit {
	/**
	 * True if the raymarching hit an surface
	 */
	public boolean hit = false;

	/**
	 * the point of impact in world-space
	 */
	public Vector3 hp = null;

	/**
	 * Distance from the camera
	 */
	public float camDst = 0;

	/**
	 * The amount of marched steps
	 */
	public int steps = 0;

	/**
	 * The closest the ray was to a surface
	 */
	public float closestDistance = Float.MAX_VALUE;
	/**
	 * How close the hitpoint {@link #hp} is to the real surface
	 */
	public float hitDist         = Float.MAX_VALUE;
}
