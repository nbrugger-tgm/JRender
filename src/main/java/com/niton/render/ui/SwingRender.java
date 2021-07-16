package com.niton.render.ui;

import com.niton.render.RenderingThread;
import com.niton.render.RenderingThread.PixelTask;
import com.niton.render.SwingShader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

/**
 * Uses a jpanel to render a shader onto it
 *
 * (still useable as a jpanel if you want)
 */
public class SwingRender extends JPanel {

	private final ThreadGroup threadGroup;
	private final SwingShader<?> shader;
	BufferedImage  img;
	WritableRaster raster;
	private final RenderingThread<?>[]        threads;


	//in this pool there are many PixelTask instances ready to be used
	//this is to avoid retundant object initialization
	private final BlockingQueue<PixelTask> pixelTaskPool  = new LinkedBlockingQueue<>(1024);
	private final BlockingQueue<PixelTask> completedTasks = new LinkedBlockingQueue<>(1024);
	//Threads get the tasks from here
	private final BlockingQueue<PixelTask> taskQueue = new LinkedBlockingQueue<>(1024);


	public<R> SwingRender(SwingShader<R> shader, Supplier<R> runtimeProducer) {
		this.shader = shader;
		threadGroup = new ThreadGroup("Renderers");
		int cores = Runtime.getRuntime().availableProcessors()-2;
		threads = new RenderingThread[cores];
		for (int i  = 0;i<cores;i++){
			threads[i] = new RenderingThread<R>(
					shader,
					taskQueue, completedTasks, threadGroup,
					runtimeProducer.get());
			threads[i].start();
		}
		threadGroup.setDaemon(true);
		createPixelBuffer(1,1);
	}

	private void createPixelBuffer(int w,int h) {
		img    = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		raster = img.getRaster();
		while (pixelTaskPool.remainingCapacity()>0){
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
		long start = System.currentTimeMillis();
		new Thread(()->{
			for (int i = 0; i < w; i++) {
				for (int y = 0; y < h; y++) {
					//calculating UVs and interpreting the result of the shader
					try {
						PixelTask task = pixelTaskPool.take();
						task.x = i;
						task.y = y;
						task.raster = raster;
						taskQueue.put(task);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		},"Pixel task creator").start();
		//paint image to UI
		for (int i = 0; i < dimens; i++) {
			try {
				PixelTask completed = completedTasks.take();
				raster.setPixel(completed.x,completed.y,completed.output);
				pixelTaskPool.add(completed);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		System.out.println("Rendering time : "+(System.currentTimeMillis()-start)+"ms");
		g2d.drawImage(img,0,0,null);
	}
}
