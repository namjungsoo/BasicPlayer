package com.duongame.basicplayer.data;

import java.io.File;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by js296 on 2017-06-06.
 */

//REALM
public class MovieFile extends RealmObject {
    @PrimaryKey
    public String absolutePath;
    @Required
    public String path;
    @Required
    public String parent;
    @Required
    public String name;
    @Required
    public String timeText;

    // 썸네일을 저장할 UUID.png
    // 저장된 파일을 읽으면 새로 추출할 필요가 없다.
    // 마이그레이션을 해야 한다.
//    public String thumbnail;

    @Ignore
    public boolean isLoadingThumbnail;
    @Ignore
    public boolean isLoadingTimeText;

    public MovieFile() {
    }

    public MovieFile(File f, String t) {
        absolutePath = f.getAbsolutePath();
        path = f.getPath();
        parent = f.getParent();
        name = f.getName();
        timeText = t;
    }

    public String toString() {
        return String.format("absolutePath=%s path=%s parent=%s name=%s timeText=%s", absolutePath, path, parent, name, timeText);
    }
}
