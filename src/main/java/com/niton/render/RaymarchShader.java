package com.niton.render;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.Shader;
import com.niton.render.world.Surface;

public abstract class RaymarchShader implements Shader<RaymarchSceneShader.RaymarchRuntime> {
	/**
	 * Variables with heavy performanc/quality impact, play around with it
	 */

	//z=1  (scene)
	//z=0  (viewport)
	//z=-1 (camera)
	//ro = ray origin -> start of the raycast
	protected static final Vector3        ro                   = new Vector3(0f, 0f, -1);
	//the distance from the surface at wich a ray "hits" a surface
	protected static final float          MIN_DST              = .0001f;//.0 -> 0.0
	//if a ray goes further than this dist the ray is seen as "hitting no object" -> into the void
	protected static final float          MAX_DST              = 20;
	//the amount of raymarching steps before interrupting (influences rendering around edges)
	protected static final int            MAX_STEPS            = 2000;
	//Distance at which the fog is the only thing you see -> cant see beyond this distance
	protected static final float          MAX_FOG_DIST         = MAX_DST * 1.75f;
	//how much carving the heightmap causes             value in world space
	protected static final float          PBR_STRENGTH         = 0.05f;
	//VERY CPU HEAVY    defines how many steps to make to march thru pseudo space caused by heightmaps
	//makes "fake" edges from heightmaps look way better, recommended range 10-70
	protected static final int            PBR_STEPS            = 50;
	//If a point is visible in reflection it wont be rendered if it was just visible by taht percentage
	//0.01 -> 1% -> if a surface is 50% reflective and it reflects a surface which itself if 50%
	//reflective the next reflected pixel will just make 25% of the color in the currently rendered pixel
	//if this percentage is very low (eg. bellow REF_THRESHOLD) it is not worth rendering it
	//HIGHT performance inpact, recommended 0.2 to 0.01
	protected static final float          REFLECTION_THRESHOLD = 0.01f;
	//the disdance at which heightmaps are not applied anymore (LOD in the end of the day)
	protected static final float          MAX_HMAP_DIST        = 3f;
	protected static final Vector3        WHITE                = new Vector3(1, 1, 1);
	protected static final Vector3        FOG_COLOR            = new Vector3(WHITE).scl(0.6f);
	//color of the "sky". There is no sky, this color is used when the ray has "no end" eg hit nothing
	protected static final Vector3        SKY_ALBEDO           = new Vector3(
			0.1f,
			0.23f,
			0.6f
	);
	protected              RenderSettings settings;
	private                int            frame                = 0;
	private                float          ratio;

	public static class RaymarchRuntime {
		//keeps deep the reflection hierarchy is, to possiby interrupt
		private float reflectionDeph;
	}

	public RenderSettings getSettings() {
		return settings;
	}

	public void setSettings(RenderSettings settings) {
		this.settings = settings;
	}

	@Override
	public void render(Vector2 screenUV, Vector3 out, RaymarchRuntime run) {
		Vector3    worldUV = screenToWorldUV(screenUV);
		Vector3    rd      = worldUV.cpy().sub(ro).nor();
		SurfaceHit hit     = raymarch(ro, rd);
		run.reflectionDeph = 1f;
		resolveSurfaceColor(ro, rd, hit, out, run);
	}

	@Override
	public void setDimension(int w, int h) {
		ratio = (float) w / h;
	}

	@Override
	public void frame() {
		frame++;
	}

