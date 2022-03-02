package com.niton.render;

import com.niton.reactj.Observer;
import com.niton.reactj.Reactable;
import com.niton.reactj.ReactiveController;
import com.niton.reactj.ReactiveProxy;
import com.niton.render.api.Renderer;
import com.niton.render.api.Shader;
import com.niton.render.example.ExampleSettings;
import com.niton.render.renderers.MultiCoreRenderer;
import com.niton.render.renderers.SingleCoreRenderer;
import com.niton.render.shaders.RaymarchSceneShader;
import com.niton.render.shaders.RaymarchShader;
import com.niton.render.shaders.EndlessSphereShader;
import com.niton.render.ui.ExampleSettingUi;
import com.niton.render.ui.JShaderPanel;
import com.niton.render.ui.ReactableSettings;
import com.niton.render.ui.RenderSettingUI;
import com.niton.render.example.ExampleRaymarchScenes;
import com.niton.render.world.RaymarchScene;

import javax.swing.*;

import java.awt.*;
import java.util.Arrays;

import static com.niton.reactj.ReactiveProxy.create;
import static com.niton.reactj.ReactiveProxy.createProxy;
import static java.lang.Integer.max;

public class Launcher
{
    //nothing is animated atm so no need to enable this
    //set this to true if you want to animate a moving light (looks nice)
    //framerate is horrible tho
    //if you want to have it somewhat smooth make the render window tiny
    static Shader<?>[] shaders = {
        new EndlessSphereShader(),
        new RaymarchSceneShader(ExampleRaymarchScenes.scene1),
        new RaymarchSceneShader(ExampleRaymarchScenes.scene2),
        new RaymarchSceneShader(ExampleRaymarchScenes.scene3),
        new RaymarchSceneShader(ExampleRaymarchScenes.scene4)
    };
    static JFrame frame = new JFrame();
    static ExampleSettings env;
    public static void main(String[] args) throws Throwable
    {
        //the frame to render on
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(420, 360);
        frame.setVisible(true);

        env = create(ExampleSettings.class,shaders.length);
        var envObserver = new Observer<>() {
            @Override
            public void onChange(String s, Object o) {
                Renderer renderer = env.isUseMultipleThreads() ?
                    new MultiCoreRenderer(env.getRenderingThreads()) :
                    new SingleCoreRenderer();
                var shaderPanel = new JShaderPanel(renderer, shaders[env.getCurrentShader()]);
                frame.getContentPane().removeAll();
                frame.getContentPane().add(shaderPanel);
            }
        };
        envObserver.bind(env);
        initRenderSettings();
        animationCycle(frame.getContentPane());

    }

    private static void initRenderSettings()
    {
        var settingProxy = create(ReactableSettings.class);
        //you dont need to understand this
        //if you WANT to understand : https://github.com/nbrugger-tgm/reactj
        for (Shader<?> shader : shaders) {
            if (shader instanceof RaymarchShader<?> rShader)
                rShader.setSettings(settingProxy);
        }
        openSettingsUI(settingProxy, frame.getContentPane());
    }

    private static void animationCycle(Component frame) throws InterruptedException {
        var delta = 0;
        var lastFrame = System.currentTimeMillis();
        while (true) {
            if (env.isAnimated()) {
                frame.repaint();
                frame.validate();
            }
            delta = (int) (System.currentTimeMillis()-lastFrame);
            lastFrame = System.currentTimeMillis();
            if(delta<16)
                Thread.sleep(max(0,16-delta));
        }
    }

    private static void openSettingsUI(
        ReactableSettings settings,
        Component r
    ) {
        //creates the UI for the enable/disable buttons
        JFrame settingFrame = new JFrame();
        settingFrame.getContentPane().setLayout(new GridLayout(1, 1));

        //you dont need to understand this
        //if you WANT to understand : https://github.com/nbrugger-tgm/reactj
        RenderSettingUI ui = new RenderSettingUI();
        ui.renderEvent.addListener(e -> r.validate());
        ui.renderEvent.addListener(e -> r.repaint());
        ui.setData(settings);

        settingFrame.getContentPane().add(ui.getView());

        ExampleSettingUi exampleSettingUi = new ExampleSettingUi();
        exampleSettingUi.setData(env);
        settingFrame.getContentPane().add(exampleSettingUi.getView());

        settingFrame.pack();
        settingFrame.setVisible(true);
    }
}
