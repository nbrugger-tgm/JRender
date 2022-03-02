package com.niton.render.example;

import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.SignedDisdanceFunction;
import com.niton.render.material.Material;
import com.niton.render.raymarching.SurfaceHit;
import com.niton.render.shape.RaymarchShape;
import com.niton.render.shape.RaymarchSphere;
import com.niton.render.world.Light;
import com.niton.render.world.RaymarchScene;

import java.util.List;
import java.util.function.Function;

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
	private static final float         floorPos    = -0.75f;

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
							new Vector3(.5f, .5f, -.5f),
							rgb(220, 220, 245),
							.5f
					),
					new Light(
							new Vector3(-.5f, 1.5f, -.5f),
							rgb(232, 187, 149),
							.5f
					)
			)
	);

	private static Vector3 rgb(int r, int g, int b)
	{
		return new Vector3(255f/r, 255f/g, 255f/b);
	}

	public static final RaymarchScene scene2 = new RaymarchScene(
		List.of(
			new RaymarchShape(
				ExampleRaymarchScenes::sdfFloor,
				ExampleRaymarchScenes::planeUVMap,
				new Material(Vector3.Zero.add(1))
			),
			new RaymarchSphere(ExampleMaterials.slabs,new Vector3(0, 0, 1), .8f)
		),
		List.of(

			new Light(
				new Vector3(0, 3, -2),
				new Vector3(0.95f, 0.925f, .925f),
				1.4f
			),
			new Light(
				new Vector3(.5f, .5f, -.5f),
				rgb(220, 220, 245),
				.5f
			),
			new Light(
				new Vector3(-.5f, 1.5f, -.5f),
				rgb(232, 187, 149),
				.5f
			)
		)
	);

	public static final RaymarchScene scene3 = new RaymarchScene(
		List.of(
			new RaymarchShape(
				ExampleRaymarchScenes.sdfFloor(-0.2f),
				ExampleRaymarchScenes::planeUVMap,
				slabs
			)
		),
		List.of(
			new Light(
				new Vector3(0, 0, -1),
				new Vector3(0.95f, 0.925f, .925f),
				.97f
			),
			new Light(
				new Vector3(0, 0, -2),
				new Vector3(0.95f, 0.86f, .7f),
				.75f
			),
			new Light(
				new Vector3(.5f, .5f, -.5f),
				rgb(220, 220, 245),
				.5f
			),
			new Light(
				new Vector3(-.5f, 1.5f, -.5f),
				rgb(232, 187, 149),
				.5f
			)
		)
	);
	public static final RaymarchScene scene4 = new RaymarchScene(
		List.of(
			new RaymarchShape(
				ExampleRaymarchScenes.sdfFloor(-0.25f),
				ExampleRaymarchScenes::planeUVMap,
				futureMetal
			)
		),
		List.of(
			new Light(
				new Vector3(0, 0, -1.2f),
				new Vector3(0.95f, 0.925f, .925f),
				1.2f
			),
			new Light(
				new Vector3(.5f, 1.5f, -1),
				new Vector3(0.95f, 0.925f, .925f),
				.8f
			),
			new Light(
				new Vector3(-.5f, 1.5f, -.5f),
				new Vector3(.98f, .9f, .5f),
				.8f
			)
		)
	);

	private static float sdfFloor(Vector3 p) {
		return p.y - floorPos;
	}
	private static SignedDisdanceFunction sdfFloor(float pos) {
		return p->p.y - pos;
	}

	/**
	 * returns the UV cord for a hitpoint on a horizontal plane
	 */
	private static Vector3 planeUVMap(SurfaceHit hp) {
		return new Vector3(Math.abs(hp.hp.x % 1), Math.abs(hp.hp.z % 1), 0);
	}
}
