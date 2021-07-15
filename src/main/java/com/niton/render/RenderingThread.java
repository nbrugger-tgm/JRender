package com.niton.render;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.awt.image.WritableRaster;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class RenderingThread extends Thread {
	private final SwingShader              shader;
	private final BlockingQueue<PixelTask> taksQueue;
	private final BlockingQueue<PixelTask>         completedPool;

	public RenderingThread(SwingShader shader,
	                       BlockingQueue<PixelTask> taksQueue,
	                       BlockingQueue<PixelTask> completedPool,
	                       ThreadGroup group) {
		super(group,"Renderer");
		this.shader        = shader;
		this.taksQueue     = taksQueue;
		this.completedPool = completedPool;
	}


	@Override
	public void run() {
		Vector3 output;
		while(true) {
			PixelTask task;
			try {
				task = taksQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			output = shader.render(task.uvCordinate);
			output.scl(255);

			task.output = new float[]{
					Math.min(255, output.x),
					Math.min(255, output.y),
					Math.min(255, output.z)
			};
			try {
				completedPool.put(task);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	public static class PixelTask  {
		public Vector2        uvCordinate;
		public int            x;
		public int            y;
		public WritableRaster raster;
		public float[] output;
	}
}
