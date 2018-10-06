package com.editor.ucs.piu.download;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.unit.UnitSample;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * サンプルのucsファイルをダウンロードするアクティビティを表すクラス
 */
public class DownloadActivity extends AppCompatActivity {
    // デバッグ用のタグ
    private static final String TAG = "DownloadActivity";

    /**
     * ダウンロードできるすべてのサンプル情報のマップ
     * (キーの値) := サンプル情報のインデックス名
     */
    Map<String, UnitSample> unitSampleMap = new ConcurrentSkipListMap<>();

    /**
     * サンプルのバージョン名のリスト
     */
    List<String> versionList;

    /**
     * ダウンロードする対象のサンプル情報
     */
    UnitSample unitSample = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        // ダウンロードできるサンプルのucsファイルの情報を表示
        new DisplayAsyncTask(this).execute();

        final DownloadActivity downloadActivity = this;

        // サンプルのucsファイルをダウンロードできるサンプル情報のリストビューを取得
        ListView listView = findViewById(R.id.downloadListView);
        // 「フィルター」ボタンを取得
        Button filterButton = findViewById(R.id.downloadFilterButton);

        // リストビューの速いスクロールを有効にする
        listView.setFastScrollEnabled(true);
        // リストビューのアイテムをタップした時のリスナーをセット
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // タップしたサンプル情報のインスタンスを取得
                UnitSample unitSample = new ArrayList<>(downloadActivity.unitSampleMap.values()).get(i);
                // ログ出力
                Log.d(TAG, "onPostExecute:downloadUrl=" + unitSample.downloadUrl);

                // ダウンロードする対象のサンプル情報をセット
                downloadActivity.unitSample = unitSample;

                // 内部ストレージへダウンロードしたサンプル用のファイルを書き込むパーミッションが拒否されるかどうかチェック
                if (ContextCompat.checkSelfPermission(downloadActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    /*
                     * 上記パーミッションの許可をリクエストするダイアログを表示
                     * その後の動作は、DownloadActivity#onRequestPermissionsResult()で定義する
                     */
                    ActivityCompat.requestPermissions(downloadActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CommonParameters.PERMISSION_SAMPLE_DOWNLOAD);
                } else {
                    // ダウンロードしたファイルを格納するディレクトリを指定させるダイアログの表示
                    new ChooserDialog().with(downloadActivity)
                            .withFilter(true, false)
                            .withStartFile(Environment.getExternalStorageDirectory().getPath())
                            .withRowLayoutView(R.layout.item_chooser)
                            .withResources(R.string.dialog_title_selectDirectory, android.R.string.ok, android.R.string.cancel)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String downloadDirectory, File pathFile) {
                                    // サンプルのucsファイルをダウンロードする
                                    new DownloadAsyncTask(downloadActivity, downloadDirectory).execute();
                                }
                            }).build().show();
                }

            }
        });

        // 「フィルター」ボタンのリスナーをセット
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // リストビューをフィルタリングするダイアログの表示
                FilterDialogFragment.newInstance(downloadActivity).show(downloadActivity.getSupportFragmentManager(), CommonParameters.DIALOG_FRAGMENT_FROM_DOWNLOAD_ACTIVITY);
            }
        });

        // AdMobの初期化
        MobileAds.initialize(this, CommonParameters.ADMOB_APP_ID);
        ((AdView) findViewById(R.id.downloadAdView)).loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // ログ出力
        Log.d(TAG, "onRequestPermissionsResult:requestCode=" + requestCode);

        switch (requestCode) {
            // 内部ストレージへダウンロードしたサンプル用のファイルを書き込むパーミッションが拒否されるかどうかチェック
            case CommonParameters.PERMISSION_SAMPLE_DOWNLOAD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 上記ダイアログで「許可」を選択した場合、ダウンロードしたファイルを格納するディレクトリを指定させるダイアログの表示
                    final DownloadActivity downloadActivity = this;
                    new ChooserDialog().with(this)
                            .withFilter(true, false)
                            .withStartFile(Environment.getExternalStorageDirectory().getPath())
                            .withRowLayoutView(R.layout.item_chooser)
                            .withResources(R.string.dialog_title_selectDirectory, android.R.string.ok, android.R.string.cancel)
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String downloadDirectory, File pathFile) {
                                    // サンプルのucsファイルをダウンロードする
                                    new DownloadAsyncTask(downloadActivity, downloadDirectory).execute();
                                }
                            }).build().show();
                } else {
                    // 上記ダイアログで「許可しない」を選択した場合、ダウンロードできなかった旨のトーストを出力
                    Toast.makeText(this, R.string.toast_permissionError_download, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}