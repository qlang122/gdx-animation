package me.winter.gdx.animation;

import com.badlogic.gdx.utils.Array;

/**
 * Represents a mainline in a Spriter SCML file. A mainline holds only keys and occurs only once in an animation. The
 * mainline is responsible for telling which draw order the sprites have and how the objects are related to each other,
 * i.e. which bone is the root and which objects are the children.
 *
 * @author Alexander Winter
 */
public class Mainline {
    private final Array<MainlineKey> keys;
    private final Pair<MainlineKey, Integer> tempKeyPair;

    public Mainline(int keys) {
        this.keys = new Array<>(keys);
        tempKeyPair = new Pair<>();
    }

    public Mainline(Mainline other) {
        this.keys = new Array<>(other.keys.size);
        tempKeyPair = new Pair<>();

        for (MainlineKey key : other.keys)
            keys.add(new MainlineKey(key));
    }

    /**
     * Returns the last previous MainlineKey before specified time
     *
     * @param time       the time a key has to be before
     * @param wrapAround true if should wrap around the timeline, otherwise false
     * @return last previous key before specified time, when not found first one is returned
     */
    public MainlineKey getKeyBeforeTime(int time, boolean wrapAround) {
        MainlineKey found = wrapAround ? keys.get(keys.size - 1) : keys.get(0);

        int index = wrapAround ? keys.size - 1 : 0;

        for (int i = 0; i < keys.size; i++) {
            MainlineKey key = keys.get(i);
            if (key.time > time) break;
            found = key;
            index = i;
        }

        return found;
    }

    public Pair<MainlineKey, Integer> getKeyBeforeTime2(int time, boolean wrapAround) {
        MainlineKey found = wrapAround ? keys.get(keys.size - 1) : keys.get(0);

        int index = wrapAround ? keys.size - 1 : 0;

        for (int i = 0; i < keys.size; i++) {
            MainlineKey key = keys.get(i);
            if (key.time > time) break;
            found = key;
            index = i;
        }

        tempKeyPair.clear();
        tempKeyPair.first = found;
        tempKeyPair.second = index;
        return tempKeyPair;
    }

    public MainlineKey getKey(int index) {
        if (index >= 0 && index < keys.size) {
            keys.get(index);
        }
        return null;
    }

    public MainlineKey next(MainlineKey previous, boolean wrapAround) {
        int index = keys.indexOf(previous, true);

        if (index + 1 == keys.size)
            return wrapAround ? keys.get(0) : keys.get(keys.size - 1);

        return keys.get(index + 1);
    }

    public Array<MainlineKey> getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return "Mainline{" +
                "keys=" + keys +
                '}';
    }
}
