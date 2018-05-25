package com.dudu.fatidog.view.font;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Administrator on 2016/7/20.
 */
public class CustomFontUtils {

    private static CustomFontUtils mInstance;

    private Typeface mDINLightTextTypeface, mDINRegularTextTypeface, mFZLFontTextTypeface, mDINLightEditTypeface;

    public static CustomFontUtils getInstance() {
        if (mInstance == null) {
            mInstance = new CustomFontUtils();
        }
        return mInstance;
    }

    public void init(Context context) {
        mDINLightTextTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/DINNextLTPro-Light.otf");
        mDINRegularTextTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/DINNextLTPro-Regular.otf");
        mFZLFontTextTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/FZLTXHK.TTF");
        mDINLightEditTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/FZLTXHK.TTF");
    }

    public Typeface getDINLightTextTypeface() {
        return mDINLightTextTypeface;
    }

    public Typeface getDINRegularTextTypeface() {
        return mDINRegularTextTypeface;
    }

    public Typeface getFZLFontTextTypeface() {
        return mFZLFontTextTypeface;
    }

    public Typeface getDINLightEditTypeface() {
        return mDINLightEditTypeface;
    }
}
