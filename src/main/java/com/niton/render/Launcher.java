package com.niton.render;


import com.niton.reactj.Observer;
import com.niton.reactj.ReactiveController;
import com.niton.reactj.ReactiveProxy;
import com.niton.reactj.mvc.Listener;
import com.niton.render.ui.ReactableSettings;
import com.niton.render.ui.RenderSettingUI;
import com.niton.render.ui.SwingRender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class Launcher {
	//nothing is animated atm so no need to enable this
	//set this to true if you want to animate a moving light (looks nice)
	//framerate is horrible tho
	//if you want to have it somewhat smooth make the render window tiny
	static boolean animated = false;
	public static void main(String[] args) throws Throwable {
		RaymarchShader shader = new RaymarchShader();

		//you dont need to understand this
		//if you WANT to understand : https://github.com/nbrugger-tgm/reactj
		ReactiveProxy<ReactableSettings> settingProxy = ReactiveProxy.createProxy(ReactableSettings.class);
		shader.settings = settingProxy.getObject();

		//the frame to render on
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(420,360);
		SwingRender r = new SwingRender();
		r.setShader(shader);
		frame.getContentPane().add(r);
		frame.setVisible(true);


		//creates the UI for the enable/disable buttons
		JFrame settingFrame = new JFrame();
		settingFrame.getContentPane().setLayout(new GridLayout(1,1));

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
		if(animated)
			while(true){
				r.repaint();
				Thread.sleep(10);//bcs the rendering delay isnt horrible enought :)
			}
	}
}
