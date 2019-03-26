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
}
