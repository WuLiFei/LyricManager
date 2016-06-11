package com.mariostudio.lyricmanager.activitys;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mariostudio.lyricmanager.R;
import com.mariostudio.lyricmanager.managers.LyricManager;
import com.mariostudio.lyricmanager.utils.ProgressTextUtils;
import com.mariostudio.lyricmanager.views.LyricView;
import com.mariostudio.lyricmanager.windows.SettingWindow;


public class MainActivity extends BaseActivity implements OnClickListener, LyricManager.OnProgressChangedListener, OnPreparedListener, OnCompletionListener, OnSeekBarChangeListener,
        SettingWindow.OnTextColorChangeListener, SettingWindow.OnTextSizeChangeListener {

    private MediaPlayer mediaPlayer;
    private LyricManager lyricManager;

    private SeekBar seekBar;
    private TextView emptyView;
    private LyricView lyricView;
    private ImageView lyricSetting;
    private TextView textMax, textProgress;
    private ImageView btnPrevious, btnPlay, btnNext;

    private final int MSG_REFRESH = 0x167;
    private final int MSG_SCROLL = 0x349;
    private final String names[] = {"demo01", "demo02", "demo03"};
    private State state = State.STATE_STOP;
    private boolean userTouch = false;
    private boolean isPlay = false;
    private int lineNumber = -1;
    private int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAllViews();
        mediaPlayerSetup();
    }

    private void initAllViews() {
        lyricManager = LyricManager.getInstance(this);
        lyricManager.setOnProgressChangedListener(this);

        textMax = (TextView) findViewById(android.R.id.text2);
        textProgress = (TextView) findViewById(android.R.id.text1);
        seekBar = (SeekBar) findViewById(android.R.id.progress);
        seekBar.setOnSeekBarChangeListener(this);
        lyricSetting = (ImageView) findViewById(android.R.id.icon);
        lyricSetting.setOnClickListener(this);
        emptyView = (TextView) findViewById(android.R.id.empty);
        lyricView = (LyricView) findViewById(R.id.lyric_view);
        lyricView.setEmptyView(emptyView);
        btnPrevious = (ImageView) findViewById(android.R.id.button1);
        btnPrevious.setOnClickListener(this);
        btnPlay = (ImageView) findViewById(android.R.id.button2);
        btnPlay.setOnClickListener(this);
        btnNext = (ImageView) findViewById(android.R.id.button3);
        btnNext.setOnClickListener(this);
    }

    private void mediaPlayerSetup() {
        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = this.getAssets().openFd(names[position] + ".mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH:
                    if(!userTouch) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        lyricManager.setCurrentTimeMillis(mediaPlayer.getCurrentPosition());
                    }
                    textProgress.setText(ProgressTextUtils.getProgressText(mediaPlayer.getCurrentPosition()));

                    handler.sendEmptyMessageDelayed(MSG_REFRESH, 120);
                    break;
                case MSG_SCROLL:
                    lyricManager.setCurrentTimeMillis(seekBar!=null ? seekBar.getProgress() : 0);
                    break;
            }
        }
    };

    @Override
    public void onProgressChanged(String singleLine, boolean refresh) {

    }

    @Override
    public void onProgressChanged(SpannableStringBuilder stringBuilder, int lineNumber, boolean refresh) {
        if(this.lineNumber != lineNumber || refresh) {
            this.lineNumber = lineNumber;
            lyricView.setText(stringBuilder);
            lyricView.setCurrentPosition(lineNumber);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if(null != seekBar && null != mediaPlayer) {
            textMax.setText(ProgressTextUtils.getProgressText(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(0);
        }
        state = State.STATE_PREPARE;
        start();
        setupLyric();
        handler.sendEmptyMessage(MSG_REFRESH);
    }

    private boolean stop() {
        handler.removeMessages(MSG_REFRESH);
        lyricManager.setFileStream(null);
        if(null != mediaPlayer && state != State.STATE_STOP) {
            btnPlay.setImageResource(R.mipmap.m_icon_player_play_normal);
            state = State.STATE_STOP;
            mediaPlayer.stop();
            return true;
        } else {
            return false;
        }
    }

    private void pause() {
        if(mediaPlayer != null && state == State.STATE_PLAYING) {
            btnPlay.setImageResource(R.mipmap.m_icon_player_play_normal);
            state = State.STATE_PAUSE;
            mediaPlayer.pause();
        }
    }

    private void start() {
        if(mediaPlayer != null && (state == State.STATE_PAUSE || state == State.STATE_PREPARE)) {
            btnPlay.setImageResource(R.mipmap.m_icon_player_pause_normal);
            state = State.STATE_PLAYING;
            mediaPlayer.start();
        }
    }

    private void previous() {
        if(stop()) {
            mediaPlayer.release();
            mediaPlayer = null;
            position--;
            if(position < 0) {
                position = 2;
            }
            mediaPlayerSetup();
        }
    }

    private void next() {
        if(stop()) {
            mediaPlayer.release();
            mediaPlayer = null;
            position++;
            if(position > 2) {
                position = 0;
            }
            mediaPlayerSetup();
        }
    }

    private void setupLyric() {
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(names[position] + ".lrc");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lyricManager.setFileStream(inputStream);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case android.R.id.button1:
                previous();
                break;
            case android.R.id.button2:
                if(state == State.STATE_PLAYING) {
                    pause();
                } else {
                    if(state == State.STATE_PAUSE || state == State.STATE_PREPARE) {
                        start();
                    }
                }
                break;
            case android.R.id.button3:
                next();
                break;
            case android.R.id.icon:
                SettingWindow settingWindow = new SettingWindow(MainActivity.this);
                settingWindow.setOnTextColorChangeListener(MainActivity.this);
                settingWindow.setOnTextSizeChangeListener(MainActivity.this);
                settingWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTextColorChanged(int color) {
        lyricManager.setSelectedTextColor(color);
    }

    @Override
    public void onTextSizeChanged(float proportion) {
        lyricView.getTextView().setTextSize(24 + proportion * 24);
    }

    public enum State {

        STATE_STOP(0x250),
        STATE_PAUSE(0x251),
        STATE_PLAYING(0x252),
        STATE_PREPARE(0x253);

        private int state;

        State(int state) {
            this.state = state;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser && null != mediaPlayer && (state == State.STATE_PLAYING || state == State.STATE_PAUSE)) {
            mediaPlayer.seekTo(progress);
        }
        handler.removeMessages(MSG_SCROLL);
        handler.sendEmptyMessageDelayed(MSG_SCROLL, 240);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        userTouch = true;
        if(null != mediaPlayer && state == State.STATE_PLAYING){
            isPlay = true;
            pause();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        userTouch = false;
        if(null != mediaPlayer && state == State.STATE_PAUSE && isPlay){
            isPlay = false;
            start();
        }
    }
}
