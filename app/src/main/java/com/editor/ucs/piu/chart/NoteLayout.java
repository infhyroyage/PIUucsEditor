package com.editor.ucs.piu.chart;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.buttons.ButtonsLayout;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.main.MainCommonFunctions;
import com.editor.ucs.piu.unit.UnitBlock;
import com.editor.ucs.piu.unit.UnitNote;
import com.editor.ucs.piu.unit.UnitProcess;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 譜面のレイアウト上にあるノートのレイアウトを表すクラス
 */
public class NoteLayout extends FrameLayout {
    // デバッグ用のタグ
    private static final String TAG = "NoteLayout";

    /**
     * ノートのレイアウトにセットしてあるノートの全情報を格納するマップ
     * (キーの値) := 10 * (ノートorホールドの始点の行番号) + (列番号)
     * NOTE : ノートの位置の一意性は、「ノートorホールドの始点の行番号」と「列番号」との直積により表現可能
     */
    public SortedMap<Integer, UnitNote> noteMap = new TreeMap<>();

    /**
     * ホールドの1点目の行番号を保持する配列
     * 添え字は列を表し、Single譜面でもDouble譜面でも10個の要素数を持つ
     * 	・要素が0           : ホールドの1点目をまだ設置していない
     * 	・要素が0より大きい : ホールドの1点目を既に設置しており、その行番号
     */
    public int[] holdEdge = new int[10];

    public NoteLayout(@NonNull Context context) {
        super(context);
    }

