package com.editor.ucs.piu.unit;

import android.util.Log;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.NoteLayout;
import com.editor.ucs.piu.chart.PointerLayout;
import com.editor.ucs.piu.chart.SelectedAreaLayout;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.main.MainCommonFunctions;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * ユーザによる1回分の譜面編集プロセスを表すクラス
 */
public class UnitProcess {
    // デバッグ用のタグ
    private static final String TAG = "UnitProcess";

    /**
     * 1回分の譜面編集プロセス内で、ノートのレイアウトに追加したノートの集合
     */
    private Set<UnitNote> addedNotes = new HashSet<>();

    /**
     * 1回分の譜面編集プロセス内で、ノートのレイアウトから削除したノートの集合
     */
    private Set<UnitNote> removedNotes = new HashSet<>();

    /**
     * 1回分の譜面編集プロセス内で、ノートのレイアウトに追加した譜面のブロックのマップ
     * (キーの値) := 追加した後での譜面のブロックの添え字番号
     */
    private NavigableMap<Integer, UnitBlock> addedBlockMap = new TreeMap<>();

    /**
     * 1回分の譜面編集プロセス内で、ノートのレイアウトから削除した譜面のブロックのマップ
     * (キーの値) := 削除する前での譜面のブロックの添え字番号
     */
    private NavigableMap<Integer, UnitBlock> removedBlockMap = new TreeMap<>();

    /**
     * コンストラクタ
     *
     * @param addedNotes   ノートのレイアウトに追加したノートのビューの集合
     * @param removedNotes ノートのレイアウトから削除したノートのビューの集合
     */
    public UnitProcess(Set<UnitNote> addedNotes, Set<UnitNote> removedNotes) {
        if (addedNotes != null) {
            this.addedNotes.addAll(addedNotes);
        }
        if (removedNotes != null) {
            this.removedNotes.addAll(removedNotes);
        }
    }

    /**
     * コンストラクタ
     *
     * @param addedBlockMap   ノートのレイアウトに追加した譜面のブロックのマップ
     * @param removedBlockMap ノートのレイアウトから削除した譜面のブロックのマップ
     */
    public UnitProcess(NavigableMap<Integer, UnitBlock> addedBlockMap, NavigableMap<Integer, UnitBlock> removedBlockMap) {
        if (addedBlockMap != null) {
            this.addedBlockMap.putAll(addedBlockMap);
        }
        if (removedBlockMap != null) {
            this.removedBlockMap.putAll(removedBlockMap);
        }
    }

    /**
     * コンストラクタ
     *
     * @param addedNotes    ノートのレイアウトに追加したノートのビューの集合
     * @param removedNotes  ノートのレイアウトから削除したノートのビューの集合
     * @param addedBlockMap ノートのレイアウトに追加した譜面のブロックのマップ
     * @param removedBlocks ノートのレイアウトから削除した譜面のブロックのマップ
     */
    public UnitProcess(Set<UnitNote> addedNotes, Set<UnitNote> removedNotes, NavigableMap<Integer, UnitBlock> addedBlockMap, NavigableMap<Integer, UnitBlock> removedBlocks) {
        if (addedNotes != null) {
            this.addedNotes.addAll(addedNotes);
        }
        if (removedNotes != null) {
            this.removedNotes.addAll(removedNotes);
        }
        if (addedBlockMap != null) {
            this.addedBlockMap.putAll(addedBlockMap);
        }
        if (removedBlocks != null) {
            this.removedBlockMap.putAll(removedBlocks);
        }
    }

