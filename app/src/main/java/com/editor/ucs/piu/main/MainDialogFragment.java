package com.editor.ucs.piu.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.PointerLayout;
import com.editor.ucs.piu.chart.SelectedAreaLayout;
import com.editor.ucs.piu.unit.UnitBlock;

/**
 * メイン画面のアクティビティ上で表示するダイアログを扱うクラス
 */
public class MainDialogFragment extends DialogFragment {
    // デバッグ用のタグ
    private static final String TAG = "MainDialogFragment";

    /**
     * メイン画面のアクティビティ
     */
    private static MainActivity mainActivity;

    /**
     * メイン画面のアクティビティ上で表示するダイアログのインスタンスを生成する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param type         ダイアログのタイプ
     * @param title        ダイアログのタイトル
     * @param message      ダイアログのメッセージの文字列
     * @param args         メッセージの文字列で指定する引数
     * @return メイン画面のアクティビティ上で表示するダイアログのインスタンス
     */
    public static MainDialogFragment newInstance(MainActivity mainActivity, CommonDialogType type, int title, int message, Object... args) {
        MainDialogFragment thisFragment = new MainDialogFragment();
        MainDialogFragment.mainActivity = mainActivity;

        Bundle bundle = new Bundle();
        bundle.putSerializable(CommonParameters.BUNDLE_TYPE, type);
        bundle.putInt(CommonParameters.BUNDLE_TITLE, title);
        bundle.putString(CommonParameters.BUNDLE_MESSAGE, mainActivity.getString(message, args));
        thisFragment.setArguments(bundle);

        return thisFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // 譜面のレイアウトを取得
        final ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ポインターのレイアウトの取得
        final PointerLayout pointerLayout = mainActivity.findViewById(R.id.pointerLayout);
        // 選択領域のレイアウトの取得
        final SelectedAreaLayout selectedAreaLayout = mainActivity.findViewById(R.id.selectedAreaLayout);

        // ダイアログを生成し、タイトルを取得してセット
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        if (getArguments() != null) {
            builder.setTitle(getArguments().getInt(CommonParameters.BUNDLE_TITLE));
        }

        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new NullPointerException("inflater is null.");
        }

        // ダイアログの種類に応じて、アイコン・ボタンの文字列・ボタンのリスナーをセット
        final CommonDialogType type;
        if ((type = (CommonDialogType) getArguments().getSerializable(CommonParameters.BUNDLE_TYPE)) == null) {
            throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
        }
        // ログ出力
        Log.d(TAG, "onCreateDialog:type=" + type);

