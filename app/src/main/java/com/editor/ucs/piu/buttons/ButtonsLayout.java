package com.editor.ucs.piu.buttons;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.ChartScrollView;
import com.editor.ucs.piu.chart.NoteLayout;
import com.editor.ucs.piu.chart.PointerLayout;
import com.editor.ucs.piu.chart.SelectedAreaLayout;
import com.editor.ucs.piu.download.DownloadActivity;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.main.MainCommonFunctions;
import com.editor.ucs.piu.main.MainDialogFragment;
import com.editor.ucs.piu.main.Ucs;
import com.editor.ucs.piu.unit.UnitBlock;
import com.editor.ucs.piu.unit.UnitProcess;
import com.editor.ucs.piu.setting.SettingActivity;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * メイン画面のボタン群のレイアウトを表すクラス
 */
public class ButtonsLayout extends LinearLayout implements AdapterView.OnItemSelectedListener {
    // デバッグ用のタグ
    private static final String TAG = "ButtonsLayout";

    /**
     * メイン画面のアクティビティ
     */
    private MainActivity mainActivity;

    /**
     * ボタン群のレイアウトに表示するボタンの種別を示すスピナー
     */
    public Spinner spinner;

    /**
     * 「ブロック最上段へ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveTop;
    /**
     * 「1個上ブロックへ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveUpperBlock;
    /**
     * 「(Beat値)個上へ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveUpperBeat;
    /**
     * 「1個上へ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveUpperRow;
    /**
     * 「ポインターへスクロール」ボタンのインスタンス
     */
    private RectangularButton buttonScrollPointer;
    /**
     * 「1個下へ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveLowerRow;
    /**
     * 「(Beat値)個下へ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveLowerBeat;
    /**
     * 「1個下ブロックへ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveLowerBlock;
    /**
     * 「ブロック最下段へ移動」ボタンのインスタンス
     */
    private RectangularButton buttonMoveBottom;

    /**
     * 「元に戻す」ボタンのインスタンス
     */
    private RectangularButton buttonEditUndo;
    /**
     * 「やり直し」ボタンのインスタンス
     */
    private RectangularButton buttonEditRedo;
    /**
     * 「譜面選択モードON、OFF」トグルボタンのインスタンス
     */
    public RectangularToggleButton toggleButtonEditSelect;
    /**
     * 「譜面編集ロックON、OFF」トグルボタンのインスタンス
     */
    private RectangularToggleButton toggleButtonEditLock;
    /**
     * 「選択範囲を上下回転」ボタンのインスタンス
     */
    private RectangularButton buttonEditUpDown;
    /**
     * 「選択範囲を左右回転」ボタンのインスタンス
     */
    private RectangularButton buttonEditLeftRight;
    /**
     * 「選択範囲を削除」ボタンのインスタンス
     */
    private RectangularButton buttonEditDelete;
    /**
     * 「選択範囲を切取り」ボタンのインスタンス
     */
    private RectangularButton buttonEditCut;
    /**
     * 「選択範囲をコピー」ボタンのインスタンス
     */
    private RectangularButton buttonEditCopy;
    /**
     * 「貼り付け」ボタンのインスタンス
     */
    public RectangularButton buttonEditPaste;

    /**
     * 「ブロック追加」ボタンのインスタンス
     */
    public RectangularButton buttonBlockAdd;
    /**
     * 「ブロック設定」ボタンのインスタンス
     */
    private RectangularButton buttonBlockSetting;
    /**
     * 「ブロック分割」ボタンのインスタンス
     */
    private RectangularButton buttonBlockSplit;
    /**
     * 「ブロック結合」ボタンのインスタンス
     */
    private RectangularButton buttonBlockMerge;
    /**
     * 「ブロック削除」ボタンのインスタンス
     */
    public RectangularButton buttonBlockDelete;

    /**
     * 「ucsファイル新規作成」ボタンのインスタンス
     */
    private RectangularButton buttonFileNew;
    /**
     * 「ucsファイルを開く」ボタンのインスタンス
     */
    private RectangularButton buttonFileOpen;
    /**
     * 「ucsファイル名前変更」ボタンのインスタンス
     */
    private RectangularButton buttonFileRename;
    /**
     * 「ucsファイル上書き保存」ボタンのインスタンス
     */
    public RectangularButton buttonFileSave;
    /**
     * 「ucsファイル別ディレクトリで保存」ボタンのインスタンス
     */
    public RectangularButton buttonFileSaveAs;
    /**
     * 「ucsファイルサンプルダウンロード」ボタンのインスタンス
     */
    private RectangularButton buttonFileDownload;

