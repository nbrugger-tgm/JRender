package com.niton.render.example;

import com.niton.reactj.ProxySubject;

public class ExampleSettings implements ProxySubject
{
    private final int availableShaders;
    private int currentShader = 0;
    private boolean animated = true;
    private boolean useMultipleThreads = true;
    private int renderingThreads = Runtime.getRuntime().availableProcessors();

    public ExampleSettings(int availableShaders)
    {
        this.availableShaders = availableShaders;
    }

    public int getCurrentShader()
    {
        return currentShader;
    }

    public void setCurrentShader(int currentShader)
    {
        this.currentShader = currentShader;
    }

    public boolean isAnimated()
    {
        return animated;
    }

    public void setAnimated(boolean animated)
    {
        this.animated = animated;
    }

    public boolean isUseMultipleThreads()
    {
        return useMultipleThreads;
    }

    public void setUseMultipleThreads(boolean useMultipleThreads)
    {
        this.useMultipleThreads = useMultipleThreads;
    }

    public int getRenderingThreads()
    {
        return renderingThreads;
    }

    public void setRenderingThreads(int renderingThreads)
    {
        this.renderingThreads = renderingThreads;
    }
}
