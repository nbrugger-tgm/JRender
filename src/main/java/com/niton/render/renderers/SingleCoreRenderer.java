package com.niton.render.renderers;

import static com.niton.internal.Vectors.apply;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.RenderTarget;
import com.niton.render.api.Renderer;
import com.niton.render.api.Shader;

public class SingleCoreRenderer implements Renderer {
	private final Vector3      out = new Vector3();
	private final Vector2      uv  = new Vector2();
	private       RenderTarget target;


	@Override
	public <R> void render(Shader<R> shader) {
		int w = target.getWidth();
		int h = target.getHeight();

		long start = System.currentTimeMillis();
		shader.setDimension(w, h);
		shader.frame();

		R runtime = shader.createRuntime();

		for (int y = 0; y < h; y++) {
			float uy = (float) y / h;
			for (int x = 0; x < w; x++) {
				float ux = (float) x / w;
				uv.set(ux, uy);
				shader.render(uv, out, runtime);
				out.scl(255);
				apply(out, i -> max(i, 0));
				apply(out, i -> min(i, 255));

				target.draw(x, y, out);
			}
		}

		System.out.printf(
				"Rendering time (%dx%d) : %dms%n",
				w,
				h,
				System.currentTimeMillis() - start
		);
	}

	@Override
	public void setTarget(RenderTarget target) {
		this.target = target;
	}
}
