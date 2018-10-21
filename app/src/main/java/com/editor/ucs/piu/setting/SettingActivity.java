package com.editor.ucs.piu.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.util.ArrayList;

/**
 * 詳細設定のアクティビティを表すクラス
 */
public class SettingActivity extends AppCompatActivity {
    /**
     * 詳細設定のリストビューのアダプター
     */
    static SettingAdapter settingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 詳細設定のリストビューを取得
        ListView listView = findViewById(R.id.settingListView);
        // リストビューに格納するアイテムのリストを生成
        final ArrayList<CommonDialogType> itemList = new ArrayList<>(0);

        // 「作業ボタン群の表示位置」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_BUTTONS_POSITION);
        // 「バイブレーション」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_VIBRATION);
        // 「奇数番目のブロックの色」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_BLOCK_ODD_COLOR);
        // 「偶数番目のブロックの色」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_BLOCK_EVEN_COLOR);
        // 「ポインターの色(通常)」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_POINTER_COLOR);
        // 「ポインターの色(譜面選択)」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_SELECTED_POINTER_COLOR);
        // 「枠線の色」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_FRAME_COLOR);
        // 「ブロック情報のテキストの色」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_BLOCK_TEXT_COLOR);
        // 「ライセンス表記」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_LICENSE);
        // 「バージョン」のアイテムを作成し、詳細設定のリストビューに追加
        itemList.add(CommonDialogType.LIST_VERSION);

        // リストビューにアダプターをセット
        settingAdapter = new SettingAdapter(this, 0, itemList);
        listView.setAdapter(settingAdapter);
        // リストビューのアイテムをタップした時のリスナーをセット
        final SettingActivity settingActivity = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CommonDialogType item = itemList.get(i);
                switch (item) {
                    case LIST_BLOCK_EVEN_COLOR:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_blockEvenColor).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_BLOCK_ODD_COLOR:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_blockOddColor).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_BLOCK_TEXT_COLOR:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_blockTextColor).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_BUTTONS_POSITION:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_buttonsPosition).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_FRAME_COLOR:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_frameColor).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_LICENSE:
                        // ライセンス情報を表示するアクティビティにインテントする
                        OssLicensesMenuActivity.setActivityTitle(getString(R.string.textView_listView_title_license));
                        startActivity(new Intent(settingActivity, OssLicensesMenuActivity.class));
                        break;
                    case LIST_POINTER_COLOR:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_pointerColor).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_SELECTED_POINTER_COLOR:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_pointerSelectedColor).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                    case LIST_VIBRATION:
                        SettingDialogFragment.newInstance(settingActivity, item, R.string.textView_listView_title_vibration).show(settingActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY);
                        break;
                }
            }
        });

        // AdMobの初期化
        MobileAds.initialize(this, CommonParameters.ADMOB_APP_ID);
        ((AdView) findViewById(R.id.settingAdView)).loadAd(new AdRequest.Builder().build());
    }
}