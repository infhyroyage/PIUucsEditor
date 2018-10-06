package com.editor.ucs.piu.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.buttons.ButtonsLayout;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.NoteLayout;
import com.editor.ucs.piu.chart.PointerLayout;
import com.editor.ucs.piu.chart.SelectedAreaLayout;
import com.editor.ucs.piu.unit.UnitBlock;
import com.editor.ucs.piu.unit.UnitNote;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;

/**
 * 編集中のucsファイルを扱うクラス
 */
public class Ucs {
    // デバッグ用のタグ
    private static final String TAG = "Ucs";

    /**
     * ucs/mp3ファイルが存在するディレクトリ
     * ucsファイル新規作成、かつ、未保存時のみnullが格納される
     */
    public String fileDir;

    /**
     * ucs/mp3ファイル名(拡張子.ucs/mp3を除く)
     */
    public String fileName;

    /**
     * ucsファイルがPerformance譜面かどうかのフラグ
     */
    boolean isPerformance;

    /**
     * コンストラクタ
     * インスタンスはread()、createEmptyUcs()実行時に作成して返すためprivateにする
     *
     * @param fileDir ucs/mp3ファイルが存在するディレクトリ
     * @param fileName ucsファイル名(拡張子.ucsを除く)
     * @param isPerformance ucsファイルがPerformance譜面かどうかのフラグ
     */
    private Ucs(String fileDir, String fileName, boolean isPerformance) {
        this.fileDir = fileDir;
        this.fileName = fileName;
        this.isPerformance = isPerformance;
    }

    /**
     * ucsファイルのインスタンスを新規作成する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param fileName ucsファイル名(拡張子.ucsを除く)
     * @param isPerformance ucsファイルがPerformance譜面かどうかのフラグ
     */
    static void createEmptyUcs(MainActivity mainActivity, String fileName, boolean isPerformance) {
        // ucsファイルのインスタンスをセット
        mainActivity.ucs =  new Ucs(null, fileName, isPerformance);
    }

