package com.mariostudio.lyricmanager.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by MarioStudio on 2016/6/10.
 */

public class LyricView extends RelativeLayout implements View.OnTouchListener {

    private View mEmptyView; // 类似ListView的emptyView，在歌词内容为空时展示
    private TextView mTextView; // 歌词内容展示控件
    private ScrollView mScrollView; // 实现滑动的基础

    private boolean autoScroll = false;
    private boolean userTouch = false; // 用户是否触屏标记
    private int paddingValue = 0;
    private int mPosition = 0; // 控制当前TextView的第几行为当前播放位置

    private int resetType = 1; // 控制恢复动画的方向，是向上恢复还是向下恢复
    private final int MSG_USER_TOUCH = 0x349;

    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWithContext(context);
    }

    /**
     * 设置emptyView
     * */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        if(null != mEmptyView && null != mTextView) {
            if((mTextView.getText().toString() == null || "".equals(mTextView.getText().toString().trim()))) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 获得emptyView
     * */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * 想要获得ScrollView的高度必须使用addOnGlobalLayoutListener监听事件
     * 否则获取到的ScrollView的高度就有可能是0
     * */
    private void initWithContext(Context context) {
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                paddingValue = (getHeight() - getPaddingBottom() - getPaddingTop()) / 2;
                if (null != mTextView && null != mScrollView) {
                    mTextView.setPadding(0, paddingValue, 0, paddingValue); //设置TextView的Padding值
                    mScrollView.fullScroll(ScrollView.FOCUS_UP);
                }
                LyricView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    /**
     * 设置当前滚动的位置
     * */
    public void setCurrentPosition(int position) {
        if(mPosition != position) {
            mPosition = position;
            if(!userTouch) {
                doScroll(position);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mScrollView = new ScrollView(getContext());
        LayoutParams scrollViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        mScrollView.setVerticalScrollBarEnabled(false); // 设置竖直反向淡入淡出效果，ScrollView原有属性
        mScrollView.setLayoutParams(scrollViewParams);
        mScrollView.setVerticalFadingEdgeEnabled(true);
        mScrollView.setOnTouchListener(this);
        mScrollView.setFadingEdgeLength(220);  //设置竖直反向淡入淡出效果长度，ScrollView原有属性

        mTextView = new TextView(getContext());
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(16.0f); // 设置TextView字体大小
        mTextView.setLineSpacing(6, 1.5f); // 设置TextView行间距大小
        mTextView.setPadding(0, paddingValue, 0, paddingValue);

        mScrollView.addView(mTextView, new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

        this.addView(mScrollView);
    }

    /**
     * 设置歌词文本内容
     * */
    public void setText(CharSequence charSequence) {
        if(mTextView != null) {
            mTextView.setText(charSequence);
            if(charSequence== null || "".equals(charSequence.toString().trim())) {
                if(mEmptyView != null) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            } else {
                if(mEmptyView != null) {
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener longClickListener) {
        if(mTextView != null) {
            mTextView.setOnLongClickListener(longClickListener);
        }
    }

    /**
     * 获得TextView的行高
     * */
    public int getLineHeight() {
        Log.e(getClass().getName().toString(), "**********************" + getTextView().getLineHeight());
        return getTextView().getLineHeight();
    }

    /**
     * 获得TextView的行数
     * */
    public int getLineCount() {
        return getTextView().getLineCount();
    }

    public TextView getTextView() {
        return mTextView;
    }

    @Override
    public int getPaddingTop() {
        return getTextView().getPaddingTop();
    }

    /**
     * 执行滚动动画
     * */
    private void doScroll(int position) {
        if(null != mScrollView) {
            if(!userTouch) {
                Animator animator = setupScroll(position);
                animator.start();
            }
        }
    }

    /**
     * 初始化滚动动画
     * */
    private Animator setupScroll(int position) {
        int startY = mScrollView.getScrollY();
        int endY = getLineHeight() * position + getPaddingTop() - getHeight() / 2 + getLineHeight() / 2;
        ValueAnimator animator = ValueAnimator.ofInt(startY, endY);
        animator.setDuration(600);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                mScrollView.smoothScrollTo(0, value);
            }
        });
        return animator;
    }

    private float downY = 0;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                down(event);
                break;
            case MotionEvent.ACTION_MOVE:
                move(event);
                break;
            case MotionEvent.ACTION_UP:
                up(event);
                break;
            default:
                break;
        }
        return false;
    }

    private void down(MotionEvent event) {
        Log.e("Log.e", "*********************Down");
        downY = event.getY();
        handler.removeMessages(MSG_USER_TOUCH); // 倘若线程池中有MSG_USER_TOUCH事件，先移除。
        userTouch = true; // 设置用户点击标记。
    }

    private void move(MotionEvent event) {
        float moveY = Math.abs(event.getY() - downY);
        if(mScrollView.getScrollY() <= 0 && (event.getY() - downY) > 0) {
            // 倘若ScrollView已经滑到顶部，则开始设置TextView的paddingTop值
            resetType = 1;
            autoScroll = true;
            mTextView.setPadding(mTextView.getPaddingLeft(), (int)(moveY / 1.8f + mTextView.getPaddingTop()), mTextView.getPaddingRight(), mTextView.getPaddingBottom());
        }
        if(mScrollView.getScrollY() >= (mTextView.getHeight() - mScrollView.getHeight() - mScrollView.getPaddingTop() - mScrollView.getPaddingBottom()) && (event.getY() - downY) < 0) {
            // 倘若ScrollView已经滑到底部，则开始设置TextView的paddingBottom值
            resetType = -1;
            autoScroll = true;
            mTextView.setPadding(mTextView.getPaddingLeft(), mTextView.getPaddingTop(), mTextView.getPaddingRight(), (int) (moveY / 1.2f + mTextView.getPaddingBottom()));
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
        downY = event.getY();
    }

    private void up(MotionEvent event) {
        resetViewHeight();
    }

    public void resetViewHeight() {
        if(autoScroll && (mTextView.getPaddingBottom() > paddingValue || mTextView.getPaddingTop() > paddingValue)) {
            reset();
        } else {
            handler.sendEmptyMessageDelayed(MSG_USER_TOUCH, 1200);  // 动画完成1.2s后， userTouch = false， 恢复自动滚动事件。
        }
    }

    /**
     * 当ScrollView被OverScroll则执行恢复动画
     * */
    public void reset() {
        ValueAnimator animator = null;
        if(resetType == 1) {
            animator = ValueAnimator.ofFloat(mTextView.getPaddingTop(), paddingValue);
            animator.setInterpolator(new OvershootInterpolator(0.7f));
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    mTextView.setPadding(mTextView.getPaddingLeft(), (int)value, mTextView.getPaddingRight(), mTextView.getPaddingBottom());
                    mScrollView.fullScroll(ScrollView.FOCUS_UP);  // ScrollView 滑动到顶部实现代码，相对于回到底部，此处代码可有可无，具体原因可以动动脑子思考一下哦！
                }
            });
        } else {
            animator = ValueAnimator.ofFloat(mTextView.getPaddingBottom(), paddingValue);
            animator.setInterpolator(new OvershootInterpolator(0.7f));
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    mTextView.setPadding(mTextView.getPaddingLeft(), mTextView.getPaddingTop(), mTextView.getPaddingRight(), (int)value);
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN); // ScrollView 滑动到底部实现代码，此处必须添加，否则回弹效果不能体现，动画显得生硬。
                }
            });
        }
        //动画完成后延迟一秒取消触摸事件
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                handler.sendEmptyMessageDelayed(MSG_USER_TOUCH, 1200);  // 动画完成1.2s后， userTouch = false， 恢复自动滚动事件。
            }
        });
        animator.start();
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_USER_TOUCH:
                    userTouch = false;
                    break;
            }
        }
    };
}

