package me.winter.gdx.animation.scml;

import me.winter.gdx.animation.drawable.TextureSpriteDrawable;

class Folder {
    public int folderId = 0;
    public int fileId = 0;
    public String name = "";
    public TextureSpriteDrawable drawable;

    public Folder() {
    }

    public Folder(int folderId, int fileId) {
        this.folderId = folderId;
        this.fileId = fileId;
    }

    public Folder(int folderId, int fileId, String name) {
        this.folderId = folderId;
        this.fileId = fileId;
        this.name = name;
    }

    public Folder(int folderId, int fileId, String name, TextureSpriteDrawable drawable) {
        this.folderId = folderId;
        this.fileId = fileId;
        this.name = name;
        this.drawable = drawable;
    }

    public int getKey() {
        return (folderId << 16) + fileId;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "folderId=" + folderId +
                ", fileId=" + fileId +
                ", name='" + name + '\'' +
                '}';
    }
}
