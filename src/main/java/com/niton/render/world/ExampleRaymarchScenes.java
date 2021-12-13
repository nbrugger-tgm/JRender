package com.niton.render.world;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.Metal;
import com.niton.render.raymarching.SurfaceHit;
import com.niton.render.shape.RaymarchShape;
import com.niton.render.shape.RaymarchSphere;

import java.util.List;


//z=1  (scene)
//z=0  (viewport)
//z=-1 (camera)
public class ExampleRaymarchScenes {
	//"geometry data"
	private static final Vector3       ball        = new Vector3(
			0.3f,
			0.1f,
			0.8f
	);
	private static final float         ballRadius  = 0.5f;
	private static final Vector3       ballColor   = new Vector3(
			0.15f,
			0.5f,
			1f
	);
	private static final Vector3       ball2       = new Vector3(
			0.0f,
			0.2f,
			2.8f
	);
	private static final float         ball2Radius = 0.4f;
	private static final float         floorPos    = -0.5f;
	private static final Material      silver      = new Metal(0.95f);
	private static final Material      futureMetal = new Material("spaceship-panels1");
	private static final Material      slabs       = new Material("rock-slab-wall");
	private static final Material      rust        = new Material()
			.setName("rustediron2")
			.setUseTexture(true)
			.setUseMetallicMap(true)
			.setUseNormalMap(true)
			.readMaps();
	public static final  RaymarchScene scene1      = new RaymarchScene(List.of(
			new RaymarchShape(
					ExampleRaymarchScenes::sdfFloor,
					ExampleRaymarchScenes::planeUVMap,
					futureMetal
			),
			new RaymarchSphere(rust, ball, ballRadius),
			new RaymarchSphere(futureMetal, ball2, ball2Radius),
			new RaymarchSphere(new Vector3(1, 0, 1), 0.1f)
	), List.of(
			new Light(
					new Vector3(.3f, .5f, -.5f),
					new Vector3(1, .5f, 1),
					.6f
			),
			new Light(
					new Vector3(-.3f, 2f, -.5f),
					new Vector3(.8f, 1, .8f),
					.2f
			)
	));
	Material test = new Material()
			.setName("test")
			.setUseTexture(true)
			.setUseMetallicMap(true)
			.setUseHeightMap(true)
			.readMaps();

	private static float sdfFloor(Vector3 p) {
		return p.y - floorPos;
	}

	/**
	 * returns the UV cord for a hitpoint on a horizontal plane
	 */
	private static Vector3 planeUVMap(SurfaceHit hp) {
		return new Vector3(Math.abs(hp.hp.x % 1), Math.abs(hp.hp.z % 1), 0);
	}
}
