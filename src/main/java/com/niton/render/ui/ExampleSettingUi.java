package com.niton.render.ui;

import java.awt.*;
import java.util.stream.IntStream;

import javax.swing.*;

import com.niton.reactj.ReactiveBinder;
import com.niton.reactj.mvc.ReactiveView;
import com.niton.reactj.swing.components.JRCheckBox;
import com.niton.render.example.ExampleSettings;

public class ExampleSettingUi extends ReactiveView<JPanel, ExampleSettings>
{
    private JRCheckBox multiThread;
    private JComboBox<Integer> shader;
    private JRCheckBox animated;
    private JComboBox<Integer> threads;

    @Override
    protected JPanel createView()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1));

        multiThread = new JRCheckBox();
        multiThread.setText("Multi-threading");
        panel.add(multiThread);

        panel.add(new JLabel("Threads (only w. multi-threading)"));
        threads = new JComboBox<>();
        panel.add(threads);

        animated = new JRCheckBox();
        animated.setText("Animated");
        panel.add(animated);

        panel.add(new JLabel("Scene/Shader"));
        shader = new JComboBox<>();
        panel.add(shader);

        return panel;
    }

    @Override
    public void createBindings(ReactiveBinder<ExampleSettings> binder) {
        binder.bind(
            "availableShaders",
            shader::setModel,
            (Integer max) -> new DefaultComboBoxModel<>(IntStream.range(1,max+1).boxed().toArray(Integer[]::new))
        );
        threads.setModel(new DefaultComboBoxModel<>(IntStream.of(1,2,4,6,8,16,32,64).boxed().toArray(Integer[]::new)));

        binder.bindBi("currentShader",shader::setSelectedItem,shader::getSelectedIndex);
        shader.addActionListener(binder::react);

        multiThread.biBindSelected("useMultipleThreads",binder);

        binder.bindBi("renderingThreads",threads::setSelectedItem,threads::getSelectedItem);
        threads.addActionListener(binder::react);

        animated.biBindSelected("animated",binder);
    }
}
