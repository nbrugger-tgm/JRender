package com.niton.render;

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
import java.util.function.Supplier;

public class MultiCoreRenderer<R> implements Renderer<R> {
	private final Supplier<R>     runtimeProducer;
	private final ExecutorService executor;
	private       RenderTarget    renderTarget;

	private class RowRenderTask implements Runnable {
		private final int       y;
		private final Shader<R> shader;
		private final R         runtime = runtimeProducer.get();

		public RowRenderTask(
				int row,
				Shader<R> shader
		) {
			this.y      = row;
			this.shader = shader;
		}

		@Override
		public void run() {
			int     width  = renderTarget.getWidth();
			int     height = renderTarget.getHeight();
			Vector3 out    = new Vector3();
			Vector2 uv     = new Vector2();
			float   uvY    = (float) y / (float) height;
			for (int x = 0; x < width; x++) {
				float uvX = (float) x / width;
				uv.set(uvX, uvY);
				shader.render(uv, out, runtime);
				out.scl(255);
				out.clamp(0, 255);
				renderTarget.draw(x, y, out);
			}
		}
	}

	public MultiCoreRenderer(Supplier<R> runtimeProducer, int maxThreads) {
		this.runtimeProducer = runtimeProducer;
		executor             = Executors.newFixedThreadPool(maxThreads);
	}


	@Override
	public synchronized void render(Shader<R> shader) {
		int w = renderTarget.getWidth();
		int h = renderTarget.getHeight();

		long start = System.currentTimeMillis();

		//prepare the shader for a new frame
		shader.setDimension(w, h);
		shader.frame();

		List<Callable<Object>> rows = new ArrayList<>();
		for (int y = 0; y < h; y++) {
			rows.add(Executors.callable(new RowRenderTask(y, shader)));
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
		renderTarget = target;
	}
}
