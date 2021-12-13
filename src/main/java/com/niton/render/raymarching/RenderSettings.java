package com.niton.render.raymarching;

public class RenderSettings {
	private boolean useTextures;
	private boolean useNormalMaps;
	private boolean useHeightMap;
	private boolean useReflections;
	private boolean useSurfaceLight;
	private boolean useFog;
	private boolean useDirectLight;
	private boolean useShadows = true;

	public boolean useTextures() {
		return useTextures;
	}

	public void setUseTextures(boolean useTextures) {
		this.useTextures = useTextures;
	}

	public boolean useNormalMaps() {
		return useNormalMaps;
	}

	public void setUseNormalMaps(boolean useNormalMaps) {
		this.useNormalMaps = useNormalMaps;
	}

	public boolean useHeightMap() {
		return useHeightMap;
	}

	public void setUseHeightMap(boolean useHeightMap) {
		this.useHeightMap = useHeightMap;
	}

	public boolean useReflections() {
		return useReflections;
	}

	public void setUseReflections(boolean useReflections) {
		this.useReflections = useReflections;
	}

	public boolean useSurfaceLight() {
		return useSurfaceLight;
	}

	public void setUseSurfaceLight(boolean useSurfaceLight) {
		this.useSurfaceLight = useSurfaceLight;
	}

	public boolean useFog() {
		return useFog;
	}

	public void setUseFog(boolean useFog) {
		this.useFog = useFog;
	}

	public boolean useDirectLight() {
		return useDirectLight;
	}

	public void setUseDirectLight(boolean useDirectLight) {
		this.useDirectLight = useDirectLight;
	}

	public void setUseShadows(boolean useShadows) {
		this.useShadows = useShadows;
	}

	public boolean useShadows() {
		return useShadows;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("RenderSettings{");
		sb.append("useTextures=").append(useTextures);
		sb.append(", useNormalMaps=").append(useNormalMaps);
		sb.append(", useHeightMap=").append(useHeightMap);
		sb.append(", useReflections=").append(useReflections);
		sb.append(", useSurfaceLight=").append(useSurfaceLight);
		sb.append(", useFog=").append(useFog);
		sb.append(", useDirectLight=").append(useDirectLight);
		sb.append('}');
		return sb.toString();
	}
}
