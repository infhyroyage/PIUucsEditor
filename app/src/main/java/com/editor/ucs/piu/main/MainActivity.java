package com.editor.ucs.piu.main;

import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.CommonDialogType;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.buttons.ButtonsLayout;
import com.editor.ucs.piu.chart.ChartLayout;
import com.editor.ucs.piu.chart.ChartScrollView;
import com.editor.ucs.piu.chart.PointerLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

/**
 * メイン画面のアクティビティを表すクラス
 */
public class MainActivity extends AppCompatActivity {
    // デバッグ用のタグ
    private static final String TAG = "MainActivity";

    /**
     * ucsファイルのインスタンス
     * 譜面のレイアウトがメイン画面に表示されているかどうかは、このインスタンスがnullかどうかで判別できる
     */
    public Ucs ucs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ログ出力
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // メイン画面のアクティビティにおける共通関数に自身のインスタンスをセット
        MainCommonFunctions.mainActivity = this;
        // 譜面のスクロールビューとボタン群のレイアウトの配置を更新
        MainCommonFunctions.updateLayoutPosition();

        // ボタン群のレイアウトの初期化処理を行う
        ((ButtonsLayout) findViewById(R.id.buttonsLayout)).initializeOnCreate(this);

        // 端末のボタンで音量調節できるようにセット
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // AdMobの初期化
        MobileAds.initialize(this, CommonParameters.ADMOB_APP_ID);
        ((AdView) findViewById(R.id.mainAdView)).loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onStop() {
        // ログ出力
        Log.d(TAG, "onStop");

        // 再生中の動作を中断する
        ((ChartScrollView) findViewById(R.id.chartScrollView)).interrupt();

        /*
         * 譜面のレイアウトがメイン画面に表示されている場合のみ、
         * 現在指しているポインターの行番号をファイル名ごとにプリファレンスに保存
         */
        if (ucs != null) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(ucs.fileName, ((PointerLayout) findViewById(R.id.pointerLayout)).row)
                    .apply();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        // 譜面編集が行われている状態で「戻る」ボタンを押した時、譜面データを破棄するかどうかのダイアログを表示
        if (((ChartLayout) findViewById(R.id.chartLayout)).editCount != 0) {
            MainDialogFragment.newInstance(this, CommonDialogType.ON_BACK_PRESSED, R.string.dialog_title_destroy, R.string.dialog_message_destroy).show(getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // ログ出力
        Log.d(TAG, "onRequestPermissionsResult:requestCode=" + requestCode);

        final MainActivity mainActivity = this;

        switch (requestCode) {
            // 内部ストレージからucsファイルを読み込むパーミッションの許可を求めるダイアログの場合
            case CommonParameters.PERMISSION_UCS_READ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 上記ダイアログで「許可」を選択した場合、外部ストレージ上にあるucsファイルを指定させて、その絶対パスを取得するダイアログの表示
                    new ChooserDialog().with(this)
                            .withFilter(false, false, "ucs")
                            .withStartFile(Environment.getExternalStorageDirectory().getPath())
                            .withRowLayoutView(R.layout.item_chooser)
                            .withResources(R.string.dialog_title_selectUcsFile, android.R.string.ok, android.R.string.cancel)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String path, File pathFile) {
                                    // 指定したucsファイルの情報を読み込み、そのインスタンスを生成する
                                    Ucs.read(mainActivity, path);
                                }
                            }).build().show();
                } else {
                    // 上記ダイアログで「許可しない」を選択した場合、ucsファイルを開かなかった旨のトーストを出力
                    Toast.makeText(this, R.string.toast_permissionError_open, Toast.LENGTH_SHORT).show();
                }
                break;
            // 内部ストレージへucsファイルを書き込むパーミッションの許可を求めるダイアログの場合
            case CommonParameters.PERMISSION_UCS_SAVE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 上記ダイアログで「許可」を選択した場合、ucsファイルを書き込む
                    mainActivity.ucs.write(mainActivity, mainActivity.ucs.fileDir);
                } else {
                    // 上記ダイアログで「許可しない」を選択した場合、ucsファイルを開かなかった旨のトーストを出力
                    Toast.makeText(this, R.string.toast_permissionError_save, Toast.LENGTH_SHORT).show();
                }
                break;
            // 内部ストレージの別ディレクトリへucsファイルを書き込むパーミッションの許可を求めるダイアログの場合
            case CommonParameters.PERMISSION_UCS_SAVE_AS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 上記ダイアログで「許可」を選択した場合、ディレクトリを指定させるダイアログの表示
                    new ChooserDialog().with(this)
                            .withFilter(true, false)
                            .withStartFile(Environment.getExternalStorageDirectory().getPath())
                            .withRowLayoutView(R.layout.item_chooser)
                            .withResources(R.string.dialog_title_selectDirectory, android.R.string.ok, android.R.string.cancel)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String path, File pathFile) {
                                    // 指定したディレクトリに対し、ucsファイルを書き込む
                                    mainActivity.ucs.write(mainActivity, path);
                                }
                            }).build().show();
                } else {
                    // 上記ダイアログで「許可しない」を選択した場合、ucsファイルを開かなかった旨のトーストを出力
                    Toast.makeText(this, R.string.toast_permissionError_save, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
