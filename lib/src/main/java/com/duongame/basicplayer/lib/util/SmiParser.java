package com.duongame.basicplayer.lib.util;

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
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(smiFile), charset));
        final ArrayList<String> lineList = new ArrayList<String>();

        String line = reader.readLine();
        while(line != null) {
//            Log.d(TAG, line);
            lineList.add(line);
            line = reader.readLine();
        }

        Subtitle subtitle = null;
        subtitleList = new ArrayList<Subtitle>();

        Long time = -1l;

        for (int i = 0; i < lineList.size(); i++) {
            line = lineList.get(i).toUpperCase();

            // sync start와 pclass가 같이 있는 경우에 처리
            if (line.startsWith(SYNC_START) && line.contains(P_CLASS)) {
                // sync 처리
                String sync = line.replace(SYNC_START, "");
                sync = sync.substring(0, sync.indexOf(">"));

                final Scanner scanner = new Scanner(sync);
                time = scanner.nextLong();

                // p class 처리
                String pclass = line.substring(line.indexOf(P_CLASS));
                pclass = pclass.substring(pclass.indexOf(">") + 1);

                // 여기서 종결이 될수도, 안될수도 있다.
                // nbsp 다음에 content가 올 경우가 있다.
                // 그래서 무조건 종료하면 안된다.
                if (pclass.startsWith(NBSP)) {
                    if(subtitle != null)
                        subtitle.end = time;
//                    subtitleList.add(subtitle);
                } else {
                    // 최초 자막이 아니고, 마지막에 시간이 없을때는 현재시간을 마지막 시간으로 하자
                    if (subtitle != null) {
                        if(subtitle.end == -1) {
                            // 이전 자막 끝나는 시간을 현재 시간으로 하자
                            subtitle.end = time - 1;// 1을 뺀다.
                        }
                        subtitleList.add(subtitle);
                    }

                    subtitle = new Subtitle();
                    subtitle.start = time;
                    subtitle.content = pclass;
                }
            } else {
                // /body가 있으면 종료 한다.
                if (line.contains(BODY_CLOSE)) {
                    if (subtitle != null) {
                        subtitle.content += line.substring(0, line.indexOf(BODY_CLOSE));
                    }
                    break;
                }

                // 그게 아니면 현재 content에 넣는다.
                if (subtitle != null) {
                    // content가 왔는데 end가 -1이 아니라면, 새로 생성해야한다.
                    if (subtitle.end != -1) {
                        subtitleList.add(subtitle);
                        subtitle = new Subtitle();
                        subtitle.start = time;
                        subtitle.content = "";
                    }
                    subtitle.content += line;
                }
            }
        }

        // 마지막의 end는 시간을 찾을 방법이 없다.
        // end는 다음의 start-1이기 때문이다.
        if (subtitle != null && subtitle.end == -1)
            subtitleList.add(subtitle);


        for(int i=0; i<subtitleList.size(); i++) {
            subtitle = subtitleList.get(i);
            // B태그 제거
            subtitle.content = subtitle.content.replace("<B>", "");
            subtitle.content = subtitle.content.replace("</B>", "");

            // I태그 제거
            subtitle.content = subtitle.content.replace("<I>", "");
            subtitle.content = subtitle.content.replace("</I>", "");

            // BR태그 변환
            subtitle.content = subtitle.content.replace("<BR>", "\n");

            // RUBY, TR태그 음...
            subtitle.content = subtitle.content.replace("<RUBY>", "");
            subtitle.content = subtitle.content.replace("</RUBY>", "");
            subtitle.content = subtitle.content.replace("<TR>", "");
            subtitle.content = subtitle.content.replace("</TR>", "");

            // FONT COLOR 태그 지금은 지원안함
            // 폰트는 여러번 올수 있다.
//            subtitle.content = subtitle.content.replace("<FONT COLOR=", "");

            int indexFont = subtitle.content.indexOf("<FONT COLOR=");
            while(indexFont != -1) {
                final int indexClose = subtitle.content.indexOf(">");
                final String font = subtitle.content.substring(indexFont, indexClose+1);

                subtitle.content = subtitle.content.replace(font, "");

                indexFont = subtitle.content.indexOf("<FONT COLOR=");
            }

            subtitle.content = subtitle.content.replace("</FONT>", "");

//            Log.d(TAG, "i="+i + " start=" +subtitle.start + " end="+subtitle.end + " content="+subtitle.content);
        }
        reader.close();
    }
}
