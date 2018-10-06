package com.editor.ucs.piu.buttons;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * ボタン群のレイアウトでのボタンを表すクラス
 */
public class RectangularButton extends AppCompatButton {
    public RectangularButton(Context context) {
        super(context);
    }

    public RectangularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangularButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 縦の長さを横の長さの0.75倍にする
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int) (0.75 * MeasureSpec.getSize(widthMeasureSpec)));
    }
}
