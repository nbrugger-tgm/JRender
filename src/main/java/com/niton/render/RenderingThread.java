package com.niton.render;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.awt.image.WritableRaster;
import java.util.concurrent.BlockingQueue;

public class RenderingThread<R> extends Thread {
	private final SwingShader<R>              shader;
	private final R runtime;
	private final BlockingQueue<PixelTask> taksQueue;
	private final BlockingQueue<PixelTask>         completedPool;

	public RenderingThread(SwingShader<R> shader,
	                       BlockingQueue<PixelTask> taksQueue,
	                       BlockingQueue<PixelTask> completedPool,
	                       ThreadGroup group, R runtime) {
		super(group,"Renderer");
		this.shader        = shader;
		this.taksQueue     = taksQueue;
		this.completedPool = completedPool;
		this.runtime       = runtime;
	}


	@Override
	public void run() {
		Vector2 uv = new Vector2();
		Vector3 output = new Vector3();
		while(true) {
			try {
				PixelTask task = taksQueue.take();


				float ux = (float) task.x/task.raster.getWidth();
				float uy = (float) task.y/task.raster.getHeight();
				uv.set(ux,uy);
				shader.render(uv,output,runtime);
				output.scl(255);

				task.output[0] = Math.min(255, output.x);
				task.output[1] = Math.min(255, output.y);
				task.output[2] = Math.min(255, output.z);
				completedPool.put(task);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	public static class PixelTask  {
		public int            x;
		public int            y;
		public WritableRaster raster;
		public final float[] output = new float[3];
	}
}
