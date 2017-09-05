/*
 * Copyright (c) 2017. The ReadyShowShow@gmail Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jian.cropimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 杨健
 * 缩放图片的View
 */
@SuppressWarnings("ClickableViewAccessibility")
public class CropZoomImageView extends android.support.v7.widget.AppCompatImageView
        implements OnTouchListener {
    private final Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;

    private float SCALE_MAX;
    private float SCALE_MIN;

    private Context context;

    private boolean isFirstInit = true;

    /**
     * 用于存放矩阵
     */
    private final float[] matrixValues = new float[9];

    /**
     * 缩放的手势检
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private final Matrix mScaleMatrix = new Matrix();

    /**
     * 用于双击
     */
    private GestureDetector mGestureDetector;

    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    private boolean isAutoScale;

    private int mTouchSlop;

    private float mLastX;
    private float mLastY;

    private boolean isCanDrag;
    private int lastPointerCount;
    /**
     * 水平方向与View的边
     */
    private int mHorizontalPadding = 0;

    private int mHVerticalPadding;

    public CropZoomImageView(Context context) {
        this(context, null);
    }

    public CropZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setScaleType(ScaleType.MATRIX);
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isFirstInit) {
                    Drawable d = getDrawable();
                    if (d == null)
                        return;
                    initMaxAndMinScale(d);
                    int width = getWidth();
                    int height = getHeight();
                    int drawableW = d.getIntrinsicWidth();
                    int drawableH = d.getIntrinsicHeight();
                    float scale = getInitScaleSize(d);
                    mScaleMatrix.postTranslate((width - drawableW) / 2,
                            (height - drawableH) / 2);
                    mScaleMatrix.postScale(scale, scale, getWidth() / 2,
                            getHeight() / 2);
                    // 图片移动至屏幕中央
                    setImageMatrix(mScaleMatrix);
                    isFirstInit = false;
                }
            }
        };
        OnScaleGestureListener onScaleGestureListener = new OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = getScale();
                float scaleFactor = detector.getScaleFactor();

                if (getDrawable() == null)
                    return true;

                /**
                 * 缩放的范围控制
                 */
                if ((scale < SCALE_MAX && scaleFactor > 1.0f)
                        || (scale > SCALE_MIN && scaleFactor < 1.0f)) {
                    /**
                     * �?��值最小�?判断
                     */
                    if (scaleFactor * scale < SCALE_MIN) {
                        scaleFactor = SCALE_MIN / scale;
                    }
                    if (scaleFactor * scale > SCALE_MAX) {
                        scaleFactor = SCALE_MAX / scale;
                    }
                    /**
                     * 设置缩放比例
                     */
                    mScaleMatrix.postScale(scaleFactor, scaleFactor,
                            detector.getFocusX(), detector.getFocusY());
                    checkBorder();
                    setImageMatrix(mScaleMatrix);
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        };
        SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale == true)
                    return true;

                float x = e.getX();
                float y = e.getY();
                if (getScale() < (SCALE_MAX + SCALE_MIN) / 2) {
                    CropZoomImageView.this.postDelayed(
                            new AutoScaleRunnable(SCALE_MAX, x, y), 16);
                    isAutoScale = true;
                } else {
                    CropZoomImageView.this.postDelayed(
                            new AutoScaleRunnable(SCALE_MIN, x, y), 16);
                    isAutoScale = true;
                }

                return true;
            }
        };
        mGestureDetector = new GestureDetector(context, simpleOnGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
        this.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        lastPointerCount = pointerCount;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }
                if (isCanDrag) {
                    if (getDrawable() != null) {

                        RectF rectF = getMatrixRectF();
                        // 如果宽度小于屏幕宽度，则禁止左右移动
                        if (rectF.width() <= getWidth() - mHorizontalPadding * 2) {
                            dx = 0;
                        }
                        // 如果高度小雨屏幕高度，则禁止上下移动
                        if (rectF.height() <= getHeight() - getHVerticalPadding()
                                * 2) {
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkBorder();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                break;
        }
        return true;
    }

    /**
     * 获得当前的缩放比
     *
     * @return
     */
    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
    }

    private void initMaxAndMinScale(Drawable drawable) {
        float scale = 1.0f;
        int drawableW = drawable.getIntrinsicWidth();
        int drawableH = drawable.getIntrinsicHeight();
        int frameSize = getWidth() - mHorizontalPadding * 2;
        // 大图
        if (drawableW > frameSize && drawableH < frameSize) {
            scale = 1.0f * frameSize / drawableH;
        } else if (drawableH > frameSize && drawableW < frameSize) {
            scale = 1.0f * frameSize / drawableW;
        } else if (drawableW > frameSize && drawableH > frameSize) {
            float scaleW = frameSize * 1.0f / drawableW;
            float scaleH = frameSize * 1.0f / drawableH;
            scale = Math.max(scaleW, scaleH);
        }

        // 太小的图片放大处�?
        if (drawableW < frameSize && drawableH > frameSize) {
            scale = 1.0f * frameSize / drawableW;
        } else if (drawableH < frameSize && drawableW > frameSize) {
            scale = 1.0f * frameSize / drawableH;
        } else if (drawableW < frameSize && drawableH < frameSize) {
            float scaleW = 1.0f * frameSize / drawableW;
            float scaleH = 1.0f * frameSize / drawableH;
            scale = Math.max(scaleW, scaleH);
        }
        SCALE_MIN = scale;
        SCALE_MAX = scale * 4;
    }

    private float getInitScaleSize(Drawable drawable) {
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        int drawableW = drawable.getIntrinsicWidth();
        int drawableH = drawable.getIntrinsicHeight();
        float heightScale = 1f * frameHeight / drawableH;
        float widthScale = 1f * frameWidth / drawableW;
        return Math.max(heightScale, widthScale);
    }

    /**
     * 边界检测
     */
    private void checkBorder() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int frameWidth = getWidth();
        int frameHeight = getHeight();
        // 如果宽或高大于屏幕，则控制范围; 这里的0.001是因为精度丢失会产生问题，但是误差一般很小，
        if (rect.width() + 0.0001 >= frameWidth - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < frameWidth - mHorizontalPadding) {
                deltaX = frameWidth - mHorizontalPadding - rect.right;
            }
        }
