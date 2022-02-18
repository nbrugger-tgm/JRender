package com.niton.render.raymarching;

public class RaymarchSettings {
	private float minDist  = .0001f;//.0 -> 0.0
	//if a ray goes further than this dist the ray is seen as "hitting no object" -> into the void
	private float maxDist  = 20;
	//the amount of raymarching steps before interrupting (influences rendering around edges)
	private int   maxSteps = 5_000;

	public float getMinDist() {
		return minDist;
	}

	public void setMinDist(float minDist) {
		this.minDist = minDist;
	}

	public float getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(float maxDist) {
		this.maxDist = maxDist;
	}

	public int getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}
}
