package com.niton.render.shaders;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.material.Material;
import com.niton.render.material.MaterialFeature;
import com.niton.render.raymarching.HeightMappedSurface;
import com.niton.render.material.MaterialRenderSettings;
import com.niton.render.material.MaterialSurface;
import com.niton.render.raymarching.SurfaceHit;
import com.niton.render.shape.AbstractRaymarchShape;

import static com.niton.render.material.MaterialFeature.AMBIENT_OCCLUSION;
import static com.niton.render.material.MaterialFeature.HEIGHT_MAP;
import static com.niton.render.material.MaterialFeature.METALLIC;
import static com.niton.render.material.MaterialFeature.NORMALS;
import static com.niton.render.material.MaterialFeature.TEXTURE;
import static java.lang.Math.*;

/**
 * Physical based rendering shader. Supports multiple effects like:
 * <ul>
 *     <li>Shadows</li>
 *     <li>Lights</li>
 *     <li>Textures</li>
 *     <li>HD Heightmaps</li>
 *     <li>Ambient Occlusion</li>
 *     <li>Reflections</li>
 * </ul>
 */
public abstract class PbrRaymarchShader<R extends PbrRaymarchShader.Runtime>
		extends RaymarchShader<R> {

	protected MaterialRenderSettings materialRenderSettings = new MaterialRenderSettings();

	public static class Runtime extends RaymarchShader.Runtime {
		public AbstractRaymarchShape hitObject;
		//keeps deep the reflection hierarchy is, to possiby interrupt
		public float                 reflectiveVisibility;
		public boolean               passthruRendered;
		public final Vector3 aoScaler = new Vector3();
	}

	@Override
	protected void prepareRuntime(Runtime runtime) {
		runtime.reflectiveVisibility = 1f;
		runtime.passthruRendered     = false;
	}

	@Override
	public void resolveSurfaceColor(
			Vector3 ro,
			Vector3 rd,
			SurfaceHit hit,
			Vector3 out,
			R run
	) {
		out.set(materialRenderSettings.getSkyAlbedo());

		var hitObject = getHitObject(run);

		if (hit.hit) {
			renderSurfaceHit(rd, hit, out, run, hitObject);
			if (run.passthruRendered)
				return;
		}
		if (settings.useFog()) {
			//fog just scales with disdance to cam
			float fog = getFog(hit.camDst);
			out.lerp(materialRenderSettings.getFogColor(), fog);
		}
		//in-lense light
		//this should act like lens flare and speculars , but it doesnt
		//needs to be fixed
		if (settings.useDirectLight()) {
			Vector3 directLight = getDirectLight(ro, rd, run);
			pow(directLight, 16f);
			out.add(directLight);
		}
	}

	private void renderSurfaceHit(
			Vector3 rd,
			SurfaceHit hit,
			Vector3 out,
			R run,
			AbstractRaymarchShape hitObject
	) {
		//normal of the surface calculated using sampling (no normal maps just math)
		Vector3 normal = getNormal(hit, run);
		//the uv in 3D object space
		Vector3 surfaceUV = hitObject.getUVCord(hit);
		//the UV in 2d object space
		Vector2 surface2UV = surfaceUVToObjectSpace(surfaceUV);
		if (surfaceUV.x < 0 || surfaceUV.y < 0 || surfaceUV.x > 1 || surfaceUV.y > 1) {
			System.err.println("Bad surface UV!");
			System.err.println("UV     : " + surfaceUV);
			System.err.println("UV2    : " + surface2UV);
			System.err.println("Object : " + hitObject);
		}
		MaterialSurface surface;
		//reading the regarding point from height/normal etc map
		surface = hitObject.mat.getPoint(surface2UV);

		//apply height map
		if (settings.useHeightMap() && hitObject.mat.isFeatureEnabled(HEIGHT_MAP) &&materialRenderSettings.getMaxHmapDist() > hit.camDst) {
			//the way i apply heightmaps might be bad or "non standard" as i have no idea
			//how this is normally done (also restricted myself from googling).
			//What i have done is very CPU intensive but it works
			HeightMappedSurface hMapSurface = applyHeightMap(
					hit,
					hitObject, normal,
					surface,
					rd,
					out,
					run
			);
			surface = hMapSurface.surf;
			normal  = hMapSurface.normal;
			if (hMapSurface.passthru) {
				out.set(hMapSurface.albedo);
				return;
			}
		}

		if (settings.useTextures() && hitObject.mat.isFeatureEnabled(TEXTURE))
			out.set(surface.albedo);
		else
			out.set(1, 1, 1);

		//apply normal map
		if (settings.useNormalMaps() && hitObject.mat.isFeatureEnabled(NORMALS))
			//broken for spheres, WELP, no idea why
			applyNormalMap(normal, surface);


		//offset for further ray casting
		//if i dont do this i cant cast a ray from the surface
		//as it would hit the surface itself
		hit.hp.mulAdd(normal, raymarchSettings.getMinDist() * 2);


		//light data
		if (settings.useSurfaceLight())
			//light up surface if suitable
			applyLight(hit, out, normal, run);

		//apply reflections (uses recursion for GPUs you would need a workaround)
		if (settings.useReflections() && hitObject.mat.isFeatureEnabled(METALLIC))
			//Ambient occlusion should work the same just with scl() instead of lerp()
			applyReflection(hit, out, surface, rd, normal, run);
		if(settings.useAmbientOcclusion()){
			applyAoMap(hit, hitObject.mat, surface, out, run);
		}
	}

	private void applyAoMap(SurfaceHit hit, Material mat, MaterialSurface surface, Vector3 out, R run)
	{

		float ao ;
		if(mat.isFeatureEnabled(AMBIENT_OCCLUSION)){
			ao = 1-surface.ao;
		} else {
			ao = hit.steps/500f;
			ao = Math.max(0,ao);
			ao = Math.min(1,ao);
		}
		ao *= 0.8f;
		out.scl(1-ao);
	}

	public AbstractRaymarchShape getHitObject(Runtime runtime) {
		return runtime.hitObject;
	}

	protected Vector2 surfaceUVToObjectSpace(Vector3 surfaceUV) {
		return new Vector2(surfaceUV.x, surfaceUV.y);//.add(1, 1).scl(0.5f);
	}

	/**
	 * recalculates the surface and hitpoint based of the heightmap
	 * <p>
	 * a selfmade alogrithm
	 * <p>
	 * Doesn't looks good on spheres but they are a lil buggy anyways
	 *
	 * @param hit       the impact point
	 * @param hitObject
	 * @param normal    the mathematical normal
	 * @param surface   the expected surface props
	 * @param rd        ray direction
	 * @param albedo
	 * @param run
	 *
	 * @return a new surface and hitpoint
	 */
	protected HeightMappedSurface applyHeightMap(
			SurfaceHit hit,
			AbstractRaymarchShape hitObject, Vector3 normal,
			MaterialSurface surface,
			Vector3 rd,
			Vector3 albedo,
			R run
	) {

		/*
		 * Instead of explaining every line here is a summery.
		 * If the surface i hit is not on the height of the geomentry (eg heighmap tells me there
		 * is a carving) i take little steps INTO the geometry, until i am at the same height as the
		 * heightmap tells me.
		 *
		 * At every step i evaluate:
		 *  * how deep am i bellow surface
		 *  * calculate the HP at the surface ABOVE the HP
		 *  * calculate uv and based of that surface(needs the uv)
		 *  * read the height from new UV
		 *
		 * After that i "push" the hitpoint to the surface of the object to get the proper UV,
		 *  texture(surface) and normal for further effects to apply on top of it
		 */
		var pbr = materialRenderSettings;
		final float PBR_STRENGTH = pbr.getPbrStrength();
		final float PBR_STEPS    = pbr.getPbrSteps();
		float step = (PBR_STRENGTH / PBR_STEPS) * Math.max(
			0.3f,
			rd.dot(normal)
		);
		float               posDeph    = 0;//depth we are into the surface
		HeightMappedSurface mappedSurf = new HeightMappedSurface();
		mappedSurf.surf     = surface;
		mappedSurf.normal   = normal;
		mappedSurf.albedo   = albedo;
		mappedSurf.passthru = false;
		int i = 0;
		while (mappedSurf.surf.depth * PBR_STRENGTH - posDeph >= step / 2 && i < (PBR_STEPS * PBR_STEPS)) {
			hit.hp.mulAdd(rd, step);
			hit.camDst += step;

			//if hp dist to obj > MINDIST*1.5 -> raycast -> replace hp

			float dstToSurf = -hitObject.sdf(hit.hp);

			//ray passed thru object
			if (dstToSurf < -raymarchSettings.getMinDist() * 1.1f) {
				SurfaceHit behindHit = raymarch(hit.hp, rd, run);
				behindHit.camDst += hit.camDst;
				resolveSurfaceColor(getCameraPos(), rd, behindHit, mappedSurf.albedo, run);
				mappedSurf.passthru = true;
				mappedSurf.normal = getNormal(hit, run);
				return mappedSurf;
			}

			hit.hp.mulAdd(mappedSurf.normal, dstToSurf);
			hit.hitDist = dstToSurf;

			Vector3 surfaceUV  = hitObject.getUVCord(hit);
			Vector2 surface2UV = surfaceUVToObjectSpace(surfaceUV);

			hitObject.mat.getPoint(surface2UV,mappedSurf.surf);
			posDeph += dstToSurf;
			i++;
		}
		mappedSurf.normal = getNormal(hit, run);
		return mappedSurf;
	}

	/**
	 * Applies the normal map to the normal of the surface (directly modifies the "normal" param)
	 *
	 * @param normal normal to modify (and use to calculate)
	 */
	protected void applyNormalMap(Vector3 normal, MaterialSurface surface) {
		Vector3    base = new Vector3(0, 0, 1);
		Quaternion q    = new Quaternion().setFromCross(base, normal);
		normal.set(q.transform(surface.normal)).nor();
	}

	/**
	 * Changes the color/brightness of a surface depending of ho much light reaches it
	 *
	 * @param hit
	 * @param albedo
	 * @param normal
	 * @param runtime
	 */
	protected void applyLight(SurfaceHit hit, Vector3 albedo, Vector3 normal, R runtime) {
		Vector3 directLight = getDirectLight(hit.hp, normal, runtime);
		albedo.scl(directLight);
	}

	/**
	 * Applies reflection on the color/surface/pixel
	 * Uses {@link #resolveSurfaceColor(Vector3, Vector3, SurfaceHit, Vector3, R)}
	 */
	protected void applyReflection(
			SurfaceHit hit,
			Vector3 albedo,
			MaterialSurface surface,
			Vector3 rd,
			Vector3 normal,
			R run
	) {
		float old = run.reflectiveVisibility;
		run.reflectiveVisibility *= surface.refect;
		if (run.reflectiveVisibility > materialRenderSettings.getReflectionThreshold()) {
			//calculate direction to shoot at
			Vector3 reflectVec = getReflectionVector(rd, normal);

			//calculate color of the ray
			SurfaceHit reflectionSurface = raymarch(hit.hp, reflectVec, run);
			Vector3    reflectionAlbedo  = new Vector3();
			resolveSurfaceColor(
					hit.hp, reflectVec,
					reflectionSurface, reflectionAlbedo,
					run
			);

			albedo.lerp(reflectionAlbedo, surface.refect);
		}
		run.reflectiveVisibility = old;
	}

	protected float getFog(float dst) {
		return min(1, dst / materialRenderSettings.getMaxFogDist());
	}

	/**
	 * Calculates how much light directly hits a surface and returns the color [0..1] as vector
	 *
	 * @param surfacePoint  the point of the surface (worldspace) to get the light intensity for
	 * @param surfaceNormal the normal of this point
	 * @param runtime
	 *
	 * @return light as color vector
	 */
	protected abstract Vector3 getDirectLight(
			Vector3 surfacePoint,
			Vector3 surfaceNormal,
			R runtime
	);

	//applys Math.pow to every component of a vec3
	protected void pow(Vector3 directLight, float i) {
		directLight.x = (float) Math.pow(directLight.x, i);
		directLight.y = (float) Math.pow(directLight.y, i);
		directLight.z = (float) Math.pow(directLight.z, i);
	}

	protected Vector3 getReflectionVector(Vector3 rd, Vector3 normal) {
		return rd.cpy().sub(normal.scl(rd.dot(normal)*2));//just math
	}

	/**
	 * @param point         point to get light impact for
	 * @param surfaceNormal normal of the surface the point resides on
	 * @param lightDir      the direction of the light to calculate for
	 * @param lVecLen       the size of the vector from point to light
	 * @param runtime
	 *
	 * @return float [0,1] how intense the light lights this point
	 */
	protected float getImpactLight(
			Vector3 point,
			Vector3 surfaceNormal,
			Vector3 lightDir,
			float lVecLen,
			R runtime
	) {
		SurfaceHit lightDis = raymarch(point, lightDir, runtime);
		float      light    = 0;
		if (!lightDis.hit || lightDis.camDst >= lVecLen)
			light = max(0f, surfaceNormal.dot(lightDir));
		return light;
	}
}
