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

package com.unit.cropimage;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * @author jian
 * 为了方便的覆盖一个自定义的图片在ImageView上
 * 这个类的显示效果是第一个View所在的位置是没有背景颜色的
 * 如果设置了背景，则第一个View周围显示该背景颜色
 * 这个类能获取第一个子View的位置
 */
public class CoverView extends FrameLayout {

    public CoverView(@NonNull Context context) {
        super(context);
    }

    public CoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private int[] getVisibleArea() {
        if(getChildCount() > 0) {
            View visibleAreaView = getChildAt(0);
            return null;
        } else {
            return null;
        }
    }
}
