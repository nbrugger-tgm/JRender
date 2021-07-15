package com.niton.render.ui;

import com.niton.reactj.ReactiveBinder;
import com.niton.reactj.ReactiveController;
import com.niton.reactj.ReactiveProxy;
import com.niton.reactj.mvc.EventManager;
import com.niton.reactj.mvc.ReactiveView;
import com.niton.reactj.swing.components.JRButton;
import com.niton.reactj.swing.components.JRCheckBox;
import com.niton.reactj.swing.components.JRComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

//you dont need to understand this
//if you WANT to understand : https://github.com/nbrugger-tgm/reactj
public class RenderSettingUI extends ReactiveView<JPanel, ReactiveProxy<ReactableSettings>> {
	private JRCheckBox                     useTextures;
	private JRCheckBox                     useNormalMaps;
	private JRCheckBox                     useHeightMap;
	private JRCheckBox                     useReflections;
	private JRCheckBox                     useSurfaceLight;
	private JRCheckBox                     useFog;
	private JRCheckBox                     useDirectLight;
	private JButton                        render;
	public final EventManager<ActionEvent> renderEvent = new EventManager<>();

	@Override
	protected JPanel createView() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(7, 1));
		useTextures = new JRCheckBox();
		useTextures.setText("textures");
		panel.add(useTextures);
		useNormalMaps = new JRCheckBox();
		useNormalMaps.setText("normal maps");
		panel.add(useNormalMaps);
		useHeightMap = new JRCheckBox();
		useHeightMap.setText("height map");
		panel.add(useHeightMap);
		useReflections = new JRCheckBox();
		useReflections.setText("reflections");
		panel.add(useReflections);
		useSurfaceLight = new JRCheckBox();
		useSurfaceLight.setText("surface light");
		panel.add(useSurfaceLight);
		useFog = new JRCheckBox();
		useFog.setText("fog");
		panel.add(useFog);
		useDirectLight = new JRCheckBox();
		useDirectLight.setText("direct light");
		panel.add(useDirectLight);
		render = new JButton("Render");
		panel.add(render);
		return panel;
	}

	@Override
	public void createBindings(ReactiveBinder reactiveBinder) {
		//you dont need to understand this
		//if you WANT to understand : https://github.com/nbrugger-tgm/reactj
		useTextures.biBindSelected("useTextures",reactiveBinder);
		useNormalMaps.biBindSelected("useNormalMaps",reactiveBinder);
		useHeightMap.biBindSelected("useHeightMap",reactiveBinder);
		useReflections.biBindSelected("useReflections",reactiveBinder);
		useSurfaceLight.biBindSelected("useSurfaceLight",reactiveBinder);
		useFog.biBindSelected("useFog",reactiveBinder);
		useDirectLight.biBindSelected("useDirectLight",reactiveBinder);
		render.addActionListener(event -> renderEvent.fire(event));
	}
}