    /**
     * 外部ストレージにあるucsファイルの読み込み処理を実行する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public static void open(final MainActivity mainActivity) {
        // 外部ストレージにあるファイルを読み込むパーミッションをリクエスト
        if (MainCommonFunctions.requestMainPermission(Manifest.permission.READ_EXTERNAL_STORAGE, CommonParameters.PERMISSION_UCS_READ)) {
            // リクエストするダイアログが表示しなかった場合は、直接外部ストレージ上にあるucsファイルを指定させるダイアログの表示
            new ChooserDialog().with(mainActivity)
                    .withFilter(false, false, "ucs")
                    .withStartFile(Environment.getExternalStorageDirectory().getPath())
                    .withRowLayoutView(R.layout.item_chooser)
                    .withResources(R.string.dialog_title_selectUcsFile, android.R.string.ok, android.R.string.cancel)
                    .withChosenListener(new ChooserDialog.Result() {
                        @Override
                        public void onChoosePath(String path, File pathFile) {
                            // 指定したucsファイルの情報を読み込み、そのインスタンスを生成する
                            read(mainActivity, path);
                        }
                    }).build().show();
        }
    }

    /**
     * 指定したucsファイルの絶対パスから、その情報を読み込み、そのインスタンスを生成する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param path 指定したucsファイルの絶対パス
     */
    static void read(MainActivity mainActivity, String path) {
        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトを取得
        SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // ボタン群のレイアウトに表示するボタンの種別を示すスピナーを取得
        Spinner spinner = buttonsLayout.spinner;

        // ucs/mp3ファイルが存在するディレクトリを取得
        String fileDir = path.substring(0, path.lastIndexOf("/"));
        // ucsファイル名(拡張子.ucsを除く)を取得
        String fileNameExtension = path.substring(path.lastIndexOf("/") + 1);
        String fileName = fileNameExtension.substring(0, fileNameExtension.lastIndexOf("."));
        // ログ出力
        Log.d(TAG, "read:path=" + path);
        Log.d(TAG, "read:fileDir=" + fileDir);
        Log.d(TAG, "read:fileName=" + fileName);

        BufferedReader br = null;
        try {
            // ファイル読み込み用のバッファの生成
            br = new BufferedReader(new FileReader(path));

            // 先頭行が":Format=1"かどうかチェック
            int rowNum = 0;
            String rowStr = br.readLine();
            if (rowStr == null) {
                // ucsファイルの終端に到達
                throw new EndOfUcsException(rowNum);
            }
            rowNum++;
            if (!rowStr.equals(":Format=1")) {
                throw new IllegalUcsFormatException(rowNum);
            }

            // 譜面形式の取得
            rowStr = br.readLine();
            if (rowStr == null) {
                // ucsファイルの終端に到達
                throw new EndOfUcsException(rowNum);
            }
            rowNum++;
            boolean isPerformance;
            switch (rowStr) {
                case ":Mode=Single":
                    // Single譜面の場合
                    chartLayout.columnSize = 5;
                    isPerformance = false;
                    break;
                case ":Mode=Double":
                    // Double譜面の場合
                    chartLayout.columnSize = 10;
                    isPerformance = false;
                    break;
                case ":Mode=S-Performance":
                    // Single Performance譜面の場合
                    chartLayout.columnSize = 5;
                    isPerformance = true;
                    break;
                case ":Mode=D-Performance":
                    // Double Performance譜面の場合
                    chartLayout.columnSize = 10;
                    isPerformance = true;
                    break;
                default:
                    // 上記以外の文字の場合は不正フォーマットとする
                    throw new IllegalUcsFormatException(rowNum);
            }

            int isSingle = (chartLayout.columnSize == 5) ? 5 : 0;

            // 全ブロックの行数の総和を初期化
            int accumulatedRow = 0;
            int oldAccumulatedRow = 0;

            // 指定したucsファイルのブロック、ノートの情報を格納するリストを要素数0で初期化
            List<UnitBlock> blockList = new ArrayList<>(0);
            List<UnitNote> noteList = new ArrayList<>(0);

            // 各列におけるホールドの始点の行数情報を0で初期化
            int[] startHolds = new int[chartLayout.columnSize];
            // 各列における中抜きホールドの中間にかぶせる始点の一時情報を0で初期化
            int[] startHollows = new int[chartLayout.columnSize];
            // 各列における中抜きホールドの中間にかぶせる始点、終点の行番号のリストを要素数0で初期化
            List<List<Integer>> hollowStartLists = new ArrayList<>();
            List<List<Integer>> hollowGoalLists = new ArrayList<>();
            for (int i = 0; i < chartLayout.columnSize; i++) {
                hollowStartLists.add(new ArrayList<Integer>());
                hollowGoalLists.add(new ArrayList<Integer>());
            }

            // ブロック内の1行ごとに文字列を読み込む
            UnitBlock currentBlock = null;
            while ((rowStr = br.readLine()) != null) {
                rowNum++;

                if (rowStr.equals("")) {
                    // 改行のみの行の場合は、不正フォーマットとする
                    throw new IllegalUcsFormatException(rowNum);
                }

                if (rowStr.charAt(0) == ':') {
                    // 2個目以降の譜面のブロックのヘッダーに到達した場合、前の譜面のブロックの行数が0になっていないかどうかチェック
                    if (accumulatedRow > 0 && accumulatedRow - oldAccumulatedRow == 0) {
                        throw new IllegalUcsFormatException(rowNum);
                    }

                    // 譜面のブロックの行数を更新して、そのブロックをリストに格納
                    if (currentBlock != null) {
                        currentBlock.rowLength = accumulatedRow - oldAccumulatedRow;
                        oldAccumulatedRow = accumulatedRow;
                        blockList.add(currentBlock);
                    }

                    // 新しい譜面のブロックのヘッダーを解析し、そのインスタンスを生成
                    currentBlock = analyzeBlockHeader(br, rowNum, rowStr);
                    rowNum = currentBlock.rowLength;
                    continue;
                }

                // 新しい譜面のブロックのヘッダーに到達していない場合、その行の列数をチェック
                if (rowStr.length() != chartLayout.columnSize) {
                    throw new IllegalUcsFormatException(rowNum);
                }

                // 単ノート、(中抜け)ホールドの情報を解析し、そのインスタンスを追加
                accumulatedRow++;
                for (byte i = 0; i < chartLayout.columnSize; i++) {
                    switch (rowStr.charAt(i)) {
                        // 単ノートの場合
                        case 'X':
                            // 不正なホールドの記述かどうかチェック
                            if (startHolds[i] > 0 || startHollows[i] > 0) {
                                throw new IllegalUcsFormatException(rowNum);
                            }
                            // 単ノートをリストに追加
                            noteList.add(new UnitNote(mainActivity, (byte) (i + isSingle), accumulatedRow));
                            break;
                        // ホールドor中抜けホールドの始点の場合
                        case 'M':
                            // 不正なホールドの記述かどうかチェック
                            if (startHolds[i] > 0 || startHollows[i] > 0) {
                                throw new IllegalUcsFormatException(rowNum);
                            }
                            startHolds[i] = accumulatedRow;
                            startHollows[i] = accumulatedRow;
                            break;
                        // ホールドor中抜けホールドの中間の場合
                        case 'H':
                            // 不正なホールドの記述かどうかチェック
                            if (startHolds[i] == 0) {
                                throw new IllegalUcsFormatException(rowNum);
                            }
                            // 中抜きホールドかどうかチェック
                            if (startHollows[i] == 0) {
                                startHollows[i] = accumulatedRow;
                            }
                            break;
                        // ホールドor中抜けホールドの終点の場合
                        case 'W':
                            // 不正なホールドの記述かどうかチェック
                            if (startHolds[i] == 0) {
                                throw new IllegalUcsFormatException(rowNum);
                            }
                            // 中抜きホールドかどうかチェック
                            if (hollowStartLists.get(i).size() > 0 && hollowGoalLists.get(i).size() > 0 && startHollows[i] != 0) {
                                hollowStartLists.get(i).add(startHollows[i]);
                                hollowGoalLists.get(i).add(accumulatedRow + 1);
                            }
                            // ホールドor中抜きホールドをリストに追加
                            noteList.add(new UnitNote(mainActivity, (byte) (i + isSingle), startHolds[i], accumulatedRow, hollowStartLists.get(i), hollowGoalLists.get(i)));
                            startHolds[i] = 0;
                            startHollows[i] = 0;
                            hollowStartLists.get(i).clear();
                            hollowGoalLists.get(i).clear();
                            break;
                        // 何もないor中抜けホールドの中間の場合
                        case '.':
                            // 中抜きホールドかどうかチェック
                            if (startHollows[i] > 0) {
                                hollowStartLists.get(i).add(startHollows[i]);
                                hollowGoalLists.get(i).add(accumulatedRow);
                                startHollows[i] = 0;
                            }
                            break;
                        // 上記以外の文字の場合は不正フォーマットとする
                        default:
                            throw new IllegalUcsFormatException(rowNum);
                    }
                }
            }

            // 最後のブロックをリストに格納
            currentBlock.rowLength = accumulatedRow - oldAccumulatedRow;
            blockList.add(currentBlock);

            // 読み込んだブロックを、全情報を格納したリストに初期化して追加
            chartLayout.blockList.clear();
            chartLayout.blockList.addAll(blockList);

            // 読み込んだノートを、全情報を格納したマップに初期化して追加
            noteLayout.noteMap.clear();
            for (UnitNote unitNote : noteList) {
                noteLayout.noteMap.put(10 * unitNote.start + unitNote.column, unitNote);
            }

            // 譜面のレイアウトがメイン画面に表示されている場合、オープンする前のポインターの位置を保存
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            if (mainActivity.ucs != null) {
                sharedPreferences.edit()
                        .putInt(mainActivity.ucs.fileName, pointerLayout.row)
                        .apply();
            }
            /*
             * 各ファイル名でプリファレンスに以前保存したポインターの行番号を取得
             * まだ1度も保存されていない場合は、先頭の行番号である1を取得
             */
            int preferenceRow = sharedPreferences.getInt(fileName, 1);
            // ポインターの示す行番号を、取得した行番号が全行数を超える場合は終端の行、そうでない場合は取得した行番号をセット
            pointerLayout.row = (preferenceRow > accumulatedRow) ? accumulatedRow : preferenceRow;

            // ホールドの1点目の情報を初期化
            noteLayout.holdEdge = new int[10];

            // 選択領域の始点・終点を初期化
            selectedAreaLayout.selectedEdge = new int[2];

            // 譜面のレイアウトのリセット
            chartLayout.reset(mainActivity);

            // アクションバーかサイドバーのスピナーを表示させる
            spinner.setVisibility(VISIBLE);
            // スピナーを移動系のボタン種別の添え字である0に選択
            spinner.setSelection(0);
            // ボタン群のレイアウトに移動関連のボタンを表示
            buttonsLayout.showMoveButtons();

            // 譜面編集プロセスを格納するリストをすべて削除
            chartLayout.undoProcessStack.clear();
            chartLayout.redoProcessStack.clear();

            // ucsファイルのインスタンスをセット
            mainActivity.ucs = new Ucs(fileDir, fileName, isPerformance);

            // 譜面編集回数を0にリセット
            chartLayout.editCount = 0;
            // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
            MainCommonFunctions.updateFileTextViewAtActionBar();
            // 上記テキストビューの下側に、その譜面フォーマットを格納するテキストビューを更新
            MainCommonFunctions.updateFormatTextViewAtActionBar();
        } catch (EndOfUcsException e) {
            // アラートダイアログの出力
            MainDialogFragment.newInstance(mainActivity, CommonDialogType.ALERT, R.string.dialog_title_ucsReadError, R.string.dialog_message_endOfUcsError, e.rowNum).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
        } catch (IllegalUcsFormatException e) {
            // アラートダイアログの出力
            MainDialogFragment.newInstance(mainActivity, CommonDialogType.ALERT, R.string.dialog_title_ucsReadError, R.string.dialog_message_illegalUcsFormatError, e.rowNum).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
        } catch (IOException e) {
            // ログ出力
            Log.e(TAG, e.getMessage(), e);

            // ucsファイル読み込み中に例外が発生した旨のトーストの出力
            Toast.makeText(mainActivity, R.string.toast_ioException_ucsRead, Toast.LENGTH_SHORT).show();
        } finally {
            if (br != null) {
                // ファイル読み込み用のバッファを閉じる
                try {
                    br.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイル読み込み中に例外が発生した旨のトーストの出力
                    Toast.makeText(mainActivity, R.string.toast_ioException_ucsRead, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * ファイル読み込み用のバッファが示すノートを解析する
     *
     * @param br ファイル読み込み用のバッファ
     * @param rowNum ファイル読み込み用のバッファが示す行番号
     * @param rowStr ファイル読み込み用のバッファが示す行番号での文字列
     * @return 譜面のブロックのインスタンス(rowLengthにはヘッダーの終端の行番号が格納)
     * @throws IOException ucsファイル読み込み中に例外が発生した場合
     */
    private static UnitBlock analyzeBlockHeader(BufferedReader br, int rowNum, String rowStr) throws IOException {
        // ログ出力
        Log.d(TAG, "analyzeBlockHeader:rowNum=" + rowNum);

        // 譜面のブロックのBpm値の読み込みを行う
        float bpm;
        if (!rowStr.substring(0, 5).equals(":BPM=")) {
            // ":BPM="からスタートしていない
            throw new IllegalUcsFormatException(rowNum);
        }
        try {
            bpm = Float.parseFloat(rowStr.substring(5));
        } catch (NumberFormatException e) {
            // BPM値が浮動小数点型ではない
            throw new IllegalUcsFormatException(rowNum);
        }

        // 譜面のブロックのDelay値の読み込みを行う
        float delay;
        rowStr = br.readLine();
        if (rowStr == null) {
            // ucsファイルの終端に到達
            throw new EndOfUcsException(rowNum);
        }
        rowNum++;
        if (!rowStr.substring(0, 7).equals(":Delay=")) {
            // ":Delay="からスタートしていない
            throw new IllegalUcsFormatException(rowNum);
        }
        try {
            delay = Float.parseFloat(rowStr.substring(7));
        } catch (NumberFormatException e) {
            // Delay値が浮動小数点型ではない
            throw new IllegalUcsFormatException(rowNum);
        }

        // 譜面のブロックのBeat値の読み込みを行う
        byte beat;
        rowStr = br.readLine();
        if (rowStr == null) {
            // ucsファイルの終端に到達
            throw new EndOfUcsException(rowNum);
        }
        rowNum++;
        if (!rowStr.substring(0, 6).equals(":Beat=")) {
            // ":Beat="からスタートしていない
            throw new IllegalUcsFormatException(rowNum);
        }
        try {
            beat = Byte.parseByte(rowStr.substring(6));
        } catch (NumberFormatException e) {
            // Beat値が浮動小数点型ではない
            throw new IllegalUcsFormatException(rowNum);
        }

        // 譜面のブロックのSplit値の読み込みを行う
        short split;
        rowStr = br.readLine();
        if (rowStr == null) {
            // ucsファイルの終端に到達
            throw new EndOfUcsException(rowNum);
        }
        rowNum++;
        if (!rowStr.substring(0, 7).equals(":Split=")) {
            // ":Split="からスタートしていない
            throw new IllegalUcsFormatException(rowNum);
        }
        try {
            split= Short.parseShort(rowStr.substring(7));
        } catch (NumberFormatException e) {
            // Split値が浮動小数点型ではない
            throw new IllegalUcsFormatException(rowNum);
        }

        // 譜面のブロックのインスタンスを生成して返す
        return new UnitBlock(bpm, delay, beat, (byte) (split - 1), rowNum);
    }


    /**
     * 譜面のレイアウトに存在するブロック・ノートの全情報から、ucsファイルを書き込む
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param fileDir ucs/mp3ファイルが存在するディレクトリ
     */
    public void write(MainActivity mainActivity, String fileDir) {
        // ログ出力
        Log.d(TAG, "write:fileDir=" + fileDir);

        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        Process p = null;
        try {
            fw = new FileWriter(fileDir + "/" + fileName + ".ucs", false);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);

            // ucsファイルの1、2行目をセット
            pw.println(":Format=1\r");
            if (isPerformance) {
                pw.println((chartLayout.columnSize == 5) ? ":Mode=S-Performance\r" : ":Mode=D-Performance\r");
            } else {
                pw.println((chartLayout.columnSize == 5) ? ":Mode=Single\r" : ":Mode=Double\r");
            }

            // 全ブロックの行数の総和を初期化
            int accumulatedRow = 0;
            // 各列における(中抜き)ホールドのインスタンスを格納する配列をnullで初期化
            UnitNote[] holds = new UnitNote[10];
            // 各列における中抜きホールドの中間にかぶせる始点のリストのインデックスを0で初期化
            int[] hollowIdx = new int[10];

            for (UnitBlock unitBlock : chartLayout.blockList) {
                // ブロックの情報を格納
                if (unitBlock.bpm % 1 == 0.0f) {
                    pw.println(":BPM=" + String.valueOf((int) unitBlock.bpm) + '\r');
                } else {
                    pw.println(":BPM=" + String.valueOf(unitBlock.bpm) + '\r');
                }
                if (unitBlock.delay % 1 == 0.0f) {
                    pw.println(":Delay=" + String.valueOf((int) unitBlock.delay) + '\r');
                } else {
                    pw.println(":Delay=" + String.valueOf(unitBlock.delay) + '\r');
                }
                pw.println(":Beat=" + String.valueOf(unitBlock.beat) + '\r');
                pw.println(":Split=" + String.valueOf((short) (unitBlock.split + 1)) + '\r');

                for (int j = 0; j < unitBlock.rowLength; j++) {
                    accumulatedRow++;
                    for (byte k = (byte) (10 - chartLayout.columnSize); k < 10; k++) {
                        if (noteLayout.noteMap.containsKey(10 * accumulatedRow + k)) {
                            UnitNote unitNote = noteLayout.noteMap.get(10 * accumulatedRow + k);
                            if (unitNote.goal - unitNote.start > 0) {
                                // 指定した行、列番号に(中抜き)ホールドの始点がある場合
                                pw.print('M');
                                holds[k] = unitNote;
                            } else {
                                // 指定した行、列番号に単ノートがある場合
                                pw.print('X');
                            }
                        } else {
                            if (holds[k] == null) {
                                // 指定した行、列番号にノートおよび(中抜き)ホールドが無い場合
                                pw.print('.');
                            } else if (holds[k].goal == accumulatedRow) {
                                // 指定した行、列番号に(中抜き)ホールドの終点がある場合
                                pw.print('W');
                                holds[k] = null;
                                hollowIdx[k] = 0;
                            } else if (holds[k].hollowGoalList.size() > 0 && holds[k].hollowGoalList.get(hollowIdx[k]) <= accumulatedRow) {
                                // 指定した行、列番号が中抜きホールドの中間の途中でかぶせてない場合
                                pw.print('.');
                                if (hollowIdx[k] != holds[k].hollowStartList.size() - 1 && accumulatedRow + 1 == holds[k].hollowStartList.get(hollowIdx[k] + 1)) {
                                    // 次の行で再度かぶせる場合はインデックスをインクリメント
                                    hollowIdx[k]++;
                                }
                            } else {
                                // 指定した行、列番号が(中抜き)ホールドの途中の場合
                                pw.print('H');
                            }
                        }
                    }
                    pw.println('\r');
                }
            }

            // ucsファイルの権限を644にする
            p = Runtime.getRuntime().exec("chmod 644 " + fileDir + "/" + fileName + ".ucs");
            p.waitFor();

            if (this.fileDir == null) {
                // ucs、mp3ファイルが存在するディレクトリを更新
                this.fileDir = fileDir;
                // 「ucsファイル上書き保存」ボタンを表示
                ((ButtonsLayout) mainActivity.findViewById(R.id.buttonsLayout)).buttonFileSave.setVisibility(View.VISIBLE);
            }

            // 譜面編集回数を0にリセット
            chartLayout.editCount = 0;
            // メイン画面のアクションバーにあるucsファイル名のテキストビューを更新
            MainCommonFunctions.updateFileTextViewAtActionBar();

            // 正常に新規保存が完了した旨のトーストを出力
            Toast.makeText(mainActivity, mainActivity.getString(R.string.toast_save, fileDir + "/" + fileName + ".ucs"), Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            // ログ出力
            Log.e(TAG, e.getMessage(), e);

            // ucsファイル書き込み中に例外が発生した旨のトーストの出力
            Toast.makeText(mainActivity, R.string.toast_ioException_ucsWrite, Toast.LENGTH_SHORT).show();
        } finally {
            if (p != null) {
                p.destroy();
            }

            if (pw != null) {
                pw.close();
            }

            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイル書き込み中に例外が発生した旨のトーストの出力
                    Toast.makeText(mainActivity, R.string.toast_ioException_ucsWrite, Toast.LENGTH_SHORT).show();
                }
            }

            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイル書き込み中に例外が発生した旨のトーストの出力
                    Toast.makeText(mainActivity, R.string.toast_ioException_ucsWrite, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * ucsファイルが不正フォーマットだった例外クラス
     */
    private static final class IllegalUcsFormatException extends IllegalStateException {
        /**
         * 終端での行番号
         */
        int rowNum;

        /**
         * コンストラクタ
         *
         * @param rowNum 終端での行番号
         */
        IllegalUcsFormatException(int rowNum) {
            this.rowNum = rowNum;
        }
    }

    /**
     * ブロックのヘッダーの読み込み中にucsファイルの終端に到達した例外クラス
     */
    private static final class EndOfUcsException extends IllegalStateException {
        /**
         * 終端での行番号
         */
        int rowNum;

        /**
         * コンストラクタ
         *
         * @param rowNum 終端での行番号
         */
        EndOfUcsException(int rowNum) {
            this.rowNum = rowNum;
        }
    }
}
