package com.niton.render;


import com.niton.reactj.ReactiveController;
import com.niton.reactj.ReactiveProxy;
import com.niton.render.api.Renderer;
import com.niton.render.renderers.MultiCoreRenderer;
import com.niton.render.renderers.SingleCoreRenderer;
import com.niton.render.shaders.BaseRaymarchShader;
import com.niton.render.shaders.EndlessSphereShader;
import com.niton.render.ui.JShaderPanel;
import com.niton.render.ui.ReactableSettings;
import com.niton.render.ui.RenderSettingUI;
import com.niton.render.world.ExampleRaymarchScenes;
import com.niton.render.world.RaymarchScene;

import javax.swing.*;
import java.awt.*;

import static com.niton.reactj.ReactiveProxy.createProxy;

public class Launcher {
	//nothing is animated atm so no need to enable this
	//set this to true if you want to animate a moving light (looks nice)
	//framerate is horrible tho
	//if you want to have it somewhat smooth make the render window tiny
	static boolean               animated           = true;
	static boolean               useMultipleThreads = true;
	static int                   renderingThreads   = Runtime.getRuntime().availableProcessors();
	static RaymarchScene         scene              = ExampleRaymarchScenes.scene1;
	static BaseRaymarchShader<?> shader             = new EndlessSphereShader();//new RaymarchSceneShader(scene);

	public static void main(String[] args) throws Throwable {

		Renderer renderer = useMultipleThreads ?
				new MultiCoreRenderer(renderingThreads) :
				new SingleCoreRenderer();


		//you dont need to understand this
		//if you WANT to understand : https://github.com/nbrugger-tgm/reactj
		var settingProxy = createProxy(ReactableSettings.class);
		shader.setSettings(settingProxy.getObject());

		//the frame to render on
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(420, 360);
		var shaderPanel = new JShaderPanel(renderer, shader);
		frame.getContentPane().add(shaderPanel);
		frame.setVisible(true);


		openSettingsUI(settingProxy, shaderPanel);

		if (animated)
			while (true) {
				shaderPanel.repaint();
				Thread.sleep(10);//bcs the rendering delay isnt horrible enought :)
			}
	}

	private static void openSettingsUI(
			ReactiveProxy<ReactableSettings> settingProxy,
			JShaderPanel r
	) {
		//creates the UI for the enable/disable buttons
		JFrame settingFrame = new JFrame();
		settingFrame.getContentPane().setLayout(new GridLayout(1, 1));

		//you dont need to understand this
		//if you WANT to understand : https://github.com/nbrugger-tgm/reactj
		RenderSettingUI ui = new RenderSettingUI();
		ui.renderEvent.addListener(e -> r.repaint());
		ReactiveController<ReactiveProxy<ReactableSettings>> setts = new ReactiveController<>(ui);
		setts.bind(settingProxy);
		ui.setData(settingProxy);

		settingFrame.getContentPane().add(ui.getView());
		settingFrame.pack();
		settingFrame.setVisible(true);
	}
}
