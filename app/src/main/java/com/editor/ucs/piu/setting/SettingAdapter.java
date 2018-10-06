package com.editor.ucs.piu.setting;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * 詳細設定のリストビューのアダプターを表す内部クラス
 */
class SettingAdapter extends ArrayAdapter<CommonDialogType> {
    // デバッグ用のタグ
    private static final String TAG = "SettingAdapter";

    /**
     * 設定画面のアクティビティ
     */
    private SettingActivity settingActivity;

    private LayoutInflater layoutInflater;

    SettingAdapter(SettingActivity settingActivity, int id, List<CommonDialogType> users) {
        super(settingActivity, id, users);

        this.settingActivity = settingActivity;
        this.layoutInflater = (LayoutInflater) settingActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        // アイテムを取得
        CommonDialogType item = getItem(position);
        if (item == null) {
            throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
        }

        // アイテムのビューをインフレートしてない場合は、アイテムから判別してインフレートする
        switch (item) {
            case LIST_BUTTONS_POSITION:
            case LIST_LICENSE:
            case LIST_VERSION:
                view = layoutInflater.inflate(R.layout.item_list_setting, parent, false);
                break;
            case LIST_BLOCK_EVEN_COLOR:
            case LIST_BLOCK_ODD_COLOR:
            case LIST_BLOCK_TEXT_COLOR:
            case LIST_POINTER_COLOR:
            case LIST_POINTER_SELECTED_COLOR:
                view = layoutInflater.inflate(R.layout.item_list_setting_color, parent, false);
                break;
            case LIST_VIBRATION:
                view = layoutInflater.inflate(R.layout.item_list_setting_switch, parent, false);
                break;
            default:
                throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
        }

        // アイテムの判別
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        switch (item) {
            case LIST_BLOCK_EVEN_COLOR:
                ((TextView) view.findViewById(R.id.settingColorTitle)).setText(R.string.textView_listView_title_blockEvenColor);
                view.findViewById(R.id.settingColorSubTitle).setVisibility(View.GONE);
                view.findViewById(R.id.settingColorColor).setBackgroundColor(Color.rgb(
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_RED, 0),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_GREEN, 48),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_EVEN_BLUE, 96)));
                break;
            case LIST_BLOCK_ODD_COLOR:
                ((TextView) view.findViewById(R.id.settingColorTitle)).setText(R.string.textView_listView_title_blockOddColor);
                view.findViewById(R.id.settingColorSubTitle).setVisibility(View.GONE);
                view.findViewById(R.id.settingColorColor).setBackgroundColor(Color.rgb(
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_RED, 96),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_GREEN, 48),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_ODD_BLUE, 0)));
                break;
            case LIST_BLOCK_TEXT_COLOR:
                ((TextView) view.findViewById(R.id.settingColorTitle)).setText(R.string.textView_listView_title_blockTextColor);
                view.findViewById(R.id.settingColorSubTitle).setVisibility(View.GONE);
                view.findViewById(R.id.settingColorColor).setBackgroundColor(Color.rgb(
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_RED, 0),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_GREEN, 255),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_BLOCK_TEXT_BLUE, 0)));
                break;
            case LIST_BUTTONS_POSITION:
                ((TextView) view.findViewById(R.id.settingTitle)).setText(R.string.textView_listView_title_buttonsPosition);
                ((TextView) view.findViewById(R.id.settingSubTitle)).setText(getContext().getResources().getString((sharedPreferences.getBoolean(CommonParameters.PREFERENCE_BUTTONS_POSITION_RIGHT, true)) ? R.string.textView_listView_subTitle_right : R.string.textView_listView_subTitle_left));
                break;
            case LIST_LICENSE:
                ((TextView) view.findViewById(R.id.settingTitle)).setText(R.string.textView_listView_title_license);
                view.findViewById(R.id.settingSubTitle).setVisibility(View.GONE);
                break;
            case LIST_POINTER_COLOR:
                ((TextView) view.findViewById(R.id.settingColorTitle)).setText(R.string.textView_listView_title_pointerColor);
                view.findViewById(R.id.settingColorSubTitle).setVisibility(View.GONE);
                view.findViewById(R.id.settingColorColor).setBackgroundColor(Color.rgb(
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_RED, 0),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_GREEN, 255),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_BLUE, 0)));
                view.findViewById(R.id.settingColorColor).getBackground().setAlpha(sharedPreferences.getInt(CommonParameters.PREFERENCE_POINTER_ALPHA, 64));
                break;
            case LIST_POINTER_SELECTED_COLOR:
                ((TextView) view.findViewById(R.id.settingColorTitle)).setText(R.string.textView_listView_title_pointerSelectedColor);
                view.findViewById(R.id.settingColorSubTitle).setVisibility(View.GONE);
                view.findViewById(R.id.settingColorColor).setBackgroundColor(Color.rgb(
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_RED, 255),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_GREEN, 0),
                        sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_BLUE, 255)));
                view.findViewById(R.id.settingColorColor).getBackground().setAlpha(sharedPreferences.getInt(CommonParameters.PREFERENCE_SELECTED_POINTER_ALPHA, 64));
                break;
            case LIST_VERSION:
                // このアプリケーションのバージョンを取得
                String versionName;
                try {
                    versionName = settingActivity.getPackageManager().getPackageInfo(settingActivity.getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    versionName = "Unknown";
                }
                ((TextView) view.findViewById(R.id.settingTitle)).setText(R.string.textView_filter_songVersion);
                ((TextView) view.findViewById(R.id.settingSubTitle)).setText(versionName);
                break;
            case LIST_VIBRATION:
                ((TextView) view.findViewById(R.id.settingSwitchTitle)).setText(R.string.textView_listView_title_vibration);
                view.findViewById(R.id.settingSwitchSubTitle).setVisibility(View.GONE);
                final Switch vibrationSwitch = view.findViewById(R.id.settingSwitchSwitch);
                vibrationSwitch.setChecked(sharedPreferences.getBoolean(CommonParameters.PREFERENCE_VIBRATION, false));
                vibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        // ログ出力
                        Log.d(TAG, "onCheckedChanged:isChecked=" + vibrationSwitch.isChecked());

                        sharedPreferences.edit()
                                .putBoolean(CommonParameters.PREFERENCE_VIBRATION, vibrationSwitch.isChecked())
                                .apply();
                    }
                });
                break;
            default:
                throw new IllegalArgumentException("The CommonDialogType argument cannot be applied.");
        }

        return view;
    }
}
