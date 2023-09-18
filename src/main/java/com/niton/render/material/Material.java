package com.niton.render.material;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.MapAsset;
import com.niton.render.world.ErrorAsset;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static com.niton.render.material.MaterialFeature.*;

/**
 * A set of a texture and maps for a material
 */
public class Material {
	public static final String DEFAULT_FORMAT = "png";

	private final Map<MaterialFeature, FeatureEntry> features = new EnumMap<>(
			MaterialFeature.class);
	private final Optional<String>                   name;

	private record FeatureEntry(boolean enabled, MapAsset asset, String fileType) {
		public FeatureEntry enable(boolean doEnable) {
			return new FeatureEntry(doEnable, asset, fileType);
		}

		public FeatureEntry withAsset(MapAsset newAsset) {
			return new FeatureEntry(enabled, newAsset, fileType);
		}

		public FeatureEntry withType(String type) {
			return new FeatureEntry(enabled, asset, type);
		}
	}

	public Material(String name) {
		this(name, DEFAULT_FORMAT);
	}
	public Material(String name, String ext) {
		this.name = Optional.of(name);
		loadSupportedFeatures(ext);
		readMaps();
	}

	public void loadSupportedFeatures() {
		loadSupportedFeatures(DEFAULT_FORMAT);
	}

	/**
	 * enables / disables each feature based on if a regarding files is present.
	 * Only looks for "png" files
	 */
	public void loadSupportedFeatures(String ext) {
		String fileName = name.orElseThrow(
				() -> new IllegalStateException("Material has no name to load with")
		);
		for (var feature : MaterialFeature.values()) {
			try (
					var stream = this.getClass().getResourceAsStream(
							feature.getFileName(fileName, ext)
					)
			) {
				enableFeature(feature, stream != null, ext);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * reads the maps for each <b>enabled</b> feature from the respective files
	 */
	private void readMaps() {
		String fileName = name.orElseThrow(
				() -> new IllegalStateException("Material has no name to load with")
		);
		try {
			for (var feature : MaterialFeature.values()) {
				if (isFeatureEnabled(feature)) {
					var settings = features.get(feature);
					var asset    = feature.loadAsset(fileName, settings.fileType);
					setFeatureAsset(feature, asset);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			setFeatureAsset(TEXTURE, new ErrorAsset());
		}
	}


	public Material enableFeature(MaterialFeature feature, boolean enable, String ext) {
		features.computeIfAbsent(feature, k -> new FeatureEntry(enable, null, ext));
		features.computeIfPresent(feature, (k, v) -> v.enable(enable));
		return this;
	}

	public boolean isFeatureEnabled(MaterialFeature feature) {
		var featEntry = features.get(feature);
		return featEntry != null && featEntry.enabled;
	}

	/**
	 * Setting a map for a feature also enables the feature if not defined otherwise using {@link
	 * #enableFeature(MaterialFeature, boolean, String)}
	 */
	public void setFeatureAsset(MaterialFeature feature, MapAsset asset) {
		features.computeIfAbsent(feature, k -> new FeatureEntry(true, asset, DEFAULT_FORMAT));
		features.computeIfPresent(feature, (k, v) -> v.withAsset(asset));
	}

	public Material(Vector3 color) {
		this();
		var albedoMap = new MapAsset(3, 1, 1);
		albedoMap.data[0] = color.x;
		albedoMap.data[1] = color.y;
		albedoMap.data[2] = color.z;
		setFeatureAsset(TEXTURE, albedoMap);
	}

	public Material() {
		name = Optional.empty();
	}

	public String getName() {
		return name.orElse(null);
	}

	public MaterialSurface getPoint(Vector2 surface2UV) {
		var surf = new MaterialSurface();
		getPoint(surface2UV,surf);
		return surf;
	}


	public void getPoint(Vector2 surface2UV,MaterialSurface surface) {
		float[] buff    = new float[3];


		if (isFeatureEnabled(TEXTURE))
			surface.albedo.set(getFeatureAsset(TEXTURE).get(surface2UV, buff));

		if (isFeatureEnabled(NORMALS))
			surface.normal.set(getFeatureAsset(NORMALS).get(surface2UV, buff))
			              .sub(0.5f).scl(2).nor();

		if (isFeatureEnabled(HEIGHT_MAP))
			surface.depth = 1 - getFeatureAsset(HEIGHT_MAP).getSingle(surface2UV);

		if (isFeatureEnabled(AMBIENT_OCCLUSION))
			surface.ao = getFeatureAsset(AMBIENT_OCCLUSION).getSingle(surface2UV);

		if (isFeatureEnabled(METALLIC))
			surface.refect = getFeatureAsset(METALLIC).getSingle(surface2UV);
	}

	public MapAsset getFeatureAsset(MaterialFeature feature) {
		return features.get(feature).asset;
	}
}