    /**
     * 「ノート音ON、OFF」トグルボタンのインスタンス
     */
    public RectangularToggleButton toggleButtonOtherNoteSound;
    /**
     * 「最初の位置から再生」ボタンのインスタンス
     */
    public RectangularButton buttonOtherPlayInitially;
    /**
     * 「ポインターの位置から再生」ボタンのインスタンス
     */
    public RectangularButton buttonOtherPlayCurrently;
    /**
     * 「譜面再生を中断」ボタンのインスタンス
     */
    public RectangularButton buttonOtherInterrupt;
    /**
     * 「譜面倍率変更」ボタンのインスタンス
     */
    public RectangularButton buttonOtherZoom;
    /**
     * 「詳細設定」ボタンのインスタンス
     */
    public RectangularButton buttonOtherSetting;

    /**
     * スピナーで選択されているビューを表すリスト
     */
    public List<View> viewList = new ArrayList<>(0);

    public ButtonsLayout(Context context) {
        super(context);
    }

    public ButtonsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * ボタン群のレイアウトの初期化処理をMainActivity#onCreate()実行時に行う
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void initializeOnCreate(final MainActivity mainActivity) {
        // メイン画面のアクティビティをセット
        this.mainActivity = mainActivity;

        // 譜面のスクロールビューを取得
        final ChartScrollView chartScrollView = mainActivity.findViewById(R.id.chartScrollView);
        // 譜面のレイアウトを取得
        final ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ポインターのレイアウトを取得
        final PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        final SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // アクションバーを取得できた場合は、そのカスタムビューをセット
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(inflate(mainActivity, R.layout.action_bar_main, null));
            actionBar.setDisplayShowCustomEnabled(true);
        }

        // ボタン群のレイアウトに表示するボタンの種別を示すスピナーをアクションバーの有無によって取得
        spinner = (actionBar != null) ? (Spinner) mainActivity.findViewById(R.id.actionBarSpinner) : (Spinner) mainActivity.findViewById(R.id.sideBarSpinner);
        // プルダウンが選択した時に起動するリスナーをセット
        spinner.setOnItemSelectedListener(this);

