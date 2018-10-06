package com.editor.ucs.piu.chart;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.unit.UnitBlock;

/**
 * 譜面のレイアウト上にあるポインターのレイアウトを表すクラス
 */
public final class PointerLayout extends FrameLayout {
    // デバッグ用のタグ
    private static final String TAG = "PointerLayout";

    /**
     * ポインターが示しているブロックのBPM値
     */
    public float bpm;
    /**
     * ポインターが示しているブロックのDelay値
     */
    public float delay;
    /**
     * ポインターが示しているブロックのBeat値
     */
    public byte beat;
    /**
     * ポインターが示しているブロックのSplit値
     */
    public byte split;
    /**
     * ポインターが示している行番号
     * 一番上の行の行番号は0ではなく1と定義する
     */
    public int row = 1;

    public PointerLayout(@NonNull Context context) {
        super(context);
    }

    public PointerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PointerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * ポインターのレイアウトとパラメーターを更新する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void update(MainActivity mainActivity) {
        // 譜面のレイアウトの取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトの取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        // ポインターの位置での位置情報を計算
        ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(row);

        // ポインターのレイアウトを譜面のレイアウトから削除し、レイアウトの大きさと位置をセットして再セット
        chartLayout.removeView(this);
        LayoutParams params = new LayoutParams((int) (chartLayout.columnSize * chartLayout.noteLength + (chartLayout.columnSize - 1) * chartLayout.frameLength), (int) chartLayout.noteLength);
        params.setMargins((chartLayout.columnSize == 5) ? (int) (5 * chartLayout.noteLength + 4 * chartLayout.frameLength) : 0, (int) information.coordinate, 0, 0);
        chartLayout.addView(this, params);

        /*
         * ノートのレイアウトの更新
         * これより、ポインターのレイアウトの上にノートのビューがセットしているように見せられる
         */
        noteLayout.update(mainActivity);

        // ポインターが示すブロックのBPM、Delay、Beat、Split値を更新
        UnitBlock unitBlock = chartLayout.blockList.get(information.idxBlock);
        bpm = unitBlock.bpm;
        delay = unitBlock.delay;
        beat = unitBlock.beat;
        split = unitBlock.split;

        // ログ出力
        Log.d(TAG, "update:row=" + row);
        Log.d(TAG, "update:bpm=" + bpm);
        Log.d(TAG, "update:delay=" + delay);
        Log.d(TAG, "update:beat=" + beat);
        Log.d(TAG, "update:split=" + split);
    }
}
