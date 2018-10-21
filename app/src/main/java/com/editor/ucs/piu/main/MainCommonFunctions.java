package com.editor.ucs.piu.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.buttons.ButtonsLayout;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.ChartScrollView;
import com.editor.ucs.piu.chart.NoteLayout;
import com.editor.ucs.piu.chart.PointerLayout;
import com.editor.ucs.piu.chart.SelectedAreaLayout;
import com.editor.ucs.piu.unit.UnitBlock;
import com.editor.ucs.piu.unit.UnitNote;
import com.editor.ucs.piu.unit.UnitProcess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.view.View.VISIBLE;

/**
 * メイン画面のアクティビティの共通動作を行う抽象クラス
 */
public abstract class MainCommonFunctions {
    // デバッグ用のタグ
    private static final String TAG = "MainCommonFunctions";

    /**
     * メイン画面のアクティビティ
     */
    static MainActivity mainActivity;

    /**
     * 「ブロック追加」のダイアログでの入力情報をもとに、ポインターが示している行の直後に譜面のブロックを新規追加する
     *
     * @param blockAddView 「ブロック追加」のダイアログのビュー
     * @param information  ポインターで示している行番号の位置情報
     */
    static void addBlockAtPointer(View blockAddView, ChartLayout.PositionInformation information) {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 追加したノートの集合
        Set<UnitNote> addedNotes = new HashSet<>();
        // 削除したノートの集合
        Set<UnitNote> removedNotes = new HashSet<>();
        // 追加した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> addedBlockMap = new TreeMap<>();
        // 削除した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> removedBlockMap = new TreeMap<>();

        // 変更後の情報を入力させるエディトテキストを取得
        EditText blockAddRowLength = blockAddView.findViewById(R.id.blockAddRowLength);
        EditText blockAddBpm = blockAddView.findViewById(R.id.blockAddBpm);
        EditText blockAddDelay = blockAddView.findViewById(R.id.blockAddDelay);
        EditText blockAddBeat = blockAddView.findViewById(R.id.blockAddBeat);
        EditText blockAddSplit = blockAddView.findViewById(R.id.blockAddSplit);

        // 行数を取得してチェック
        int rowLength;
        try {
            rowLength = Integer.parseInt(blockAddRowLength.getText().toString());
            if (rowLength == 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「行数」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_rowLength), Toast.LENGTH_SHORT).show();
            return;
        }

