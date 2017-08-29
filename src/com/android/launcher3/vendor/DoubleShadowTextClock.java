/*
 * Copyright (C) 2016-2017 Google Inc.
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

package com.android.launcher3.vendor;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextClock;

import com.android.launcher3.R;

public class DoubleShadowTextClock extends TextClock {
    private final float ambientShadowBlur;
    private final int ambientShadowColor;
    private final float keyShadowBlur;
    private final int keyShadowColor;
    private final float keyShadowOffset;

    public DoubleShadowTextClock(Context context) {
        this(context, null);
    }

    public DoubleShadowTextClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DoubleShadowTextClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray ta = context.obtainStyledAttributes(attributeSet, new int[] { R.attr.ambientShadowColor, R.attr.keyShadowColor, R.attr.ambientShadowBlur, R.attr.keyShadowBlur, R.attr.keyShadowOffset }, i, 0);
        ambientShadowColor = ta.getColor(0, 0);
        keyShadowColor = ta.getColor(1, 0);
        ambientShadowBlur = ta.getDimension(2, 0.0F);
        keyShadowBlur = ta.getDimension(3, 0.0F);
        keyShadowOffset = ta.getDimension(4, 0.0F);
        ta.recycle();
        setShadowLayer(Math.max(keyShadowBlur + keyShadowOffset, ambientShadowBlur), 0.0f, 0.0f, keyShadowColor);
		setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()));
                } catch (ActivityNotFoundException ex) { }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setShadowLayer(keyShadowBlur, 0.0f, keyShadowOffset, keyShadowColor);
        super.onDraw(canvas);
        getPaint().setShadowLayer(ambientShadowBlur, 0.0f, 0.0f, ambientShadowColor);
        super.onDraw(canvas);
    }

    public void setFormat(CharSequence charSequence) {
        setFormat24Hour(charSequence);
        setFormat12Hour(charSequence);
    }
}