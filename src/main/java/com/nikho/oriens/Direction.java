package com.nikho.oriens;

import org.joml.Quaternionf;
import org.joml.Vector2i;

import java.util.Vector;

import static java.lang.Math.*;

public enum Direction {
    UP(new Vector2i(0, 1), new Quaternionf(0,0, sin(PI*0.25), cos(PI*0.25))),
    DOWN(new Vector2i(0, -1), new Quaternionf(0,0, sin(PI*0.75), cos(PI*0.75))),
    LEFT(new Vector2i(-1, 0), new Quaternionf(0,0, sin(PI*0.5), cos(PI*0.5))),
    RIGHT(new Vector2i(1, 0), new Quaternionf());

    public final Vector2i value;
    public final Quaternionf rotation;

    Direction(Vector2i direction, Quaternionf rotation){
        this.value=direction;
        this.rotation= rotation;
    }
}
