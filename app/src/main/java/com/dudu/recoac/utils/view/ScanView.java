package com.dudu.recoac.utils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dudu.fatidog.R;

/**
 * @author luo zha
 * @CreateDate 2017-02-10 10:59.
 */
public class ScanView extends View {

    private static final String TAG = "ScanView";

    private Bitmap mScanLine;

    private Paint mPaint;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;
    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop = 0;

    private int mWidth, mHeight;

    /**
     * 刷新界面的时间
     */
    private static final int ANIMATION_DELAY = 20;

    private ScanRunnable scanRunnable;

    private boolean isStart;

    public ScanView(Context context) {
        super(context, null);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //获取所有的自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScanView);
        BitmapDrawable iconNormal = (BitmapDrawable) a.getDrawable(R.styleable.ScanView_off_standard);
        if (iconNormal != null) {
            mScanLine = iconNormal.getBitmap();
        }
        a.recycle();
        initPaint();
        scanRunnable = new ScanRunnable();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
    }

    /**
     * 通过重写onMeasure()获取用户设置的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = measureWidth(widthMeasureSpec);
        mHeight = measureHeight(heightMeasureSpec);
        Log.v(TAG, "mWidth:" + mWidth + "  mHeight:" + mHeight);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float offset = 4;
        slideTop += SPEEN_DISTANCE;
        if (slideTop >= mHeight) {
            slideTop = 0;
        }
        Rect lineRect = new Rect();
        lineRect.left = (int) (0 + offset * 2);
        lineRect.right = (int) (mWidth - offset * 2);
        lineRect.top = slideTop;
        lineRect.bottom = slideTop + mScanLine.getHeight();
        canvas.drawBitmap(mScanLine, null, lineRect, mPaint);
    }

    public void startScan() {
        slideTop = 0;
        isStart = true;
        setVisibility(VISIBLE);
        postDelayed(scanRunnable, ANIMATION_DELAY);
    }

    public void stopScan() {
        isStart = false;
        setVisibility(GONE);
        removeCallbacks(scanRunnable);
    }

    public boolean isStart() {
        return isStart;
    }

    private class ScanRunnable implements Runnable {

        @Override
        public void run() {
            postInvalidate();
            postDelayed(scanRunnable, ANIMATION_DELAY);
        }
    }

    /**
     * 获取用户设置的宽
     */
    private int measureWidth(int pWidthMeasureSpec) {
        int result = 0;
        int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);// 得到模式
        int widthSize = MeasureSpec.getSize(pWidthMeasureSpec);// 得到尺寸

        switch (widthMode) {
            /**
             * mode共有三种情况，取值分别为MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY,
             * MeasureSpec.AT_MOST。
             *
             *
             * MeasureSpec.EXACTLY是精确尺寸，
             * 当我们将控件的layout_width或layout_height指定为具体数值时如andorid
             * :layout_width="50dip"，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
             *
             *
             * MeasureSpec.AT_MOST是最大尺寸，
             * 当控件的layout_width或layout_height指定为WRAP_CONTENT时
             * ，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可
             * 。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
             *
             *
             * MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，
             * 通过measure方法传入的模式。
             */
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = widthSize;
                break;
        }
        return result;
    }

    /**
     * 获取用户设置的高
     */
    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;
        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);// 得到模式
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);// 得到尺寸

        switch (heightMode) {
            /**
             * mode共有三种情况，取值分别为MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY,
             * MeasureSpec.AT_MOST。
             *
             *
             * MeasureSpec.EXACTLY是精确尺寸，
             * 当我们将控件的layout_width或layout_height指定为具体数值时如andorid
             * :layout_width="50dip"，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
             *
             *
             * MeasureSpec.AT_MOST是最大尺寸，
             * 当控件的layout_width或layout_height指定为WRAP_CONTENT时
             * ，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可
             * 。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
             *
             *
             * MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，
             * 通过measure方法传入的模式。
             */
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }
}
