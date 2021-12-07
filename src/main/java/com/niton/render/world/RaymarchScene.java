package com.niton.render.world;

import com.niton.render.shape.AbstractRaymarchShape;

import java.util.ArrayList;
import java.util.List;

public class RaymarchScene {
	private List<AbstractRaymarchShape> objects = new ArrayList<>();
	private List<Light>                 lights  = new ArrayList<>();

	public RaymarchScene(
			List<AbstractRaymarchShape> objects,
			List<Light> lights
	) {
		this.objects = objects;
		this.lights  = lights;
	}

	public RaymarchScene() {
	}

	public void addObject(AbstractRaymarchShape object) {
		objects.add(object);
	}

	public void addLight(Light light) {
		lights.add(light);
	}

	public List<AbstractRaymarchShape> getObjects() {
		return objects;
	}

	public void setObjects(AbstractRaymarchShape... objects) {
		this.objects.clear();
		this.objects.addAll(List.of(objects));
	}

	public List<Light> getLights() {
		return lights;
	}

	public void setLights(Light... lights) {
		this.lights.clear();
		this.lights.addAll(List.of(lights));
	}

	/**
	 * Called before each frame
	 */
	public void update() {}
}
