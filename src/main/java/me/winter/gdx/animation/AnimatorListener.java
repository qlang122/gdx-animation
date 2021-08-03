package me.winter.gdx.animation;

public interface AnimatorListener {

    void onStart(MainlineKey key, int index);

    void onProgress(int index, int total);

    void onEnd(MainlineKey key, int index);
}