	/**
	 * Calculates the color of a point in WORLD space, given a raycaster origin and the direction of
	 * the ray
	 *
	 * @param ro  the origin of the ray, used to calculate disdances, the "lense of the camera"
	 * @param rd  ray direction used to calculate lightimpact into the "lense"
	 * @param hit the point at which the ray hit the surface including metadata
	 * @param out
	 *
	 * @return the color as Vec3 x,y,z->r,g,b
	 */
	public void resolveSurfaceColor(
			Vector3 ro,
			Vector3 rd,
			SurfaceHit hit,
			Vector3 out,
			RaymarchRuntime run
	) {
		out.set(SKY_ALBEDO);

		if (hit.object != null) {

			//normal of the surface calculated using sampling (no normal maps just math)
			Vector3 normal = getNormal(hit);
			//the uv in 3D object space
			Vector3 surfaceUV = hit.object.getUVCord(hit);
			//the UV in 2d object space
			Vector2 surface2UV = surfaceUVToObjectSpace(surfaceUV);
			if (surfaceUV.x < 0 || surfaceUV.y < 0 || surfaceUV.x > 1 || surfaceUV.y > 1) {
				System.err.println("Bad surface UV!");
				System.err.println("UV     : " + surfaceUV);
				System.err.println("UV2    : " + surface2UV);
				System.err.println("Object : " + hit.object);
			}
			Surface surface;
			//reading the regarding point from height/normal etc map
			surface = hit.object.mat.getPoint(surface2UV);

			//apply height map
			if (settings.useHeightMap() && MAX_HMAP_DIST > hit.camDst) {
				//the way i apply heightmaps might be bad or "non standard" as i have no idea
				//ho this is normally done (also restricted myself from googeling).
				//What i have done is very CPU intensive but it works
				HeightMappedSurface hMapSurface = applyHeightMap(hit, normal, surface, rd, out,
				                                                 run
				);
				surface = hMapSurface.surf;
				normal  = hMapSurface.normal;
				if (hMapSurface.passthru) {
					out.set(hMapSurface.albedo);
					return;
				}
			}

			if (settings.useTextures())
				out.set(surface.albedo);
			else
				out.set(1, 1, 1);

			//apply normal map
			if (settings.useNormalMaps())
				//broken for spheres, WELP, no idea why
				applyNormalMap(normal, surface);


			//offset for further ray casting
			//if i dont do this i cant cast a ray from the surface
			//as it would hit the surface itself
			hit.hp.mulAdd(normal, MIN_DST * 2);


			//light data
			if (settings.useSurfaceLight())
				//light up surface if suitable
				applyLight(hit, out, normal);

			//apply reflections (uses recursion for GPUs you would need a workaround)
			if (settings.useReflections())
				//Ambient occlusion should work the same just with scl() instead of lerp()
				applyReflection(hit, out, surface, rd, normal, run);

		}
		if (settings.isUseFog()) {
			//fog just scales with disdance to cam
			float fog = getFog(hit.camDst);
			out.lerp(FOG_COLOR, fog);
		}
		//in-lense light
		//this should act like lens flare and speculars , but it doesnt
		//needs to be fixed
		if (settings.isUseDirectLight()) {
			Vector3 directLight = getDirectLight(ro, rd);
			pow(directLight, 16f);
			out.add(directLight);
		}
	}

	//applys Math.pow to every component of a vec3
	protected void pow(Vector3 directLight, float i) {
		directLight.x = (float) Math.pow(directLight.x, i);
		directLight.y = (float) Math.pow(directLight.y, i);
		directLight.z = (float) Math.pow(directLight.z, i);
	}

	/**
	 * Calculates how much light directly hits a surface and returns the color [0..1] as vector
	 *
	 * @param surfacePoint  the point of the surface (worldspace) to get the light intensity for
	 * @param surfaceNormal the normal of this point
	 *
	 * @return light as color vector
	 */
	protected abstract Vector3 getDirectLight(Vector3 surfacePoint, Vector3 surfaceNormal);

	/**
	 * Changes the color/brightness of a surface depending of ho much light reaches it
	 *
	 * @param hit
	 * @param albedo
	 * @param normal
	 */
	protected void applyLight(SurfaceHit hit, Vector3 albedo, Vector3 normal) {
		Vector3 directLight = getDirectLight(hit.hp, normal);
		albedo.scl(directLight);
	}

	/**
	 * Applies reflection on the color/surface/pixel
	 * Uses {@link #resolveSurfaceColor(Vector3, Vector3, SurfaceHit, Vector3, RaymarchRuntime)}
	 */
	protected void applyReflection(
			SurfaceHit hit,
			Vector3 albedo,
			Surface surface,
			Vector3 rd,
			Vector3 normal,
			RaymarchRuntime run
	) {
		run.reflectionDeph *= surface.refect;
		if (run.reflectionDeph > REFLECTION_THRESHOLD) {
			//calculate direction to shoot at
			Vector3 reflectVec = getReflectionVector(rd, normal);

			//calculate color of the ray
			SurfaceHit reflectionSurface = raymarch(hit.hp, reflectVec);
			Vector3    reflectionAlbedo  = new Vector3();
			resolveSurfaceColor(
					hit.hp, reflectVec,
					reflectionSurface, reflectionAlbedo,
					run
			);

			albedo.lerp(reflectionAlbedo, surface.refect);
		}
	}

	protected Vector3 getReflectionVector(Vector3 rd, Vector3 normal) {
		return rd.cpy().add(normal).nor();//just math
	}

