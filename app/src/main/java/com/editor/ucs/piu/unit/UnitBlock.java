package com.editor.ucs.piu.unit;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.ChartScrollView;
import com.editor.ucs.piu.chart.NoteLayout;
import com.editor.ucs.piu.chart.PointerLayout;
import com.editor.ucs.piu.chart.SelectedAreaLayout;
import com.editor.ucs.piu.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * 譜面のブロックを扱うクラス
 */
public class UnitBlock {
    // デバッグ用のタグ
    private static final String TAG = "UnitBlock";

    /**
     * 譜面のブロックで定義されているBPM値
     * 範囲は0.1~999、有効数字7桁まで表記可能
     */
    public float bpm;
    /**
     * 譜面のブロックで定義されているDelay値
     * beat単位は使用せず、ms単位で統一
     * 範囲は-999999~999999、有効数字7桁まで表記可能
     */
    public float delay;
    /**
     * 譜面のブロックで定義されているBeat値
     * 範囲は1~64
     */
    public byte beat;
    /**
     * 譜面のブロックで定義されているSplit値
     * 範囲は1~128
     * byteの最大値は127なので、実際の値から1だけ引いた値を格納
     */
    public byte split;
    /**
     * 譜面のブロック内にセットできる行数
     * 範囲は自然数全体
     */
    public int rowLength;

    /**
     * 譜面のブロックの縦の長さ(px単位)
     * 値はChartScrollView.reset()実行時にセットされる
     */
    public float height;

    /**
     * 譜面のブロック内に存在する1列のレイアウトリストのマップ
     * (キーの値) := 譜面のブロックでの列番号
     */
    private Map<Byte, List<FrameLayout>> columnLayoutListMap;

    /**
     * Single譜面での左半分を覆いかぶさるための黒のレイアウト
     */
    private FrameLayout leftLayout;

    /**
     * コンストラクタ
     *
     * @param bpm 譜面のブロックで定義されているBPM値
     * @param delay 譜面のブロックで定義されているDelay値
     * @param beat 譜面のブロックで定義されているBeat値
     * @param split 譜面のブロックで定義されているSplit値
     * @param rowLength 譜面のブロックで定義されている行数
     */
    public UnitBlock(float bpm, float delay, byte beat, byte split, int rowLength) {
        this.bpm = bpm;
        this.delay = delay;
        this.beat = beat;
        this.split = split;
        this.rowLength = rowLength;

        columnLayoutListMap = new TreeMap<>();

        leftLayout = null;
    }

    /**
     * この譜面のブロックでの、指定された列番号における1列のレイアウトリストを取得する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param column この譜面のブロックでの列番号
     * @return この譜面のブロックでの1列のレイアウトリスト
     */
    public List<FrameLayout> getColumnLayoutList(final MainActivity mainActivity, final byte column) {
        // ログ出力
        Log.d(TAG, "getColumnLayoutList:columnLayoutListMap.containsKey(" + column + ")=" + columnLayoutListMap.containsKey(column));

        // 指定された列番号で既に1列のレイアウトリストが生成されている場合は、それを返す
        if (columnLayoutListMap.containsKey(column)) {
            return columnLayoutListMap.get(column);
        }

        // 譜面のスクロールビューを取得
        final ChartScrollView chartScrollView = mainActivity.findViewById(R.id.chartScrollView);
        // 譜面のレイアウトを取得
        final ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        final NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトを取得
        final PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトを取得
        final SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 1列のレイアウトのインスタンスの個数(=譜面のブロック内の境界線の個数-1)を計算
        int columnLayoutLength = (int) Math.ceil(rowLength / (split + 1.0));
        // 1列のレイアウトリストのインスタンスの生成
        List<FrameLayout> columnLayoutList = new ArrayList<>();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        final Vibrator vibrator = (Vibrator) mainActivity.getSystemService(VIBRATOR_SERVICE);

        for (int i = 0; i < columnLayoutLength; i++) {
            // 1列のレイアウトのインスタンスの生成
            FrameLayout columnLayout = new FrameLayout(mainActivity);

            // 1列のレイアウトに通常のタップ時のリスナーをセット
            columnLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 譜面編集をロックしているか&譜面が再生中かどうかのチェック
                    if (!((ToggleButton) mainActivity.findViewById(R.id.toggleButtonEditLock)).isChecked() && chartScrollView.isScrolled) {
                        // バイブレーションが「ON」の場合、25ミリ秒間バイブレーションを行う
                        if (vibrator != null && sharedPreferences.getBoolean(CommonParameters.PREFERENCE_VIBRATION, false)) {
                            vibrator.vibrate(25);
                        }

                        if (((ToggleButton) mainActivity.findViewById(R.id.toggleButtonEditSelect)).isChecked()) {
                            // 譜面選択モードがONの場合、選択領域の始点、終点を変更する
                            selectedAreaLayout.changeSelectedEdge(mainActivity);
                        } else {
                            // 譜面選択モードがOFFの場合、ノートorホールドの編集対処を行う
                            noteLayout.handleNote(mainActivity, column, false);
                        }
                    }
                }
            });

            // 1列のレイアウトに長押しのリスナーをセット
            columnLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // 譜面編集をロックしているか&譜面が再生中かどうかのチェック
                    if (!((ToggleButton) mainActivity.findViewById(R.id.toggleButtonEditLock)).isChecked() && chartScrollView.isScrolled) {
                        // バイブレーションが「ON」の場合、通常タップ時より長い50ミリ秒間バイブレーションを行う
                        if (vibrator != null && sharedPreferences.getBoolean(CommonParameters.PREFERENCE_VIBRATION, false)) {
                            vibrator.vibrate(50);
                        }

                        if (((ToggleButton) mainActivity.findViewById(R.id.toggleButtonEditSelect)).isChecked()) {
                            // 譜面選択モードがONの場合、選択領域の始点、終点を変更する
                            selectedAreaLayout.changeSelectedEdge(mainActivity);
                        } else {
                            // 譜面選択モードがOFFの場合、ノートorホールドの編集対処を行う
                            noteLayout.handleNote(mainActivity, column, true);
                        }
                    }

                    // trueを返して、この直後に通常のタップ時の動作を発生させないようにする
                    return true;
                }
            });

            // 1列のレイアウトリストに追加
            columnLayoutList.add(columnLayout);
        }

        // 1列のレイアウトリストの集合に追加
        columnLayoutListMap.put(column, columnLayoutList);

        return columnLayoutList;
    }

    /**
     * この譜面のブロックでの、指定された1列のレイアウトリストを消去する
     *
     * @param column この譜面のブロックでの列番号
     */
    public void clearColumnLayoutList(byte column) {
        columnLayoutListMap.get(column).clear();
        columnLayoutListMap.remove(column);
    }

    /**
     * Single譜面での左半分を覆いかぶさるための黒のレイアウトを取得する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @return Single譜面での左半分を覆いかぶさるための黒のレイアウト
     */
    public FrameLayout getLeftLayout(MainActivity mainActivity) {
        // 既にレイアウトが生成されている場合は、それを返す
        if (leftLayout != null) {
            return leftLayout;
        }

        // レイアウトのインスタンスを生成し、色を黒にセット
        leftLayout = new FrameLayout(mainActivity);
        leftLayout.setBackgroundColor(Color.rgb(0, 0, 0));

        return leftLayout;
    }

    /**
     * この譜面のブロックのインスタンスのコピーを返す
     *
     * @return 譜面のブロックのインスタンスのコピー
     */
    public UnitBlock copy() {
        return new UnitBlock(bpm, delay, beat, split, rowLength);
    }
}