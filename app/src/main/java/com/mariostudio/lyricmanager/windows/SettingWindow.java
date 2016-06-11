package com.mariostudio.lyricmanager.windows;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.mariostudio.lyricmanager.R;

/**
 * Created by MarioStudio on 2016/6/11.
 */

public class SettingWindow extends BasePopupWindow implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final int colors[] = {Color.parseColor("#07FA81"), Color.parseColor("#36C6F5"), Color.parseColor("#F4D124"), Color.parseColor("#F49121"), Color.parseColor("#F22462"), Color.parseColor("#1C75F2"), Color.parseColor("#Ef2320"), Color.parseColor("#0099EE")};
    private OnTextColorChangeListener colorChangeListener;
    private OnTextSizeChangeListener sizeChangeListener;

    public SettingWindow(Context context) {
        super(context);
        initSettingWindow();
        setShowAlpha(0.7f);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setAnimationStyle(android.R.style.Animation_InputMethod);
    }

    private void initSettingWindow() {
        setContentView(LayoutInflater.from(getContext()).inflate(R.layout.layout_setting_window, null));
        LinearLayout layout = (LinearLayout) getContentView().findViewById(R.id.setting_window_color_layout);
        for(int i=0,size=colors.length;i<size;i++) {
            View view = new View(getContext());
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.setBounds(0, 0, 224, 224);
            shapeDrawable.getPaint().setColor(colors[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(72, 72);
            params.setMargins(8, 16, 8, 16);
            view.setLayoutParams(params);
            view.setBackgroundDrawable(shapeDrawable);
            view.setTag(colors[i]);
            view.setOnClickListener(this);
            layout.addView(view);
        }
        SeekBar seekBar = (SeekBar) getContentView().findViewById(android.R.id.progress);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(100);
        seekBar.setProgress(0);
    }

    @Override
    public void onClick(View view) {
        Object object = view.getTag();
        if(object != null && object instanceof Integer && colorChangeListener != null) {
            colorChangeListener.onTextColorChanged((Integer) object);
            dismiss();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(sizeChangeListener != null) {
            sizeChangeListener.onTextSizeChanged(1.0f + 0.5f * progress / seekBar.getMax());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setOnTextColorChangeListener(OnTextColorChangeListener colorChangeListener) {
        this.colorChangeListener = colorChangeListener;
    }

    public void setOnTextSizeChangeListener(OnTextSizeChangeListener sizeChangeListener) {
        this.sizeChangeListener = sizeChangeListener;
    }

    public interface OnTextColorChangeListener {
        public void onTextColorChanged(int color);
    }

    public interface OnTextSizeChangeListener {
        public void onTextSizeChanged(float proportion);
    }
}
