package com.niton.render.raymarching;

import com.badlogic.gdx.math.Vector3;

public class RaymarchSettings {
	public static final Vector3 WHITE               = new Vector3(1, 1, 1);
	private             float   minDist             = .0001f;//.0 -> 0.0
	//if a ray goes further than this dist the ray is seen as "hitting no object" -> into the void
	private             float   maxDist             = 20;
	//the amount of raymarching steps before interrupting (influences rendering around edges)
	private             int     maxSteps            = 5_000;
	//Distance at which the fog is the only thing you see -> cant see beyond this distance
	private             float   maxFogDist          = maxDist * 0.9f;
	//how much carving the heightmap causes             value in world space
	private             float   pbrStrength         = 0.05f;
	//VERY CPU HEAVY    defines how many steps to make to march thru pseudo space caused by heightmaps
	//makes "fake" edges from heightmaps look way better, recommended range 10-70
	private             int     pbrSteps            = 50;
	//If a point is visible in reflection it wont be rendered if it was just visible by taht percentage
	//0.01 -> 1% -> if a surface is 50% reflective and it reflects a surface which itself if 50%
	//reflective the next reflected pixel will just make 25% of the color in the currently rendered pixel
	//if this percentage is very low (eg. bellow REF_THRESHOLD) it is not worth rendering it
	//HIGHT performance inpact, recommended 0.2 to 0.01
	private             float   reflectionThreshold = 0.02f;
	//the disdance at which heightmaps are not applied anymore (LOD in the end of the day)
	private             float   maxHmapDist         = 3f;
	private             Vector3 fogColor            = new Vector3(WHITE).scl(0.75f);
	//color of the "sky". There is no sky, this color is used when the ray has "no end" eg hit nothing
	private             Vector3 skyAlbedo           = new Vector3(
			0.1f,
			0.23f,
			0.6f
	);

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

	public float getMaxFogDist() {
		return maxFogDist;
	}

	public void setMaxFogDist(float maxFogDist) {
		this.maxFogDist = maxFogDist;
	}

	public float getPbrStrength() {
		return pbrStrength;
	}

	public void setPbrStrength(float pbrStrength) {
		this.pbrStrength = pbrStrength;
	}

	public int getPbrSteps() {
		return pbrSteps;
	}

	public void setPbrSteps(int pbrSteps) {
		this.pbrSteps = pbrSteps;
	}

	public float getReflectionThreshold() {
		return reflectionThreshold;
	}

	public void setReflectionThreshold(float reflectionThreshold) {
		this.reflectionThreshold = reflectionThreshold;
	}

	public float getMaxHmapDist() {
		return maxHmapDist;
	}

	public void setMaxHmapDist(float maxHmapDist) {
		this.maxHmapDist = maxHmapDist;
	}

	public Vector3 getFogColor() {
		return fogColor;
	}

	public void setFogColor(Vector3 fogColor) {
		this.fogColor = fogColor;
	}

	public Vector3 getSkyAlbedo() {
		return skyAlbedo;
	}

	public void setSkyAlbedo(Vector3 skyAlbedo) {
		this.skyAlbedo = skyAlbedo;
	}
}
