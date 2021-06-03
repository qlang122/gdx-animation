package me.winter.gdx.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import me.winter.gdx.animation.drawable.TintedSpriteDrawable;
import me.winter.gdx.animation.math.Curve;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents an animation of a Spriter SCML file. An animation holds {@link Timeline}s and a {@link Mainline} to
 * animate objects. Furthermore it holds a {@link #length}, a {@link #name} and whether it is {@link
 * #looping} or not.
 *
 * @author Alexander Winter
 */
public class Animation {
    @SuppressWarnings("NewApi")
    private static final Comparator<Sprite> SPRITE_COMPARATOR = Comparator.comparing(Sprite::getZIndex);

    private final String name;
    private final int length; // millis
    private boolean looping;

    private final Mainline mainline;
    private final Array<Timeline> timelines;

    private final Array<AnimatedPart> tweenedObjects; //sprites made on runtime by tweening original sprites from animation
    private final Array<Sprite> sprites;

    private final ObjectMap<String, Consumer<AnimatedPart>> transformations = new ObjectMap<>();

    /**
     * Milliseconds
     */
    private float time = 0;
    private float speed = 1f, alpha = 1f;

    private final AnimatedPart root = new AnimatedPart();

    private boolean zIndexChanged = false;

    private final Rectangle rectangle = new Rectangle();
    private final RectF rect = new RectF();
    private final Box prevBBox = new Box();

    private boolean isCanPlay = false;
    private boolean isCanAutoUpdate = true;
    private Pair<MainlineKey, Integer> currentKey;

    public Animation(String name, int length, boolean looping, Mainline mainline, Array<Timeline> timelines) {
        this.name = name;

        this.length = length;
        this.looping = looping;

        this.mainline = mainline;
        this.timelines = timelines;

        tweenedObjects = new Array<>();
        tweenedObjects.setSize(timelines.size);
        sprites = new Array<>();

        for (Timeline timeline : timelines) {
            if (timeline.getKeys().size > 0 && timeline.getKeys().get(0).getObject() instanceof Sprite) {
                Sprite sprite = new Sprite();
                tweenedObjects.set(timeline.getId(), sprite);
                sprites.add(sprite);
            } else
                tweenedObjects.set(timeline.getId(), new AnimatedPart());
        }
    }

    public Animation(Animation animation) {
        this(animation.name,
                animation.length,
                animation.looping,
                new Mainline(animation.mainline),
                Timeline.clone(animation.timelines));
    }

    public void draw(Batch batch) {
        if (zIndexChanged) {
            sprites.sort(SPRITE_COMPARATOR);
            zIndexChanged = false;
        }

        float prevColor = batch.getPackedColor();
        Color tmp = batch.getColor();
        tmp.a *= alpha;
        batch.setColor(tmp);

        for (Sprite sprite : sprites)
            sprite.draw(batch);

        batch.setPackedColor(prevColor);
    }

    /**
     * Updates this player. This means the current time gets increased by {@link #speed} and is applied to the current
     * animation.
     *
     * @param delta time in milliseconds
     */
    public void update(float delta) {
        if (isCanPlay)
            setTime(time + speed * delta);
        if (!looping && currentKey.second == mainline.getKeys().size - 1)
            pausePlay();

        if (isCanAutoUpdate)
            currentKey = mainline.getKeyBeforeTime2((int) time, looping);
        if (currentKey == null) return;

        for (Sprite sprite : sprites)
            sprite.setVisible(false);

        MainlineKey key = currentKey.first;
        for (ObjectRef ref : key.objectRefs)
            update(key, ref, (int) time);
    }

    public void update(MainlineKey key, int time) {
        if (key == null) return;

        setTime(time);
        for (Sprite sprite : sprites)
            sprite.setVisible(false);

        for (ObjectRef ref : key.objectRefs)
            update(key, ref, (int) this.time);
    }

    @SuppressWarnings("NewApi")
    protected void update(MainlineKey currentKey, ObjectRef ref, int time) {
        //Get the timelines, the ref's pointing to
        Timeline timeline = timelines.get(ref.timeline);
        if (!timeline.isVisible()) return;

        AnimatedPart tweened = tweenedObjects.get(ref.timeline);

        TimelineKey key = timeline.getKeys().get(ref.key); //get the last previous key

        TimelineKey nextKey;
        int timeOfNext;

        Consumer<AnimatedPart> transform = transformations.get(timeline.getName());

        if (ref.key + 1 == timeline.getKeys().size) {
            if (!looping) {
                //no need to tween, stay freezed at first sprite

                if (tweened instanceof Sprite
                        && key.getObject() instanceof Sprite
                        && ((Sprite) tweened).getZIndex() != ((Sprite) key.getObject()).getZIndex())
                    zIndexChanged = true;

                tweened.set(key.getObject());

                if (tweened instanceof Sprite)
                    ((Sprite) tweened).setVisible(true);

                if (transform != null)
                    transform.accept(tweened);

                AnimatedPart parent = ref.parent != null ? tweenedObjects.get(ref.parent.timeline) : root;
                tweened.unmap(parent);
                return;
            }

            nextKey = timeline.getKeys().get(0);
            timeOfNext = nextKey.getTime() + length; //wrap around
        } else {
            nextKey = timeline.getKeys().get(ref.key + 1);
            timeOfNext = nextKey.getTime();
        }

        float timeDiff = timeOfNext - key.getTime();
        float timeRatio = currentKey.curve.interpolate(0f, 1f, (time - key.getTime()) / timeDiff);

        //Tween object
        AnimatedPart obj1 = key.getObject();
        AnimatedPart obj2 = nextKey.getObject();

        Curve curve = key.getCurve();

        tweened.setAngle(curve.interpolateAngle(obj1.getAngle(), obj2.getAngle(), timeRatio, key.getSpin()));

        curve.interpolateVector(obj1.getPosition(), obj2.getPosition(), timeRatio, tweened.getPosition());
        curve.interpolateVector(obj1.getScale(), obj2.getScale(), timeRatio, tweened.getScale());

        if (tweened instanceof Sprite) {
            ((Sprite) tweened).setAlpha(curve.interpolate(((Sprite) obj1).getAlpha(), ((Sprite) obj2).getAlpha(), timeRatio));
            ((Sprite) tweened).setDrawable(((Sprite) obj1).getDrawable());

            if (((Sprite) tweened).getZIndex() != ((Sprite) obj1).getZIndex()) {
                ((Sprite) tweened).setZIndex(((Sprite) obj1).getZIndex());
                zIndexChanged = true;
            }
            ((Sprite) tweened).setVisible(true);
        }

        if (transform != null)
            transform.accept(tweened);

        tweened.unmap(ref.parent != null ? tweenedObjects.get(ref.parent.timeline) : root);
    }

    public void reset() {
        time = 0;
        update(0);
    }

    public void first() {
        isCanAutoUpdate = true;
        reset();
    }

    public void last() {
        if (currentKey == null) update(0);
        isCanAutoUpdate = false;

        MainlineKey oldKey = currentKey.first;
        int index = mainline.getKeySize() - 1;

        MainlineKey newKey = mainline.getKey(index);
        currentKey.first = newKey;
        currentKey.second = index;
        update(currentKey.first, oldKey.time + (newKey.time - oldKey.time));
    }

    public void prevKey() {
        if (currentKey == null) update(0);
        isCanAutoUpdate = false;

        MainlineKey oldKey = currentKey.first;
        Integer index = currentKey.second;
        int size = mainline.getKeySize();
        index--;
        if (index < 0) index = size - 1;
        MainlineKey newKey = mainline.getKey(index);
        currentKey.first = newKey;
        currentKey.second = index;
        update(currentKey.first, oldKey.time + (newKey.time - oldKey.time));
    }

    public void nextKey() {
        if (currentKey == null) update(0);
        isCanAutoUpdate = false;

        MainlineKey oldKey = currentKey.first;
        Integer index = currentKey.second;
        int size = mainline.getKeySize();
        index++;
        if (index > size - 1) index = 0;
        MainlineKey newKey = mainline.getKey(index);
        currentKey.first = newKey;
        currentKey.second = index;
        update(currentKey.first, oldKey.time + (newKey.time - oldKey.time));
    }

    public AnimatedPart getRoot() {
        return root;
    }

    public Array<AnimatedPart> getParts() {
        return tweenedObjects;
    }

    public ObjectMap<String, Consumer<AnimatedPart>> getTransformations() {
        return transformations;
    }

    public Array<Timeline> getTimelines() {
        return timelines;
    }

    public String getName() {
        return name;
    }

    /**
     * Time is in milliseconds
     *
     * @return current time of this animation
     */
    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        if (looping)
            while (time < 0)
                time += length;
        else if (time < 0)
            time = 0;

        if (looping)
            while (time >= length)
                time -= length;
        else if (time > length)
            time = length;

        this.time = time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getLength() {
        return length;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isDone() {
        return time == length;
    }

    public void makeTimelineVisible(Map<String, Boolean> values) {
        if (timelines == null) return;

        for (Timeline line : timelines) {
            for (Map.Entry<String, Boolean> e : values.entrySet()) {
                if (e.getKey().equals(line.getName())) {
                    line.setVisible(e.getValue());
                }
            }
        }
    }

    public void setPosition(float x, float y) {
        root.position.set(x, y);
    }

    public Vector2 getPosition() {
        return root.getPosition();
    }

    public void setAngle(float angle) {
        root.setAngle(angle);
    }

    public float getAngle() {
        return root.getAngle();
    }

    public void setScale(float scaleX, float scaleY) {
        root.setScale(scaleX, scaleY);
    }

    public void setScale(float scaleXY) {
        root.setScale(scaleXY, scaleXY);
    }

    public Vector2 getScale() {
        return root.getScale();
    }

    public void startPlay() {
        isCanPlay = true;
        isCanAutoUpdate = true;
    }

    public void pausePlay() {
        isCanPlay = false;
    }

    public void setPlay(boolean value) {
        isCanPlay = value;
        if (value) isCanAutoUpdate = true;
    }

    public boolean isPlaying() {
        return isCanPlay;
    }

    public Rectangle getBoundingRectangle(ObjectRef rootRef) {
        AnimatedPart part = rootRef == null ? this.root : timelines.get(rootRef.timeline).getKeys().get(rootRef.key).getObject();
        rect.set(part.position.x, part.position.y, part.position.x, part.position.y);
        calcBoundingRectangle(rootRef);
//        System.out.println("---->>>>" + rect + " " + rect.centerX() + " " + rect.centerY());
        rectangle.set(rect.centerX(), rect.centerY(), rect.width(), rect.height());
        return rectangle;
    }

    private void calcBoundingRectangle(ObjectRef rootRef) {
        MainlineKey currentKey = mainline.getKeyBeforeTime((int) time, looping);
        for (ObjectRef ref : currentKey.objectRefs) {
            if (ref.parent != rootRef && rootRef != null) continue;
            Timeline timeline = timelines.get(ref.timeline);
            TimelineKey key = timeline.getKeys().get(ref.key);
            this.prevBBox.calcFor(key.getObject());
            Box.setBiggerRectangle(rect, this.prevBBox.getBoundingRect(), rect);
//            this.calcBoundingRectangle(ref);
        }
    }

    public void setTransformation(String timelineName, Consumer<AnimatedPart> transformation) {
        if (transformation == null) transformations.remove(timelineName);
        else transformations.put(timelineName, transformation);
    }

    public void tintSprite(String name, Color color) {
        for (Timeline timeline : timelines) {
            if (timeline.getName().equals(name)) {
                for (TimelineKey key : timeline.getKeys()) {
                    if (key.getObject() instanceof Sprite) {
                        Sprite sprite = (Sprite) key.getObject();
                        if (!(sprite.getDrawable() instanceof TintedSpriteDrawable))
                            sprite.setDrawable(new TintedSpriteDrawable(((Sprite) key.getObject()).getDrawable(), color));
                        else ((TintedSpriteDrawable) sprite.getDrawable()).setColor(color);
                    }
                }
            }
        }
    }

    public void tintSprite(Color color) {
        for (Timeline timeline : timelines) {
            tintSpriteTimeline(timeline, color);
        }
    }

    public void tintSpriteTimeline(Timeline timeline, Color color) {
        if (timeline == null) return;
        for (TimelineKey key : timeline.getKeys()) {
            if (key.getObject() instanceof Sprite) {
                Sprite sprite = (Sprite) key.getObject();
                if (!(sprite.getDrawable() instanceof TintedSpriteDrawable))
                    sprite.setDrawable(new TintedSpriteDrawable(((Sprite) key.getObject()).getDrawable(), color));
                else ((TintedSpriteDrawable) sprite.getDrawable()).setColor(color);
            }
        }
    }

    @Override
    public String toString() {
        return "Animation{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", looping=" + looping +
                ", mainline=" + mainline +
                ", timelines=" + timelines +
                ", sprites=" + sprites +
                ", speed=" + speed +
                ", alpha=" + alpha +
                '}';
    }
}
