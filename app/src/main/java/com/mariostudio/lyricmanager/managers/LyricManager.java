package com.mariostudio.lyricmanager.managers;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import com.mariostudio.lyricmanager.models.LyricInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by MarioStudio on 2016/6/10.
 * 歌词管理类，负责歌词解析，当前歌词位置控制
 */

public class LyricManager {

    private OnProgressChangedListener progressChangedListener;
    private int selectedColor = Color.parseColor("#07FA81");
    private int normalColor = Color.parseColor("#FFFFFF");
    private LyricInfo lyricInfo = null;

    private boolean flag_refresh = false;
    private int flag_position = 0;

    private LyricManager(Context context) {}

    public static LyricManager getInstance(Context context) {
        return new LyricManager(context);
    }

    /**
     * 设置歌词流文件
     * @param inputStream 歌词文件的输入流
     * */
    public void setFileStream(final InputStream inputStream) {
        if(null != inputStream) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    decode(inputStream);
                }
            }).start();
        } else {
            lyricInfo = null;
        }
    }

    /**
     * 设置歌词文件
     * @param file 歌词文件
     * */
    public void setLyricFile(File file) {
        try {
            setFileStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据输入流解析转码成歌词
     * */
    private void decode(InputStream inputStream) {
        try {
            String line;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "GBK"));
            lyricInfo = new LyricInfo();
            while((line = bufferedReader.readLine()) != null) {
                analyzeLyric(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCurrentTimeMillis(0);
    }

    /**
     * 逐行解析歌词文件
     * */
    private void analyzeLyric(String line) {
        LineInfo lineInfo = new LineInfo();
        int index = line.lastIndexOf("]");
        if(line != null && line.startsWith("[offset:")) {
            // 时间偏移量
            String string = line.substring(8, index).trim();
            lyricInfo.setOffset(Long.parseLong(string));
            return;
        }
        if(line != null && line.startsWith("[ti:")) {
            // title 标题
            String string = line.substring(4, index).trim();
            lyricInfo.setTitle(string);
            return;
        }
        if(line != null && line.startsWith("[ar:")) {
            // artist 作者
            String string = line.substring(4, index).trim();
            lyricInfo.setArtist(string);
            return;
        }
        if(line != null && line.startsWith("[al:")) {
            // album 所属专辑
            String string = line.substring(4, index).trim();
            lyricInfo.setAlbum(string);
            return;
        }
        if(line != null && line.startsWith("[by:")) {
            return;
        }
        if(line != null && index == 9 && line.trim().length() > 10) {
            // 歌词内容
            lineInfo.setContent(line.substring(10, line.length()));
            lineInfo.setStart(analyzeStartTimeMillis(line.substring(0, 10)));
            lyricInfo.getLines().add(lineInfo);
        }
    }

    /**
     * 从字符串中获得时间值
     * */
    private long analyzeStartTimeMillis(String str) {
        long minute = Long.parseLong(str.substring(1, 3));
        long second = Long.parseLong(str.substring(4, 6));
        long millisecond = Long.parseLong(str.substring(7, 9));
        return millisecond + second * 1000 + minute * 60 * 1000;
    }

    /**
     * 根据当前时间戳值获得歌词
     * @param timeMillis long值时间戳
     * */
    public void setCurrentTimeMillis(final long timeMillis) {
        try{
            final List<LineInfo> lines = lyricInfo != null ? lyricInfo.getLines() : null;
            if(lines != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        int position = 0;
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                        for(int i = 0, size = lines.size(); i < size; i ++) {
                            if(lines.get(i).getStart() < timeMillis) {
                                position = i;
                            } else {
                                break;
                            }
                        }
                        if(position == flag_position && !flag_refresh) {
                            return;
                        }
                        flag_position = position;
                        for(int i = 0, size = lines.size(); i < size; i++) {
                            if(i != position) {
                                ForegroundColorSpan span = new ForegroundColorSpan(normalColor);
                                String line = lines.get(i).getContent();
                                SpannableString spannableString = new SpannableString(line + "\n");
                                spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                stringBuilder.append(spannableString);
                            } else {
                                ForegroundColorSpan span = new ForegroundColorSpan(selectedColor);
                                String line = lines.get(i).getContent();
                                SpannableString spannableString = new SpannableString(line + "\n");
                                spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                stringBuilder.append(spannableString);
                            }
                        }
                        Message message = new Message();
                        message.what = 0x159;
                        DataHolder dataHolder = new DataHolder();
                        dataHolder.builder = stringBuilder;
                        dataHolder.position = position;
                        dataHolder.refresh = flag_refresh;
                        dataHolder.lines = lines;
                        message.obj = dataHolder;
                        handler.sendMessage(message);

                        if(flag_refresh) {
                            flag_refresh = false;
                        }
                    }
                }).start();
            } else {
                Message message = new Message();
                message.what = 0x159;
                DataHolder dataHolder = new DataHolder();
                dataHolder.builder = null;
                dataHolder.position = -1;
                message.obj = dataHolder;
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object object = msg.obj;
            if(object != null && object instanceof DataHolder) {
                DataHolder dataHolder = (DataHolder)object;
                switch (msg.what) {
                    case 0x159:
                        if(null != progressChangedListener) {
                            progressChangedListener.onProgressChanged(dataHolder.builder, dataHolder.position, dataHolder.refresh);
                            if(dataHolder.lines != null) {
                                progressChangedListener.onProgressChanged(dataHolder.lines.get(dataHolder.position).getContent(), dataHolder.refresh);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * 数据缓存类
     * */
    class DataHolder {
        SpannableStringBuilder builder;
        List<LineInfo> lines;
        boolean refresh;
        int position;
    }

    /**
     * 注册监听事件
     * */
    public void setOnProgressChangedListener(OnProgressChangedListener progressChangedListener) {
        this.progressChangedListener = progressChangedListener;
    }

    /**
     *
     * */
    public interface OnProgressChangedListener {
        public void onProgressChanged(String singleLine, boolean refresh);
        public void onProgressChanged(SpannableStringBuilder stringBuilder, int lineNumber, boolean refresh);
    }

    /**
     * 设置选中文本颜色
     * */
    public void setSelectedTextColor(int color) {
        if(color != selectedColor) {
            selectedColor = color;
            flag_refresh = true;
        }
    }

    /**
     * 设置正常文本颜色
     * */
    public void setNormalTextColor(int color) {
        if(color != normalColor) {
            normalColor = color;
        }
    }
}