    /**
     * この1回分の譜面編集プロセスに対して元に戻す
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void undo(MainActivity mainActivity) {
        // ログ出力
        Log.d(TAG, "undo:addedBlockMap.size=" + addedBlockMap.size());
        Log.d(TAG, "undo:removedBlockMap.size=" + removedBlockMap.size());
        Log.d(TAG, "undo:addedNotes.size=" + addedNotes.size());
        Log.d(TAG, "undo:removedNotes.size=" + removedNotes.size());

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // すべての追加した譜面のブロックを削除
        for (int idxBlock : addedBlockMap.descendingKeySet()) {
            chartLayout.removeBlockView(mainActivity, idxBlock);
            chartLayout.blockList.remove(idxBlock);
        }

        // すべての削除した譜面のブロックを追加
        for (NavigableMap.Entry<Integer, UnitBlock> entry : removedBlockMap.entrySet()) {
            chartLayout.blockList.add(entry.getKey(), entry.getValue());
            chartLayout.addBlockView(mainActivity, entry.getKey());
        }

        if (!addedBlockMap.isEmpty() || !removedBlockMap.isEmpty()) {
            // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
            MainCommonFunctions.updateColorColumnLayouts();

            // 終端の余白ブロックのレイアウトの更新
            chartLayout.updateLastView(mainActivity);

            // 選択領域のレイアウトの更新
            selectedAreaLayout.update(mainActivity);

            // ポインターのレイアウトの更新
            pointerLayout.update(mainActivity);
            // ポインターの位置が示すブロックの情報を更新
            MainCommonFunctions.updateTextsAtPointer();
        }

        // すべての追加したノートのビューを削除
        for (UnitNote addedNote : addedNotes) {
            UnitNote unitNote = noteLayout.noteMap.remove(10 * addedNote.start + addedNote.column);
            noteLayout.removeNoteView(unitNote);
        }

        // すべての削除したノートのビューを追加
        for (UnitNote removedNote : removedNotes) {
            noteLayout.noteMap.put(10 * removedNote.start + removedNote.column, removedNote);
            noteLayout.addNoteView(mainActivity, removedNote);
        }

        // 譜面編集回数のデクリメント
        chartLayout.editCount--;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        MainCommonFunctions.updateFileTextViewAtActionBar();
    }

    /**
     * この1回分の譜面編集プロセスに対してやり直す
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void redo(MainActivity mainActivity) {
        // ログ出力
        Log.d(TAG, "redo:addedBlockMap.size=" + addedBlockMap.size());
        Log.d(TAG, "redo:removedBlockMap.size=" + removedBlockMap.size());
        Log.d(TAG, "redo:addedNotes.size=" + addedNotes.size());
        Log.d(TAG, "redo:removedNotes.size=" + removedNotes.size());

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // すべての削除した譜面のブロックを削除
        for (int idxBlock : removedBlockMap.descendingKeySet()) {
            chartLayout.removeBlockView(mainActivity, idxBlock);
            chartLayout.blockList.remove(idxBlock);
        }

        // すべての追加した譜面のブロックを追加
        for (NavigableMap.Entry<Integer, UnitBlock> entry : addedBlockMap.entrySet()) {
            chartLayout.blockList.add(entry.getKey(), entry.getValue());
            chartLayout.addBlockView(mainActivity, entry.getKey());
        }

        if (!addedBlockMap.isEmpty() || !removedBlockMap.isEmpty()) {
            // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
            MainCommonFunctions.updateColorColumnLayouts();

            // 終端の余白ブロックのレイアウトの更新
            chartLayout.updateLastView(mainActivity);

            // 選択領域のレイアウトの更新
            selectedAreaLayout.update(mainActivity);

            // ポインターのレイアウトの更新
            pointerLayout.update(mainActivity);
            // ポインターの位置が示すブロックの情報を更新
            MainCommonFunctions.updateTextsAtPointer();
        }

        // すべての削除したノートのビューを削除
        for (UnitNote removedNote : removedNotes) {
            UnitNote unitNote = noteLayout.noteMap.remove(10 * removedNote.start + removedNote.column);
            noteLayout.removeNoteView(unitNote);
        }

        // すべての追加したノートのビューを追加
        for (UnitNote addedNote : addedNotes) {
            noteLayout.noteMap.put(10 * addedNote.start + addedNote.column, addedNote);
            noteLayout.addNoteView(mainActivity, addedNote);
        }

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        MainCommonFunctions.updateFileTextViewAtActionBar();
    }
}
