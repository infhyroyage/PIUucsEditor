package com.editor.ucs.piu.buttons;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class RectangularToggleButton extends ToggleButton {
    public RectangularToggleButton(Context context) {
        super(context);
    }

    public RectangularToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangularToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 縦の長さを横の長さの0.75倍にする
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int) (0.75 * MeasureSpec.getSize(widthMeasureSpec)));
    }
}
