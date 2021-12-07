package com.niton.render.api;

public interface Renderer<R> {
	void render(Shader<R> shader);

	void setTarget(RenderTarget target);
}
