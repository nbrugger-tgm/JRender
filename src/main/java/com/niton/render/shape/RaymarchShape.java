package com.niton.render.shape;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.SignedDisdanceFunction;
import com.niton.render.api.UvMapper;
import com.niton.render.raymarching.SurfaceHit;
import com.niton.render.material.Material;

public class RaymarchShape extends AbstractRaymarchShape {
	public final SignedDisdanceFunction sdf;
	public final UvMapper               uvMap;

	public RaymarchShape(
			SignedDisdanceFunction sdf,
			UvMapper uvMap
	) {
		super(new Material());
		this.sdf   = sdf;
		this.uvMap = uvMap;
	}

	public RaymarchShape(
			SignedDisdanceFunction sdf,
			UvMapper uvMap,
			Material mat
	) {
		super(mat);
		this.sdf   = sdf;
		this.uvMap = uvMap;
	}

	@Override
	public float sdf(Vector3 point) {
		return sdf.sdf(point);
	}

	@Override
	public Vector3 getUVCord(SurfaceHit hp) {
		return uvMap.getUVCord(hp);
	}


}
