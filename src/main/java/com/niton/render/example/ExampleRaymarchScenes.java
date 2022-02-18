package com.niton.render.example;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.raymarching.SurfaceHit;
import com.niton.render.shape.RaymarchShape;
import com.niton.render.shape.RaymarchSphere;
import com.niton.render.world.Light;
import com.niton.render.world.RaymarchScene;

import java.util.List;

import static com.niton.render.example.ExampleMaterials.*;


//z=1  (scene)
//z=0  (viewport)
//z=-1 (camera)
public final class ExampleRaymarchScenes {
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

	public static final RaymarchScene scene1 = new RaymarchScene(
			List.of(
					new RaymarchShape(
							ExampleRaymarchScenes::sdfFloor,
							ExampleRaymarchScenes::planeUVMap,
							futureMetal
					),
					new RaymarchSphere(rust, ball, ballRadius),
					new RaymarchSphere(futureMetal, ball2, ball2Radius),
					new RaymarchSphere(new Vector3(1, 0, 1), 0.1f)
			),
			List.of(
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
			)
	);

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
