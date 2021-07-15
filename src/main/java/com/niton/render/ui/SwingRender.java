package com.niton.render.ui;

import com.badlogic.gdx.math.Vector2;
import com.niton.render.RenderingThread;
import com.niton.render.RenderingThread.PixelTask;
import com.niton.render.SwingShader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Uses a jpanel to render a shader onto it
 *
 * (still useable as a jpanel if you want)
 */
public class SwingRender extends JPanel {

	private final ThreadGroup threadGroup;
	private final SwingShader shader;
	BufferedImage  img;
	WritableRaster raster;
	private final RenderingThread[]        threads;


	//in this pool there are many PixelTask instances ready to be used
	//this is to avoid retundant object initialization
	private final Queue<PixelTask>         pixelTaskPool = new ArrayDeque<>();
	private final BlockingQueue<PixelTask> completedTasks = new LinkedBlockingQueue<>();
	//Threads get the tasks from here
	private final BlockingQueue<PixelTask> taskQueue = new LinkedBlockingQueue<>(1024);

	public SwingRender(SwingShader shader) {
		this.shader = shader;
		threadGroup = new ThreadGroup("Renderers");
		int cores = Runtime.getRuntime().availableProcessors();
		threads = new RenderingThread[cores];
		for (int i  = 0;i<cores;i++){
			threads[i] = new RenderingThread(shader,taskQueue,completedTasks,threadGroup);
			threads[i].start();
		}
		threadGroup.setDaemon(true);
		createPixelBuffer(1,1);
	}

	private void createPixelBuffer(int w,int h) {
		img    = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		raster = img.getRaster();
		while (pixelTaskPool.size()<Math.max(w*h,taskQueue.remainingCapacity())){
			pixelTaskPool.add(new PixelTask());
		}
	}


	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = (int) g.getClipBounds().getWidth(), h = (int) g.getClipBounds().getHeight();
		int dimens = w*h;

		//i render to an buffered image. Why?
		//* Its easy
		//* you could straigt up save the image as JPEG/PNG
		if(img.getWidth() != w || img.getHeight() != h)
			createPixelBuffer(w,h);
		shader.setDimension(w,h);
		shader.frame();
		System.out.println("w/h = "+w+"/"+h+" = "+w*h+" = "+pixelTaskPool.size()+" >= "+taskQueue.remainingCapacity());
		for (int i = 0; i < w; i++) {
			for (int y = 0; y < h; y++) {
				//calculating UVs and interpreting the result of the shader
				float ux = (float) i/w;
				float uy = (float) y/h;
				PixelTask task;
				synchronized (pixelTaskPool) {
					task = pixelTaskPool.poll();
				}
				task.uvCordinate = new Vector2(ux,uy);
				task.x = i;
				task.y = y;
				task.raster = raster;
				try {
					taskQueue.put(task);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//paint image to UI
		for (int i = 0; i < dimens; i++) {
			PixelTask completed;
			try {
				completed = completedTasks.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			raster.setPixel(completed.x,completed.y,completed.output);
			pixelTaskPool.add(completed);
		}
		g2d.drawImage(img,0,0,null);
	}
}
