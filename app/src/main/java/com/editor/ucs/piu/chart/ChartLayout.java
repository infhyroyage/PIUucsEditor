package com.editor.ucs.piu.chart;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.main.MainCommonFunctions;
import com.editor.ucs.piu.unit.UnitBlock;
import com.editor.ucs.piu.unit.UnitNote;
import com.editor.ucs.piu.unit.UnitProcess;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 譜面のスクロールビュー上にある譜面のレイアウトを表すクラス
 */
public class ChartLayout extends FrameLayout {
    // デバッグ用のタグ
    private static final String TAG = "ChartLayout";

    /**
     * 譜面のレイアウトにセットしている譜面のブロックの全情報を格納するリスト
     */
    public List<UnitBlock> blockList = new LinkedList<>();

    /**
     * 1行ごとのノーツ最大数(Single譜面なら5、Double譜面なら10)
     */
    public byte columnSize;

    /**
     * 譜面のスクロールビューの縦の長さ(px単位)
     */
    float chartHeight;
    /**
     * 譜面のスクロールビューの横の長さ(px単位)
     */
    float chartWidth;
    /**
     * 枠線の長さ(px単位)
     */
    float frameLength;
    /**
     * 正方形のノートの1辺の長さ(px単位)
     */
    float noteLength;

    /**
     * ユーザーが設定している現在の譜面の倍率
     */
    float zoom;

    /**
     * 終端の余白ブロック(黒色)のビューのインスタンス
     * 譜面のレイアウトにビューを追加していない場合はnullをセットすること
     */
    private FrameLayout lastLayout = null;

    /**
     * 「元に戻す」ボタンで扱う譜面編集プロセスを格納するスタック
     */
    public Stack<UnitProcess> undoProcessStack = new Stack<>();

    /**
     * 「やり直し」ボタンで扱う譜面編集プロセスを格納するスタック
     */
    public Stack<UnitProcess> redoProcessStack = new Stack<>();

    /**
     * 最後に保存してから経過した譜面編集プロセスのカウント
     * ucsファイルの新規作成、オープン、保存したとき0にリセットされる
     * 編集orやり直したら1だけインクリメントし、元に戻したら1だけデクリメントする
     */
    public int editCount;

    public ChartLayout(@NonNull Context context) {
        super(context);
    }

