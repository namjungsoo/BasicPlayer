package com.duongame.basicplayer.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import eu.j0ntech.charsetDetector.CharsetDetector;

/**
 * Created by namjungsoo on 2016-06-19.
 */
public class SmiParser {
    private static final String TAG="SmiParser";

    public class Subtitle {
        public long start;
        public long end = -1;
        public String content;
    }

    final static String SYNC_START = "<SYNC START=";
    final static String P_CLASS = "<P CLASS=";
    final static String NBSP = "&NBSP;";
    final static String BODY_CLOSE = "</BODY>";

    ArrayList<Subtitle> subtitleList;

    public ArrayList<Subtitle> getSubtitleList() {
        return subtitleList;
    }

    public void load(String smiFile) throws IOException {
        final String charset = CharsetDetector.detectCharset(smiFile);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(smiFile), charset));

        //final BufferedReader reader = new BufferedReader(new FileReader(new File(smiFile)));

        ArrayList<String> lineList = new ArrayList<String>();
        String line = reader.readLine();
        while(line != null) {

            if(!charset.equals("UTF-8")) {
//                line = new String(line.getBytes("EUC-KR"), "UTF-8");
//                CharBuffer cbuffer = CharBuffer.wrap((new String(line.getBytes(), "EUC-KR")).toCharArray());
//                Charset utf8charset = Charset.forName("UTF-8");
//                ByteBuffer bbuffer = utf8charset.encode(cbuffer);
//
//                //변환된 UTF-8 문자열
//                line = new String(bbuffer.array());
            }
            Log.d(TAG, line);
            lineList.add(line);
            line = reader.readLine();
        }

//        final List<String> lineList = Files.readAllLines(Paths.get(smiFile), Charset.forName(charset));
//        List<String> lineList = Files.readAllLines(Paths.get(smiFile));

        Subtitle subtitle = null;
        subtitleList = new ArrayList<Subtitle>();

        for (int i = 0; i < lineList.size(); i++) {
            line = lineList.get(i).toUpperCase();
            if (line.startsWith(SYNC_START) &&
                    line.contains(P_CLASS)) {
                // sync 처리
                String sync = line.replace(SYNC_START, "");
                sync = sync.substring(0, sync.indexOf(">"));

                Scanner scanner = new Scanner(sync);
                Long time = scanner.nextLong();

                // p class 처리
                String pclass = line.substring(line.indexOf(P_CLASS));
                pclass = pclass.substring(pclass.indexOf(">") + 1);

                if (pclass.equals(NBSP)) {
                    subtitle.end = time;
                    subtitleList.add(subtitle);
                } else {
                    if (subtitle != null && subtitle.end == -1)
                        subtitleList.add(subtitle);

                    subtitle = new Subtitle();
                    subtitle.start = time;
                    subtitle.content = pclass;
                }
            } else {
                if (line.contains(BODY_CLOSE)) {
                    if (subtitle != null)
                        subtitle.content += line.substring(0, line.indexOf(BODY_CLOSE));
                    break;
                }
                if (subtitle != null)
                    subtitle.content += line;
            }
        }

        if (subtitle != null && subtitle.end == -1)
            subtitleList.add(subtitle);
    }
}
