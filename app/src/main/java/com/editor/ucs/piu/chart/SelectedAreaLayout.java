package com.editor.ucs.piu.chart;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.buttons.ButtonsLayout;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.main.MainCommonFunctions;
import com.editor.ucs.piu.unit.UnitBlock;
import com.editor.ucs.piu.unit.UnitProcess;
import com.editor.ucs.piu.unit.UnitNote;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 選択領域のレイアウトを表すクラス
 */
public final class SelectedAreaLayout extends FrameLayout {
    // デバッグ用のタグ
    private static final String TAG = "SelectedAreaLayout";

    /**
     * コピーされた選択領域の行の長さを格納する値
     * 1度もコピーされていない場合は0を格納する
     */
    public int copiedLength = 0;
    
    /**
     * 選択領域の始点、終点の行番号を表す、以下に定義するような2つの要素を持つ配列
     * 	・2つとも要素が0						: 選択領域が存在しないとき
     * 	・0番目の要素が1以上、1番目の要素が0	: 選択選択モードでもう1点を選ばせるとき
     * 	・2つとも要素が1以上					: 異なる2点をつなぐ選択領域であるとき
     */
    public int[] selectedEdge = new int[2];
    
    /**
     * コピーされたノートの全情報を格納するマップ
     * (キーの値) := 10 * (選択領域の始点を0とした行番号) + (列番号)
     * NOTE : ノートの位置の一意性は、「始点の行番号」と「列番号」との直積により表現可能
     */
    private SortedMap<Integer, UnitNote> copiedNoteMap = new TreeMap<>();
    
    /**
     * 確認ダイアログのビューでの枠線の長さ(px単位)
     */
    private float frameLength;
    /**
     * 確認ダイアログのビューでの正方形のノートの1辺の長さ(px単位)
     */
    private float noteLength;

    public SelectedAreaLayout(@NonNull Context context) {
        super(context);
    }

