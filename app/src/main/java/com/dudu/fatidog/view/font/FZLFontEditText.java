package com.dudu.fatidog.view.font;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;



/**
 * Created by sunny_zhang on 2016/2/1.
 */
public class FZLFontEditText extends EditText {
    public FZLFontEditText(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setTypeface(CustomFontUtils.getInstance().getDINLightEditTypeface());
    }

    public FZLFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FZLFontEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
