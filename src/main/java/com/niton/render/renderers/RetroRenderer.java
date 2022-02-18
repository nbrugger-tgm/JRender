package com.niton.render.renderers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.niton.render.api.RenderTarget;
import com.niton.render.api.Renderer;
import com.niton.render.api.Shader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.niton.internal.Vectors.apply;
import static java.lang.Math.*;

public class RetroRenderer implements Renderer {
	private       RenderTarget    target;
	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime()
	                                                                             .availableProcessors());

	private final class RowRenderTask<R> implements Runnable {
		private final Shader<R> shader;
		private final int y;
		private final int height;
		private final int width;
		private final R   runtime;

		private RowRenderTask(
				Shader<R> shader,
				int y, int height,
				int width
		) {
			this.shader  = shader;
			this.y       = y;
			this.height  = height;
			this.width   = width;
			this.runtime = shader.createRuntime();
		}

		@Override
		public void run() {
			int   ev    = y % 2;
			float uvY   = (float) y / height;
			var   uv    = new Vector2();
			var   color = new Vector3();
			Vector3[] colors = new Vector3[width];
			for (int x = ev; x < width; x+=2) {
				float uvX = (float) x / width;
				uv.set(uvX, uvY);
				shader.render(uv, color, runtime);
				color.scl(255);
				apply(color, i -> max(i, 0));
				apply(color, i -> min(i, 255));

				//reduce color range
				color.scl(.1f);
				apply(color, i -> Float.valueOf(i.intValue()));
				color.scl(10);

				target.draw(x, y, color);
				colors[x] = color.cpy();
			}
			Vector3 accum = new Vector3();
			for (int x = 1-ev; x < width; x+=2) {
				accum.scl(0);
				if(x>0)
					accum.mulAdd(colors[x-1],.5f);
				else
					accum.scl(2);
				if (x < width-1)
					accum.mulAdd(colors[x+1],.5f);
				else
					accum.scl(2);
				target.draw(x, y, accum);
				colors[x] = accum.cpy();
			}
			for (int x = 0; x <width; x++) {
				accum.scl(0);
				accum.mulAdd(colors[x],.5f);
				if(x<width-1)
					accum.mulAdd(colors[x+1],.5f);
				else
					accum.scl(2);
				target.draw(x, y+1, accum);
			}
		}

	}

	@Override
	public <R> void render(Shader<R> shader) {
		int w = target.getWidth();
		int h = target.getHeight();

		long start = System.currentTimeMillis();

		//prepare the shader for a new frame
		shader.setDimension(w, h);
		shader.frame();

		List<Callable<Object>> rows = new ArrayList<>();
		for (int y = 0; y < h; y+=2) {
			rows.add(Executors.callable(new RowRenderTask<>(shader, y, h, w)));
		}
		try {
			executor.invokeAll(rows);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
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
