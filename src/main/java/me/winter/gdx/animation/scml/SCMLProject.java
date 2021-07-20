package me.winter.gdx.animation.scml;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;

import me.winter.gdx.animation.Entity;
import me.winter.gdx.animation.EntityNotFoundException;
import me.winter.gdx.animation.drawable.TextureSpriteDrawable;

/**
 * Represents a .SCML project file for Spriter.
 *
 * @author Alexander Winter
 */
public class SCMLProject {
    private final ArrayMap<Object, Folder> assets;
    private final Array<Entity> entities;

    public SCMLProject() {
        this.assets = new ArrayMap<>();
        this.entities = new Array<>();
    }

    public Entity getEntity(int index) {
        Entity entity = null;
        if (entities.size > index) entity = entities.get(index);
        return entity;
    }

    /**
     * Returns an copy of the requested SpriterEntity
     *
     * @param name the name of the entity
     * @return the entity with the given name
     * @throws EntityNotFoundException if the spriter entity could not be found
     */
    public Entity getEntity(String name) {
        for (Entity entity : entities)
            if (entity.getName().equals(name))
                return new Entity(entity);

        throw new EntityNotFoundException(name);
    }

    public void putAsset(int folderID, String folderName, int fileID, TextureSpriteDrawable asset) {
        assets.put(getAssetKey(folderID, fileID), new Folder(folderID, fileID, folderName, asset));
    }

    public void putAsset(String folderName, int fileID, TextureSpriteDrawable asset) {
        assets.put(folderName + fileID, new Folder(0, fileID, folderName, asset));
    }

    public void putFolderID(int folderID, String folderName) {
        assets.put(folderName, new Folder(folderID, 0, folderName));
    }

    public String getFolderName(int folderId) {
        for (ObjectMap.Entry<Object, Folder> asset : assets) {
            if (asset.value.folderId == folderId) {
                return asset.value.name;
            }
        }
        return "";
    }

    public TextureSpriteDrawable getAsset(int folderID, int fileID) {
        Folder folder = assets.get(getAssetKey(folderID, fileID));
        return folder == null ? null : folder.drawable;
    }

    public TextureSpriteDrawable getAsset(String folderName, int fileID) {
        Folder folder = assets.get(folderName + fileID);
        return folder == null ? null : folder.drawable;
    }

    public Array<Entity> getSourceEntities() {
        return entities;
    }

    public static int getAssetKey(int folder, int file) {
        return (folder << 16) + file;
    }

    @Override
    public String toString() {
        return "SCMLProject{" +
                "assets=" + assets +
                ", entities=" + entities +
                '}';
    }
}