    public ChartLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 予め格納したブロックとノートの全情報から、譜面のレイアウトを全てリセットする
     * 1回の処理時間が長いため、譜面のレイアウトの初期設定時や譜面倍率変更時のみ呼び出すこと
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void reset(MainActivity mainActivity) {
        // 譜面のスクロールビューを取得
        ChartScrollView chartScrollView = mainActivity.findViewById(R.id.chartScrollView);
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // 選択領域のレイアウトを取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        /*
         * 「譜面倍率」で設定している譜面倍率を取得
         * 取得失敗時は1.0で取得
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        zoom = sharedPreferences.getFloat(CommonParameters.PREFERENCE_ZOOM, 1.0f);

        // 既に譜面のレイアウトがメイン画面に表示されている場合
        if (mainActivity.ucs != null) {
            //譜面のレイアウトにセットしたノート、ポインター、選択範囲のレイアウトを削除して再セットする
            removeAllViews();
            addView(noteLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(pointerLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            addView(selectedAreaLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            // ノートのレイアウトにセットしたノートのビューをすべて削除する
            noteLayout.removeAllViews();
        }
        // まだ譜面のレイアウトがメイン画面に表示されていない場合
        else {
            /*
             * 譜面のスクロールビューの縦&横の長さを取得
             * NOTE : view.getHeight()、view.getWidth()はMainActivityのonCreate()時に呼び出されると0の数値を返してしまう
             */
            chartHeight = chartScrollView.getHeight();
            chartWidth = chartScrollView.getWidth();

            // 枠線の長さを1dpとし、それをpx単位へと変換
            frameLength = mainActivity.getResources().getDisplayMetrics().density;

            /*
             * 正方形であるノートの1辺の長さをpx単位で計算
             * noteLength := ((譜面のスクロールビューの横幅) - 9 * (枠線の長さ)) / 10
             */
            noteLength = (chartScrollView.getWidth() - 9 * frameLength) / 10;
            // ログ出力
            Log.d(TAG, "reset:chartHeight=" + chartHeight);
            Log.d(TAG, "reset:chartWidth=" + chartWidth);
            Log.d(TAG, "reset:frameLength=" + frameLength);
            Log.d(TAG, "reset:noteLength=" + noteLength);

            // 枠線の色を変更する
            chartScrollView.setBackgroundColor(Color.rgb(
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_FRAME_RED, 80),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_FRAME_GREEN, 80),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_FRAME_BLUE, 80)));

            // ポインターのレイアウトの色を変更し、表示する
            pointerLayout.setBackgroundColor(Color.rgb(
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_RED, 0),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_GREEN, 255),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_BLUE, 0)));
            pointerLayout.getBackground().setAlpha(sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_ALPHA, 64));
            pointerLayout.setVisibility(VISIBLE);

            // 選択領域のレイアウトの色を変更する
            selectedAreaLayout.setBackgroundColor(Color.rgb(
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_RED, 255),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN, 0),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE, 255)));
            selectedAreaLayout.getBackground().setAlpha(sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_ALPHA, 64));

            // ポインターが示すブロックの3つの情報のテキストビューを表示する
            mainActivity.findViewById(R.id.bpmTextView).setVisibility(VISIBLE);
            mainActivity.findViewById(R.id.delayTextView).setVisibility(VISIBLE);
            mainActivity.findViewById(R.id.splitTextView).setVisibility(VISIBLE);
        }

        // 各譜面のブロックのレイアウトを1つずつ譜面のレイアウトに追加する
        for (int idxBlock = 0; idxBlock < blockList.size(); idxBlock++) {
            addBlockView(mainActivity, idxBlock);
        }

        // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
        MainCommonFunctions.updateColorColumnLayouts();

        // 終端の余白ブロックのレイアウトの更新
        updateLastView(mainActivity);

        // ノートのレイアウトの更新
        noteLayout.update(mainActivity);
        // ポインターのレイアウトの更新
        pointerLayout.update(mainActivity);
        // 選択領域のレイアウトの更新
        selectedAreaLayout.update(mainActivity);

        // ポインターの位置が示すブロックの情報を更新
        MainCommonFunctions.updateTextsAtPointer();

        // ノートを1個ごと読み込み、その情報を譜面のレイアウトに更新
        for (UnitNote unitNote : noteLayout.noteMap.values()) {
            noteLayout.addNoteView(mainActivity, unitNote);
        }
    }

    /**
     * 指定された譜面のブロックのビューを譜面のレイアウトに追加する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param idxBlock     譜面のブロックの添字
     */
    public void addBlockView(MainActivity mainActivity, int idxBlock) {
        // ログ出力
        Log.d(TAG, "addBlockView:idxBlock=" + idxBlock);

        // 指定された譜面のブロックのインスタンスを取得
        UnitBlock unitBlock = blockList.get(idxBlock);

        // 指定された譜面のブロックまでの縦の長さのオフセットを計算
        float offsetHeight = 0f;
        for (int i = 0; i < idxBlock; i++) {
            offsetHeight += blockList.get(i).height;
        }

        /*
         * 指定された譜面のブロックの縦の長さをpx単位で計算して設定する
         * NOTE : unitBlock.splitは1だけ引いた値を格納している
         */
        unitBlock.height = 2 * noteLength * zoom * unitBlock.rowLength / (unitBlock.split + 1);

        // 指定された譜面のブロックのビューを、それぞれ1列ずつ譜面のレイアウトに追加
        LayoutParams unitBlockRowParams;
        for (byte column = 0; column < 10; column++) {
            // Single譜面で左半分かどうか判定する
            if (columnSize == 5 && column < 5) {
                // Single譜面での左半分を覆いかぶさるための黒のレイアウトのインスタンスの生成
                FrameLayout leftLayout = unitBlock.getLeftLayout(mainActivity);

                // 上記のレイアウトを譜面のレイアウトに追加
                unitBlockRowParams = new LayoutParams((int) (5 * noteLength + 4 * frameLength), (int) Math.ceil(unitBlock.height));
                unitBlockRowParams.setMargins(0, (int) offsetHeight, 0, 0);
                addView(leftLayout, unitBlockRowParams);

                column = 4;
            } else {
                // 1列のレイアウトリストを取得
                List<FrameLayout> columnLayoutList = unitBlock.getColumnLayoutList(mainActivity, column);

                // 補助線ごとに最後以外の1列のレイアウトを追加していく
                for (int i = 0; i < columnLayoutList.size() - 1; i++) {
                    unitBlockRowParams = new LayoutParams((int) noteLength, (int) (2 * noteLength * zoom - frameLength));
                    unitBlockRowParams.setMargins((int) (column * (noteLength + frameLength)), (int) (offsetHeight + 2 * noteLength * zoom * i + frameLength), 0, 0);
                    addView(columnLayoutList.get(i), unitBlockRowParams);
                }

                // 最後の1列のレイアウトを追加する
                int lastColumnLayoutLength = unitBlock.rowLength % (unitBlock.split + 1);
                if (lastColumnLayoutLength == 0) {
                    unitBlockRowParams = new LayoutParams((int) noteLength, (int) (2 * noteLength * zoom - 2 * frameLength));
                } else {
                    unitBlockRowParams = new LayoutParams((int) noteLength, (int) (2 * noteLength * zoom * lastColumnLayoutLength / (unitBlock.split + 1) - 2 * frameLength));
                }
                unitBlockRowParams.setMargins((int) (column * (noteLength + frameLength)), (int) (offsetHeight + 2 * noteLength * zoom * (columnLayoutList.size() - 1) + frameLength), 0, 0);
                addView(columnLayoutList.get(columnLayoutList.size() - 1), unitBlockRowParams);
            }
        }
    }

    /**
     * 指定された譜面のブロックのビューを譜面のレイアウトから削除する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param idxBlock     譜面のブロックの添字
     */
    public void removeBlockView(MainActivity mainActivity, int idxBlock) {
        // ログ出力
        Log.d(TAG, "removeBlockView:idxBlock=" + idxBlock);


        // 指定された譜面のブロックのインスタンスを取得
        UnitBlock unitBlock = blockList.get(idxBlock);

        // 指定された譜面のブロックのビューを、それぞれ1列ずつ譜面のレイアウトから削除
        for (byte column = 0; column < 10; column++) {
            // Single譜面で左半分かどうか判定する
            if (columnSize == 5 && column < 5) {
                // Single譜面での左半分を覆いかぶさるための黒のレイアウトを譜面のレイアウトから削除
                removeView(unitBlock.getLeftLayout(mainActivity));

                column = 4;
            } else {
                // 1列のレイアウトリストを取得し、それぞれの1列のレイアウトを譜面のレイアウトから削除
                for (FrameLayout columnLayout : unitBlock.getColumnLayoutList(mainActivity, column)) {
                    removeView(columnLayout);
                }

                // 1列のレイアウトリストを消去
                unitBlock.clearColumnLayoutList(column);
            }
        }
    }

    /**
     * 譜面のレイアウトにセットされている終端の余白ブロック(黒色)のビューを更新する
     * 譜面のレイアウトにセットされていない場合は、セットする
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void updateLastView(MainActivity mainActivity) {
        // 各譜面のブロックのレイアウトの縦の長さの合計値を計算
        float heightSum = 0f;
        for (int i = 0; i < blockList.size(); i++) {
            heightSum += blockList.get(i).height;
        }

        if (lastLayout == null) {
            // 終端の余白ブロックのビューのインスタンスがまだ生成されていない場合は生成し、色を設定する
            lastLayout = new FrameLayout(mainActivity);
            lastLayout.setBackgroundColor(Color.rgb(0, 0, 0));
        } else {
            // 終端の余白ブロックのビューのインスタンスが既に生成されている場合は譜面のレイアウトから削除する
            removeView(lastLayout);
        }

        // 終端の余白ブロックのビューのインスタンスを譜面のレイアウトに追加
        LayoutParams unitBlockRowParams = new LayoutParams((int) chartWidth, (int) chartHeight);
        unitBlockRowParams.setMargins(0, (int) heightSum, 0, 0);
        addView(lastLayout, unitBlockRowParams);
    }

    /**
     * 指定された位置における、譜面のレイアウトでの位置情報を取得する
     *
     * @param row 指定位置を表す行番号
     * @throws IllegalStateException ブロックが1つも存在せずに呼び出した場合
     */
    public PositionInformation calcPositionInformation(int row) {
        // ブロックが1つも存在せずに呼び出したかどうかチェック
        if (blockList.size() == 0) {
            throw new IllegalStateException("CommonParameters.blockList is empty.");
        }

        // 指定された位置でのY座標、ブロックのインデックス、ブロック内行数のオフセット、シーク時間を計算
        float coordinate = 0.0f;
        int idxBlock = 0;
        int offsetRowLength = 0;
        float seekPeriod = 0f;
        for (; idxBlock < blockList.size(); idxBlock++) {
            UnitBlock unitBlock = blockList.get(idxBlock);
            if (offsetRowLength + unitBlock.rowLength < row) {
                // Y座標のインクリメント
                coordinate += unitBlock.height;
                // ブロック内行数のオフセットのインクリメント
                offsetRowLength += unitBlock.rowLength;
                // シーク時間のインクリメント
                seekPeriod += 60000 * unitBlock.rowLength / ((unitBlock.split + 1) * unitBlock.bpm);
            } else {
                // Y座標の最終決定
                coordinate += 2 * noteLength * zoom * (row - offsetRowLength - 1) / (unitBlock.split + 1);
                // シーク時間の最終決定
                seekPeriod += 60000 * (row - offsetRowLength - 1) / ((unitBlock.split + 1) * unitBlock.bpm);
                break;
            }
        }

        // ログ出力
        Log.d(TAG, "calcPositionInformation:row=" + row);
        Log.d(TAG, "calcPositionInformation:coordinate=" + coordinate);
        Log.d(TAG, "calcPositionInformation:idxBlock=" + idxBlock);
        Log.d(TAG, "calcPositionInformation:offsetRowLength=" + offsetRowLength);
        Log.d(TAG, "calcPositionInformation:seekPeriod=" + seekPeriod);

        return new PositionInformation(coordinate, idxBlock, offsetRowLength, seekPeriod);
    }

    /**
     * 譜面のレイアウトでの位置情報を表すクラス
     */
    public static class PositionInformation {
        /**
         * Y座標
         */
        public float coordinate;
        /**
         * ブロックのインデックス
         */
        public int idxBlock;
        /**
         * ブロック内行数のオフセット
         */
        public int offsetRowLength;
        /**
         * シーク時間
         */
        float seekPeriod;

        /**
         * コンストラクタ
         *
         * @param coordinate      Y座標
         * @param idxBlock        ブロックのインデックス
         * @param offsetRowLength ブロック内行数のオフセット
         * @param seekPeriod      シーク時間
         */
        PositionInformation(float coordinate, int idxBlock, int offsetRowLength, float seekPeriod) {
            this.coordinate = coordinate;
            this.idxBlock = idxBlock;
            this.offsetRowLength = offsetRowLength;
            this.seekPeriod = seekPeriod;
        }
    }
}