//        if (rect.height() + 0.0001 >= frameHeight - 2 * getHVerticalPadding()) {
        if (rect.top > getHVerticalPadding()) {
            deltaY = -rect.top + getHVerticalPadding();
        }
        if (rect.bottom < frameWidth + getHVerticalPadding()) {
            deltaY = frameWidth + getHVerticalPadding() - rect.bottom;
        }
//        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 根据当前图片的Matrix获得图片的范
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 是否拖动行
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }

    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
    }

    private int getHVerticalPadding() {
        return mHVerticalPadding;//(getHeight() - (getWidth() - 2 * mHorizontalPadding)) / 2;
    }

    public void setHVerticalPadding(int padding) {
        mHVerticalPadding = padding;
    }

    /**
     * 剪切图片，返回剪切后的bitmap对象
     */
    public boolean clipToFile(File file) {
        if (file == null)
            return false;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        Bitmap croppedImage = Bitmap.createBitmap(bitmap, mHorizontalPadding,
                getHVerticalPadding(), getWidth() - 2 * mHorizontalPadding,
                getWidth() - 2 * mHorizontalPadding);

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            if (outputStream != null) {
                croppedImage.compress(mOutputFormat, 90, outputStream);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                }
            }
        }
        croppedImage.recycle();
        bitmap.recycle();
        return true;
    }

    /**
     * 自动缩放的任务
     */
    private class AutoScaleRunnable implements Runnable {
        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;

        /**
         * 缩放的中�?
         */
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run() {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorder();
            setImageMatrix(mScaleMatrix);

            final float currentScale = getScale();
            // 如果值在合法范围内，继续缩放
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
                CropZoomImageView.this.postDelayed(this, 16);
            } else { // 设置为目标的缩放比例
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
                checkBorder();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }

        }
    }
}
