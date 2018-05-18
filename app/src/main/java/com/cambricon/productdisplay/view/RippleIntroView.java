package com.cambricon.productdisplay.view;

/**
 * Created by dell on 18-4-4.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by dell on 18-4-4.
 */

public class RippleIntroView extends RelativeLayout implements Runnable {
    private int mMaxRadius = 80;
    private int mInterval = 40;
    private int count = 0;
    private Bitmap mCacheBitmap;
    private Paint mRipplePaint;
    private Paint mCirclePaint;
    public RippleIntroView(Context context) {
        this(context, null);
    }
    public RippleIntroView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public RippleIntroView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        mRipplePaint = new Paint();
        mRipplePaint.setAntiAlias(true);
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setColor(Color.WHITE);
        mRipplePaint.setStrokeWidth(2.f);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.WHITE);
    }
    /**
     * view大小变化时系统调用
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //获取图片view
        View mPlusChild = getChildAt(0);
        if (mPlusChild == null) return;
        //获取图片大小
        final int pw = mPlusChild.getWidth();
        final int ph = mPlusChild.getHeight();
        if (pw == 0 || ph == 0) return;
        //加号图片中心点坐标
        final float px = mPlusChild.getX() + pw / 2;
        final float py = mPlusChild.getY() + ph / 2;
        final int rw = pw / 2 - 5;
        final int rh = ph / 2;
        if (mCacheBitmap == null) {
            mCacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(mCacheBitmap);
            super.onDraw(cv);
        }
        //绘制背景图片
        canvas.drawBitmap(mCacheBitmap, 0, 0, mCirclePaint);
        int save = canvas.save();
        for (int step = count; step <= mMaxRadius; step += mInterval) {
            mRipplePaint.setAlpha(255 * (mMaxRadius - step) / mMaxRadius);
            canvas.drawCircle(px, py, (float) (rw + step), mRipplePaint);
        }
        canvas.restoreToCount(save);
        postDelayed(this, 45);
    }
    @Override
    public void run() {
        removeCallbacks(this);
        count += 2;
        count %= mInterval;
        invalidate();//重绘
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
    }
}

