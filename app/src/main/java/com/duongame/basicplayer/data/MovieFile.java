package com.duongame.basicplayer.data;

import java.io.File;

/**
 * Created by js296 on 2017-06-06.
 */

public class MovieFile {
    public File file;
    public String timeText;

    public MovieFile(File f, String t) {
        file = f;
        timeText = t;
    }
}