    public NoteLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 譜面のレイアウトをノートのレイアウトの下敷きにして、ノートのレイアウトを更新する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void update(MainActivity mainActivity) {
        // 譜面のレイアウトを取得
        FrameLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        // ノートのレイアウトを譜面のレイアウトから削除して、再度追加する
        chartLayout.removeView(this);
        chartLayout.addView(this, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * 指定されたノートのビューをノートのレイアウトに追加する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param unitNote 追加するノートのインスタンス
     */
    public void addNoteView(MainActivity mainActivity, UnitNote unitNote) {
        // ログ出力
        Log.d(TAG, "addNoteView:column=" + unitNote.column);
        Log.d(TAG, "addNoteView:start=" + unitNote.start);
        Log.d(TAG, "addNoteView:goal=" + unitNote.goal);
        Log.d(TAG, "addNoteView:hollowStartList.size=" + unitNote.hollowStartList.size());
        Log.d(TAG, "addNoteView:hollowGoalList.size=" + unitNote.hollowGoalList.size());

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        // リソースのpng画像をセットして、イメージビューを更新
        unitNote.updateAllViews(mainActivity);

        // 追加するノートの始点のイメージビューのX座標を計算
        float left = unitNote.column * (chartLayout.noteLength + chartLayout.frameLength);

        // 追加するノートの始点での位置情報を計算
        ChartLayout.PositionInformation startInformation = chartLayout.calcPositionInformation(unitNote.start);

        // ホールドor中抜きホールドの場合
        if (unitNote.start != unitNote.goal) {
            // ホールドor中抜きホールドの長さを計算する
            float holdHeight = calcHoldAndHollowHeight(mainActivity, unitNote.start, startInformation, unitNote.goal);

            // ホールドor中抜きホールドの終点のイメージビューを譜面のレイアウトに追加
            LayoutParams params = new LayoutParams((int) chartLayout.noteLength, (int) chartLayout.noteLength);
            params.setMargins((int) left, (int) (startInformation.coordinate + holdHeight), 0, 0);
            addView(unitNote.goalView, params);

            // ホールドor中抜きホールドの始点と終点との間のイメージビューを譜面のレイアウトに追加
            params = new LayoutParams((int) chartLayout.noteLength, (int) holdHeight);
            params.setMargins((int) left, (int) (startInformation.coordinate + chartLayout.noteLength / 2), 0, 0);
            addView(unitNote.holdView, params);

            for (int i = 0; i < unitNote.hollowStartList.size(); i++) {
                // 中抜きホールドの始点と終点との間にかぶせるビューの始点での位置情報を計算
                ChartLayout.PositionInformation hollowStartInformation = chartLayout.calcPositionInformation(unitNote.hollowStartList.get(i));

                // 中抜きホールドの始点と終点との間にかぶせるビューの長さを計算
                float hollowHeight = calcHoldAndHollowHeight(mainActivity, unitNote.hollowStartList.get(i), hollowStartInformation, unitNote.hollowGoalList.get(i));

                // 中抜きホールドの始点と終点との間にかぶせるビューを譜面のレイアウトに追加
                params = new LayoutParams((int) chartLayout.noteLength, (int) hollowHeight);
                params.setMargins((int) left, (int) hollowStartInformation.coordinate, 0, 0);
                addView(unitNote.hollowViewList.get(i), params);
            }
        }

        // 単ノートorホールドの始点のイメージビューを譜面のレイアウトに追加
        LayoutParams params = new LayoutParams((int) chartLayout.noteLength, (int) chartLayout.noteLength);
        params.setMargins((int) left, (int) startInformation.coordinate, 0, 0);
        addView(unitNote.startView, params);
    }

    /**
     * ホールドor中抜きホールドの始点と終点との間にかぶせるビューの長さを計算する(px単位)
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param start ホールドor中抜きホールドの始点と終点との間にかぶせるビューの始点
     * @param startInformation ホールドor中抜きホールドの始点と終点との間にかぶせるビューの始点の位置情報
     * @param goal ホールドor中抜きホールドの始点と終点との間にかぶせるビューの終点
     * @return ホールドor中抜きホールドの始点と終点との間にかぶせるビューの長さ
     */
    private float calcHoldAndHollowHeight(MainActivity mainActivity, int start, ChartLayout.PositionInformation startInformation, int goal) {
        // ログ出力
        Log.d(TAG, "calcHoldAndHollowHeight:start=" + start);
        Log.d(TAG, "calcHoldAndHollowHeight:goal=" + goal);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        // 計算するホールドor中抜きホールドの長さの初期化
        float height = 0.0f;
        // ホールドor中抜きホールドがブロックを跨ぐかどうかのフラグ
        boolean isMultiBlock = false;

        // 長さをブロックごとに計算
        for (; startInformation.idxBlock < chartLayout.blockList.size(); startInformation.idxBlock++) {
            UnitBlock unitBlock = chartLayout.blockList.get(startInformation.idxBlock);
            if (startInformation.offsetRowLength + unitBlock.rowLength < goal) {
                // ホールドor中抜きホールドの長さの途中決定
                if (isMultiBlock) {
                    // ブロックをさらに跨ぐ場合
                    height += unitBlock.height;
                } else {
                    // 始点から初めてブロックを跨ぐ場合
                    height += 2 * chartLayout.noteLength * chartLayout.zoom * (startInformation.offsetRowLength + unitBlock.rowLength - start + 1) / (unitBlock.split + 1);
                    isMultiBlock = true;
                }
                startInformation.offsetRowLength += unitBlock.rowLength;
            } else {
                // ホールドor中抜きホールドの長さの最終決定
                if (isMultiBlock) {
                    // 始点からブロックを跨いた場合
                    height += 2 * chartLayout.noteLength * chartLayout.zoom * (goal - startInformation.offsetRowLength - 1) / (unitBlock.split + 1);
                } else {
                    // 始点からブロックを跨がない場合
                    height += 2 * chartLayout.noteLength * chartLayout.zoom * (goal - start) / (unitBlock.split + 1);
                }
                break;
            }
        }

        return height;
    }

    /**
     * 指定されたノートのビューをノートのレイアウトから削除する
     *
     * @param unitNote 削除するノートのインスタンス
     */
    public void removeNoteView(UnitNote unitNote) {
        // ログ出力
        Log.d(TAG, "removeNoteView:column=" + unitNote.column);

        if (unitNote.start != unitNote.goal) {
            // ホールドor中抜きホールドの終点と、始点と終点との間の2つのイメージビューを譜面のレイアウトから削除
            removeView(unitNote.holdView);
            removeView(unitNote.goalView);

            // 中抜きホールドの始点と終点との間にかぶせるすべてのイメージビューを譜面のレイアウトから削除
            for (int i = 0; i < unitNote.hollowStartList.size(); i++) {
                removeView(unitNote.hollowViewList.get(i));
            }
        }

        // 単ノートorホールドの始点のイメージビューを譜面のレイアウトから削除
        removeView(unitNote.startView);
    }

    /**
     * ポインターが示す行番号の指定された列番号において、ノートorホールドの編集を行う
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param column ノートorホールドを追加or削除する列番号
     * @param isLongClicked 長押ししたかどうかのフラグ
     */
    public void handleNote(MainActivity mainActivity, byte column, boolean isLongClicked) {
        // ログ出力
        Log.d(TAG, "handleNote:column=" + column);
        Log.d(TAG, "handleNote:isLongClicked=" + isLongClicked);
        Log.d(TAG, "handleNote:holdEdge[" + column + "]=" + holdEdge[column]);

        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);

        // 追加したノートの集合
        Set<UnitNote> addedNotes = new HashSet<>();
        // 削除したノートの値の集合
        Set<UnitNote> removedNotes = new HashSet<>();

        // ホールドの2点目を設置したかどうかのフラグ
        boolean isSetHold = false;

        /*
         * 引数の列番号における、ポインターの示す行番号より小さい最大の行番号にあるノートのうち、
         * そのインスタンスを格納する譜面のレイアウトに存在するマップのキーpreKeyと
         * preKeyに対応するノートのインスタンスpreUnitNoteを取得
         * もし、ポインターの示す行番号より小さいノートが存在しない場合は、preKeyに0、preUnitNoteにnullを格納
         */
        int preKey = 0;
        UnitNote preUnitNote = null;
        SortedMap<Integer, UnitNote> noteHeadMap = noteMap.headMap(10 * pointerLayout.row + column);
        for (int i = pointerLayout.row; i > 0; i--) {
            if ((preUnitNote = noteHeadMap.get(10 * i + column)) != null) {
                preKey = 10 * i + column;
                break;
            }
        }

        if (preUnitNote != null && preUnitNote.start < pointerLayout.row && preUnitNote.goal >= pointerLayout.row) {
            /*
             * ホールドの設置途中によらず、
             * preUnitNoteがホールドであり、そのホールドがポインターが示す行番号を跨ぐ場合、
             * 譜面のレイアウトとノートの全情報を格納したマップからそれを削除
             */
            removeNoteView(preUnitNote);
            removedNotes.add(preUnitNote.copy(mainActivity));
            noteMap.remove(preKey);
        } else if (holdEdge[column] == 0) {
            if (noteMap.containsKey(10 * pointerLayout.row + column)) {
                /*
                 * ホールドの設置途中ではなく、引数の列番号におけるポインターの示す行番号で単ノートがある場合、
                 * 譜面のレイアウトとノートの全情報を格納したマップからその単ノートを削除
                 */
                removeNoteView(noteMap.get(10 * pointerLayout.row + column));
                UnitNote unitNote = noteMap.remove(10 * pointerLayout.row + column);
                removedNotes.add(unitNote.copy(mainActivity));
            } else if (isLongClicked) {
                /*
                 * ホールドの設置途中ではなく、引数の列番号におけるポインターの示す行番号で単ノートがなく、長押しした場合、
                 * まずホールドの1点目の情報を更新して、ホールドの1点目の情報をセットする
                 */
                holdEdge[column] = pointerLayout.row;

                /*
                 * ポインターの示す行番号でのホールドの1点目を、譜面のレイアウトとノートの全情報を格納したマップに追加
                 * 追加するホールドの1点目は、単ノート追加とは違い、「元に戻す」ボタンで元に戻さないようにする
                 */
                UnitNote unitNote = new UnitNote(mainActivity, column, pointerLayout.row);
                addNoteView(mainActivity, unitNote);
                noteMap.put(10 * pointerLayout.row + column, unitNote);

                // 追加したノートのイメージビューの下にある設置済ノートのイメージビューが下敷きになっている場合、そのノートのイメージビューを再設置する
                layAddedNotes(mainActivity, pointerLayout.row, column);
            } else {
                /*
                 * ホールドの設置途中ではなく、引数の列番号におけるポインターの示す行番号で単ノートがなく、長押ししていない場合、
                 * ポインターの示す行番号での単ノートorホールドの1点目を、譜面のレイアウトとノートの全情報を格納したマップに追加
                 */

                // ポインターの示す行番号での単ノートorホールドの1点目を、譜面のレイアウトとノートの全情報を格納したマップに追加
                UnitNote unitNote = new UnitNote(mainActivity, column, pointerLayout.row);
                addNoteView(mainActivity, unitNote);
                noteMap.put(10 * pointerLayout.row + column, unitNote);
                addedNotes.add(unitNote.copy(mainActivity));

                // 追加したノートのイメージビューの下にある設置済ノートのイメージビューが下敷きになっている場合、そのノートのイメージビューを再設置する
                layAddedNotes(mainActivity, pointerLayout.row, column);
            }
        } else {
            if (holdEdge[column] < pointerLayout.row) {
                /*
                 * ホールドの設置途中であり、ホールドの1点目が始点、2点目が終点の場合、
                 * ホールドの2点間に何か単ノートorホールドの始点が存在する場合は削除
                 */
                for (int key = 10 * holdEdge[column] + column; key < 10 * pointerLayout.row + column + 1; key += 10) {
                    if (noteMap.containsKey(key)) {
                        removeNoteView(noteMap.get(key));
                        UnitNote unitNote = noteMap.remove(key);
                        // ホールドの1点目の情報を元に戻さないようにする
                        if (key != 10 * holdEdge[column] + column) {
                            removedNotes.add(unitNote.copy(mainActivity));
                        }
                    }
                }

                // ホールドの始点の行番号を保持して、0に戻す
                int start = holdEdge[column];
                holdEdge[column] = 0;

                // ホールドの始点の行番号とポインターの示す行番号から、ホールドを譜面のレイアウトとノートの全情報を格納したマップに追加
                UnitNote unitNote = new UnitNote(mainActivity, column, start, pointerLayout.row);
                addNoteView(mainActivity, unitNote);
                noteMap.put(10 * start + column, unitNote);
                addedNotes.add(unitNote.copy(mainActivity));

                // 追加したホールドのイメージビューの下にある設置済ノートのイメージビューが下敷きになっている場合、そのノートのイメージビューを再設置する
                layAddedNotes(mainActivity, pointerLayout.row, column);

                // ホールドの2点目を設置したかどうかのフラグをONにする
                isSetHold = true;
            } else if (holdEdge[column] > pointerLayout.row) {
                /*
                 * ホールドの設置途中であり、ホールドの1点目が終点、2点目が始点の場合、
                 * ホールドの2点間に何か単ノートorホールドの始点が存在する場合は削除
                 */
                for (int key = 10 * pointerLayout.row + column; key < 10 * holdEdge[column] + column + 1; key += 10) {
                    if (noteMap.containsKey(key)) {
                        removeNoteView(noteMap.get(key));
                        UnitNote unitNote = noteMap.remove(key);
                        // ホールドの1点目の情報を元に戻さないようにする
                        if (key != 10 * holdEdge[column] + column) {
                            removedNotes.add(unitNote.copy(mainActivity));
                        }
                    }
                }

                // ホールドの終点の行番号を保持して、0に戻す
                int goal = holdEdge[column];
                holdEdge[column] = 0;

                // ホールドの終点の行番号とポインターの示す行番号から、ホールドを譜面のレイアウトとノートの全情報を格納したマップに追加
                UnitNote unitNote = new UnitNote(mainActivity, column, pointerLayout.row, goal);
                addNoteView(mainActivity, unitNote);
                noteMap.put(10 * pointerLayout.row + column, unitNote);
                addedNotes.add(unitNote.copy(mainActivity));

                // 追加したホールドのイメージビューの下にある設置済ノートのイメージビューが下敷きになっている場合、そのノートのイメージビューを再設置する
                layAddedNotes(mainActivity, goal, column);

                // ホールドの2点目を設置したかどうかのフラグをONにする
                isSetHold = true;
            }
            // ホールドの設置途中であり、1点目と2点目が等しい場合は何もしない
        }

        // ホールドの1点目を設置した場合のみ、以下の動作を行わない
        if (!addedNotes.isEmpty() || !removedNotes.isEmpty()) {
            // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
            chartLayout.undoProcessStack.push(new UnitProcess(addedNotes, removedNotes));
            // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
            if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
                mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(View.VISIBLE);
            }
            // 「やり直し」ボタンを非表示にする
            mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
            // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
            chartLayout.redoProcessStack.clear();

            // 譜面編集回数のインクリメント
            chartLayout.editCount++;
            // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
            MainCommonFunctions.updateFileTextViewAtActionBar();

            /*
             * ホールド2点目を設置した場合、以下のボタンを表示する
             * ・譜面選択モードON/OFF
             * ・貼り付け
             * ・ブロック追加
             * ・ブロック削除
             * ・UCSファイル上書き保存
             * ・UCSファイル別ディレクトリで保存
             */
            if (isSetHold) {
                if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
                    buttonsLayout.toggleButtonEditSelect.setVisibility(VISIBLE);
                    buttonsLayout.buttonEditPaste.setVisibility(VISIBLE);
                } else if (buttonsLayout.spinner.getSelectedItemPosition() == 2) {
                    buttonsLayout.buttonBlockAdd.setVisibility(VISIBLE);
                    buttonsLayout.buttonBlockDelete.setVisibility(VISIBLE);
                } else if (buttonsLayout.spinner.getSelectedItemPosition() == 3) {
                    buttonsLayout.buttonFileSave.setVisibility(VISIBLE);
                    buttonsLayout.buttonFileSaveAs.setVisibility(VISIBLE);
                } else if (buttonsLayout.spinner.getSelectedItemPosition() == 4) {
                    buttonsLayout.toggleButtonOtherNoteSound.setVisibility(VISIBLE);
                    buttonsLayout.buttonOtherPlayInitially.setVisibility(VISIBLE);
                    buttonsLayout.buttonOtherPlayCurrently.setVisibility(VISIBLE);
                }
            }
        } else {
            /*
             * ホールドの1点目を設置した場合は、以下のボタンを非表示にする
             * ・譜面選択モードON/OFF
             * ・貼り付け
             * ・ブロック追加
             * ・ブロック削除
             * ・UCSファイル上書き保存
             * ・UCSファイル別ディレクトリで保存
             */
            buttonsLayout.toggleButtonEditSelect.setVisibility(GONE);
            buttonsLayout.buttonEditPaste.setVisibility(GONE);
            buttonsLayout.buttonBlockAdd.setVisibility(GONE);
            buttonsLayout.buttonBlockDelete.setVisibility(GONE);
            buttonsLayout.buttonFileSave.setVisibility(GONE);
            buttonsLayout.buttonFileSaveAs.setVisibility(GONE);
            buttonsLayout.toggleButtonOtherNoteSound.setVisibility(GONE);
            buttonsLayout.buttonOtherPlayInitially.setVisibility(GONE);
            buttonsLayout.buttonOtherPlayCurrently.setVisibility(GONE);
        }
    }

    /**
     * 追加したホールドのビューの下にある設置済ノートのビューが下敷きになっている場合、
     * そのノートのビューを下敷きにさせるように設置し直す
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param row ノートの行番号orホールドの終点の行番号
     * @param column ノートの行番号orホールドの終点の列番号
     */
    public void layAddedNotes(MainActivity mainActivity, int row, byte column) {
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        // 指定したしたノートorホールドの終点が示すブロックの位置情報を取得
        ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(row);

        // もう一度追加し直すためのpx単位での長さをセット
        float corLength = chartLayout.noteLength;
        if (information.offsetRowLength + chartLayout.blockList.get(information.idxBlock).rowLength == row) {
            if (information.idxBlock != chartLayout.blockList.size() - 1) {
                information.offsetRowLength += chartLayout.blockList.get(information.idxBlock).rowLength;
                information.idxBlock++;
                corLength = 2 * chartLayout.noteLength * chartLayout.zoom / (chartLayout.blockList.get(information.idxBlock).split + 1);
            }
        } else {
            corLength = 2 * chartLayout.noteLength * chartLayout.zoom / (chartLayout.blockList.get(information.idxBlock).split + 1);
        }

        /*
         * ノートorホールドのイメージビューを追加し直す
         * 追加し直したノートのイメージビューが新たに下敷きになっている場合もあるので、
         * 追加し直した後は、もう一度追加し直すためのpx単位での長さを0fにリセットすること
         */
        for (int corRow = 1; corLength < chartLayout.noteLength; corRow++) {
            UnitNote corNote;
            if ((corNote = noteMap.get(10 * (row + corRow) + column)) != null) {
                removeNoteView(corNote);
                addNoteView(mainActivity, corNote);
                corLength = 0f;
            }

            // ブロックの終端かどうかチェックし、上記の長さを更新する
            if (information.offsetRowLength + chartLayout.blockList.get(information.idxBlock).rowLength == row + corRow) {
                information.offsetRowLength += chartLayout.blockList.get(information.idxBlock).rowLength;
                information.idxBlock++;
            }
            corLength += 2 * chartLayout.noteLength * chartLayout.zoom / (chartLayout.blockList.get(information.idxBlock).split + 1);
        }
    }
}
