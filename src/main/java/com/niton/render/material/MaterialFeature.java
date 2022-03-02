package com.niton.render.material;

import com.niton.render.api.MapAsset;
import com.niton.render.shaders.RaymarchSceneShader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public enum MaterialFeature {
	AMBIENT_OCCLUSION("ao", 1),
	HEIGHT_MAP("height", 1),
	TEXTURE("albedo", 3),
	NORMALS("normal", 3),
	METALLIC("metallic", 1);
	private static final String FILE_FORMAT = "/%s_%s.%s";
	private final        String fileSuffix;
	private final        int    componentCount;

	MaterialFeature(String fileSuffix, int componentCount) {
		this.fileSuffix     = fileSuffix;
		this.componentCount = componentCount;
	}

	public int getComponentCount() {
		return componentCount;
	}

	public MapAsset loadAsset(String fileName) throws IOException {
		return this.loadAsset(fileName, "png");
	}

	public MapAsset loadAsset(String fileName, String extension) throws IOException {
		return new MapAsset(loadMap(fileName, extension), componentCount);
	}

	public BufferedImage loadMap(String name, String extension) throws IOException {
		return ImageIO.read(aquireClasspathStream(name, extension));
	}

	private InputStream aquireClasspathStream(String name, String extension) {
		return requireNonNull(RaymarchSceneShader.class.getResourceAsStream(
				getFileName(name, extension)
		));
	}

	public String getFileName(String name, String extension) {
		return String.format(
				FILE_FORMAT,
				name,
				fileSuffix,
				extension
		);
	}

	public BufferedImage loadMap(String name) throws IOException {
		return this.loadMap(name, "png");
	}

	public String getFileSuffix() {
		return fileSuffix;
	}
}
