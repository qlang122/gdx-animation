package me.winter.gdx.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import me.winter.gdx.animation.drawable.SpriteDrawable;

/**
 * Represents an object in a Spriter SCML file. A file has the same properties
 * as a bone with an alpha and file extension.
 *
 * @author Alexander Winter
 */
public class Sprite extends AnimatedPart {
    private int folder;
    private String folderName;
    private int file;
    private SpriteDrawable drawable;
    private float alpha;
    private int zIndex;
    private boolean visible = true;

    public Sprite() {
        this(null, -1, "", -1, new Vector2(0, 0), new Vector2(1f, 1f), 0f, 1f, 0);
    }

    public Sprite(Sprite other) {
        super(other);

        this.drawable = other.drawable;
        this.alpha = other.alpha;
        this.zIndex = other.zIndex;
    }

    public Sprite(SpriteDrawable drawable, int folder, String folderName, int file, Vector2 position, Vector2 scale, float angle, float alpha, int zIndex) {
        super(position, scale, angle);

        this.folder = folder;
        this.folderName = folderName;
        this.file = file;
        this.alpha = alpha;
        this.drawable = drawable;
        this.zIndex = zIndex;
    }

    public void draw(Batch batch) {
        if (drawable != null && visible)
            drawable.draw(this, batch);
    }

    /**
     * Sets the values of this object to the values of the given object.
     *
     * @param object the object
     */
    @Override
    public void set(AnimatedPart object) {
        super.set(object);

        if (object instanceof Sprite) {
            this.alpha = ((Sprite) object).alpha;
            this.drawable = ((Sprite) object).drawable;
            this.zIndex = ((Sprite) object).zIndex;
        }
    }

    public int getFolder() {
        return folder;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getFile() {
        return file;
    }

    public SpriteDrawable getDrawable() {
        return drawable;
    }

    public void setDrawable(SpriteDrawable drawable) {
        this.drawable = drawable;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "Sprite{" +
                "drawable=" + drawable +
                ", alpha=" + alpha +
                ", zIndex=" + zIndex +
                ", visible=" + visible +
                ", position=" + position +
                ", scale=" + scale +
                ", angle=" + angle +
                '}';
    }
}
