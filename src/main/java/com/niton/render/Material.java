package com.niton.render;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/**
 * A set of a texture and maps for a material
 */
public class Material {
	public boolean
			useAOMap = true,
			useHeightMap = true,
			useTexture = true,
			useNormalMap = true,
			useMetallicMap = true
					;

	public Material setUseAOMap(boolean useAOMap) {
		this.useAOMap = useAOMap;
		return this;
	}

	public Material setUseHeightMap(boolean useHeightMap) {
		this.useHeightMap = useHeightMap;
		return this;
	}

	public Material setUseMetallicMap(boolean useMetallicMap) {
		this.useMetallicMap = useMetallicMap;
		return this;
	}

	public Material setUseNormalMap(boolean useNormalMap) {
		this.useNormalMap = useNormalMap;
		return this;
	}

	public Material setUseTexture(boolean useTexture) {
		this.useTexture = useTexture;
		return this;
	}

	public Material setName(String name) {
		this.name = name;
		return this;
	}

	private String        name;
	//component = map[3*(y*width+x)+compIndex]
	//redIndex = 0
	//greenIndex = 1
	//blueIndex = 2
	private MapAsset albedoMap;
	private MapAsset normalMap;
	private MapAsset heightMap;
	private MapAsset aoMap;
	private MapAsset metallicMap;

	public Material(String name) throws IOException {
		this.name = name;
		readMaps();
	}
	public Material(){
		useAOMap = false;
		useMetallicMap = false;
		useHeightMap = false;
		useNormalMap = false;
		useTexture = false;
	}

	public Material readMaps() throws IOException {
		albedoMap   = useTexture ?     new MapAsset(loadMap("albedo"),3)   : null;
		normalMap   = useNormalMap ?   new MapAsset(loadMap("normal"),3)   : null;
		heightMap   = useHeightMap ?   new MapAsset(loadMap("height"),1)   : null;
		aoMap       = useAOMap ?       new MapAsset(loadMap("ao"),1)   : null;
		metallicMap = useMetallicMap ? new MapAsset(loadMap("metallic"),1) : null;
		System.gc();
		return this;
	}

	private final static String format = "/%s_%s.png";
	private BufferedImage loadMap(String type) throws IOException {
		return ImageIO.read(Objects.requireNonNull(RaymarchShader.class.getResourceAsStream(String.format(
				format,
				this.name,
				type
		))));
	}

	private float scalarUvMap(Vector2 surfaceUV, float[] map,int width,int height) {
		int x = (int) (width * surfaceUV.x);
		int y = (int) (height * surfaceUV.y);
		return map[y*width+x];
	}
	public Surface getPoint(Vector2 surface2UV) {
		Surface sur = new Surface();
		float[] buff = new float[3];
		if (useTexture)
			sur.albedo.set(albedoMap.get(surface2UV,buff));
		if (useNormalMap)
			sur.normal.set(normalMap.get(surface2UV,buff)).sub(0.5f).scl(2).nor();
		if (useHeightMap)
			sur.depth = 1 - heightMap.get(surface2UV,buff)[0];
		if (useAOMap)
			sur.ao = aoMap.get(surface2UV,buff)[0];
		if (useMetallicMap)
			sur.refect = metallicMap.get(surface2UV,buff)[0];
		return sur;
	}
}
