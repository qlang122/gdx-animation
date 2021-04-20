package me.winter.gdx.animation;

import com.badlogic.gdx.utils.Array;

/**
 * Represents a time line in a Spriter SCML file. A time line holds an {@link #id}, a {@link #name} and at least one
 * {@link TimelineKey}.
 *
 * @author Alexander Winter
 */
public class Timeline {
    private final int id;
    private final String name;

    private final Array<TimelineKey> keys;

    private boolean isVisible = true;

    public Timeline(int id, String name, Array<TimelineKey> timelineKeys) {
        this.id = id;
        this.name = name;
        this.keys = timelineKeys;
    }

    public Timeline(Timeline timeline) {
        this.id = timeline.id;
        this.name = timeline.name;
        this.keys = new Array<>(timeline.getKeys().size);

        for (TimelineKey key : timeline.getKeys())
            keys.add(new TimelineKey(key));
    }

    @Override
    public Timeline clone() {
        return new Timeline(this);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Array<TimelineKey> getKeys() {
        return keys;
    }

    public static Array<Timeline> clone(Array<Timeline> timelines) {
        Array<Timeline> copy = new Array<>();

        for (Timeline timeline : timelines)
            copy.add(timeline.clone());

        return copy;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public String toString() {
        return "Timeline{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", keys=" + keys +
                ", isVisible=" + isVisible +
                '}';
    }
}