    public SelectedAreaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectedAreaLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * ノート貼り付け時の確認ダイアログでのブロック・ノートのレイアウトをリセットする
     * 倍率は「x1」固定であり、ブロックの色は偶数個数のブロックの色と一致する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param copiedChartView 確認ダイアログのビュー
     */
    public void reset(MainActivity mainActivity, View copiedChartView) {
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        // 確認ダイアログでのブロック・ノートのレイアウトを取得
        FrameLayout noteLayout = copiedChartView.findViewById(R.id.copiedChartNoteLayout);
        FrameLayout blockLayout = copiedChartView.findViewById(R.id.copiedChartBlockLayout);
        blockLayout.removeAllViews();

        // ノートのレイアウトにノーツが存在する場合は、それらをすべて削除する
        if (noteLayout.getChildCount() > 0) {
            noteLayout.removeAllViews();
        }
        // ノートのレイアウトにノーツが存在しない場合は初期化処理を行う
        else {
            // 確認ダイアログのビューでの枠線の長さを1dpとし、それをpx単位へと変換
            frameLength = copiedChartView.getResources().getDisplayMetrics().density;

            /*
             * 正方形であるノートの1辺の長さをpx単位で計算
             * noteLength := ((確認ダイアログのボタンのレイアウトの横幅) - 9 * (枠線の長さ)) / 10
             * NOTE : view.getWidth()はMainActivityのonCreate()時に呼び出されると0の数値を返してしまう
             */
            View buttonsLayout = copiedChartView.findViewById(R.id.copiedChartButtonsLayout);
            buttonsLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            noteLength = (buttonsLayout.getMeasuredWidth() - 9 * frameLength) / 10;
            // ログ出力
            Log.d(TAG, "reset:noteLength=" + noteLength);
        }

        // ブロックのレイアウトに設置する下地のブロックをセット
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        for (byte column = 0; column < chartLayout.columnSize; column++) {
            // 1列ブロックを1列ごとに生成し、長さ・マージン・色を決定
            FrameLayout clmLayout = new FrameLayout(mainActivity);
            LayoutParams unitBlockRowParams = new LayoutParams((int) noteLength, (int) (noteLength * copiedLength - 2 * frameLength));
            unitBlockRowParams.setMargins((int) (column * (noteLength + frameLength)), (int) frameLength, 0, 0);
            clmLayout.setBackgroundColor(Color.rgb(
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_RED, 96),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_GREEN, 48),
                    sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_BLUE, 0)));
            // 1列ブロックをブロックのレイアウトに追加
            blockLayout.addView(clmLayout, unitBlockRowParams);
        }

        // ブロックのレイアウトにノートのレイアウトを追加
        blockLayout.addView(noteLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // ノートのレイアウトにノートを追加
        for (Map.Entry<Integer, UnitNote> entry : copiedNoteMap.entrySet()) {
            // ログ出力
            Log.d(TAG, "reset:key=" + entry.getKey());

            /*
             * ノートのインスタンスを新たに生成
             * NOTE : 譜面のレイアウトに既に該当ノートが設置している旨のIllegalStateExceptionがスローされるのを防ぐため
             */
            UnitNote unitNote = new UnitNote(mainActivity, entry.getValue().column, entry.getValue().start, entry.getValue().goal);

            // 追加する単ノートorホールドの始点のイメージビューの左座標を計算
            float left = (unitNote.column + chartLayout.columnSize - 10) * (noteLength + frameLength);

            // 単ノートorホールドの始点のイメージビューの上座標を計算
            float top = noteLength * (entry.getKey() / 10);

            if (unitNote.start != unitNote.goal) {
                // ホールドの場合は、そのpx単位での長さを決定
                float holdHeight = noteLength * (unitNote.goal - unitNote.start);

                // ホールドの終点のイメージビューをノートのレイアウトに追加
                LayoutParams params = new LayoutParams((int) noteLength, (int) noteLength);
                params.setMargins((int) left, (int) (top + holdHeight), 0, 0);
                noteLayout.addView(unitNote.goalView, params);

                // ホールドの始点と終点との間のイメージビューをノートのレイアウトに追加
                params = new LayoutParams((int) noteLength, (int) holdHeight);
                params.setMargins((int) left, (int) (top + noteLength / 2), 0, 0);
                noteLayout.addView(unitNote.holdView, params);
            }

            // 単ノートorホールドの始点のイメージビューをノートのレイアウトに追加
            LayoutParams params = new LayoutParams((int) noteLength, (int) noteLength);
            params.setMargins((int) left, (int) top, 0, 0);
            noteLayout.addView(unitNote.startView, params);
        }
    }

    /**
     * 選択領域のレイアウトの位置を更新する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void update(MainActivity mainActivity) {
        // ログ出力
        Log.d(TAG, "update:selectedEdge[0]=" + selectedEdge[0]);
        Log.d(TAG, "update:selectedEdge[1]=" + selectedEdge[1]);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        if (selectedEdge[0] > 0) {
            // 選択領域の始点での位置情報を計算
            ChartLayout.PositionInformation start = chartLayout.calcPositionInformation(selectedEdge[0]);

            // 選択領域の縦幅を計算
            float selectedAreaHeight = chartLayout.noteLength;
            // 選択領域の終点が指定されている場合は、そこの位置情報を計算
            if (selectedEdge[1] > 0) {
                ChartLayout.PositionInformation goal = chartLayout.calcPositionInformation(selectedEdge[1]);
                selectedAreaHeight += goal.coordinate - start.coordinate;
            }

            // このレイアウトを譜面のレイアウトから削除し、レイアウトの大きさと位置をセットして再セット
            chartLayout.removeView(this);
            LayoutParams params = new LayoutParams((int) (chartLayout.columnSize * chartLayout.noteLength + (chartLayout.columnSize - 1) * chartLayout.frameLength), (int) selectedAreaHeight);
            params.setMargins((chartLayout.columnSize == 5) ? (int) (5 * chartLayout.noteLength + 4 * chartLayout.frameLength) : 0, (int) start.coordinate, 0, 0);
            chartLayout.addView(this, params);

            /*
             * ノートのレイアウトの更新
             * これより、選択領域のレイアウトの上にノートのビューがセットしているように見せられる
             */
            noteLayout.update(mainActivity);

            // 選択領域のレイアウトを表示にする
            setVisibility(VISIBLE);
        } else {
            // 選択領域のレイアウトを非表示にする
            setVisibility(GONE);
        }
    }

    /**
     * 選択領域の始点、終点を変更する
     * 
     * @param mainActivity メイン画面のアクティビティ
     */
    public void changeSelectedEdge(MainActivity mainActivity) {
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);

        // ボタン群のレイアウトに表示するボタンの種別を示すスピナーを取得
        Spinner spinner = ((ButtonsLayout) mainActivity.findViewById(R.id.buttonsLayout)).spinner;

        if (selectedEdge[0] > 0 && selectedEdge[1] == 0) {
            // 選択領域の1点が既に決まっている場合、選んだもう1点を繋げるか、既に決まっている1点の場所を削除する
            if (selectedEdge[0] < pointerLayout.row) {
                selectedEdge[1] = pointerLayout.row;
            } else if (selectedEdge[0] > pointerLayout.row) {
                selectedEdge[1] = selectedEdge[0];
                selectedEdge[0] = pointerLayout.row;
            } else {
                selectedEdge[0] = 0;
            }
        } else {
            // 選んだ選択領域の1点のみ更新する
            selectedEdge[0] = pointerLayout.row;
            selectedEdge[1] = 0;
        }

        // スピナーが「編集」になっていて、選択されているノートが存在するとき、選択ノートを編集するボタンを表示し、それ以外は非表示にする
        if (spinner.getSelectedItemPosition() == 1) {
            if (selectedEdge[1] > 0) {
                mainActivity.findViewById(R.id.buttonEditCut).setVisibility(View.VISIBLE);
                mainActivity.findViewById(R.id.buttonEditCopy).setVisibility(View.VISIBLE);
                mainActivity.findViewById(R.id.buttonEditDelete).setVisibility(View.VISIBLE);
                mainActivity.findViewById(R.id.buttonEditUpDown).setVisibility(View.VISIBLE);
                mainActivity.findViewById(R.id.buttonEditLeftRight).setVisibility(View.VISIBLE);
            } else {
                mainActivity.findViewById(R.id.buttonEditCut).setVisibility(View.GONE);
                mainActivity.findViewById(R.id.buttonEditCopy).setVisibility(View.GONE);
                mainActivity.findViewById(R.id.buttonEditDelete).setVisibility(View.GONE);
                mainActivity.findViewById(R.id.buttonEditUpDown).setVisibility(View.GONE);
                mainActivity.findViewById(R.id.buttonEditLeftRight).setVisibility(View.GONE);
            }
        }

        // 選択領域のレイアウトを更新する
        update(mainActivity);
        // ポインターのレイアウトを更新する
        pointerLayout.update(mainActivity);
    }

    /**
     * 選択領域に存在するノートをコピーする
     * コピーしたノートの情報はファイルが新しく変更されても保持される
     * ホールドの終端が選択領域の終点を突き抜けている場合はコピーしない
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void copySelectedNotes(MainActivity mainActivity) {
        // ログ出力
        Log.d(TAG, "copySelectedNotes:selectedEdge[0]=" + selectedEdge[0]);
        Log.d(TAG, "copySelectedNotes:selectedEdge[1]=" + selectedEdge[1]);

        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        
        // コピーされたノートの全情報を格納したマップの情報をすべて消去する
        copiedNoteMap.clear();

        // 選択領域の長さをコピーする
        copiedLength = selectedEdge[1] - selectedEdge[0] + 1;

        // ホールドの終端が選択領域の終点を突き抜けているかどうかを判断し、選択領域に存在するノートの情報を1つずつコピーする
        SortedMap<Integer, UnitNote> selectedNotesMap = noteLayout.noteMap.subMap(selectedEdge[0] * 10, (selectedEdge[1] + 1) * 10);
        for (Map.Entry<Integer, UnitNote> entry : selectedNotesMap.entrySet()) {
            if (entry.getValue().goal <= selectedEdge[1]) {
                copiedNoteMap.put(entry.getKey() - selectedEdge[0] * 10, entry.getValue());
            }
        }
    }

    /**
     * ポインターの示す場所からコピーしたノートを貼り付け、貼り付けた範囲を選択領域として表示する
     * ポインターの示す場所にホールドを含む場合は、そのホールドを削除する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void pasteCopiedNotes(MainActivity mainActivity) {
        // ログ出力
        Log.d(TAG, "pasteCopiedNotes:selectedEdge[0]=" + selectedEdge[0]);
        Log.d(TAG, "pasteCopiedNotes:selectedEdge[1]=" + selectedEdge[1]);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);

        // 追加したノートの集合
        Set<UnitNote> addedNotes = new HashSet<>();
        // 削除したノートの集合
        Set<UnitNote> removedNotes = new HashSet<>();

        /*
         * 貼り付ける範囲の始点を求める
         * 貼り付ける範囲が最下段を突き抜けないかどうかをチェックする
         */
        int pasteRow = 0;
        for (UnitBlock unitBlock : chartLayout.blockList) {
            pasteRow += unitBlock.rowLength;
        }
        if (pointerLayout.row + copiedLength > pasteRow) {
            pasteRow -= copiedLength;
        } else {
            pasteRow = pointerLayout.row;
        }

        // 貼り付ける範囲のノートをすべて削除する
        SortedMap<Integer, UnitNote> removalNotesMap = new TreeMap<>(noteLayout.noteMap.subMap(pasteRow * 10, (pasteRow + copiedLength) * 10));
        for (Map.Entry<Integer, UnitNote> entry : removalNotesMap.entrySet()) {
            noteLayout.removeNoteView(entry.getValue());
            removedNotes.add(entry.getValue().copy(mainActivity));
            noteLayout.noteMap.remove(entry.getKey());
        }

        // 貼り付ける範囲の始点にホールドを含む場合は、そのホールドを削除する
        for (int i = 10 - chartLayout.columnSize; i < 10; i++) {
            for (int preRow = pasteRow - 1; preRow > 0; preRow--) {
                UnitNote preNote;
                if ((preNote = noteLayout.noteMap.get(preRow * 10 + i)) != null) {
                    if (pasteRow <= preNote.goal) {
                        noteLayout.removeNoteView(preNote);
                        removedNotes.add(preNote.copy(mainActivity));
                        noteLayout.noteMap.remove(preRow * 10 + i);
                    }
                    break;
                }
            }
        }

        // コピー済のノートを貼り付ける
        for (Map.Entry<Integer, UnitNote> entry : copiedNoteMap.entrySet()) {
            UnitNote unitNote = new UnitNote(mainActivity, entry.getValue().column, entry.getKey() / 10 + pasteRow, entry.getKey() / 10 + entry.getValue().goal - entry.getValue().start + pasteRow);
            noteLayout.addNoteView(mainActivity, unitNote);
            noteLayout.noteMap.put(unitNote.start * 10 + unitNote.column, unitNote);
            addedNotes.add(unitNote.copy(mainActivity));
        }

        // 選択領域の始点・終点の変更
        selectedEdge[0] = pasteRow;
        selectedEdge[1] = pasteRow + copiedLength - 1;

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedNotes, removedNotes));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (((ButtonsLayout) mainActivity.findViewById(R.id.buttonsLayout)).spinner.getSelectedItemPosition() == 1) {
            mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(View.VISIBLE);
        }
        // 「やり直し」ボタンを非表示にする
        mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
        // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
        chartLayout.redoProcessStack.clear();

        // 選択領域のレイアウトの位置を更新
        update(mainActivity);
        // ポインターのレイアウトの位置を更新
        pointerLayout.update(mainActivity);

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        MainCommonFunctions.updateFileTextViewAtActionBar();
    }

    /**
     * 選択領域に存在するノートを譜面のレイアウトから削除する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void removeSelectedNotes(MainActivity mainActivity) {
        // ログ出力
        Log.d(TAG, "removeSelectedNotes:selectedEdge[0]=" + selectedEdge[0]);
        Log.d(TAG, "removeSelectedNotes:selectedEdge[1]=" + selectedEdge[1]);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        // 削除したノートの集合
        Set<UnitNote> removedNotes = new HashSet<>();

        // 選択領域のノートをすべて削除する
        SortedMap<Integer, UnitNote> selectedNotesMap = new TreeMap<>(noteLayout.noteMap.subMap(selectedEdge[0] * 10, (selectedEdge[1] + 1) * 10));
        for (Map.Entry<Integer, UnitNote> entry : selectedNotesMap.entrySet()) {
            noteLayout.removeNoteView(entry.getValue());
            removedNotes.add(entry.getValue().copy(mainActivity));
            noteLayout.noteMap.remove(entry.getKey());
        }

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(null, removedNotes));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (((ButtonsLayout) mainActivity.findViewById(R.id.buttonsLayout)).spinner.getSelectedItemPosition() == 1) {
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
    }

    /**
     * 選択領域に存在するノートを上下/左右方向に回転する
     * ホールドの始点・終点の両方が選択領域内に存在するときは、そのホールドの回転を行う
     * 逆にどちらか一方存在しないときはホールドを回転せず、回転してきたノートがそのホールドと被った場合は、そのホールドを削除する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param direction 回転方向
     */
    public void rotateSelectedArea(MainActivity mainActivity, RotationDirection direction) {
        // ログ出力
        Log.d(TAG, "rotateSelectedArea:selectedEdge[0]=" + selectedEdge[0]);
        Log.d(TAG, "rotateSelectedArea:selectedEdge[1]=" + selectedEdge[1]);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        // 追加したノートの集合
        Set<UnitNote> addedNotes = new HashSet<>();
        // 削除したノートの集合
        Set<UnitNote> removedNotes = new HashSet<>();

        // 回転対象のノートのインスタンスのマップを取得
        Map<Integer, UnitNote> rotatedNotesMap = new TreeMap<>();
        for (Map.Entry<Integer, UnitNote> entry : new TreeMap<>(noteLayout.noteMap.subMap(selectedEdge[0] * 10, (selectedEdge[1] + 1) * 10)).entrySet()) {
            if (entry.getValue().goal <= selectedEdge[1]) {
                // 選択領域内に存在する単ノート/ホールドを譜面のレイアウトから削除
                UnitNote unitNote = noteLayout.noteMap.remove(entry.getKey());
                noteLayout.removeNoteView(unitNote);
                removedNotes.add(unitNote.copy(mainActivity));

                // 上下/左右方向に回転する
                switch (direction) {
                    case UP_DOWN:
                        switch (unitNote.column % 5) {
                            case 0:
                            case 3:
                                unitNote.column++;
                                break;
                            case 1:
                            case 4:
                                unitNote.column--;
                                break;
                        }
                        break;
                    case LEFT_RIGHT:
                        unitNote.column = (byte) ((chartLayout.columnSize == 5) ? 14 - unitNote.column : 9 - unitNote.column);
                        break;
                }
                rotatedNotesMap.put(unitNote.start * 10 + unitNote.column, unitNote);
            }
        }

        // 左右回転したノートが選択領域内に存在しないホールドと被ったかどうかチェックし、譜面のレイアウトに追加する
        for (Map.Entry<Integer, UnitNote> entry : rotatedNotesMap.entrySet()) {
            // 上方向の行のチェック
            for (int beforeRow = entry.getValue().start - 1; beforeRow > 0; beforeRow--) {
                UnitNote beforeNote;
                if ((beforeNote = noteLayout.noteMap.get(beforeRow * 10 + entry.getValue().column)) != null) {
                    if (entry.getValue().start <= beforeNote.goal) {
                        noteLayout.noteMap.remove(beforeNote.start * 10 + beforeNote.column);
                        removedNotes.add(beforeNote.copy(mainActivity));
                        noteLayout.removeNoteView(beforeNote);
                    }
                    break;
                }
            }

            // 下方向の行のチェック
            int totalRow = 0;
            for (UnitBlock unitBlock : chartLayout.blockList) {
                totalRow += unitBlock.rowLength;
            }
            for (int afterRow = entry.getValue().start + 1; afterRow < totalRow + 1; afterRow++) {
                UnitNote afterNote;
                if ((afterNote = noteLayout.noteMap.get(afterRow * 10 + entry.getValue().column)) != null) {
                    if (afterNote.start <= entry.getValue().goal) {
                        noteLayout.noteMap.remove(afterNote.start * 10 + afterNote.column);
                        removedNotes.add(afterNote.copy(mainActivity));
                        noteLayout.removeNoteView(afterNote);
                    }
                    break;
                }
            }

            // 左右回転したノートを譜面のレイアウトに追加
            noteLayout.addNoteView(mainActivity, entry.getValue());
            noteLayout.noteMap.put(entry.getKey(), entry.getValue());
            addedNotes.add(entry.getValue().copy(mainActivity));
        }

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedNotes, removedNotes));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (((ButtonsLayout) mainActivity.findViewById(R.id.buttonsLayout)).spinner.getSelectedItemPosition() == 1) {
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
    }

    /**
     * ノート貼り付け時の確認ダイアログ上にある上下、左右回転ボタンで動作するすべてのノートの回転動作を行う
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param copiedChartView 確認ダイアログのビュー
     * @param direction 回転方向
     */
    public void rotateCopiedNotes(MainActivity mainActivity, View copiedChartView, RotationDirection direction) {
        // ログ出力
        Log.d(TAG, "rotateCopiedNotes:direction=" + direction);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        for (Map.Entry<Integer, UnitNote> entry : copiedNoteMap.entrySet()) {
            UnitNote unitNote = entry.getValue();

            // 上下、左右回転を行う
            switch (direction) {
                case UP_DOWN:
                    switch (unitNote.column % 5) {
                        case 0:
                        case 3:
                            unitNote.column++;
                            break;
                        case 1:
                        case 4:
                            unitNote.column--;
                            break;
                    }
                    break;
                case LEFT_RIGHT:
                    unitNote.column = (byte) ((chartLayout.columnSize == 5) ? 14 - unitNote.column : 9 - unitNote.column);
                    break;
            }

            // 回転前のノートのデータを更新
            copiedNoteMap.put(entry.getKey(), unitNote);
        }

        // ブロック・ノートのレイアウトを更新する
        reset(mainActivity, copiedChartView);
    }

    /**
     * 譜面の回転方向を指定する列挙型
     */
    public enum RotationDirection {
        /**
         * 上下回転
         */
        UP_DOWN,

        /**
         * 左右回転
         */
        LEFT_RIGHT,
    }
}