        switch (type) {
            case ALERT:
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(android.R.string.ok, null);

                return builder.create();
            case BUTTON_BLOCK_ADD:
                // 「ブロック追加」のダイアログのビューを取得
                final View blockAddView = inflater.inflate(R.layout.dialog_block_add, (ViewGroup) mainActivity.findViewById(R.id.blockAddLayout));

                // 現在ポインターで示している行番号の位置情報を取得
                final ChartLayout.PositionInformation blockAddInformation = chartLayout.calcPositionInformation(pointerLayout.row);

                // ポインターが示す譜面のブロックを取得
                UnitBlock addBlock = chartLayout.blockList.get(blockAddInformation.idxBlock);

                /*
                 * 変更後の情報を入力させるエディトテキストに、ポインターが示す譜面ブロックの情報と同じ値をセット
                 * ただし、Delay値は0.0をセット
                 */
                ((EditText) blockAddView.findViewById(R.id.blockAddRowLength)).setText(String.valueOf(addBlock.rowLength));
                ((EditText) blockAddView.findViewById(R.id.blockAddBpm)).setText(String.valueOf(addBlock.bpm));
                ((EditText) blockAddView.findViewById(R.id.blockAddDelay)).setText("0.0");
                ((EditText) blockAddView.findViewById(R.id.blockAddBeat)).setText(String.valueOf(addBlock.beat));
                ((EditText) blockAddView.findViewById(R.id.blockAddSplit)).setText(String.valueOf(addBlock.split + 1));

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(blockAddView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 「ブロック追加」のダイアログでの入力情報をもとに、ポインターが示している行の直後に譜面のブロックを新規追加
                                MainCommonFunctions.addBlockAtPointer(blockAddView, blockAddInformation);
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_BLOCK_DELETE:
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // ポインターが示している譜面のブロックを削除
                                MainCommonFunctions.deleteBlockAtPointer();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_BLOCK_MERGE:
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // ポインターが示している譜面のブロックと、その次の譜面のブロックを結合
                                MainCommonFunctions.mergeBlocksAtPointer();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_BLOCK_SETTING:
                // 「ブロック設定」のダイアログのビューを取得
                final View blockSettingView = inflater.inflate(R.layout.dialog_block_setting, (ViewGroup) mainActivity.findViewById(R.id.blockSettingLayout));

                // ポインターが示す行番号の位置情報を取得
                final ChartLayout.PositionInformation blockSettingInformation = chartLayout.calcPositionInformation(pointerLayout.row);

                // ポインターが示す譜面のブロックを取得
                UnitBlock settingBlock = chartLayout.blockList.get(blockSettingInformation.idxBlock);

                /*
                 * ポインターが示すブロックの情報を変更前のテキストビューにセット
                 * NOTE : byteの最大値は127なので、Splitは実際の値から1だけ引いた値を格納
                 */
                ((TextView) blockSettingView.findViewById(R.id.blockSettingBpmFrom)).setText(String.valueOf(settingBlock.bpm));
                ((TextView) blockSettingView.findViewById(R.id.blockSettingDelayFrom)).setText(String.valueOf(settingBlock.delay));
                ((TextView) blockSettingView.findViewById(R.id.blockSettingBeatFrom)).setText(String.valueOf(settingBlock.beat));
                ((TextView) blockSettingView.findViewById(R.id.blockSettingSplitFrom)).setText(String.valueOf(settingBlock.split + 1));

                // 変更後の情報を入力させるエディトテキストを取得
                EditText bpmTo = blockSettingView.findViewById(R.id.blockSettingBpmTo);
                EditText delayTo = blockSettingView.findViewById(R.id.blockSettingDelayTo);
                EditText beatTo = blockSettingView.findViewById(R.id.blockSettingBeatTo);
                EditText splitTo = blockSettingView.findViewById(R.id.blockSettingSplitTo);

                // それぞれのエディトテキストに変更前のテキストビューと同じ値をセット
                bpmTo.setText(String.valueOf(settingBlock.bpm));
                delayTo.setText(String.valueOf(settingBlock.delay));
                beatTo.setText(String.valueOf(settingBlock.beat));
                splitTo.setText(String.valueOf(settingBlock.split + 1));

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(blockSettingView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 「ブロック設定」のダイアログでの入力情報をもとに、ポインターが示している譜面のブロックの情報を更新
                                MainCommonFunctions.updateBlockSettingAtPointer(blockSettingView, blockSettingInformation);
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_BLOCK_SPLIT:
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // ポインターが示している譜面のブロックを分割
                                MainCommonFunctions.splitBlocksAtPointer();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_FILE_NEW:
                // 「ucsファイル新規作成」のダイアログのビューを取得
                final View newUcsView = inflater.inflate(R.layout.dialog_file_new, (ViewGroup) mainActivity.findViewById(R.id.newUcsLayout));

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(newUcsView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 「ucsファイル新規作成」のダイアログでの入力情報をもとにucsファイルを新規作成
                                MainCommonFunctions.createNewUcsFile(newUcsView);
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_FILE_NEW_CHANGED:
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(R.string.dialog_button_destroy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 変更された譜面データを破棄する場合、新規ucsファイル名と譜面形式を入力させるアラートダイアログの表示
                                MainDialogFragment.newInstance(mainActivity, CommonDialogType.BUTTON_FILE_NEW, R.string.dialog_title_createNewUcs, R.string.dialog_title_createNewUcs).show(mainActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_FILE_OPEN_CHANGED:
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(R.string.dialog_button_destroy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // ucsファイルのオープン処理を実行
                                Ucs.open(mainActivity);
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_FILE_RENAME_EXISTED:
                final EditText editText = new EditText(mainActivity);
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(mainActivity.ucs.fileName);

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(editText)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 入力したucsファイル名(拡張子.ucsは含まない)を取得してリネームする
                                MainCommonFunctions.renameUcsFileName(editText.getText().toString());
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case BUTTON_OTHER_PLAY_INITIALLY:
            case BUTTON_OTHER_PLAY_CURRENTLY:
                final View playConfirmationView = inflater.inflate(R.layout.dialog_play_confirmation, (ViewGroup) mainActivity.findViewById(R.id.playConfirmationLayout));

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(playConfirmationView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 再生したい位置を示す行番号を取得
                                int startRow = (type == CommonDialogType.BUTTON_OTHER_PLAY_INITIALLY) ? 1 : pointerLayout.row;
                                // 再生動作を行う
                                MainCommonFunctions.executePlaying(playConfirmationView, startRow);
                            }
                        });

                return builder.create();
            case BUTTON_OTHER_ZOOM:
                // 現在の譜面倍率を取得し、それを譜面倍率のラジオボタンのデフォルトとしてセット
                final View zoomSettingView = inflater.inflate(R.layout.dialog_zoom, (ViewGroup) mainActivity.findViewById(R.id.zoomLayout));
                final RadioGroup radioGroup = zoomSettingView.findViewById(R.id.zoomRadioGroup);
                ((RadioButton) radioGroup.getChildAt(PreferenceManager.getDefaultSharedPreferences(mainActivity).getInt(CommonParameters.PREFERENCE_ZOOM_INDEX, CommonParameters.PREFERENCE_ZOOM_INDEX_DEFAULT))).setChecked(true);

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(zoomSettingView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 選択したラジオボタンでの譜面倍率を基に、譜面のレイアウトを更新
                                MainCommonFunctions.updateZoom((RadioButton) zoomSettingView.findViewById(radioGroup.getCheckedRadioButtonId()));
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case ON_BACK_PRESSED:
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getArguments().getString(CommonParameters.BUNDLE_MESSAGE))
                        .setPositiveButton(R.string.dialog_button_destroy, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // 変更された譜面データを破棄する場合、MainActivityを終了
                                mainActivity.finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            case PASTE_COPIED_NOTES:
                final View copiedChartView = inflater.inflate(R.layout.dialog_paste_copied_notes, (ViewGroup) mainActivity.findViewById(R.id.copiedChartLayout));

                // 上下/左右回転を行うボタンのリスナーをセット
                copiedChartView.findViewById(R.id.copiedChartUpDownButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedAreaLayout.rotateCopiedNotes(mainActivity, copiedChartView, SelectedAreaLayout.RotationDirection.UP_DOWN);
                    }
                });
                copiedChartView.findViewById(R.id.copiedChartLeftRightButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedAreaLayout.rotateCopiedNotes(mainActivity, copiedChartView, SelectedAreaLayout.RotationDirection.LEFT_RIGHT);
                    }
                });

                // 確認ダイアログに存在するブロック・ノートのレイアウトをリセットする
                selectedAreaLayout.reset(mainActivity, copiedChartView);

                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setView(copiedChartView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // ポインターの示す場所からコピーしたノートを貼り付け、貼り付けた範囲を選択領域として表示する
                                selectedAreaLayout.pasteCopiedNotes(mainActivity);
                            }
                        }).setNegativeButton(android.R.string.cancel, null);

                return builder.create();
            default:
                throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
        }
    }
}