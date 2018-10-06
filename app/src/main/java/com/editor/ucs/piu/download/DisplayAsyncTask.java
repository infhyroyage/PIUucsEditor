package com.editor.ucs.piu.download;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.editor.ucs.piu.CommonParameters;
import com.editor.ucs.piu.R;
import com.editor.ucs.piu.unit.UnitSample;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ダウンロードできるサンプルのucsファイルの情報を表示するクラス
 */
class DisplayAsyncTask extends AsyncTask<Void, Void, String> {
    // デバッグ用のタグ
    private static final String TAG = "DisplayAsyncTask";

    /**
     * AsyncTask内のContextの弱参照性を考慮した、サンプルのucsファイルをダウンロードするアクティビティ
     */
    private WeakReference<DownloadActivity> weakReference;

    /**
     * コンストラクタ
     *
     * @param downloadActivity サンプルのucsファイルをダウンロードするアクティビティ
     */
    DisplayAsyncTask(DownloadActivity downloadActivity) {
        this.weakReference = new WeakReference<>(downloadActivity);
    }

    /**
     * UIスレッドとは非同期で、サンプルのucsファイルをダウンロードできるWebページに接続し、
     * ダウンロードできるサンプルのucsファイルの情報を取得する
     *
     * @param voids なし(要素数0の配列)
     * @return 正常に取得した場合はnull、取得に失敗した場合はその旨のメッセージ
     */
    @Override
    protected String doInBackground(Void... voids) {
        String message = null;

        // サンプルのucsファイルをダウンロードするアクティビティのインスタンスを取得
        final DownloadActivity downloadActivity = weakReference.get();

        // プログレスバーを取得
        final ProgressBar progressBar = downloadActivity.findViewById(R.id.downloadProgressBar);

        try {
            // サンプルのucsファイルをダウンロードできるWebページのURLへ接続
            Document document = Jsoup.connect(CommonParameters.URL_SAMPLE_UCS_OVERALL).userAgent(CommonParameters.USER_AGENT).get();
            // ログ出力
            Log.d(TAG, "doInBackground:url=" + CommonParameters.URL_SAMPLE_UCS_OVERALL);

            // class属性が「download_tab」であるエレメントを取得
            Elements downloadTabElements = document.getElementsByClass("download_tab");
            // エレメントが1個以外の場合はスクレイピングエラーとする
            if (downloadTabElements.size() != 1) {
                throw new IllegalStateException("downloadTabElements.size=" + downloadTabElements.size());
            }

            // タグ名が「a」である全エレメントから、URLへの追加文字列とバージョン名をセット
            final List<String> additionalUrlList = new ArrayList<>();
            downloadActivity.versionList = new ArrayList<>();
            for (Element aElement : downloadTabElements.get(0).getElementsByTag("a")) {
                String hrefValue = aElement.attr("href");
                String additionalUrl = hrefValue.substring(hrefValue.indexOf('?'));
                String version = aElement.text();

                // バージョン名が「ALL TUNES」の場合はセットしない
                if (!version.equals("ALL TUNES")) {
                    additionalUrlList.add(additionalUrl);
                    downloadActivity.versionList.add(version);

                    // ログ出力
                    Log.d(TAG, "doInBackground:additionalUrl=" + additionalUrl);
                    Log.d(TAG, "doInBackground:version=" + version);
                }
            }
            // エレメントが1個も取得できなかった場合はスクレイピングエラーとする
            if (downloadActivity.versionList.size() == 0) {
                throw new IllegalStateException("versionList.size=" + downloadActivity.versionList.size());
            }

            downloadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // プログレスバーの進捗をリセット
                    progressBar.setProgress(0);
                    // プログレスバーの最大値をセット
                    progressBar.setMax(downloadActivity.versionList.size());
                    // プログレスバーを表示
                    progressBar.setVisibility(View.VISIBLE);

                    // テキストビューの文字をサンプル情報取得中に変更
                    ((TextView) downloadActivity.findViewById(R.id.downloadTextView)).setText(R.string.textView_gettingSamples);
                }
            });

            // スレッドプールの生成
            ExecutorService service = Executors.newFixedThreadPool(downloadActivity.versionList.size());

            // 各バージョンごとに、サンプルのucsファイルをダウンロードできるWebページからサンプル情報を並列で取得
            for (int i = 0; i < downloadActivity.versionList.size(); i++) {
                service.submit(new SampleSubListThread(downloadActivity, downloadActivity.versionList.get(i), additionalUrlList.get(i)));
            }

            // 新規動作の受付を終了する
            service.shutdown();
            // 上記の動作がすべて終わるまで待機する
            while (true) {
                if (service.isTerminated()) {
                    break;
                }
            }

            // サンプルの情報が1個も取得できなかった場合はスクレイピングエラーとする
            if (downloadActivity.unitSampleMap.size() == 0) {
                throw new IllegalStateException("unitSampleMap.size=" + downloadActivity.unitSampleMap.size());
            }

            // バージョン名を指定しない「指定なし」のバージョン名をセット
            downloadActivity.versionList.add(0, downloadActivity.getString(R.string.textView_filter_versionUnspecified));
        } catch (IOException e) {
            // ログ出力
            Log.e(TAG, e.getMessage(), e);

            // 通信エラーの文章をセット
            message = downloadActivity.getString(R.string.textView_connectionError);
        } catch (IllegalStateException e) {
            // ログ出力
            Log.e(TAG, e.getMessage(), e);

            // スクレイピングエラーの旨のメッセージIDをセット
            message = downloadActivity.getString(R.string.textView_htmlFormatError);
        }

        return message;
    }

    /**
     * doInBackground()の動作を実行した後にUIスレッドで実行する
     *
     * @param message 正常に取得した場合はnull、取得に失敗した場合はその旨のメッセージ
     */
    @Override
    protected void onPostExecute(String message) {
        // サンプルのucsファイルをダウンロードするアクティビティのインスタンスを取得
        final DownloadActivity downloadActivity = weakReference.get();

        // 途中経過orエラー時のテキストビューを取得
        TextView downloadTextView = downloadActivity.findViewById(R.id.downloadTextView);
        // サンプルのucsファイルをダウンロードできるサンプル情報のリストビューを取得
        ListView listView = downloadActivity.findViewById(R.id.downloadListView);
        // 「フィルター」ボタンを取得
        Button filterButton = downloadActivity.findViewById(R.id.downloadFilterButton);

        // doInBackground()で異常発生時には、その旨のメッセージをテキストビューにセットして終了
        if (message != null) {
            // 途中経過orエラー時のテキストビューにエラーの文章をセット
            downloadTextView.setText(message);
            return;
        }

        // 途中経過orエラー時のテキストビューを非表示
        downloadActivity.findViewById(R.id.downloadTextView).setVisibility(View.GONE);

        // リストビューにアダプターをセット
        listView.setAdapter(new DownloadAdapter(downloadActivity, 0, new ArrayList<>(downloadActivity.unitSampleMap.values())));
        // サンプルのucsファイルをダウンロードできるサンプルのリストビューを表示
        listView.setVisibility(View.VISIBLE);

        // 「フィルター」ボタンを表示
        filterButton.setVisibility(View.VISIBLE);
    }

    /**
     * 指定されたバージョンでの、ダウンロードできるサンプルのucsファイルの情報のサブリストを取得する動作を実行するスレッドのクラス
     */
    private class SampleSubListThread implements Callable<Void> {
        /**
         * サンプルのucsファイルをダウンロードするアクティビティ
         */
        private DownloadActivity downloadActivity;

        /**
         * バージョン名
         */
        private String version;

        /**
         * URLへの追加文字列
         */
        private String additionalUrl;

        /**
         * コンストラクタ
         *
         * @param downloadActivity サンプルのucsファイルをダウンロードするアクティビティ
         * @param version バージョン名
         * @param additionalUrl URLへの追加文字列
         */
        SampleSubListThread(DownloadActivity downloadActivity, String version, String additionalUrl) {
            this.downloadActivity = downloadActivity;
            this.version = version;
            this.additionalUrl = additionalUrl;
        }

        /**
         * ダウンロードできるサンプルのucsファイルの情報のサブマップを取得する
         *
         * @return null固定
         * @throws IOException 通信エラーが発生した場合
         */
        @Override
        public Void call() throws IOException {
            // 指定されたバージョンでの、ダウンロードできるサンプルのucsファイルの情報のサブマップ
            Map<String, UnitSample> unitSampleSubMap = new HashMap<>();

            // プログレスバーを取得
            final ProgressBar progressBar = downloadActivity.findViewById(R.id.downloadProgressBar);

            // バージョンごとの各ページに対して繰り返す(暫定的にページ数の上限を8とした)
            for (int page = 1; page <= 8; page++) {
                // ページごとの、サンプルのucsファイルをダウンロードできるWebページのURLへ接続
                Document document = Jsoup.connect(CommonParameters.URL_SAMPLE_UCS_OVERALL + additionalUrl + "&page=" + page).userAgent(CommonParameters.USER_AGENT).get();
                // ログ出力
                Log.d(TAG, "call:url=" + CommonParameters.URL_SAMPLE_UCS_OVERALL + additionalUrl + "&page=" + page);

                // tbodyタグのエレメントを取得
                Elements tbodyElements = document.getElementsByTag("tbody");
                // エレメントが1個以外の場合はスクレイピングエラーとする
                if (tbodyElements.size() != 1) {
                    throw new IllegalStateException("tbodyElements.size=" + tbodyElements.size());
                }

                // tbodyタグの子タグであるtrタグが1つのみの場合は、ページ終端なのでbreakする
                if (tbodyElements.get(0).children().size() == 1) {
                    break;
                }

                // サンプルの情報の1列を示す、子タグのtrタグすべてを順次スクレイピング
                for (Element trElement : tbodyElements.get(0).children()) {
                    // trタグの子タグがtdタグの場合はヘッダ情報なので除外
                    if (!trElement.child(0).tagName().equals("td")) {
                        continue;
                    }

                    // サンプルのインデックス名を取得
                    Elements elements = trElement.getElementsByClass("download_cs_number");
                    if (elements.size() != 1) {
                        throw new IllegalStateException("[download_cs_number]elements.size=" + elements.size());
                    }
                    String index = elements.get(0).text();
                    // ログ出力
                    Log.d(TAG, "call:index=" + index);

                    // サンプルの曲名を取得
                    elements = trElement.getElementsByClass("list_song_title");
                    if (elements.size() != 1) {
                        throw new IllegalStateException("[list_song_title]elements.size=" + elements.size());
                    }
                    String songName = elements.get(0).text();
                    // ログ出力
                    Log.d(TAG, "call:songName=" + songName);

                    // サンプルの曲のアーティスト名を取得
                    elements = trElement.getElementsByClass("list_song_artist");
                    if (elements.size() != 1) {
                        throw new IllegalStateException("[list_song_artist]elements.size=" + elements.size());
                    }
                    String songArtist = elements.get(0).text().substring(2);
                    // ログ出力
                    Log.d(TAG, "call:songArtist=" + songArtist);

                    // サンプルの曲のBPM値の文字列を取得
                    elements = trElement.getElementsByClass("download_bpm");
                    if (elements.size() != 1) {
                        throw new IllegalStateException("[download_bpm]elements.size=" + elements.size());
                    }
                    String songBpm = elements.get(0).text();
                    // ログ出力
                    Log.d(TAG, "call:songBpm=" + songBpm);

                    // サンプルのzipファイルをダウンロードできるURLを取得
                    elements = trElement.getElementsByClass("download_source");
                    if (elements.size() != 1) {
                        throw new IllegalStateException("[download_source]elements.size=" + elements.size());
                    }
                    elements = elements.get(0).getElementsByTag("a");
                    if (elements.size() != 1) {
                        throw new IllegalStateException("[a]elements.size=" + elements.size());
                    }
                    String hrefValue = elements.attr("href");
                    String downloadUrl = CommonParameters.URL_SAMPLE_UCS_DOWNLOAD + hrefValue.substring(hrefValue.indexOf('?'));
                    // ログ出力
                    Log.d(TAG, "call:downloadUrl=" + downloadUrl);

                    // ダウンロードできるサンプルのucsファイルの情報のサブリストに追加
                    unitSampleSubMap.put(index, new UnitSample(index, songName, songArtist, songBpm, version, downloadUrl));
                }
            }

            // プログレスバーの進捗を1段階上げる
            downloadActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                }
            });

            // ダウンロードできるすべてのサンプル情報のマップに追加
            downloadActivity.unitSampleMap.putAll(unitSampleSubMap);

            return null;
        }
    }
}
