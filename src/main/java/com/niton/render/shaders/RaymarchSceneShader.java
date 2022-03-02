package com.niton.render.shaders;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.shape.AbstractRaymarchShape;
import com.niton.render.world.Light;
import com.niton.render.world.RaymarchScene;

/**
 * This is the class for the shader itsself
 * <p>
 * A shader computes the color of a pixel in a 3D scene projected to a 2d screen
 */
public class RaymarchSceneShader extends PbrRaymarchShader<PbrRaymarchShader.Runtime> {

	//ro = ray origin -> start of the raycast
	private static final Vector3       ro = new Vector3(0f, 0f, -1);
	private final        RaymarchScene scene;


	/**
	 * You can modify the scene using their setters, but you cannot re-assign it
	 */
	public RaymarchSceneShader(RaymarchScene scene) {this.scene = scene;}

	@Override
	protected Vector3 getDirectLight(
			Vector3 surfacePoint,
			Vector3 surfaceNormal,
			Runtime runtime
	) {
		Vector3 directLight = new Vector3();
		for (Light l : scene.getLights()) {
			Vector3 lightDir = l.position.cpy().sub(surfacePoint);
			float   lVecLen  = lightDir.len();
			lightDir.nor();
			float lightImpact = getImpactLight(surfacePoint, surfaceNormal, lightDir, lVecLen,
			                                   runtime
			);
			float fog = 1;
			if (settings.useFog()) {
				fog -= getFog(Math.min(lVecLen, materialRenderSettings.getMaxFogDist()));
			}
			directLight.mulAdd(l.color, l.strenght * lightImpact * fog);
		}
		return directLight;
	}

	@Override
	public Vector3 getCameraPos() {
		return ro;
	}

	@Override
	protected float sdf(Vector3 p, Runtime runtime) {
		float                 dst      = raymarchSettings.getMaxDist();
		AbstractRaymarchShape hitShape = null;

		var objects = scene.getObjects().size();
		//this for loop basically is chaining many Math.min(shapeDis,dst) calls for
		//using fori instead of for each for performance, (no iterator initialisation)
		for (int i = 0; i < objects; i++)
		{
			var shape = scene.getObjects().get(i);
			float shapeDis = shape.sdf(p);
			if (shapeDis < dst) {
				dst      = shapeDis;
				hitShape = shape;
			}
		}
		runtime.hitObject = hitShape;
		return dst;
	}

	@Override
	public Runtime createRuntime() {
		return new Runtime();
	}
}
