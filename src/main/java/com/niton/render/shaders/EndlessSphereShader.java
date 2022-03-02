package com.niton.render.shaders;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.SignedDisdanceFunction;
import com.niton.render.raymarching.SurfaceHit;

import static com.niton.internal.Vectors.apply;
import static java.lang.Math.*;

public class EndlessSphereShader extends RaymarchShader<EndlessSphereShader.Runtime> {

	//ro = ray origin -> start of the raycast (world-space)
	private final Vector3 camera    = new Vector3(0f, 0f, -1);
	private final Vector3 offset    = new Vector3(0.1f, 0.1f, 0f);
	private final Vector3 glowColor = new Vector3(1, 1, 1);
	private final Vector3 light     = new Vector3(0, 2, -1);
	private final Vector3 fogColor =  new Vector3(0,1,0);

	private float getMaxFogDist(){
		return raymarchSettings.getMaxDist()*0.9f;
	}

	public static class Runtime extends RaymarchShader.Runtime {
		private int reflectDepth;
	}

	@Override
	public void frame() {
		super.frame();
		camera.z = time * .3f;
		camera.y = (float) sin(time * .2) * 2;
	}

	@Override
	public Vector3 getCameraPos() {
		return camera;
	}

	@Override
	protected void prepareRuntime(Runtime runtime) {
		runtime.reflectDepth = 0;
	}

	@Override
	protected void resolveSurfaceColor(
			Vector3 ro, Vector3 rd, SurfaceHit hit, Vector3 result, Runtime runtime
	) {
		float fog = lerp(0, getMaxFogDist(), hit.camDst);
		if (settings.useFog() && fog > .975f) {
			result.set(fogColor);
		} else if (!hit.hit) {
			result.set(fogColor);
			applyGlow(hit, result);
		} else {
			if (settings.useFog() && fog == 1) {
				return;
			}
			Vector3 normal       = getNormal(hit, runtime);
			float   stepsPerUnit = hit.steps / hit.camDst;
			float   c2           = lerp(0, 50, stepsPerUnit);
			float   c3           = lerp(-1, 1, (float) sin(hit.hp.z * 0.3f));
			float   c1           = lerp(-1, 1, (float) sin(time * 0.3f));
			result.set(.1f + (c2) * .9f, .1f * c1 * c2, (.1f + .1f * c1 + c3 * .8f));
			if (settings.useSurfaceLight())
				applyLight(hit, result, runtime);
			if (settings.useReflections())
				applyReflection(hit, rd, result, runtime);
		}
		if (settings.useFog()) {
			result.lerp(fogColor, fog);
		}
	}

	@Override
	protected float sdf(
			Vector3 currentPosition, Runtime runtime
	) {
		return repeatSdf(currentPosition, .75f, q -> q.dst(offset) - 0.3f);
	}

	public float repeatSdf(Vector3 p, float c, SignedDisdanceFunction sdf) {
		Vector3 q = new Vector3();
		q.set(p);
		apply(q, Math::abs);
		q.scl(.5f * c);
		apply(q, i -> i % c);
		q.sub(.5f * c);
		return sdf.sdf(q);
	}

	private void applyReflection(SurfaceHit hit, Vector3 rd, Vector3 result, Runtime runtime) {
		if (runtime.reflectDepth > 4)
			return;
		final float reflectivity = .3f;

		Vector3    normal       = getNormal(hit, runtime);
		Vector3    ro           = hit.hp.cpy().mulAdd(normal, raymarchSettings.getMinDist() * 1.1f);
		Vector3    reflectVect  = rd.cpy().add(normal).nor();
		SurfaceHit bounce       = raymarch(ro, reflectVect, runtime);
		Vector3    reflectColor = new Vector3();

		runtime.reflectDepth++;
		resolveSurfaceColor(ro, reflectVect, bounce, reflectColor, runtime);
		runtime.reflectDepth--;

		result.lerp(reflectColor, reflectivity);
	}

	private void applyGlow(SurfaceHit hit, Vector3 result) {
		float minDist = getRaymarchSettings().getMinDist();
		float glow    = 1 - lerp(minDist, minDist * 10, hit.closestDistance);
		result.lerp(glowColor, glow * 0.2f);
	}

	private float lerp(float min, float max, float value) {
		float valAboveMin = value - min;
		float range       = max - min;
		return max(min(valAboveMin / range, 1f), 0f);
	}

	private void applyLight(SurfaceHit hit, Vector3 result, Runtime runtime) {
		Vector3 normal      = getNormal(hit, runtime);
		Vector3 lightVec    = light.cpy().sub(hit.hp).nor();
		float   lightImpact = max(normal.dot(lightVec), 0f);
		result.scl(lightImpact);
	}

	@Override
	public Runtime createRuntime() {
		return new Runtime();
	}


}
