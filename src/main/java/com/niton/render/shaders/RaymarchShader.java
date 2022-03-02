package com.niton.render.shaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.Shader;
import com.niton.render.raymarching.RaymarchSettings;
import com.niton.render.raymarching.RenderSettings;
import com.niton.render.raymarching.SurfaceHit;

/**
 * A shader that is based on raymarching, just implements the raymarch algorithm, no textures or
 * anything else
 */
public abstract class RaymarchShader<R extends RaymarchShader.Runtime>
		implements Shader<R> {
	private final long             creationTime     = System.currentTimeMillis();
	/**
	 * The time since the shader was created (in seconds)
	 */
	protected     float            time;
	/**
	 * the number of the currently rendered frame
	 */
	protected     int              frame;
	/**
	 * Time since the last frame (seconds)
	 */
	protected     float            delta;
	/**
	 * Width/height ratio of the current frame
	 */
	protected     float            ratio;
	protected     RenderSettings   settings;
	protected     RaymarchSettings raymarchSettings = new RaymarchSettings();
	private       long             lastFrame;

	/**
	 * Keep data that changes during the rendering of a single pixel in here,
	 * if needed in a global context. <b>Never keep data that is changed by rendering
	 * in the Shader itself as it will break multi thread rendering</b>
	 */
	public static class Runtime {
		final Vector3 rd = new Vector3();
		final Map<Vector2,Vector3> screenUvCache = new HashMap<>();
	}

	public RaymarchSettings getRaymarchSettings() {
		return raymarchSettings;
	}

	public RenderSettings getSettings() {
		return settings;
	}

	public void setSettings(RenderSettings settings) {
		this.settings = settings;
	}

	@Override
	public void render(Vector2 screenUV, Vector3 result, R runtime) {
		Vector3 ro      = getCameraPos();
		var rd = runtime.rd.cpy();
		rd.set(
			//screenToWorldFunction.apply(screenUV)
			runtime.screenUvCache.computeIfAbsent(screenUV, screenToWorldFunction)
		);
		rd.nor();

		prepareRuntime(runtime);

		SurfaceHit hit = raymarch(ro, rd, runtime);

		resolveSurfaceColor(ro, rd, hit, result, runtime);
	}

	private final Function<Vector2,Vector3> screenToWorldFunction = this::screenToWorld;

	private Vector3 screenToWorld(Vector2 uv)
	{
		Vector3 toCache = new Vector3();
		screenToWorldUV(uv, toCache);
		return toCache;
	}

	@Override
	public void setDimension(int w, int h) {
		ratio = (float) w / h;
	}

	@Override
	public void frame() {
		frame++;
		long thisFrame = System.currentTimeMillis();
		delta = (thisFrame - lastFrame) / 1000f;
		time  = (thisFrame - creationTime) / 1000f;
	}

	public abstract Vector3 getCameraPos();

	/**
	 * Calculates the position of the pixel(screenUV) in the 3d environment.
	 *
	 * @param screenUV the UV of the screen pixel [0..1]
	 *
	 * @return the screenUv in 3d space [n..-n]
	 */
	protected Vector3 screenToWorldUV(Vector2 screenUV, Vector3 worldUv) {
		screenUV.sub(0.5f, 0.5f);
		screenUV.y = -screenUV.y;
		return worldUv.set(screenUV.x * ratio, screenUV.y, 1);
	}

	/**
	 * Prepare the runtime for rendering a pixel
	 */
	protected abstract void prepareRuntime(R runtime);

	/**
	 * This method shoots a ray from a given point into a given direction by using the SDF
	 * (singed disdance function) to compute the disdance to the geometry of the world.
	 * <p>
	 * It returns the hitpoint with metadata about the object hit by the ray
	 *
	 * @param ro origin of the ray
	 * @param rd direction
	 *
	 * @return the hit surface
	 */
	//how raymarching works is a lil complicated to understand watch this of you need help:
	//"Raymarching for dummies"
	protected SurfaceHit raymarch(Vector3 ro, Vector3 rd, R runtime) {
		var currentPosition = ro.cpy();
		SurfaceHit hit             = new SurfaceHit();

		final int   MAX_STEPS 	  = raymarchSettings.getMaxSteps();
		final float MAX_DIST 	  = raymarchSettings.getMaxDist();
		final float MAX_CAM_DIST  = raymarchSettings.getMaxCamDist();
		final float MIN_DIST 	  = raymarchSettings.getMinDist();
		float       dist     	  = MAX_DIST;
		for (int i = 0; i < MAX_STEPS; i++) {
			hit.steps = i + 1;
			dist      = sdf(currentPosition, runtime);

			if (dist < hit.closestDistance)
				hit.closestDistance = dist;

			//march forward
			currentPosition.mulAdd(rd, dist);
			hit.camDst += dist;


			//into the void
			if (dist > MAX_DIST) {
				return hit;
			}
			if(hit.camDst > MAX_CAM_DIST) {
				return hit;
			}

			//yay we hit a surface
			if (dist < MIN_DIST) {
				hit.hit     = true;
				hit.hp      = currentPosition;
				hit.hitDist = dist;
				return hit;
			}
		}
		hit.hit     = true;
		hit.hitDist = dist;
		hit.hp      = currentPosition;
		//this is just "most likely true".
		//This happens when a ray travels parallel or very close to a surface
		//without close enough to be a hit
		return hit;
	}

	/**
	 * Calculates the color of a point in WORLD space, given a raycaster origin and the direction of
	 * the ray
	 *
	 * @param ro     the origin of the ray
	 * @param rd     ray direction
	 * @param hit    the point at which the ray hit the surface including metadata
	 * @param result the vector to write the color into (the color as Vec3 x,y,z->r,g,b)
	 */
	protected abstract void resolveSurfaceColor(
			Vector3 ro,
			Vector3 rd,
			SurfaceHit hit,
			Vector3 result,
			R runtime
	);

	protected abstract float sdf(Vector3 currentPosition, R runtime);

	/**
	 * Dont know the name of the algorithm
	 * <p>
	 * Computes the normal for a point  on a surface
	 * <p>
	 * Stolen and modified from a yt video
	 *
	 * @param p the surface to get the normal from
	 */
	protected Vector3 getNormal(SurfaceHit p, R runtime) {
		//float d = sdf(p.hp); //bcs why tho
		float   d    = p.hitDist;
		Vector3 offX = p.hp.cpy();
		offX.x -= 0.01;

		Vector3 offY = p.hp.cpy();
		offY.y -= 0.01;

		Vector3 offZ = p.hp.cpy();
		offZ.z -= 0.01;
		Vector3 n = new Vector3(
				d - sdf(offX, runtime),
				d - sdf(offY, runtime),
				d - sdf(offZ, runtime)
		);

		return n.nor();
	}
}
