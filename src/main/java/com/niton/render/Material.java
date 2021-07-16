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
	private BufferedImage albedoMap;
	private BufferedImage normalMap;
	private BufferedImage heightMap;
	private BufferedImage aoMap;
	private BufferedImage metallicMap;

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
		albedoMap = useTexture?
				ImageIO.read(Objects.requireNonNull(RaymarchShader.class.getResourceAsStream("/"+ this.name +"_albedo.png"))):null;
		normalMap = useNormalMap?ImageIO.read(Objects.requireNonNull(RaymarchShader.class.getResourceAsStream("/"+ this.name +"_normal.png"))):null;
		heightMap = useHeightMap?
				ImageIO.read(Objects.requireNonNull(RaymarchShader.class.getResourceAsStream("/"+ this.name +"_height.png"))):null;
		aoMap     = useAOMap?
				ImageIO.read(Objects.requireNonNull(RaymarchShader.class.getResourceAsStream("/"+ this.name +"_height.png"))):null;
		metallicMap = useMetallicMap?
				ImageIO.read(Objects.requireNonNull(RaymarchShader.class.getResourceAsStream("/"+ this.name +"_metallic.png"))):null;
		return this;
	}
	final float[] pxl = new float[4];
	private synchronized Vector3 uvMap(Vector2 surfaceUV, BufferedImage map) {
		try {
			map.getRaster().getPixel(
					(int) ((map.getWidth() * surfaceUV.x)),
					(int) ((map.getHeight() * surfaceUV.y)),
					pxl);
		}catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		return new Vector3(pxl).scl(1/255f);
	}

	public synchronized Surface getPoint(Vector2 surface2UV) {
		Surface sur = new Surface();
		if(useTexture)
			sur.albedo = uvMap(surface2UV, albedoMap);
		if(useNormalMap)
			sur.normal = uvMap(surface2UV,normalMap).sub(0.5f).scl(2).nor();
		if(useHeightMap)
			sur.depth = 1-uvMap(surface2UV, heightMap).x;
		if(useAOMap)
			sur.ao = uvMap(surface2UV,aoMap).x;
		if(useMetallicMap)
			sur.refect = uvMap(surface2UV,metallicMap).x;
		return sur;
	}
}
