package com.niton.render;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.shape.AbstractRaymarchShape;
import com.niton.render.world.Light;
import com.niton.render.world.RaymarchScene;

/**
 * This is the class for the shader itsself
 * <p>
 * A shader computes the color of a pixel in a 3D scene projected to a 2d screen
 */
public class RaymarchSceneShader extends RaymarchShader {

	private final RaymarchScene scene;

	/**
	 * You can modify the scene using their setters, but you cannot re-assign it
	 */
	public RaymarchSceneShader(RaymarchScene scene) {this.scene = scene;}


	@Override
	protected Vector3 getDirectLight(Vector3 surfacePoint, Vector3 surfaceNormal) {
		Vector3 directLight = new Vector3();
		for (Light l : scene.getLights()) {
			Vector3 lightDir = l.position.cpy().sub(surfacePoint);
			float   lVecLen  = lightDir.len();
			lightDir.nor();
			float lightImpact = getImpactLight(surfacePoint, surfaceNormal, lightDir, lVecLen);
			float fog         = 1;
			if (settings.isUseFog()) {
				fog -= getFog(Math.min(lVecLen, MAX_FOG_DIST));
			}
			directLight.mulAdd(l.color, l.strenght * lightImpact * fog);
		}
		return directLight;
	}

	@Override
	protected float getImpactLight(
			Vector3 point,
			Vector3 surfaceNormal,
			Vector3 lightDir,
			float lVecLen
	) {
		SurfaceHit lightDis = raymarch(point, lightDir);
		float      light    = 0;
		if (lightDis.camDst >= lVecLen)
			light = Math.max(0f, surfaceNormal.dot(lightDir));
		return light;
	}

	@Override
	protected SurfaceHit sdf(Vector3 p) {
		float                 dst      = MAX_DST;
		AbstractRaymarchShape hitShape = null;

		//this for loop basically is chaining many Math.min(shapeDis,dst) calls for
		for (AbstractRaymarchShape shape : scene.getObjects()) {
			float shapeDis = shape.sdf(p);
			if (shapeDis < dst) {
				dst      = shapeDis;
				hitShape = shape;
			}
		}
		return new SurfaceHit(hitShape, p, dst, 0);
	}

}
