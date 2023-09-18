package com.niton.render.example;

import com.niton.render.material.Material;
import com.niton.render.material.MaterialFeature;

public final class ExampleMaterials {
    public static final Material polishedSilver = new Metal(0.975f);
    public static final Material silver = new Metal(0.78f);
    public static final Material mattSilver = new Metal(0.12f);
    public static final Material futureMetal = new Material("spaceship-panels1");
    public static final Material slabs = new Material("rock-slab-wall");
    public static final Material rust = new Material("rustediron2");
    public static final Material test = new Material("test");
    public static final Material rock = new Material("rock","jpg");
    public static final Material paintedWood = new Material("painted-wood","jpg");

    static {
    }

    private ExampleMaterials() {
    }
}
