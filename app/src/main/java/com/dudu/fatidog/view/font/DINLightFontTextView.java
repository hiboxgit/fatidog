package com.dudu.fatidog.view.font;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by sunny_zhang on 2016/1/30.
 */
public class DINLightFontTextView extends TextView {

    public DINLightFontTextView(Context context) {
        super(context);
        init(context);
    }

    public DINLightFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DINLightFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
     /*   AssetManager assetManager = context.getAssets();
        Typeface font = Typeface.createFromAsset(assetManager, "DINNextLTPro-Light.otf");*/
        setTypeface(CustomFontUtils.getInstance().getDINLightTextTypeface());
    }
}
