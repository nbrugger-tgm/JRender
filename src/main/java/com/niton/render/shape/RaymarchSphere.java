package com.niton.render.shape;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.SurfaceHit;
import com.niton.render.world.Material;

public class RaymarchSphere extends AbstractRaymarchShape {
	private final Vector3 center;
	private final float   radius;

	public RaymarchSphere(Vector3 center, float radius) {
		super(new Material());
		this.center = center;
		this.radius = radius;
	}

	public RaymarchSphere(Material mat, Vector3 center, float radius) {
		super(mat);
		this.center = center;
		this.radius = radius;
	}

	public Vector3 getUVCord(SurfaceHit hp) {
		return abs(center.cpy().sub(hp.hp));
	}

	private Vector3 abs(Vector3 sub) {
		sub.x = Math.abs(sub.x);
		sub.y = Math.abs(sub.y);
		sub.z = Math.abs(sub.z);
		return sub;
	}

	@Override
	public float sdf(Vector3 point) {
		return center.cpy().sub(point).len() - radius;
	}
}