	/**
	 * recalculates the surface and hitpoint based of the heightmap
	 * <p>
	 * a selfmade alogrithm
	 * <p>
	 * Doesn't looks good on spheres but they are a lil buggy anyways
	 *
	 * @param hit     the impact point
	 * @param normal  the mathematical normal
	 * @param surface the expected surface props
	 * @param rd      ray direction
	 * @param albedo
	 * @param run
	 *
	 * @return a new surface and hitpoint
	 */
	protected HeightMappedSurface applyHeightMap(
			SurfaceHit hit,
			Vector3 normal,
			Surface surface,
			Vector3 rd,
			Vector3 albedo,
			RaymarchRuntime run
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

			float dstToSurf = -hit.object.sdf(hit.hp);

			//ray passed thru object
			if (dstToSurf < -MIN_DST * 1.2f) {
				SurfaceHit behindHit = raymarch(hit.hp, rd);
				behindHit.camDst += hit.camDst;
				resolveSurfaceColor(ro, rd, behindHit, mappedSurf.albedo, run);
				mappedSurf.passthru = true;
				return mappedSurf;
			}

			hit.hp.mulAdd(mappedSurf.normal, dstToSurf);
			hit.dst = dstToSurf;

			Vector3 surfaceUV  = hit.object.getUVCord(hit);
			Vector2 surface2UV = surfaceUVToObjectSpace(surfaceUV);

			mappedSurf.surf   = hit.object.mat.getPoint(surface2UV);
			mappedSurf.normal = getNormal(hit);
			posDeph += dstToSurf;
			i++;

		}
		return mappedSurf;
	}

	protected Vector2 surfaceUVToObjectSpace(Vector3 surfaceUV) {
		return new Vector2(surfaceUV.x, surfaceUV.y);//.add(1, 1).scl(0.5f);
	}

	/**
	 * Applies the normal map to the normal of the surface (directly modifies the "normal" param)
	 *
	 * @param normal normal to modify (and use to calculate)
	 */
	protected void applyNormalMap(Vector3 normal, Surface surface) {
		Vector3    base = new Vector3(0, 0, 1);
		Quaternion q    = new Quaternion().setFromCross(base, normal);
		normal.set(q.transform(surface.normal)).nor();
	}

	protected Vector3 screenToWorldUV(Vector2 screenUV) {
		screenUV.sub(0.5f, 0.5f);
		screenUV.y *= -1;
		return new Vector3(ro.x + screenUV.x * ratio, ro.y + screenUV.y, ro.z + 1);
	}

	protected float getFog(float dst) {
		return Math.min(1, dst / MAX_FOG_DIST);
	}

	/**
	 * @param point         point to get light impact for
	 * @param surfaceNormal normal of the surface the point resides on
	 * @param lightDir      the direction of the light to calculate for
	 * @param lVecLen       the size of the vector from point to light
	 *
	 * @return float [0,1] how intense the light lights this point
	 */
	protected abstract float getImpactLight(
			Vector3 point,
			Vector3 surfaceNormal,
			Vector3 lightDir,
			float lVecLen
	);

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
	protected SurfaceHit raymarch(Vector3 ro, Vector3 rd) {
		Vector3 check = ro.cpy();
		float   dst   = 0;

		for (int i = 0; i < MAX_STEPS; i++) {
			SurfaceHit hit = sdf(check);

			//march forward
			check.mulAdd(rd, hit.dst);
			hit.hp = check;
			dst += hit.dst;

			hit.camDst = dst;

			//into the void
			if (hit.camDst >= MAX_DST || hit.dst >= MAX_DST)
				break;

			//yay we hit terrain
			if (hit.dst < MIN_DST) {
				return hit;
			}
		}
		//result of into he void
		return new SurfaceHit(null, null, MAX_DST, MAX_DST);
	}

	/**
	 * The singed disdance function
	 * <p>
	 * Calculates the disdance to ANY geometry and the closest object
	 *
	 * @param p the point to calculate the disdance for
	 *
	 * @return the "hit"
	 */
	protected abstract SurfaceHit sdf(Vector3 p);

	/**
	 * Dont know the name of the algorithm
	 * <p>
	 * Computes the normal for a point  on a surface
	 * <p>
	 * Stolen and modified from a yt video
	 *
	 * @param p the surface to get the normal from
	 *
	 * @return
	 */
	protected Vector3 getNormal(SurfaceHit p) {
		//float d = sdf(p.hp); //bcs why tho
		float   d    = MIN_DST;
		Vector3 offX = p.hp.cpy();
		offX.x -= 0.01;

		Vector3 offY = p.hp.cpy();
		offY.y -= 0.01;

		Vector3 offZ = p.hp.cpy();
		offZ.z -= 0.01;
		Vector3 n = new Vector3(
				d - sdf(offX).dst,
				d - sdf(offY).dst,
				d - sdf(offZ).dst
		);

		return n.nor();
	}
}