        // BPM値を取得してチェック
        float bpm;
        try {
            bpm = Float.parseFloat(blockAddBpm.getText().toString());
            if (bpm < 0.1f || bpm > 999.0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「BPM」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_bpm), Toast.LENGTH_SHORT).show();
            return;
        }

        // Delay値を取得してチェック
        float delay;
        try {
            delay = Float.parseFloat(blockAddDelay.getText().toString());
            if (delay < -999999.0f || delay > 999999.0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Delay」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_delay), Toast.LENGTH_SHORT).show();
            return;
        }

        // Beat値を取得してチェック
        byte beat;
        try {
            beat = Byte.parseByte(blockAddBeat.getText().toString());
            if (beat == 0 || beat > 64) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Beat」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_beat), Toast.LENGTH_SHORT).show();
            return;
        }

        // Split値を取得してチェック
        short split;
        try {
            split = Short.parseShort(blockAddSplit.getText().toString());
            if (split == 0 || split > 128) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Split」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_split), Toast.LENGTH_SHORT).show();
            return;
        }

        // 現在ポインターが示しているブロックの情報を取得
        UnitBlock upperBlock = chartLayout.blockList.get(information.idxBlock);

        /*
         * 追加するブロックを生成
         * NOTE : byteの最大値は127なので、Splitは実際の値から1だけ引いた値を格納
         */
        UnitBlock newBlock = new UnitBlock(bpm, delay, beat, (byte) (split - 1), rowLength);

        // 現在示しているポインター位置がブロックの最後尾ではない場合、その位置の下でブロックを分割する
        if (pointerLayout.row != information.offsetRowLength + chartLayout.blockList.get(information.idxBlock).rowLength) {
            // 現在ポインターで示されているブロックから、分割後の下側ブロックを生成
            UnitBlock lowerBlock = new UnitBlock(upperBlock.bpm, upperBlock.delay, upperBlock.beat, upperBlock.split, information.offsetRowLength + upperBlock.rowLength - pointerLayout.row);

            // 分割後の上側の譜面のブロックをセット
            removedBlockMap.put(information.idxBlock, upperBlock.copy());
            chartLayout.removeBlockView(mainActivity, information.idxBlock);
            upperBlock.rowLength -= lowerBlock.rowLength;
            chartLayout.blockList.set(information.idxBlock, upperBlock);
            chartLayout.addBlockView(mainActivity, information.idxBlock);
            addedBlockMap.put(information.idxBlock, upperBlock.copy());

            // 追加する譜面のブロックを挿入
            chartLayout.blockList.add(information.idxBlock + 1, newBlock);
            chartLayout.addBlockView(mainActivity, information.idxBlock + 1);
            addedBlockMap.put(information.idxBlock + 1, newBlock.copy());

            // 分割後の下側の譜面のブロックを挿入
            chartLayout.blockList.add(information.idxBlock + 2, lowerBlock);
            chartLayout.addBlockView(mainActivity, information.idxBlock + 2);
            addedBlockMap.put(information.idxBlock + 2, lowerBlock.copy());

            // 上記の譜面のブロックより後にある譜面のブロックのビューを更新
            for (int i = information.idxBlock + 3; i < chartLayout.blockList.size(); i++) {
                removedBlockMap.put(i - 2, chartLayout.blockList.get(i).copy());
                chartLayout.removeBlockView(mainActivity, i);
                chartLayout.addBlockView(mainActivity, i);
                addedBlockMap.put(i, chartLayout.blockList.get(i).copy());
            }
        } else {
            // 現在示しているポインター位置がブロックの最後尾の場合、追加する譜面のブロックのみ挿入
            chartLayout.blockList.add(information.idxBlock + 1, newBlock);
            chartLayout.addBlockView(mainActivity, information.idxBlock + 1);
            addedBlockMap.put(information.idxBlock + 1, newBlock.copy());

            // 上記の譜面のブロックより後にある譜面のブロックのビューを更新
            for (int i = information.idxBlock + 2; i < chartLayout.blockList.size(); i++) {
                removedBlockMap.put(i - 1, chartLayout.blockList.get(i).copy());
                chartLayout.removeBlockView(mainActivity, i);
                chartLayout.addBlockView(mainActivity, i);
                addedBlockMap.put(i, chartLayout.blockList.get(i).copy());
            }
        }

        // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
        updateColorColumnLayouts();

        // 終端の余白ブロックのレイアウトの更新
        chartLayout.updateLastView(mainActivity);

        // 追加した譜面のブロックから下にある全ノートを、その譜面のブロックの行数分だけ下へずらす
        SortedMap<Integer, UnitNote> tmpMap = new TreeMap<>();
        for (int key : new ArrayList<>(noteLayout.noteMap.tailMap(10 * (pointerLayout.row + 1)).keySet())) {
            // ずらす前のノートを削除
            UnitNote unitNote = noteLayout.noteMap.remove(key);
            noteLayout.removeNoteView(unitNote);
            removedNotes.add(unitNote.copy(mainActivity));
            // ノートの行数を変更してずらす
            unitNote.start += newBlock.rowLength;
            unitNote.goal += newBlock.rowLength;
            for (int i = 0; i < unitNote.hollowStartList.size(); i++) {
                unitNote.hollowStartList.set(i, unitNote.hollowStartList.get(i) + newBlock.rowLength);
                unitNote.hollowGoalList.set(i, unitNote.hollowGoalList.get(i) + newBlock.rowLength);
            }
            tmpMap.put(key + 10 * newBlock.rowLength, unitNote);
        }
        // ずらした後のノートを追加
        for (Map.Entry<Integer, UnitNote> entry : tmpMap.entrySet()) {
            noteLayout.noteMap.put(entry.getKey(), entry.getValue());
            noteLayout.addNoteView(mainActivity, entry.getValue());
            addedNotes.add(entry.getValue().copy(mainActivity));
        }

        // 選択領域のレイアウトの更新
        selectedAreaLayout.update(mainActivity);

        // ポインターのレイアウトの更新
        pointerLayout.update(mainActivity);

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedNotes, removedNotes, addedBlockMap, removedBlockMap));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
            mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(VISIBLE);
        }
        // 「やり直し」ボタンを非表示にする
        mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
        // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
        chartLayout.redoProcessStack.clear();

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
    }

    /**
     * 指定したucsファイル名が不正かどうかチェックする
     *
     * @param ucsFileName ucsファイル名(拡張子.ucsは含まない)
     * @return ucsファイル名が不正の場合はtrue、正常の場合はfalse
     */
    private static boolean checkUcsFileName(String ucsFileName) {
        /*
         * ucsファイル名が空文字である場合や、/、\、?、*、:、|、"、<、>の
         * いずれかの文字が含んでいる場合は、不正なucsファイル名とする
         */
        return ucsFileName.equals("") || ucsFileName.matches("^.*[/|\\\\|?|*|:|\\||\"|<|>].*$");
    }

    /**
     * 「ucsファイル新規作成」のダイアログでの入力情報をもとにucsファイルを新規作成する
     *
     * @param newUcsView 「ucsファイル新規作成」のダイアログのビュー
     */
    static void createNewUcsFile(View newUcsView) {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 入力したucsファイル名(拡張子.ucsは含まない)を取得し、不正かどうかチェック
        String ucsFileName = ((EditText) newUcsView.findViewById(R.id.newUcsFileName)).getText().toString();
        if (checkUcsFileName(ucsFileName)) {
            // 「ucsファイル名」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_fileName), Toast.LENGTH_SHORT).show();
            return;
        }

        // 行数を取得してチェック
        int rowLength;
        try {
            rowLength = Integer.parseInt(((EditText) newUcsView.findViewById(R.id.newUcsRowLength)).getText().toString());
            if (rowLength == 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「行数」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_rowLength), Toast.LENGTH_SHORT).show();
            return;
        }

        // BPM値を取得してチェック
        float bpm;
        try {
            bpm = Float.parseFloat(((EditText) newUcsView.findViewById(R.id.newUcsBpm)).getText().toString());
            if (bpm < 0.1f || bpm > 999.0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「BPM」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_bpm), Toast.LENGTH_SHORT).show();
            return;
        }

        // Delay値を取得してチェック
        float delay;
        try {
            delay = Float.parseFloat(((EditText) newUcsView.findViewById(R.id.newUcsDelay)).getText().toString());
            if (delay < -999999.0f || delay > 999999.0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Delay」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_delay), Toast.LENGTH_SHORT).show();
            return;
        }

        // Beat値を取得してチェック
        byte beat;
        try {
            beat = Byte.parseByte(((EditText) newUcsView.findViewById(R.id.newUcsBeat)).getText().toString());
            if (beat == 0 || beat > 64) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Beat」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_beat), Toast.LENGTH_SHORT).show();
            return;
        }

        // Split値を取得してチェック
        short split;
        try {
            split = Short.parseShort(((EditText) newUcsView.findViewById(R.id.newUcsSplit)).getText().toString());
            if (split == 0 || split > 128) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Split」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_split), Toast.LENGTH_SHORT).show();
            return;
        }

        // 入力した譜面形式の取得
        boolean isPerformance = false;
        RadioGroup radioGroup = newUcsView.findViewById(R.id.newUcsRadioGroup);
        switch (radioGroup.getCheckedRadioButtonId()) {
        // 「Single」の場合
        case R.id.newUcsSingle:
            chartLayout.columnSize = 5;
            isPerformance = false;
            break;
        // 「Single Performance」の場合
        case R.id.newUcsSinglePerformance:
            chartLayout.columnSize = 5;
            isPerformance = true;
            break;
        // 「Double」の場合
        case R.id.newUcsDouble:
            chartLayout.columnSize = 10;
            isPerformance = false;
            break;
        // 「Double Performance」の場合
        case R.id.newUcsDoublePerformance:
            chartLayout.columnSize = 10;
            isPerformance = true;
            break;
        }

        /*
         * 初期パラメータ値で定義された初期の譜面のブロックを1個生成
         * NOTE : byteの最大値は127なので、Splitは実際の値から1だけ引いた値を格納
         */
        UnitBlock unitBlock = new UnitBlock(bpm, delay, beat, (byte) (split - 1), rowLength);

        // 生成したブロックを、全情報を格納したリストに初期化して追加
        chartLayout.blockList.clear();
        chartLayout.blockList.add(unitBlock);

        // ノートの全情報を格納したマップを初期化
        noteLayout.noteMap.clear();

        // 譜面のレイアウトがメイン画面に表示されている場合、オープンする前のポインターの位置を保存
        if (mainActivity.ucs != null) {
            PreferenceManager.getDefaultSharedPreferences(mainActivity).edit()
                    .putInt(mainActivity.ucs.fileName, pointerLayout.row)
                    .apply();
        }
        // ポインターの行番号を、先頭である1をセット
        pointerLayout.row = 1;

        // ホールドの1点目の情報を初期化
        noteLayout.holdEdge = new int[10];

        // 選択領域の始点・終点を初期化
        selectedAreaLayout.selectedEdge = new int[2];

        // 譜面のレイアウトのリセット
        chartLayout.reset(mainActivity);

        // アクションバーかサイドバーのスピナーを表示させる
        buttonsLayout.spinner.setVisibility(VISIBLE);
        // スピナーを移動系のボタン種別の添え字である0に選択
        buttonsLayout.spinner.setSelection(0);
        // ボタン群のレイアウトに移動関連のボタンを表示
        buttonsLayout.showMoveButtons();

        // 譜面編集プロセスを格納するスタックをすべて削除
        chartLayout.undoProcessStack.clear();
        chartLayout.redoProcessStack.clear();

        // ucsファイルのインスタンスを新規作成する
        Ucs.createEmptyUcs(mainActivity, ucsFileName, isPerformance);

        // 譜面編集回数を0にリセット
        chartLayout.editCount = 0;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
        // 上記テキストビューの下側に、その譜面フォーマットを格納するテキストビューを更新
        updateFormatTextViewAtActionBar();
    }

    /**
     * ポインターが示している譜面のブロックを削除する
     */
    static void deleteBlockAtPointer() {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 追加したノートの集合
        Set<UnitNote> addedNotes = new HashSet<>();
        // 削除したノートの集合
        Set<UnitNote> removedNotes = new HashSet<>();
        // 追加した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> addedBlockMap = new TreeMap<>();
        // 削除した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> removedBlockMap = new TreeMap<>();

        // ブロックを削除する直前にポインターで示している行番号の位置情報を取得
        ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(pointerLayout.row);

        // ポインターが一番最後のブロックを示していたら、その直前のブロックの一番下に移動し、そうでない場合は直後のブロックの一番上に移動
        pointerLayout.row = information.offsetRowLength + ((information.idxBlock + 1 == chartLayout.blockList.size()) ? 0 : 1);

        // 現在ポインターで示しているブロックを削除
        removedBlockMap.put(information.idxBlock, chartLayout.blockList.get(information.idxBlock).copy());
        chartLayout.removeBlockView(mainActivity, information.idxBlock);
        UnitBlock deletedBlock = chartLayout.blockList.remove(information.idxBlock);

        // 現在ポインターで示している譜面のブロックから先の譜面のブロックのビューを更新
        for (int i = information.idxBlock; i < chartLayout.blockList.size(); i++) {
            removedBlockMap.put(i + 1, chartLayout.blockList.get(i).copy());
            chartLayout.removeBlockView(mainActivity, i);
            chartLayout.addBlockView(mainActivity, i);
            addedBlockMap.put(i, chartLayout.blockList.get(i).copy());
        }

        // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
        updateColorColumnLayouts();

        // 終端の余白ブロックのレイアウトの更新
        chartLayout.updateLastView(mainActivity);

        // 削除した譜面のブロックから下にある全ノートを、そのブロックの行数分だけ上へずらす
        SortedMap<Integer, UnitNote> tmpMap = new TreeMap<>();
        for (int key : new ArrayList<>(noteLayout.noteMap.tailMap(10 * (information.offsetRowLength + 1)).keySet())) {
            // 削除した譜面のブロックと、そこから下の譜面のブロックに属する、ずらす前のノートを削除
            UnitNote unitNote = noteLayout.noteMap.remove(key);
            noteLayout.removeNoteView(unitNote);
            removedNotes.add(unitNote.copy(mainActivity));
            // 削除した譜面のブロックから下にあるノートのキー値だけ変更してずらす
            if (key >= 10 * (information.offsetRowLength + deletedBlock.rowLength + 1)) {
                unitNote.start -= deletedBlock.rowLength;
                unitNote.goal -= deletedBlock.rowLength;
                for (int i = 0; i < unitNote.hollowStartList.size(); i++) {
                    unitNote.hollowStartList.set(i, unitNote.hollowStartList.get(i) - deletedBlock.rowLength);
                    unitNote.hollowGoalList.set(i, unitNote.hollowGoalList.get(i) - deletedBlock.rowLength);
                }
                tmpMap.put(key - 10 * deletedBlock.rowLength, unitNote);
            }
        }
        // ずらした後のノートだけ追加
        for (Map.Entry<Integer, UnitNote> entry : tmpMap.entrySet()) {
            noteLayout.noteMap.put(entry.getKey(), entry.getValue());
            noteLayout.addNoteView(mainActivity, entry.getValue());
            addedNotes.add(entry.getValue().copy(mainActivity));
        }

        // 選択領域のレイアウトの更新
        selectedAreaLayout.update(mainActivity);

        // ポインターのレイアウトの更新
        pointerLayout.update(mainActivity);
        // ポインターの位置が示すブロックの情報を更新
        updateTextsAtPointer();

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedNotes, removedNotes, addedBlockMap, removedBlockMap));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
            mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(VISIBLE);
        }
        // 「やり直し」ボタンを非表示にする
        mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
        // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
        chartLayout.redoProcessStack.clear();

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
    }

    /**
     * 再生確認ダイアログを今後表示しないかどうか確認して、再生動作を実行する
     *
     * @param playConfirmationView 再生確認ダイアログのビュー
     * @param startRow             再生したい位置を示す行番号
     */
    static void executePlaying(View playConfirmationView, int startRow) {
        // 譜面のスクロールビューを取得
        ChartScrollView chartScrollView = mainActivity.findViewById(R.id.chartScrollView);

        // 「もう一度表示しない」のチェックボックスを取得
        CheckBox checkBox = playConfirmationView.findViewById(R.id.playConfirmationCheckBox);

        // 「もう一度表示しない」のチェックボックスのチェック状態をプリファレンスに保存する
        PreferenceManager.getDefaultSharedPreferences(mainActivity).edit()
                .putBoolean(CommonParameters.PREFERENCE_PLAY_CONFIRMATION, checkBox.isChecked())
                .apply();

        // 譜面を再生する
        chartScrollView.play(mainActivity, startRow);
    }

    /**
     * ポインターが示している譜面のブロックと、その次の譜面のブロックを結合する
     */
    static void mergeBlocksAtPointer() {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 追加したノートの集合
        Set<UnitNote> addedNotes = new HashSet<>();
        // 削除したノートの集合
        Set<UnitNote> removedNotes = new HashSet<>();
        // 追加した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> addedBlockMap = new TreeMap<>();
        // 削除した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> removedBlockMap = new TreeMap<>();

        // 現在ポインターで示している行番号の位置情報を取得
        ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(pointerLayout.row);

        // 現在ポインターで示している譜面のブロックを取得
        UnitBlock upperBlock = chartLayout.blockList.get(information.idxBlock);

        // 現在ポインターで示している譜面のブロックの1つ後の譜面のブロックを取得
        UnitBlock lowerBlock = chartLayout.blockList.get(information.idxBlock + 1);

        // 現在ポインターで示している譜面のブロックを結合
        removedBlockMap.put(information.idxBlock, upperBlock.copy());
        upperBlock.rowLength += lowerBlock.rowLength;
        chartLayout.blockList.set(information.idxBlock, upperBlock);
        addedBlockMap.put(information.idxBlock, upperBlock.copy());

        // 現在ポインターで示している譜面のブロックのビューを更新
        chartLayout.removeBlockView(mainActivity, information.idxBlock);
        chartLayout.addBlockView(mainActivity, information.idxBlock);

        // 現在ポインターで示している譜面のブロックの1つ後の譜面のブロックのビューを削除
        chartLayout.removeBlockView(mainActivity, information.idxBlock + 1);

        // 現在ポインターで示している譜面のブロックの1つ後の譜面のブロックを削除
        removedBlockMap.put(information.idxBlock + 1, lowerBlock.copy());
        chartLayout.blockList.remove(information.idxBlock + 1);

        // 結合した2つの譜面のブロックのSplit値が等しくない場合
        if (upperBlock.split != lowerBlock.split) {
            // 結合した譜面のブロックより後の譜面のブロックのビューを再度追加
            for (int i = information.idxBlock + 1; i < chartLayout.blockList.size(); i++) {
                removedBlockMap.put(i + 1, chartLayout.blockList.get(i).copy());
                chartLayout.removeBlockView(mainActivity, i);
                chartLayout.addBlockView(mainActivity, i);
                addedBlockMap.put(i, chartLayout.blockList.get(i).copy());
            }

            // 終端の余白ブロックのレイアウトの更新
            chartLayout.updateLastView(mainActivity);

            // 再度追加した譜面のブロックに属するノートのビューを再度追加する
            for (UnitNote unitNote : noteLayout.noteMap.tailMap(10 * (information.offsetRowLength + upperBlock.rowLength - lowerBlock.rowLength + 1)).values()) {
                removedNotes.add(unitNote.copy(mainActivity));
                noteLayout.removeNoteView(unitNote);
                noteLayout.addNoteView(mainActivity, unitNote);
                addedNotes.add(unitNote.copy(mainActivity));
            }
        }

        // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
        updateColorColumnLayouts();

        // 選択領域のレイアウトの更新
        selectedAreaLayout.update(mainActivity);

        // ポインターのレイアウトの更新
        pointerLayout.update(mainActivity);
        // ポインターの位置が示すブロックの情報を更新
        updateTextsAtPointer();

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedNotes, removedNotes, addedBlockMap, removedBlockMap));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
            mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(VISIBLE);
        }
        // 「やり直し」ボタンを非表示にする
        mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
        // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
        chartLayout.redoProcessStack.clear();

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
    }

    /**
     * 指定したucsファイル名にリネームする
     * 不正なucsファイル名の場合はリネームしない
     *
     * @param ucsFileName ucsファイル名(拡張子.ucsは含まない)
     */
    static void renameUcsFileName(String ucsFileName) {
        // 入力したucsファイル名が不正かどうかチェック
        if (checkUcsFileName(ucsFileName)) {
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_fileName), Toast.LENGTH_SHORT).show();
            return;
        }

        // 変更前のファイル名でプリファレンスに以前保存したポインターの行番号を削除
        PreferenceManager.getDefaultSharedPreferences(mainActivity).edit()
                .remove(mainActivity.ucs.fileName)
                .apply();

        // ucsファイル名の更新
        mainActivity.ucs.fileName = ucsFileName;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
    }

    /**
     * メイン画面のアクティビティのパーミッションを依頼する
     *
     * @param permission  チェックするパーミッション種別
     * @param requestCode パーミッションの許可を求めるダイアログを識別するためのコード
     * @return パーミッションの許可を求めるダイアログを表示しなかった場合はtrue、表示した場合はfalse
     */
    public static boolean requestMainPermission(String permission, int requestCode) {
        // 指定したパーミッションが拒否されるかどうかチェック
        if (ContextCompat.checkSelfPermission(mainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // 拒否された場合、パーミッションの許可を求める種別を指定
            String[] requestPermissions;
            switch (permission) {
            // 外部ストレージへの読み込みのパーミッションの場合
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    requestPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                } else {
                    requestPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                }
                break;
            // 外部ストレージへの書き込みのパーミッションの場合
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                requestPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                break;
            default:
                throw new IllegalArgumentException("The kind of permission is not supported.");
            }

            /*
             * パーミッションの許可をリクエストするダイアログを表示
             * その後の動作は、MainActivity#onRequestPermissionsResult()でrequestCodeごとに定義する
             */
            ActivityCompat.requestPermissions(mainActivity, requestPermissions, requestCode);

            return false;
        }

        return true;
    }

    /**
     * ポインターが示している譜面のブロックを分割する
     * ポインターが示す行番号は下側の譜面のブロックに属し、その1つ前の行番号との間が境界となる
     */
    static void splitBlocksAtPointer() {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 追加した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> addedBlockMap = new TreeMap<>();
        // 削除した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> removedBlockMap = new TreeMap<>();

        // 現在ポインターで示している行番号の位置情報を取得
        ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(pointerLayout.row);

        // 現在ポインターで示している譜面のブロックを取得
        UnitBlock upperBlock = chartLayout.blockList.get(information.idxBlock);

        /*
         * 現在ポインターで示されているブロックから、分割後の下側ブロックを生成
         * Delay値は上から2列目以降は意味がないので0にする
         */
        UnitBlock lowerBlock = new UnitBlock(upperBlock.bpm, 0, upperBlock.beat, upperBlock.split, information.offsetRowLength + upperBlock.rowLength - pointerLayout.row + 1);

        // 分割後の上側の譜面のブロックをセット
        removedBlockMap.put(information.idxBlock, upperBlock.copy());
        upperBlock.rowLength -= lowerBlock.rowLength;
        chartLayout.blockList.set(information.idxBlock, upperBlock);
        addedBlockMap.put(information.idxBlock, upperBlock.copy());

        // 分割後の下側の譜面のブロックを挿入
        chartLayout.blockList.add(information.idxBlock + 1, lowerBlock);
        addedBlockMap.put(information.idxBlock + 1, lowerBlock.copy());

        // 分割後の上側の譜面のブロックのビューを更新する
        chartLayout.removeBlockView(mainActivity, information.idxBlock);
        chartLayout.addBlockView(mainActivity, information.idxBlock);
        // 分割後の下側の譜面のブロックのビューを更新する
        chartLayout.addBlockView(mainActivity, information.idxBlock + 1);

        // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
        updateColorColumnLayouts();

        // 選択領域のレイアウトの更新
        selectedAreaLayout.update(mainActivity);

        // ポインターのレイアウトの更新
        pointerLayout.update(mainActivity);
        // ポインターの位置が示すブロックの情報を更新
        updateTextsAtPointer();

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedBlockMap, removedBlockMap));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
            mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(VISIBLE);
        }
        // 「やり直し」ボタンを非表示にする
        mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
        // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
        chartLayout.redoProcessStack.clear();

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
    }

    /**
     * 「ブロック設定」のダイアログでの入力情報をもとに、ポインターが示している譜面のブロックの情報を更新する
     *
     * @param blockSettingView 「ブロック設定」のダイアログのビュー
     * @param information      ポインターで示している行番号の位置情報
     */
    static void updateBlockSettingAtPointer(View blockSettingView, ChartLayout.PositionInformation information) {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトの取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // 追加した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> addedBlockMap = new TreeMap<>();
        // 削除した譜面のブロックのマップ
        NavigableMap<Integer, UnitBlock> removedBlockMap = new TreeMap<>();

        // 変更後の情報を入力させるエディトテキストを取得
        EditText blockSettingBpmTo = blockSettingView.findViewById(R.id.blockSettingBpmTo);
        EditText blockSettingDelayTo = blockSettingView.findViewById(R.id.blockSettingDelayTo);
        EditText blockSettingBeatTo = blockSettingView.findViewById(R.id.blockSettingBeatTo);
        EditText blockSettingSplitTo = blockSettingView.findViewById(R.id.blockSettingSplitTo);

        // 現在ポインターで示している譜面のブロックを取得
        UnitBlock currentBlock = chartLayout.blockList.get(information.idxBlock);

        // BPM値を取得してチェック
        float bpm;
        try {
            bpm = Float.parseFloat(blockSettingBpmTo.getText().toString());
            if (bpm < 0.1f || bpm > 999.0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「BPM」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_bpm), Toast.LENGTH_SHORT).show();
            return;
        }

        // Delay値を取得してチェック
        float delay;
        try {
            delay = Float.parseFloat(blockSettingDelayTo.getText().toString());
            if (delay < -999999.0f || delay > 999999.0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Delay」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_delay), Toast.LENGTH_SHORT).show();
            return;
        }

        // Beat値を取得してチェック
        byte beat;
        try {
            beat = Byte.parseByte(blockSettingBeatTo.getText().toString());
            if (beat == 0 || beat > 64) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Beat」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_beat), Toast.LENGTH_SHORT).show();
            return;
        }

        // Split値を取得してチェック
        short split;
        try {
            split = Short.parseShort(blockSettingSplitTo.getText().toString());
            if (split == 0 || split > 128) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // 「Split」に異常値を入力した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_formatError_split), Toast.LENGTH_SHORT).show();
            return;
        }

        // Split値を変更したかどうかのフラグをセット
        boolean isChangedSplit = currentBlock.split + 1 != split;

        /*
         * 現在ポインターで示している譜面のブロック情報を変更してセット
         * NOTE : byteの最大値は127なので、Splitは実際の値から1だけ引いた値を格納
         */
        removedBlockMap.put(information.idxBlock, currentBlock.copy());
        currentBlock.bpm = bpm;
        currentBlock.delay = delay;
        currentBlock.beat = beat;
        currentBlock.split = (byte) (split - 1);
        chartLayout.blockList.set(information.idxBlock, currentBlock);
        addedBlockMap.put(information.idxBlock, currentBlock.copy());

        // Split値を変更した場合のみ、譜面のレイアウトを更新する
        if (isChangedSplit) {
            // ポインターで示している譜面のブロック以下にある譜面のブロックのビューを更新
            for (int i = information.idxBlock; i < chartLayout.blockList.size(); i++) {
                chartLayout.removeBlockView(mainActivity, i);
                chartLayout.addBlockView(mainActivity, i);
            }

            // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
            updateColorColumnLayouts();

            // 終端の余白ブロックのレイアウトの更新
            chartLayout.updateLastView(mainActivity);

            // ポインターで示している譜面のブロック以下にある譜面のブロックに属するノートのビューを更新
            for (UnitNote unitNote : noteLayout.noteMap.tailMap(10 * (information.offsetRowLength + 1)).values()) {
                noteLayout.removeNoteView(unitNote);
                noteLayout.addNoteView(mainActivity, unitNote);
            }

            // 選択領域のレイアウトの更新
            selectedAreaLayout.update(mainActivity);
        }

        // ポインターのレイアウトの更新
        pointerLayout.update(mainActivity);
        // ポインターの位置が示すブロックの情報を更新
        updateTextsAtPointer();

        // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
        chartLayout.undoProcessStack.push(new UnitProcess(addedBlockMap, removedBlockMap));
        // スピナーが「編集」を選択している場合、「元に戻す」ボタンを表示する
        if (buttonsLayout.spinner.getSelectedItemPosition() == 1) {
            mainActivity.findViewById(R.id.buttonEditUndo).setVisibility(VISIBLE);
        }
        // 「やり直し」ボタンを非表示にする
        mainActivity.findViewById(R.id.buttonEditRedo).setVisibility(View.GONE);
        // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスを消去
        chartLayout.redoProcessStack.clear();

        // 譜面編集回数のインクリメント
        chartLayout.editCount++;
        // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
        updateFileTextViewAtActionBar();
    }

    /**
     * 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新する
     */
    public static void updateColorColumnLayouts() {
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);

        for (int idx = 0; idx < chartLayout.blockList.size(); idx++) {
            // 1列のレイアウトの色の決定
            int color = (idx % 2 == 0) ?
                    Color.rgb(
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_RED, CommonParameters.PREFERENCE_BLOCK_ODD_RED_DEFAULT),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_GREEN, CommonParameters.PREFERENCE_BLOCK_ODD_GREEN_DEFAULT),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_BLUE, CommonParameters.PREFERENCE_BLOCK_ODD_BLUE_DEFAULT)) :
                    Color.rgb(
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_RED, 0),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_GREEN, 48),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_BLUE, 96));

            // 1列のレイアウトの色をセット
            for (byte column = (byte) (10 - chartLayout.columnSize); column < 10; column++) {
                for (FrameLayout columnLayout : chartLayout.blockList.get(idx).getColumnLayoutList(mainActivity, column)) {
                    columnLayout.setBackgroundColor(color);
                }
            }
        }
    }

    /**
     * メイン画面のアクションバーにあるucsファイル名のテキストビューを更新する
     */
    public static void updateFileTextViewAtActionBar() {
        if (mainActivity.getSupportActionBar() != null) {
            // アクションバーにあるucsファイル名のテキストビューを取得
            TextView actionBarFileTextView = mainActivity.findViewById(R.id.actionBarFileTextView);
            if (actionBarFileTextView != null) {
                // 譜面編集回数に応じて「*」をつけるかどうか判定
                if (((ChartLayout) mainActivity.findViewById(R.id.chartLayout)).editCount != 0) {
                    actionBarFileTextView.setText(mainActivity.getResources().getString(R.string.textView_actionBar_asterisk, mainActivity.ucs.fileName));
                } else {
                    actionBarFileTextView.setText(mainActivity.ucs.fileName);
                }
            } else {
                throw new NullPointerException("actionBarFileTextView is null.");
            }
        }
    }

    /**
     * メイン画面のアクションバーにあるucsファイル名のテキストビューの下側に、譜面フォーマットを格納するテキストビューを更新する
     */
    static void updateFormatTextViewAtActionBar() {
        if (mainActivity.getSupportActionBar() != null) {
            TextView textView = mainActivity.findViewById(R.id.actionBarFormatTextView);
            if (textView != null) {
                if (textView.getVisibility() == View.GONE) textView.setVisibility(View.VISIBLE);
                String formatStr = (((ChartLayout) mainActivity.findViewById(R.id.chartLayout)).columnSize == 5) ? "Single" : "Double";
                if (mainActivity.ucs.isPerformance) formatStr += " Performance";
                textView.setText(formatStr);
            }
        }
    }

    /**
     * 譜面のスクロールビューと、ボタン群のレイアウトの場所を更新する
     */
    public static void updateLayoutPosition() {
        // 譜面のスクロールビューを取得
        ChartScrollView chartScrollView = mainActivity.findViewById(R.id.chartScrollView);
        // ボタン群のレイアウトと譜面のスクロールビューを取得
        LinearLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);

        // ボタン群のレイアウトを右側に配置させるかどうかのフラグを取得
        boolean isRight = PreferenceManager.getDefaultSharedPreferences(mainActivity)
                .getBoolean(CommonParameters.PREFERENCE_BUTTONS_POSITION_RIGHT, CommonParameters.PREFERENCE_BUTTONS_POSITION_RIGHT_DEFAULT);
        // ログ出力
        Log.d(TAG, "updateLayoutPosition:isRight=" + isRight);

        if (isRight) {
            // ボタン群のレイアウトを右側に寄せる
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) buttonsLayout.getLayoutParams();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            } else {
                params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
            }
            buttonsLayout.setLayoutParams(params);

            // 譜面のスクロールビューを左側に寄せる
            params = (RelativeLayout.LayoutParams) chartScrollView.getLayoutParams();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(RelativeLayout.RIGHT_OF, 0);
                params.addRule(RelativeLayout.LEFT_OF, R.id.buttonsLayout);
            } else {
                params.removeRule(RelativeLayout.END_OF);
                params.addRule(RelativeLayout.START_OF, R.id.buttonsLayout);
            }
            chartScrollView.setLayoutParams(params);
        } else {
            // ボタン群のレイアウトを左側に寄せる
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) buttonsLayout.getLayoutParams();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            } else {
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
            }
            buttonsLayout.setLayoutParams(params);

            // 譜面のスクロールビューを右側に寄せる
            params = (RelativeLayout.LayoutParams) chartScrollView.getLayoutParams();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.addRule(RelativeLayout.LEFT_OF, 0);
                params.addRule(RelativeLayout.RIGHT_OF, R.id.buttonsLayout);
            } else {
                params.removeRule(RelativeLayout.START_OF);
                params.addRule(RelativeLayout.END_OF, R.id.buttonsLayout);
            }
            chartScrollView.setLayoutParams(params);
        }
    }

    /**
     * ポインターが示すブロックの情報のテキストビューと、そのボタンの(Beat値)のテキストを更新する
     */
    public static void updateTextsAtPointer() {
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);

        // ポインターが示すブロックの3つの情報のテキストビューを取得
        TextView bpmTextView = mainActivity.findViewById(R.id.bpmTextView);
        TextView delayTextView = mainActivity.findViewById(R.id.delayTextView);
        TextView splitTextView = mainActivity.findViewById(R.id.splitTextView);

        // 3つのテキストビューの色をRGB値として取得
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        int textColor = Color.rgb(
                sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_RED, CommonParameters.PREFERENCE_BLOCK_TEXT_RED_DEFAULT),
                sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_GREEN, CommonParameters.PREFERENCE_BLOCK_TEXT_GREEN_DEFAULT),
                sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_BLUE, CommonParameters.PREFERENCE_BLOCK_TEXT_BLUE_DEFAULT));

        // 3つのテキストビューの色とテキストをセット
        bpmTextView.setTextColor(textColor);
        bpmTextView.setText(mainActivity.getString(R.string.textView_bpm_float, pointerLayout.bpm));
        delayTextView.setTextColor(textColor);
        delayTextView.setText(mainActivity.getString(R.string.textView_delay_float, pointerLayout.delay));
        splitTextView.setTextColor(textColor);
        // NOTE : byteの最大値は127なので、実際の値から1だけ引いた値を格納
        splitTextView.setText(mainActivity.getString(R.string.textView_split_short, (short) (pointerLayout.split + 1)));

        // ボタンの(Beat値)のテキストを更新
        ((Button) mainActivity.findViewById(R.id.buttonMoveUpperBeat)).setText(mainActivity.getString(R.string.button_move_upperBeat, pointerLayout.beat));
        ((Button) mainActivity.findViewById(R.id.buttonMoveLowerBeat)).setText(mainActivity.getString(R.string.button_move_lowerBeat, pointerLayout.beat));
    }

    /**
     * メイン画面のアクティビティ上で表示するダイアログでの、選択したラジオボタンでの譜面倍率をもとに、譜面のレイアウトを更新する
     *
     * @param checkedRadioButton 選択した譜面倍率を表すラジオボタン
     */
    static void updateZoom(RadioButton checkedRadioButton) {
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);

        if (checkedRadioButton != null) {
            // 取得したラジオボタンの添字番号を保存し、その倍率をセット
            PreferenceManager.getDefaultSharedPreferences(mainActivity)
                    .edit()
                    .putInt(CommonParameters.PREFERENCE_ZOOM_INDEX, Integer.parseInt(checkedRadioButton.getTag().toString()))
                    .putFloat(CommonParameters.PREFERENCE_ZOOM, Float.parseFloat(checkedRadioButton.getText().toString().substring(1)))
                    .apply();
        }

        // 譜面のレイアウトをリセット
        chartLayout.reset(mainActivity);

        // ポインターの位置での位置情報を計算し、そこへスクロールビューを動かす
        mainActivity.findViewById(R.id.chartScrollView).scrollTo(0, (int) chartLayout.calcPositionInformation(pointerLayout.row).coordinate);
    }

    // 抽象staticクラスなのでコンストラクタはprivateにする
    private MainCommonFunctions() {
    }
}
