package me.winter.gdx.animation;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import me.winter.gdx.animation.drawable.SpriteDrawable;
import me.winter.gdx.animation.drawable.TextureSpriteDrawable;

/**
 * Represents a box, which consists of four points: top-left, top-right, bottom-left and bottom-right.
 * A box is responsible for checking collisions and calculating a bounding box for a {@link AnimatedPart}.
 *
 * @author Trixt0r
 */
public class Box {
    public final Vector2[] points;
    private final RectF rect;

    /**
     * Creates a new box with no witdh and height.
     */
    public Box() {
        this.points = new Vector2[4];
        for (int i = 0; i < 4; i++) {
            this.points[i] = new Vector2(0, 0);
        }
        this.rect = new RectF(0, 0, 0, 0);
    }

    /**
     * Calculates its four points for the given bone or object with the given info.
     *
     * @param part the AnimatedPart
     * @throws NullPointerException if info or boneOrObject is <code>null</code>
     */
    public void calcFor(AnimatedPart part) {
        float width = 0;
        float height = 0;
        float pivotX = 0;
        float pivotY = 0;
        if (part instanceof Sprite) {
            SpriteDrawable drawable = ((Sprite) part).getDrawable();
            Vector2 pivot = new Vector2();
            Rectangle size = new Rectangle();
            if (drawable instanceof TextureSpriteDrawable) {
                pivot = ((TextureSpriteDrawable) drawable).getPivot();
                size = ((TextureSpriteDrawable) drawable).getSize();
            }
            width = size.width * part.scale.x;
            height = size.height * part.scale.y;
            pivotX = width * pivot.x;
            pivotY = height * pivot.y;
//            System.out.println("------>>" + width + " " + height + " " + pivotX + " " + pivotY);
        }

        this.points[0].set(-pivotX, -pivotY);//left,top
        this.points[1].set(width - pivotX, -pivotY);//top,right
        this.points[2].set(-pivotX, height - pivotY);//left,bottom
        this.points[3].set(width - pivotX, height - pivotY);//right,bottom

        for (int i = 0; i < 4; i++)
            this.points[i].rotate(part.angle);
        for (int i = 0; i < 4; i++)
            this.points[i].add(part.position);
    }

    /**
     * Returns a bounding box for this box.
     *
     * @return the bounding box
     */
    public RectF getBoundingRect() {
        this.rect.set(points[0].x, points[0].y, points[0].x, points[0].y);
        this.rect.left = Math.min(Math.min(Math.min(Math.min(points[0].x, points[1].x), points[2].x), points[3].x), this.rect.left);
        this.rect.right = Math.max(Math.max(Math.max(Math.max(points[0].x, points[1].x), points[2].x), points[3].x), this.rect.right);
        this.rect.top = Math.min(Math.min(Math.min(Math.min(points[0].y, points[1].y), points[2].y), points[3].y), this.rect.top);
        this.rect.bottom = Math.max(Math.max(Math.max(Math.max(points[0].y, points[1].y), points[2].y), points[3].y), this.rect.bottom);
        return this.rect;
    }

    /**
     * Returns whether the given coordinates lie inside the box of the given bone or object.
     *
     * @param part the AnimatedPart
     * @param x    the x coordinate
     * @param y    the y coordinate
     * @return <code>true</code> if the given point lies in the box
     * @throws NullPointerException if info or boneOrObject is <code>null</code>
     */
    public boolean collides(AnimatedPart part, float x, float y) {
        float width = 0;
        float height = 0;
        float pivotX = 0;
        float pivotY = 0;
        if (part instanceof Sprite) {
            SpriteDrawable drawable = ((Sprite) part).getDrawable();
            Vector2 pivot = new Vector2();
            Rectangle size = new Rectangle();
            if (drawable instanceof TextureSpriteDrawable) {
                pivot = ((TextureSpriteDrawable) drawable).getPivot();
                size = ((TextureSpriteDrawable) drawable).getSize();
            }
            width = size.width * part.scale.x;
            height = size.height * part.scale.y;
            pivotX = width * pivot.x;
            pivotY = height * pivot.y;
        }

        Vector2 point = new Vector2(x - part.position.x, y - part.position.y);
        point.setAngleDeg(-part.angle);

        return point.x >= -pivotX && point.x <= width - pivotX && point.y >= -pivotY && point.y <= height - pivotY;
    }

    /**
     * Returns whether this box is inside the given rectangle.
     *
     * @param rect the rectangle
     * @return <code>true</code> if one of the four points is inside the rectangle
     */
    public boolean isInside(Rectangle rect) {
        boolean inside = false;
        for (Vector2 p : points)
            inside |= isInside(rect, p.x, p.y);
        return inside;
    }

    private boolean isInside(Rectangle rect, float x, float y) {
        return x >= rect.x && x <= rect.x + rect.width && y <= rect.y + rect.height && y >= rect.y;
    }

    /**
     * Creates a bigger rectangle of the given two and saves it in the target.
     *
     * @param rect1  the first rectangle
     * @param rect2  the second rectangle
     * @param target the target to save the new bounds.
     */
    public static void setBiggerRectangle(RectF rect1, RectF rect2, RectF target) {
        target.left = Math.min(rect1.left, rect2.left);
        target.right = Math.max(rect1.right, rect2.right);
        target.top = Math.min(rect1.top, rect2.top);
        target.bottom = Math.max(rect1.bottom, rect2.bottom);
    }
}
