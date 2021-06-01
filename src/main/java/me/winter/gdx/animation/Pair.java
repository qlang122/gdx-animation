package me.winter.gdx.animation;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
    public A first;
    public B second;

    public Pair() {
    }

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public void clear() {
        first = null;
        second = null;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
