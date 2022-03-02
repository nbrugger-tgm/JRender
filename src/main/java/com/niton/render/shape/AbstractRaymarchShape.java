package com.niton.render.shape;

import com.niton.render.api.SignedDisdanceFunction;
import com.niton.render.api.UvMapper;
import com.niton.render.material.Material;

/**
 * Defines a shape in the realm of raymarch.
 * Besides sphere cube and plane i dont know how to define this things to sadly
 * <p>
 * The good part, you can look up this part as it is very generic and the code is the same (roughly)
 * no matter of the language used. Because it is Raymach specific and not java/rendering specific
 */
public abstract class AbstractRaymarchShape implements SignedDisdanceFunction,
                                                       UvMapper {
	public Material mat;

	protected AbstractRaymarchShape(Material mat) {
		this.mat = mat;
	}
}
