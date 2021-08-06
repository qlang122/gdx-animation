package me.winter.gdx.animation;

public interface AnimatorListener {

    void onStart(Animation animation, int index);

    void onProgress(Animation animation, int index, int total);

    void onEnd(Animation animation, int index);
}
