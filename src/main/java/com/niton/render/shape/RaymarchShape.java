package com.niton.render.shape;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.*;

public class RaymarchShape extends AbstractRaymarchShape {
	public final SignedDisdanceFunction sdf;
	public final UVMapGenerator         uvMap;

	public RaymarchShape(SignedDisdanceFunction sdf,
	                     UVMapGenerator uvMap) {
		super(new Material());
		this.sdf   = sdf;
		this.uvMap = uvMap;
	}

	public RaymarchShape(SignedDisdanceFunction sdf,
	                     UVMapGenerator uvMap,
	                     Material mat) {
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
