package com.mariostudio.lyricmanager.models;

import com.mariostudio.lyricmanager.managers.LineInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarioStudio on 2016/6/10.
 */

public class LyricInfo {

    private List<LineInfo> lines;
    private String artist;
    private String title;
    private String album;
    private long offset;

    public List<LineInfo> getLines() {
        if(lines == null) {
            lines = new ArrayList<>();
        }
        return lines;
    }

    public void setLines(List<LineInfo> lines) {
        this.lines = lines;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
