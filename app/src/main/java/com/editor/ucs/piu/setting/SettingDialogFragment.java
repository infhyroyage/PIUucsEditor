package com.editor.ucs.piu.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.main.MainCommonFunctions;

/**
 * 詳細設定のアクティビティ上で表示するダイアログを扱うクラス
 */
public class SettingDialogFragment extends DialogFragment {
    // デバッグ用のタグ
    private static final String TAG = "SettingDialogFragment";

    /**
     * 設定画面のアクティビティ
     */
    private static SettingActivity settingActivity;

    static SettingDialogFragment newInstance(SettingActivity settingActivity, CommonDialogType type, int title) {
        SettingDialogFragment thisFragment = new SettingDialogFragment();
        SettingDialogFragment.settingActivity = settingActivity;

        Bundle bundle = new Bundle();
        bundle.putSerializable(CommonParameters.BUNDLE_TYPE, type);
        bundle.putInt(CommonParameters.BUNDLE_TITLE, title);
        thisFragment.setArguments(bundle);

        return thisFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // ダイアログを生成し、タイトルを取得してセット
        AlertDialog.Builder builder = new AlertDialog.Builder(settingActivity);
        if (getArguments() != null) {
            builder.setTitle(getArguments().getInt(CommonParameters.BUNDLE_TITLE));
        }

        LayoutInflater inflater = (LayoutInflater) settingActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new NullPointerException("inflater is null.");
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(settingActivity);

        // ダイアログの種類に応じて、アイコン・ボタンの文字列・ボタンのリスナーをセット
        CommonDialogType type;
        if ((type = (CommonDialogType) getArguments().getSerializable(CommonParameters.BUNDLE_TYPE)) == null) {
            throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
        } else {
            // ログ出力
            Log.d(TAG, "onCreateDialog:type=" + type);

            switch (type) {
                case LIST_BLOCK_EVEN_COLOR:
                    return createColorChangeDialog(new int[]{sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_RED, 0), sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_GREEN, 48), sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_BLUE, 96)}, builder, type);
                case LIST_BLOCK_ODD_COLOR:
                    return createColorChangeDialog(new int[]{sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_RED, 96), sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_GREEN, 48), sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_BLUE, 0)}, builder, type);
                case LIST_BLOCK_TEXT_COLOR:
                    return createColorChangeDialog(new int[]{sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_RED, 0), sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_GREEN, 255), sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_BLUE, 0)}, builder, type);
                case LIST_BUTTONS_POSITION:
                    // 選択した位置でのラジオボタンを取得
                    final View positionView = inflater.inflate(R.layout.dialog_setting_position, (ViewGroup) settingActivity.findViewById(R.id.positionLayout));
                    final RadioButton radioButtonRight = positionView.findViewById(R.id.positionRadioButtonRight);
                    final RadioButton radioButtonLeft = positionView.findViewById(R.id.positionRadioButtonLeft);

                    // 現在選択中の位置から、上記ラジオボタンの一方のみをチェック
                    if (PreferenceManager.getDefaultSharedPreferences(settingActivity).getBoolean(CommonParameters.PREFERENCE_BUTTONS_POSITION_RIGHT, true)) {
                        radioButtonRight.setChecked(true);
                        radioButtonLeft.setChecked(false);
                    } else {
                        radioButtonLeft.setChecked(true);
                        radioButtonRight.setChecked(false);
                    }

                    // ラジオボタンを一方のみ選択させるようにリスナーを生成
                    View.OnClickListener listenerRight = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            radioButtonRight.setChecked(true);
                            radioButtonLeft.setChecked(false);
                        }
                    };
                    View.OnClickListener listenerLeft = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            radioButtonLeft.setChecked(true);
                            radioButtonRight.setChecked(false);
                        }
                    };

                    // 位置選択のラジオボタンの下に表示するイメージビューを取得し、png画像とリスナーをセット
                    ImageView imageViewRight = positionView.findViewById(R.id.positionImageViewRight);
                    ImageView imageViewLeft = positionView.findViewById(R.id.positionImageViewLeft);
                    imageViewRight.setImageResource(R.drawable.position_buttons_right);
                    imageViewLeft.setImageResource(R.drawable.position_buttons_left);

                    // ラジオボタンとイメージビューの2つにリスナーをセット
                    radioButtonRight.setOnClickListener(listenerRight);
                    radioButtonLeft.setOnClickListener(listenerLeft);
                    imageViewRight.setOnClickListener(listenerRight);
                    imageViewLeft.setOnClickListener(listenerLeft);

                    builder.setIcon(android.R.drawable.ic_dialog_info)
                            .setView(positionView)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    // 選択したラジオボタンを保存
                                    PreferenceManager.getDefaultSharedPreferences(settingActivity)
                                            .edit()
                                            .putBoolean(CommonParameters.PREFERENCE_BUTTONS_POSITION_RIGHT, radioButtonRight.isChecked())
                                            .apply();

                                    // ボタン群のレイアウトの場所を更新
                                    MainCommonFunctions.updateLayoutPosition();

                                    // 変更後のボタン群のレイアウトの場所を詳細設定のリストビューのアダプターに反映
                                    SettingActivity.settingAdapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton(android.R.string.cancel, null);

                    return builder.create();
                case LIST_FRAME_COLOR:
                    return createColorChangeDialog(new int[]{sharedPreferences.getInt(CommonParameters.PREFERENCE_FRAME_RED, 80), sharedPreferences.getInt(CommonParameters.PREFERENCE_FRAME_GREEN, 80), sharedPreferences.getInt(CommonParameters.PREFERENCE_FRAME_BLUE, 80)}, builder, type);
                case LIST_POINTER_COLOR:
                    return createColorChangeDialog(new int[]{sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_ALPHA, 64), sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_RED, 0), sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_GREEN, 255), sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_BLUE, 0)}, builder, type);
                case LIST_POINTER_SELECTED_COLOR:
                    return createColorChangeDialog(new int[]{sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_ALPHA, 64), sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_RED, 255), sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN, 0), sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE, 255)}, builder, type);
                default:
                    throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
            }
        }
    }

    /**
     * ARGB値を変更するダイアログをビルダーから生成して返す
     * 第1引数beforeには変更前のARGB値(0〜255)の配列であり、次のように構成する必要がある
     * ・要素数が3つ : 0番目から順にR、G、B値
     * ・要素数が4つ : 0番目から順にA、R、G、B値
     *
     * @param before           上記の条件を満たす(A、)R、G、B値
     * @param builder          ARGB値を変更するダイアログのビルダー
     * @param commonDialogType 変更する色を表すタイプ
     * @return ARGB値を変更するダイアログ
     * @throws IllegalArgumentException 第1引数beforeが上記の条件を満たしていない場合
     */
    private Dialog createColorChangeDialog(int[] before, AlertDialog.Builder builder, final CommonDialogType commonDialogType) throws IllegalArgumentException {
        // 引数のエラーチェック
        if (before.length < 3)
            throw new IllegalArgumentException("The number of arguments is too less.");
        if (before.length > 4)
            throw new IllegalArgumentException("The number of arguments is too many.");
        for (int b : before) {
            if (b < 0 || b > 255)
                throw new IllegalArgumentException("ARGB parameters are out of range.");
        }

        LayoutInflater inflater = (LayoutInflater) settingActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new NullPointerException("inflater is null.");
        }

        // ARGB値を変更するビューを生成
        final View colorChangeView = inflater.inflate(R.layout.dialog_setting_change_color, (ViewGroup) settingActivity.findViewById(R.id.colorChangeLayout));

        // Alpha値を変動させるかどうかのフラグを生成し、変動させない場合はAlpha値を変更させる列を非表示にする
        final boolean isAlpha = before.length == 4;
        if (!isAlpha)
            colorChangeView.findViewById(R.id.colorChangeAlphaTableRow).setVisibility(View.GONE);

        // 変更前後の色を表示するフレームを取得
        final FrameLayout from = colorChangeView.findViewById(R.id.colorChangeFrom);
        final FrameLayout to = colorChangeView.findViewById(R.id.colorChangeTo);
        // フレームの初期値を変更前のARGB値にセット
        from.setBackgroundColor(Color.rgb(before[before.length - 3], before[before.length - 2], before[before.length - 1]));
        if (isAlpha) from.getBackground().setAlpha(before[0]);
        to.setBackgroundColor(Color.rgb(before[before.length - 3], before[before.length - 2], before[before.length - 1]));
        if (isAlpha) to.getBackground().setAlpha(before[0]);

        // 変更後のARGB値を出力するテキストビューを取得し、初期テキストを変更前のARGB値としてセット
        final TextView alphaTextView = isAlpha ? (TextView) colorChangeView.findViewById(R.id.colorChangeAlphaTextView) : null;
        if (isAlpha) alphaTextView.setText(getString(R.string.textView_color_alpha, before[0]));
        final TextView redTextView = colorChangeView.findViewById(R.id.colorChangeRedTextView);
        redTextView.setText(getString(R.string.textView_color_red, before[before.length - 3]));
        final TextView greenTextView = colorChangeView.findViewById(R.id.colorChangeGreenTextView);
        greenTextView.setText(getString(R.string.textView_color_green, before[before.length - 2]));
        final TextView blueTextView = colorChangeView.findViewById(R.id.colorChangeBlueTextView);
        blueTextView.setText(getString(R.string.textView_color_blue, before[before.length - 1]));

        // ARGB値を変更する4つのシークバーを取得し、初期値として変更前のARGB値とシークバーの値が変更された時のリスナーをセット
        final SeekBar alphaSeekBar = (isAlpha) ? (SeekBar) colorChangeView.findViewById(R.id.colorChangeAlphaSeekBar) : null;
        if (isAlpha) {
            alphaSeekBar.setProgress(before[0]);
            alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    // 変更後のAlpha値をテキストビューとフレームにセット
                    alphaTextView.setText(getString(R.string.textView_color_alpha, i));
                    to.getBackground().setAlpha(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
        final SeekBar redSeekBar = colorChangeView.findViewById(R.id.colorChangeRedSeekBar);
        final SeekBar greenSeekBar = colorChangeView.findViewById(R.id.colorChangeGreenSeekBar);
        final SeekBar blueSeekBar = colorChangeView.findViewById(R.id.colorChangeBlueSeekBar);
        redSeekBar.setProgress(before[before.length - 3]);
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // 変更後のRed値をテキストビューとフレームにセット
                redTextView.setText(getString(R.string.textView_color_red, i));
                to.setBackgroundColor(Color.rgb(i, greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        greenSeekBar.setProgress(before[before.length - 2]);
        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // 変更後のGreen値をテキストビューとフレームにセット
                greenTextView.setText(getString(R.string.textView_color_green, i));
                to.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), i, blueSeekBar.getProgress()));
                if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        blueSeekBar.setProgress(before[before.length - 1]);
        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // 変更後のBlue値をテキストビューとフレームにセット
                blueTextView.setText(getString(R.string.textView_color_blue, i));
                to.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), i));
                if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // ARGB値を変更する8つのボタンを取得し、リスナーをセット
        if (isAlpha) {
            colorChangeView.findViewById(R.id.colorChangeAlphaMinusButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int nowProgress = alphaSeekBar.getProgress();
                    if (nowProgress > 0) {
                        // 変更後のAlpha値をテキストビューとフレームとシークバーにセット
                        alphaTextView.setText(getString(R.string.textView_color_alpha, nowProgress - 1));
                        to.getBackground().setAlpha(nowProgress - 1);
                        alphaSeekBar.setProgress(nowProgress - 1);
                    }
                }
            });
            colorChangeView.findViewById(R.id.colorChangeAlphaPlusButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int nowProgress = alphaSeekBar.getProgress();
                    if (nowProgress < 255) {
                        // 変更後のAlpha値をテキストビューとフレームとシークバーにセット
                        alphaTextView.setText(getString(R.string.textView_color_alpha, nowProgress + 1));
                        to.getBackground().setAlpha(nowProgress + 1);
                        alphaSeekBar.setProgress(nowProgress + 1);
                    }
                }
            });
        }
        colorChangeView.findViewById(R.id.colorChangeRedMinusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nowProgress = redSeekBar.getProgress();
                if (nowProgress > 0) {
                    // 変更後のRed値をテキストビューとフレームとシークバーにセット
                    redTextView.setText(getString(R.string.textView_color_red, nowProgress - 1));
                    to.setBackgroundColor(Color.rgb(nowProgress - 1, greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                    if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
                    redSeekBar.setProgress(nowProgress - 1);
                }
            }
        });
        colorChangeView.findViewById(R.id.colorChangeRedPlusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nowProgress = redSeekBar.getProgress();
                if (nowProgress < 255) {
                    // 変更後のRed値をテキストビューとフレームとシークバーにセット
                    redTextView.setText(getString(R.string.textView_color_red, nowProgress + 1));
                    to.setBackgroundColor(Color.rgb(nowProgress + 1, greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                    if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
                    redSeekBar.setProgress(nowProgress + 1);
                }
            }
        });
        colorChangeView.findViewById(R.id.colorChangeGreenMinusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nowProgress = greenSeekBar.getProgress();
                if (nowProgress > 0) {
                    // 変更後のGreen値をテキストビューとフレームとシークバーにセット
                    greenTextView.setText(getString(R.string.textView_color_green, nowProgress - 1));
                    to.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), nowProgress - 1, blueSeekBar.getProgress()));
                    if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
                    greenSeekBar.setProgress(nowProgress - 1);
                }
            }
        });
        colorChangeView.findViewById(R.id.colorChangeGreenPlusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nowProgress = greenSeekBar.getProgress();
                if (nowProgress < 255) {
                    // 変更後のGreen値をテキストビューとフレームとシークバーにセット
                    greenTextView.setText(getString(R.string.textView_color_green, nowProgress + 1));
                    to.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), nowProgress + 1, blueSeekBar.getProgress()));
                    if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
                    greenSeekBar.setProgress(nowProgress + 1);
                }
            }
        });
        colorChangeView.findViewById(R.id.colorChangeBlueMinusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nowProgress = blueSeekBar.getProgress();
                if (nowProgress > 0) {
                    // 変更後のBlue値をテキストビューとフレームとシークバーにセット
                    blueTextView.setText(getString(R.string.textView_color_blue, nowProgress - 1));
                    to.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), nowProgress - 1));
                    if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
                    blueSeekBar.setProgress(nowProgress - 1);
                }
            }
        });
        colorChangeView.findViewById(R.id.colorChangeBluePlusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nowProgress = blueSeekBar.getProgress();
                if (nowProgress < 255) {
                    // 変更後のBlue値をテキストビューとフレームとシークバーにセット
                    blueTextView.setText(getString(R.string.textView_color_blue, nowProgress + 1));
                    to.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), nowProgress + 1));
                    if (isAlpha) to.getBackground().setAlpha(alphaSeekBar.getProgress());
                    blueSeekBar.setProgress(nowProgress + 1);
                }
            }
        });


        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setView(colorChangeView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(settingActivity);
                        // どの色を変更するのか場合分けする
                        switch (commonDialogType) {
                            case LIST_BLOCK_EVEN_COLOR:
                                // 変更後のRGB値を保存
                                sharedPreferences.edit()
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_EVEN_RED, redSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_EVEN_GREEN, greenSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_EVEN_BLUE, blueSeekBar.getProgress())
                                        .apply();

                                // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
                                MainCommonFunctions.updateColorColumnLayouts();
                                break;
                            case LIST_BLOCK_ODD_COLOR:
                                // 変更後のRGB値を保存
                                sharedPreferences.edit()
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_ODD_RED, redSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_ODD_GREEN, greenSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_ODD_BLUE, blueSeekBar.getProgress())
                                        .apply();

                                // 譜面のブロック内に存在する、すべての1列のレイアウトの色を更新
                                MainCommonFunctions.updateColorColumnLayouts();
                                break;
                            case LIST_BLOCK_TEXT_COLOR:
                                // 変更後のRGB値を保存
                                sharedPreferences.edit()
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_TEXT_RED, redSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_TEXT_GREEN, greenSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_BLOCK_TEXT_BLUE, blueSeekBar.getProgress())
                                        .apply();

                                // ポインターの位置が示すブロックの情報を更新
                                MainCommonFunctions.updateTextsAtPointer();

                                break;
                            case LIST_FRAME_COLOR:
                                // 変更後のRGB値を保存
                                sharedPreferences.edit()
                                        .putInt(CommonParameters.PREFERENCE_FRAME_RED, redSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_FRAME_GREEN, greenSeekBar.getProgress())
                                        .putInt(CommonParameters.PREFERENCE_FRAME_BLUE, blueSeekBar.getProgress())
                                        .apply();

                                // ポインターの位置が示すブロックの情報を更新
                                MainCommonFunctions.updateTextsAtPointer();

                                break;
                            case LIST_POINTER_COLOR:
                                FrameLayout pointerLayout = settingActivity.findViewById(R.id.pointerLayout);
                                if (alphaSeekBar != null) {
                                    // 変更後のARGB値を保存
                                    sharedPreferences.edit()
                                            .putInt(CommonParameters.PREFERENCE_POINTER_ALPHA, alphaSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_POINTER_RED, redSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_POINTER_GREEN, greenSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_POINTER_BLUE, blueSeekBar.getProgress())
                                            .apply();
                                    // 変更後のポインターの色(通常モード)をポインターのレイアウトに反映
                                    pointerLayout.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                                    pointerLayout.getBackground().setAlpha(alphaSeekBar.getProgress());
                                } else {
                                    // 変更後のRGB値を保存
                                    sharedPreferences.edit()
                                            .putInt(CommonParameters.PREFERENCE_POINTER_RED, redSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_POINTER_GREEN, greenSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_POINTER_BLUE, blueSeekBar.getProgress())
                                            .apply();
                                    // 変更後のポインターの色(通常モード)をポインターのレイアウトに反映
                                    pointerLayout.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                                }
                                break;
                            case LIST_POINTER_SELECTED_COLOR:
                                pointerLayout = settingActivity.findViewById(R.id.pointerLayout);
                                ToggleButton selectedToggleButton = settingActivity.findViewById(R.id.toggleButtonEditSelect);
                                if (alphaSeekBar != null) {
                                    // 変更後のARGB値を保存
                                    sharedPreferences.edit()
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_ALPHA, alphaSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_RED, redSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN, greenSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE, blueSeekBar.getProgress())
                                            .apply();
                                    if (selectedToggleButton.isChecked()) {
                                        // 変更後のポインターの色(譜面選択モード)をポインターのレイアウトに反映
                                        pointerLayout.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                                        pointerLayout.getBackground().setAlpha(alphaSeekBar.getProgress());

                                        // 選択領域の色を変更
                                        FrameLayout selectedAreaLayout = settingActivity.findViewById(R.id.selectedAreaLayout);
                                        selectedAreaLayout.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                                        selectedAreaLayout.getBackground().setAlpha(alphaSeekBar.getProgress());
                                    }
                                } else {
                                    // 変更後のRGB値を保存
                                    sharedPreferences.edit()
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_RED, redSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN, greenSeekBar.getProgress())
                                            .putInt(CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE, blueSeekBar.getProgress())
                                            .apply();
                                    if (selectedToggleButton.isChecked()) {
                                        // 変更後のポインターの色(譜面選択モード)をポインターのレイアウトに反映
                                        pointerLayout.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));

                                        // 選択領域の色を変更
                                        settingActivity.findViewById(R.id.selectedAreaLayout).setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                                    }
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("The commonDialogType argument cannot be applied.");
                        }

                        // 変更後のテキストの色を詳細設定のリストビューのアダプターに反映
                        SettingActivity.settingAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }
}