        // プリファレンスのインスタンスを取得
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);

        // 「ブロック最上段へ移動」ボタンを取得してリスナーをセット
        buttonMoveTop = mainActivity.findViewById(R.id.buttonMoveTop);
        buttonMoveTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 最初のブロックの上端へポインターの位置を動かす
                if (pointerLayout.row > 1) {
                    int preRow = pointerLayout.row;
                    pointerLayout.row = 1;
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「1個上ブロックへ移動」ボタンを取得してリスナーをセット
        buttonMoveUpperBlock = mainActivity.findViewById(R.id.buttonMoveUpperBlock);
        buttonMoveUpperBlock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1個上のブロックの下端へポインターの位置を動かす
                if (pointerLayout.row > 1) {
                    int preRow = pointerLayout.row;
                    if (pointerLayout.row > chartLayout.blockList.get(0).rowLength) {
                        int accumulatedRow = 0;
                        for (UnitBlock unitBlock : chartLayout.blockList) {
                            accumulatedRow += unitBlock.rowLength;
                            if (pointerLayout.row <= accumulatedRow) {
                                pointerLayout.row = accumulatedRow - unitBlock.rowLength;
                                break;
                            }
                        }
                    } else {
                        pointerLayout.row = 1;
                    }
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「(Beat値)個上へ移動」ボタンを取得してリスナーをセット
        buttonMoveUpperBeat = mainActivity.findViewById(R.id.buttonMoveUpperBeat);
        buttonMoveUpperBeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // (Beat値)個上のノートへポインターの位置を動かす
                if (pointerLayout.row > 1) {
                    int preRow = pointerLayout.row;
                    if (pointerLayout.row - pointerLayout.beat > 1) {
                        pointerLayout.row -= pointerLayout.beat;
                    } else {
                        pointerLayout.row = 1;
                    }
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「1個上へ移動」ボタンを取得してリスナーをセット
        buttonMoveUpperRow = mainActivity.findViewById(R.id.buttonMoveUpperRow);
        buttonMoveUpperRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1個上のノートへポインターの位置を動かす
                if (pointerLayout.row > 1) {
                    int preRow = pointerLayout.row;
                    pointerLayout.row -= 1;
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「ポインターへスクロール」ボタンを取得してリスナーをセット
        buttonScrollPointer = mainActivity.findViewById(R.id.buttonScrollPointer);
        buttonScrollPointer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポインターの位置での位置情報を計算し、そこへスクロールビューを動かす
                chartScrollView.smoothScrollTo(0, (int) chartLayout.calcPositionInformation(pointerLayout.row).coordinate);
            }
        });

        // 「1個下へ移動」ボタンを取得してリスナーをセット
        buttonMoveLowerRow = mainActivity.findViewById(R.id.buttonMoveLowerRow);
        buttonMoveLowerRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1個下のノートへポインターの位置を動かす
                int sum = 0;
                for (UnitBlock unitBlock : chartLayout.blockList)
                    sum += unitBlock.rowLength;
                if (pointerLayout.row != sum) {
                    int preRow = pointerLayout.row;
                    pointerLayout.row += 1;
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「(Beat値)個下へ移動」ボタンを取得してリスナーをセット
        buttonMoveLowerBeat = mainActivity.findViewById(R.id.buttonMoveLowerBeat);
        buttonMoveLowerBeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // (Beat値)個下のノートへポインターの位置を動かす
                int sum = 0;
                for (UnitBlock unitBlock : chartLayout.blockList)
                    sum += unitBlock.rowLength;
                if (pointerLayout.row != sum) {
                    int preRow = pointerLayout.row;
                    if (pointerLayout.row + pointerLayout.beat < sum) {
                        pointerLayout.row += pointerLayout.beat;
                    } else {
                        pointerLayout.row = sum;
                    }
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「1個上ブロックへ移動」ボタンを取得してリスナーをセット
        buttonMoveLowerBlock = mainActivity.findViewById(R.id.buttonMoveLowerBlock);
        buttonMoveLowerBlock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1個下のブロックの上端へポインターの位置を動かす
                int accumulatedRow = 0;
                for (int i = 0; i < chartLayout.blockList.size(); i++) {
                    accumulatedRow += chartLayout.blockList.get(i).rowLength;
                    if (pointerLayout.row <= accumulatedRow) {
                        int preRow = pointerLayout.row;
                        if (i != chartLayout.blockList.size() - 1) {
                            pointerLayout.row = accumulatedRow + 1;
                        } else {
                            pointerLayout.row = accumulatedRow;
                        }
                        pointerLayout.update(mainActivity);
                        checkTexts(mainActivity, preRow);
                        break;
                    }
                }
            }
        });

        // 「ブロック最下段へ移動」ボタンを取得してリスナーをセット
        buttonMoveBottom = mainActivity.findViewById(R.id.buttonMoveBottom);
        buttonMoveBottom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 最後のブロックの下端へポインターの位置を動かす
                int sum = 0;
                for (UnitBlock unitBlock : chartLayout.blockList) sum += unitBlock.rowLength;
                if (pointerLayout.row != sum) {
                    int preRow = pointerLayout.row;
                    pointerLayout.row = sum;
                    pointerLayout.update(mainActivity);
                    checkTexts(mainActivity, preRow);
                }
            }
        });

        // 「元に戻す」ボタンを取得してリスナーをセット
        buttonEditUndo = mainActivity.findViewById(R.id.buttonEditUndo);
        buttonEditUndo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 「元に戻す」ボタンで扱う譜面編集プロセスが残り1つの場合、そのボタンを非表示にする
                if (chartLayout.undoProcessStack.size() == 1) {
                    buttonEditUndo.setVisibility(GONE);
                }
                // 「元に戻す」ボタンで扱う譜面編集プロセスをポップして、元に戻す
                UnitProcess process = chartLayout.undoProcessStack.pop();
                process.undo(mainActivity);
                // 「やり直し」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
                chartLayout.redoProcessStack.push(process);
                // 「やり直し」ボタンを表示する
                buttonEditRedo.setVisibility(VISIBLE);
            }
        });

        // 「やり直し」ボタンを取得してリスナーをセット
        buttonEditRedo = mainActivity.findViewById(R.id.buttonEditRedo);
        buttonEditRedo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 「やり直し」ボタンで扱う譜面編集プロセスが残り1つの場合、そのボタンを非表示にする
                if (chartLayout.redoProcessStack.size() == 1) {
                    buttonEditRedo.setVisibility(GONE);
                }
                // 「やり直し」ボタンで扱う譜面編集プロセスをポップして、元に戻す
                UnitProcess process = chartLayout.redoProcessStack.pop();
                process.redo(mainActivity);
                // 「元に戻す」ボタンで扱う譜面編集プロセスのインスタンスをプッシュ
                chartLayout.undoProcessStack.push(process);
                // 「元に戻す」ボタンを表示する
                buttonEditUndo.setVisibility(VISIBLE);
            }
        });

        // 「譜面選択モードON、OFF」トグルボタンを取得してリスナーをセット
        toggleButtonEditSelect = mainActivity.findViewById(R.id.toggleButtonEditSelect);
        toggleButtonEditSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // トグルボタンがOFFからONとなった場合
                    pointerLayout.setBackgroundColor(Color.rgb(
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_RED, CommonParameters.PREFERENCE_SELECTED_POINTER_RED_DEFAULT),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN, CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN_DEFAULT),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE, CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE_DEFAULT)));
                    pointerLayout.getBackground().setAlpha(sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_ALPHA, CommonParameters.PREFERENCE_SELECTED_POINTER_ALPHA_DEFAULT));
                } else {
                    // トグルボタンがONからOFFとなった場合
                    pointerLayout.setBackgroundColor(Color.rgb(
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_RED, CommonParameters.PREFERENCE_POINTER_RED_DEFAULT),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_GREEN, CommonParameters.PREFERENCE_POINTER_GREEN_DEFAULT),
                            sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_BLUE, CommonParameters.PREFERENCE_POINTER_BLUE_DEFAULT)));
                    pointerLayout.getBackground().setAlpha(sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_ALPHA, CommonParameters.PREFERENCE_POINTER_ALPHA_DEFAULT));

                    // 選択領域の1点だけが選択された場合は1点の選択領域を削除し、選択領域とポインターのレイアウトを更新する
                    if (selectedAreaLayout.selectedEdge[0] > 0 && selectedAreaLayout.selectedEdge[1] == 0) {
                        selectedAreaLayout.selectedEdge = new int[2];
                        selectedAreaLayout.update(mainActivity);
                        pointerLayout.update(mainActivity);
                    }
                }
            }
        });

        // 「譜面編集ロックON、OFF」トグルボタンを取得
        toggleButtonEditLock = mainActivity.findViewById(R.id.toggleButtonEditLock);

        // 「選択範囲を上下回転」ボタンを取得してリスナーをセット
        buttonEditUpDown = mainActivity.findViewById(R.id.buttonEditUpDown);
        buttonEditUpDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択領域に存在するノートを上下方向に回転する
                selectedAreaLayout.rotateSelectedArea(mainActivity, SelectedAreaLayout.RotationDirection.UP_DOWN);
            }
        });

        // 「選択範囲を左右回転」ボタンを取得してリスナーをセット
        buttonEditLeftRight = mainActivity.findViewById(R.id.buttonEditLeftRight);
        buttonEditLeftRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択領域に存在するノートを左右方向に回転する
                selectedAreaLayout.rotateSelectedArea(mainActivity, SelectedAreaLayout.RotationDirection.LEFT_RIGHT);
            }
        });

        // 「選択範囲を削除」ボタンを取得してリスナーをセット
        buttonEditDelete = mainActivity.findViewById(R.id.buttonEditDelete);
        buttonEditDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択領域に存在するノートの情報を削除する
                selectedAreaLayout.removeSelectedNotes(mainActivity);
            }
        });

        // 「選択範囲を切取り」ボタンを取得してリスナーをセット
        buttonEditCut = mainActivity.findViewById(R.id.buttonEditCut);
        buttonEditCut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択領域に存在するノートの情報を切り取る
                selectedAreaLayout.copySelectedNotes(mainActivity);
                selectedAreaLayout.removeSelectedNotes(mainActivity);
                Toast.makeText(mainActivity, R.string.toast_copyFinished, Toast.LENGTH_SHORT).show();
            }
        });

        // 「選択範囲をコピー」ボタンを取得してリスナーをセット
        buttonEditCopy = mainActivity.findViewById(R.id.buttonEditCopy);
        buttonEditCopy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 選択領域に存在するノートの情報をコピーする
                selectedAreaLayout.copySelectedNotes(mainActivity);
                Toast.makeText(mainActivity, R.string.toast_copyFinished, Toast.LENGTH_SHORT).show();
            }
        });

        // 「貼り付け」ボタンを取得してリスナーをセット
        buttonEditPaste = mainActivity.findViewById(R.id.buttonEditPaste);
        buttonEditPaste.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAreaLayout.copiedLength > 0) {
                    // 現在のポインターの位置からコピーしたノートを貼り付ける確認ダイアログの表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.PASTE_COPIED_NOTES, R.string.dialog_title_checkPasteNotes, R.string.dialog_title_checkPasteNotes).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    Toast.makeText(mainActivity, R.string.toast_notCopiedError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 「ブロック追加」ボタンを取得してリスナーをセット
        buttonBlockAdd = mainActivity.findViewById(R.id.buttonBlockAdd);
        buttonBlockAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポインターの位置での次の行数から、入力した情報に基づくブロックを追加する確認ダイアログの表示
                MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_BLOCK_ADD, R.string.button_block_add, R.string.button_block_add).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
            }
        });

        // 「ブロック設定」ボタンを取得してリスナーをセット
        buttonBlockSetting = mainActivity.findViewById(R.id.buttonBlockSetting);
        buttonBlockSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポインターの位置でのブロックを設定するダイアログの表示
                MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_BLOCK_SETTING, R.string.button_block_setting, R.string.button_block_setting).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
            }
        });

        // 「ブロック分割」ボタンを取得してリスナーをセット
        buttonBlockSplit = mainActivity.findViewById(R.id.buttonBlockSplit);
        buttonBlockSplit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポインターの位置での位置情報を取得
                ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(pointerLayout.row);

                // ポインターの位置が各ブロックの一番上かどうかチェック
                if (pointerLayout.row != information.offsetRowLength + 1) {
                    // ポインターの位置での直上でブロックを分割する確認ダイアログの表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_BLOCK_SPLIT, R.string.button_block_split, R.string.dialog_message_blockSplit).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // ポインターの位置が一番上のため分割できない旨のトーストを出力
                    Toast.makeText(mainActivity, R.string.toast_blockSplitError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 「ブロック結合」ボタンを取得してリスナーをセット
        buttonBlockMerge = mainActivity.findViewById(R.id.buttonBlockMerge);
        buttonBlockMerge.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポインターの位置での位置情報を取得
                ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(pointerLayout.row);

                // ポインターの位置が最後尾のブロックかどうかチェック
                if (information.idxBlock != chartLayout.blockList.size() - 1) {
                    // ポインターの位置でのブロックと、その1つ後のブロックを結合する確認ダイアログの表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_BLOCK_MERGE, R.string.button_block_merge, R.string.dialog_message_blockMerge).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // ポインターの位置が最後尾のブロックのため結合できない旨のトーストを出力
                    Toast.makeText(mainActivity, R.string.toast_blockMergeError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 「ブロック削除」ボタンを取得してリスナーをセット
        buttonBlockDelete = mainActivity.findViewById(R.id.buttonBlockDelete);
        buttonBlockDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ブロックが残り1個しか無いかどうかチェック
                if (chartLayout.blockList.size() > 1) {
                    // ポインターの位置でのブロックを削除するダイアログの表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_BLOCK_DELETE, R.string.button_block_delete, R.string.dialog_message_blockDelete).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // ブロックが1個しか存在しないため削除できない旨のトーストを出力
                    Toast.makeText(mainActivity, R.string.toast_blockDeleteError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 「ucsファイル新規作成」ボタンを取得してリスナーをセット
        buttonFileNew = mainActivity.findViewById(R.id.buttonFileNew);
        buttonFileNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chartLayout.editCount != 0) {
                    // 譜面データが1回でも変更した(ChartScrollView.isChangedのフラグがONの)状態で、譜面データを破棄するかどうかのダイアログを表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_FILE_NEW_CHANGED, R.string.dialog_title_destroy, R.string.dialog_message_destroy).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // 新規ucsファイル名と譜面形式を入力させるアラートダイアログの表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_FILE_NEW, R.string.dialog_title_createNewUcs, R.string.dialog_title_createNewUcs).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                }
            }
        });

        // 「ucsファイルを開く」ボタンを取得してリスナーをセット
        buttonFileOpen = mainActivity.findViewById(R.id.buttonFileOpen);
        buttonFileOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chartLayout.editCount != 0) {
                    // 譜面データが1回でも変更した状態で、譜面データを破棄するかどうかのダイアログを表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_FILE_OPEN_CHANGED, R.string.dialog_title_destroy, R.string.dialog_message_destroy).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // ucsファイルのオープン処理を実行
                    Ucs.open(mainActivity);
                }
            }
        });

        // 「ucsファイル名前変更」ボタンを取得してリスナーをセット
        buttonFileRename = mainActivity.findViewById(R.id.buttonFileRename);
        buttonFileRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 譜面データが存在する(SideBar.isExistedのフラグがONの)状態で、ucsファイルのファイル名変更のダイアログを表示
                MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_FILE_RENAME_EXISTED, R.string.dialog_title_changeUcsName, R.string.textView_newUcs_name).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
            }
        });

        // 「ucsファイル上書き保存」ボタンを取得してリスナーをセット
        buttonFileSave = mainActivity.findViewById(R.id.buttonFileSave);
        buttonFileSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 外部ストレージにあるファイルを書き込むパーミッションをリクエスト
                if (MainCommonFunctions.requestMainPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, CommonParameters.PERMISSION_UCS_SAVE)) {
                    // ucsファイルを書き込む
                    mainActivity.ucs.write(mainActivity, mainActivity.ucs.fileDir);
                }
            }
        });

        // 「ucsファイル別ディレクトリで保存」ボタンを取得してリスナーをセット
        buttonFileSaveAs = mainActivity.findViewById(R.id.buttonFileSaveAs);
        buttonFileSaveAs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 外部ストレージにある別ディレクトリへファイルを書き込むパーミッションをリクエスト
                if (MainCommonFunctions.requestMainPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, CommonParameters.PERMISSION_UCS_SAVE_AS)) {
                    // リクエストするダイアログが表示しなかった場合は、ディレクトリを指定させるダイアログの表示
                    new ChooserDialog().with(mainActivity)
                            .withFilter(true, false)
                            .withStartFile(Environment.getExternalStorageDirectory().getPath())
                            .withRowLayoutView(R.layout.item_chooser)
                            .withResources(R.string.dialog_title_selectDirectory, android.R.string.ok, android.R.string.cancel)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String path, File pathFile) {
                                    // 指定したディレクトリに対し、ucsファイルを生成する
                                    mainActivity.ucs.write(mainActivity, path);
                                }
                            }).build().show();
                }
            }
        });

        // 「ucsファイルサンプルダウンロード」ボタンを取得してリスナーをセット
        buttonFileDownload = mainActivity.findViewById(R.id.buttonFileDownload);
        buttonFileDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // サンプルのucsファイルをダウンロードできるアクティビティへ明示的インテント
                mainActivity.startActivity(new Intent(mainActivity, DownloadActivity.class));
            }
        });

        // 「ノート音ON、OFF」トグルボタンを取得
        toggleButtonOtherNoteSound = mainActivity.findViewById(R.id.toggleButtonOtherNoteSound);

        // 「最初の位置から再生」ボタンを取得してリスナーをセット
        buttonOtherPlayInitially = mainActivity.findViewById(R.id.buttonOtherPlayInitially);
        buttonOtherPlayInitially.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButtonOtherNoteSound.isChecked() && !sharedPreferences.getBoolean(CommonParameters.PREFERENCE_PLAY_CONFIRMATION, CommonParameters.PREFERENCE_PLAY_CONFIRMATION_DEFAULT)) {
                    // 譜面再生確認ダイアログを表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_OTHER_PLAY_INITIALLY, R.string.button_other_playInitially, R.string.button_other_playInitially).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // 最初の位置から譜面を再生する
                    chartScrollView.play(mainActivity, 1);
                }
            }
        });

        // 「ポインターの位置から再生」ボタンを取得してリスナーをセット
        buttonOtherPlayCurrently = mainActivity.findViewById(R.id.buttonOtherPlayCurrently);
        buttonOtherPlayCurrently.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButtonOtherNoteSound.isChecked() && !sharedPreferences.getBoolean(CommonParameters.PREFERENCE_PLAY_CONFIRMATION, CommonParameters.PREFERENCE_PLAY_CONFIRMATION_DEFAULT)) {
                    // 譜面再生確認ダイアログを表示
                    MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_OTHER_PLAY_CURRENTLY, R.string.button_other_playCurrently, R.string.button_other_playCurrently).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                } else {
                    // ポインターの位置から譜面を再生する
                    chartScrollView.play(mainActivity, pointerLayout.row);
                }
            }
        });

        // 「譜面再生を中断」ボタンを取得してリスナーをセット
        buttonOtherInterrupt = mainActivity.findViewById(R.id.buttonOtherInterrupt);
        buttonOtherInterrupt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 譜面の再生を中断する
                chartScrollView.interrupt();

                // 「譜面の再生を中断」ボタンを非表示にする
                buttonOtherInterrupt.setVisibility(GONE);

                // 「最初の位置から再生」、「ポインターの位置から再生」、「譜面倍率変更」、「詳細設定」ボタンを表示する
                buttonOtherPlayInitially.setVisibility(VISIBLE);
                buttonOtherPlayCurrently.setVisibility(VISIBLE);
                buttonOtherZoom.setVisibility(VISIBLE);
                buttonOtherSetting.setVisibility(VISIBLE);

                // サイドバーのボタンの内容を示すスピナーを有効にする
                spinner.setEnabled(true);
            }
        });

        // 「譜面倍率変更」ボタンを取得してリスナーをセット
        buttonOtherZoom = mainActivity.findViewById(R.id.buttonOtherZoom);
        buttonOtherZoom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 譜面データが存在する場合、譜面倍率変更のダイアログを表示
                MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_OTHER_ZOOM, R.string.button_other_zoom, R.string.button_other_zoom).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);

            }
        });

        // 「詳細設定」ボタンを取得してリスナーをセット
        buttonOtherSetting = mainActivity.findViewById(R.id.buttonOtherSetting);
        buttonOtherSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 詳細設定のアクティビティへ明示的インテント
                mainActivity.startActivity(new Intent(mainActivity, SettingActivity.class));
            }
        });
    }

    /**
     * ポインターの移動により、3つのテキストビューとボタンの(Beat値)のテキストを変更するかどうかのチェック
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param preRow	ポインターを移動する前の行番号
     */
    private void checkTexts(MainActivity mainActivity, int preRow) {
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ポインターのレイアウトを取得
        PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);

        int accumulatedRow = 0;
        if (preRow < pointerLayout.row) {
            // 下へ移動した場合
            for (UnitBlock unitBlock : chartLayout.blockList) {
                accumulatedRow += unitBlock.rowLength;
                if (preRow <= accumulatedRow && pointerLayout.row > accumulatedRow) {
                    // ポインターの位置が示すブロックの情報を更新
                    MainCommonFunctions.updateTextsAtPointer();
                } else if (pointerLayout.row <= accumulatedRow) {
                    break;
                }
            }
        } else if (preRow > pointerLayout.row) {
            // 上へ移動した場合
            for (UnitBlock unitBlock : chartLayout.blockList) accumulatedRow += unitBlock.rowLength;
            for (int i = chartLayout.blockList.size() - 1; i > -1; i--) {
                accumulatedRow -= chartLayout.blockList.get(i).rowLength;
                if (preRow > accumulatedRow && pointerLayout.row <= accumulatedRow) {
                    // ポインターの位置が示すブロックの情報を更新
                    MainCommonFunctions.updateTextsAtPointer();
                } else if (preRow > accumulatedRow) {
                    break;
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
        // 選択前のビューをすべて非表示にする
        for (View view : viewList) {
            view.setVisibility(GONE);
        }
        // 選択前のビューを格納しているリストをすべて削除
        viewList.clear();

        // 選択したアイテムの添え字を取得
        int position = adapterView.getSelectedItemPosition();
        // ログ出力
        Log.d(TAG, "onItemSelected:position=" + position);
        switch (position) {
            // 「移動」が選択されたor譜面のレイアウトがメイン画面に表示されていない場合
            case 0:
                // 「移動」が選択された場合
                if (mainActivity.ucs != null) {
                    showMoveButtons();
                }
                // 譜面のレイアウトがメイン画面に表示されていない場合
                else {
                    // 「ucsファイル新規作成」、「ucsファイルを開く」、「ucsファイルサンプルダウンロード」、「詳細設定」のボタンを表示
                    buttonFileNew.setVisibility(VISIBLE);
                    buttonFileOpen.setVisibility(VISIBLE);
                    buttonFileDownload.setVisibility(VISIBLE);
                    buttonOtherSetting.setVisibility(VISIBLE);

                    // 上記のボタンを現在表示中のリストに格納
                    viewList.add(buttonFileNew);
                    viewList.add(buttonFileOpen);
                    viewList.add(buttonFileDownload);
                    viewList.add(buttonOtherSetting);
                }

                break;
            // 「編集」が選択された場合
            case 1:
                // 編集関連のボタンを表示
                if (!((ChartLayout) mainActivity.findViewById(R.id.chartLayout)).undoProcessStack.isEmpty()) {
                    buttonEditUndo.setVisibility(VISIBLE);
                }
                if (!((ChartLayout) mainActivity.findViewById(R.id.chartLayout)).redoProcessStack.isEmpty()) {
                    buttonEditRedo.setVisibility(VISIBLE);
                }
                if (Arrays.equals(((NoteLayout) mainActivity.findViewById(R.id.noteLayout)).holdEdge, new int[10])) {
                    toggleButtonEditSelect.setVisibility(VISIBLE);
                    buttonEditPaste.setVisibility(VISIBLE);
                }
                toggleButtonEditLock.setVisibility(VISIBLE);
                if (((SelectedAreaLayout) mainActivity.findViewById(R.id.selectedAreaLayout)).selectedEdge[1] > 0) {
                    buttonEditUpDown.setVisibility(VISIBLE);
                    buttonEditLeftRight.setVisibility(VISIBLE);
                    buttonEditDelete.setVisibility(VISIBLE);
                    buttonEditCut.setVisibility(VISIBLE);
                    buttonEditCopy.setVisibility(VISIBLE);
                }

                // 編集関連のボタンを現在表示中のリストに格納
                viewList.add(buttonEditUndo);
                viewList.add(buttonEditRedo);
                viewList.add(toggleButtonEditSelect);
                viewList.add(toggleButtonEditLock);
                viewList.add(buttonEditUpDown);
                viewList.add(buttonEditLeftRight);
                viewList.add(buttonEditDelete);
                viewList.add(buttonEditCut);
                viewList.add(buttonEditCopy);
                viewList.add(buttonEditPaste);

                break;
            // 「ブロック」が選択された場合
            case 2:
                // ブロック関連のボタンを表示
                if (Arrays.equals(((NoteLayout) mainActivity.findViewById(R.id.noteLayout)).holdEdge, new int[10])) {
                    buttonBlockAdd.setVisibility(VISIBLE);
                    buttonBlockDelete.setVisibility(VISIBLE);
                }
                buttonBlockSetting.setVisibility(VISIBLE);
                buttonBlockSplit.setVisibility(VISIBLE);
                buttonBlockMerge.setVisibility(VISIBLE);

                // ブロック関連のボタンを現在表示中のリストに格納
                viewList.add(buttonBlockAdd);
                viewList.add(buttonBlockSetting);
                viewList.add(buttonBlockSplit);
                viewList.add(buttonBlockMerge);
                viewList.add(buttonBlockDelete);

                break;
            // 「ファイル」が選択された場合
            case 3:
                // ファイル入出力関連のボタンを表示する
                buttonFileNew.setVisibility(VISIBLE);
                buttonFileOpen.setVisibility(VISIBLE);
                buttonFileRename.setVisibility(VISIBLE);
                if (Arrays.equals(((NoteLayout) mainActivity.findViewById(R.id.noteLayout)).holdEdge, new int[10])) {
                    if (mainActivity.ucs.fileDir != null) {
                        buttonFileSave.setVisibility(VISIBLE);
                    }
                    buttonFileSaveAs.setVisibility(VISIBLE);
                }
                buttonFileDownload.setVisibility(VISIBLE);

                // ファイル入出力関連のボタンを現在表示中のリストに格納
                viewList.add(buttonFileNew);
                viewList.add(buttonFileOpen);
                viewList.add(buttonFileRename);
                viewList.add(buttonFileSave);
                viewList.add(buttonFileSaveAs);
                viewList.add(buttonFileDownload);

                break;
            // 「その他」が選択された場合
            case 4:
                // 上記以外のボタンを表示
                if (Arrays.equals(((NoteLayout) mainActivity.findViewById(R.id.noteLayout)).holdEdge, new int[10])) {
                    toggleButtonOtherNoteSound.setVisibility(VISIBLE);
                    buttonOtherPlayInitially.setVisibility(VISIBLE);
                    buttonOtherPlayCurrently.setVisibility(VISIBLE);
                }
                buttonOtherZoom.setVisibility(VISIBLE);
                buttonOtherSetting.setVisibility(VISIBLE);

                // 上記以外のボタンを現在表示中のリストに格納
                viewList.add(toggleButtonOtherNoteSound);
                viewList.add(buttonOtherPlayInitially);
                viewList.add(buttonOtherPlayCurrently);
                viewList.add(buttonOtherZoom);
                viewList.add(buttonOtherSetting);

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    /**
     * ボタン群のレイアウトに移動関連のボタンを表示する
     */
    public void showMoveButtons() {
        // 選択前のビューをすべて非表示にする
        for (View view : viewList) {
            view.setVisibility(GONE);
        }
        // 選択前のビューを格納しているリストをすべて削除
        viewList.clear();

        // 移動関連のボタンを表示
        buttonMoveTop.setVisibility(VISIBLE);
        buttonMoveUpperBlock.setVisibility(VISIBLE);
        buttonMoveUpperBeat.setVisibility(VISIBLE);
        buttonMoveUpperRow.setVisibility(VISIBLE);
        buttonScrollPointer.setVisibility(VISIBLE);
        buttonMoveLowerRow.setVisibility(VISIBLE);
        buttonMoveLowerBeat.setVisibility(VISIBLE);
        buttonMoveLowerBlock.setVisibility(VISIBLE);
        buttonMoveBottom.setVisibility(VISIBLE);

        // 移動関連のボタンを現在表示中のリストに格納
        viewList.add(buttonMoveTop);
        viewList.add(buttonMoveUpperBlock);
        viewList.add(buttonMoveUpperBeat);
        viewList.add(buttonMoveUpperRow);
        viewList.add(buttonScrollPointer);
        viewList.add(buttonMoveLowerRow);
        viewList.add(buttonMoveLowerBeat);
        viewList.add(buttonMoveLowerBlock);
        viewList.add(buttonMoveBottom);
    }
}
