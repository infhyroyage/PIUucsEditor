package com.editor.ucs.piu.download;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.editor.ucs.piu.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * サンプルのucsファイルをダウンロードするクラス
 */
public class DownloadAsyncTask extends AsyncTask<Void, Integer, String> {
    // デバッグ用のタグ
    private static final String TAG = "DownloadAsyncTask";

    /**
     * AsyncTask内のContextの弱参照性を考慮した、サンプルのucsファイルをダウンロードするアクティビティ
     */
    private WeakReference<DownloadActivity> weakReference;

    /**
     * ダウンロードしたファイルを格納するディレクトリ
     */
    private String downloadDirectory;

    /**
     * サンプルのucsファイルをダウンロードする途中でのウェイクロックのインスタンス
     */
    private PowerManager.WakeLock wakeLock = null;

    /**
     * コンストラクタ
     *
     * @param downloadActivity サンプルのucsファイルをダウンロードするアクティビティ
     * @param downloadDirectory ダウンロードしたファイルを格納するディレクトリ
     */
    DownloadAsyncTask(DownloadActivity downloadActivity, String downloadDirectory) {
        this.weakReference = new WeakReference<>(downloadActivity);
        this.downloadDirectory = downloadDirectory;
    }

    /**
     * doInBackground()の動作を実行する前にUIスレッドで実行する
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // サンプルのucsファイルをダウンロードするアクティビティを取得
        DownloadActivity downloadActivity = weakReference.get();

        // リストビューのタップを無効化
        downloadActivity.findViewById(R.id.downloadListView).setEnabled(false);

        // サンプルのucsファイルをダウンロードする途中でのウェイクロックを生成し、CPU動作を常にONにするようにセット(最大時間:3分)
        PowerManager pm = (PowerManager) weakReference.get().getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire(3 * 60 * 1000);
        }

        // プログレスバーを取得
        ProgressBar progressBar = downloadActivity.findViewById(R.id.downloadProgressBar);
        // プログレスバーの進捗をリセット
        progressBar.setProgress(0);
        // プログレスバーの最大値をセット
        progressBar.setMax(100);
    }

    /**
     * UIスレッドとは非同期で、サンプルのucsファイルをダウンロードする
     *
     * @param voids なし(要素数0の配列)
     * @return ダウンロード成功時はnull、失敗時はその失敗の旨のメッセージ
     */
    @Override
    protected String doInBackground(Void... voids) {
        String message  = null;

        // サンプルのucsファイルをダウンロードするアクティビティを取得
        DownloadActivity downloadActivity = weakReference.get();

        // 指定したディレクトリに同名のucsファイルが存在した場合、その旨のメッセージを返す
        if (new File(downloadDirectory + "/" + downloadActivity.unitSample.index + ".ucs").exists()) {
            return downloadActivity.getString(R.string.toast_sameFileNameError, downloadDirectory + "/" + downloadActivity.unitSample.index + ".ucs");
        }

        // 指定したディレクトリに同名のmp3ファイルが存在した場合、その旨のメッセージを返す
        if (new File(downloadDirectory + "/" + downloadActivity.unitSample.index + ".mp3").exists()) {
            return downloadActivity.getString(R.string.toast_sameFileNameError, downloadDirectory + "/" + downloadActivity.unitSample.index + ".mp3");
        }

        InputStream is = null;
        FileOutputStream fos = null;
        HttpURLConnection urlConnection = null;
        try {
            // サンプルをダウンロードできるURLのサーバに接続
            urlConnection = (HttpURLConnection) new URL(downloadActivity.unitSample.downloadUrl).openConnection();
            urlConnection.connect();
            // ログ出力
            Log.d(TAG, "doInBackground:downloadUrl=" + downloadActivity.unitSample.downloadUrl);
            // 接続失敗時は通信に失敗した旨のメッセージを返す
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return downloadActivity.getString(R.string.textView_connectionError);
            }

            // 接続先のサーバからファイルをバッファ情報として読み込むストリームの生成
            is = urlConnection.getInputStream();
            // 読み込んだバッファ情報をファイルに書き込むストリームの生成
            String zipPath = downloadDirectory + "/" + downloadActivity.unitSample.index + ".zip";
            fos = new FileOutputStream(zipPath);
            // ログ出力
            Log.d(TAG, "doInBackground:zipPath=" + zipPath);

            /*
             * 接続先のサーバからzipファイルのサイズを取得する
             * サイズを通知しない場合は-1になる
             */
            int fileLength = urlConnection.getContentLength();
            // ログ出力
            Log.d(TAG, "doInBackground:fileLength=" + fileLength);

            // 接続先のサーバからzipファイルをダウンロードする
            byte data[] = new byte[2048];
            int length;
            long accumulatedLength = 0;
            while ((length = is.read(data)) != -1) {
                // 「戻る」ボタンを押して、この動作がキャンセルされた場合、ダウンロードを中断する
                if (isCancelled()) {
                    // ログ出力
                    Log.d(TAG, "doInBackground:cancel");
                    break;
                }

                // ダウンロードの進捗率を計算し、それをプログレスバーに公開する
                accumulatedLength += length;
                if (fileLength > 0) {
                    publishProgress((int) (accumulatedLength * 100 / fileLength));
                }

                // ダウンロード先の絶対パスにzipファイルを2048バイトずつ書き込む
                fos.write(data, 0, length);
            }
        } catch (IOException e) {
            // ログ出力
            Log.e(TAG, e.getMessage(), e);

            // ucsファイルダウンロード中に例外が発生した旨のメッセージをセット
            message = downloadActivity.getString(R.string.toast_ioException_ucsDownload);
        } finally {
            // 読み込んだバッファ情報をファイルに書き込むストリームのクローズ
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイルダウンロード中に例外が発生した旨のメッセージをセット
                    message = downloadActivity.getString(R.string.toast_ioException_ucsDownload);
                }
            }

            // 接続先からファイルをバッファ情報として読み込むストリームのクローズ
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイルダウンロード中に例外が発生した旨のメッセージをセット
                    message = downloadActivity.getString(R.string.toast_ioException_ucsDownload);
                }
            }

            // 接続を切断する
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return message;
    }

    /**
     * 公開したダウンロードの進捗率からプログレスバーの進捗をセットする
     *
     * @param progress ダウンロードの進捗率
     */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        // プログレスバーを取得し、進捗をセット
        ((ProgressBar) weakReference.get().findViewById(R.id.downloadProgressBar)).setProgress(progress[0]);
    }

    /**
     * doInBackground()の動作を実行した後にUIスレッドで実行する
     *
     * @param message ダウンロード成功時はnull、失敗時はその失敗の旨のメッセージ
     */
    @Override
    protected void onPostExecute(String message) {
        // サンプルのucsファイルをダウンロードする途中でのウェイクロックを開放
        if (wakeLock != null) {
            wakeLock.release();
        }

        // サンプルのucsファイルをダウンロードするアクティビティを取得
        DownloadActivity downloadActivity = weakReference.get();

        // リストビューのタップを有効化
        downloadActivity.findViewById(R.id.downloadListView).setEnabled(true);

        // ダウンロード失敗時は、その旨のトーストを出力して終了
        if (message != null) {
            Toast.makeText(downloadActivity, message, Toast.LENGTH_SHORT).show();
            return;
        }

        ZipInputStream zis = null;
        FileOutputStream fos = null;
        Process p = null;
        try {
            // zipファイルを解凍するストリームの生成
            String zipPath = downloadDirectory + "/" + downloadActivity.unitSample.index + ".zip";
            zis = new ZipInputStream(new FileInputStream(zipPath));
            // ログ出力
            Log.d(TAG, "onPostExecute:zipPath=" + zipPath);

            // 1個ずつファイルを解凍する
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 以前の解凍したファイルを書き込むストリームをクローズ
                if (fos != null) {
                    fos.close();
                }
                // 解凍したファイルを書き込むストリームの生成
                String entryPath = downloadDirectory + "/" + entry.getName();
                fos = new FileOutputStream(new File(entryPath));
                // ログ出力
                Log.d(TAG, "onPostExecute:entryPath=" + entryPath);

                // 解凍したファイルを2048バイトずつ書き込む
                byte data[] = new byte[2048];
                int length;
                while ((length = zis.read(data)) != -1) {
                    fos.write(data, 0, length);
                }

                // ucs、mp3ファイルの権限を644にする
                p = Runtime.getRuntime().exec("chmod 644 " + entryPath);
                p.waitFor();
            }
            zis.closeEntry();

            // zipファイルを削除する
            if (!new File(zipPath).delete()) {
                throw new IOException();
            }

            // 正常にダウンロードが完了した旨のトーストを出力
            Toast.makeText(downloadActivity, downloadActivity.getString(R.string.toast_download, downloadDirectory), Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            // ログ出力
            Log.e(TAG, e.getMessage(), e);

            // ucsファイルダウンロード中に例外が発生した旨のトーストを出力
            Toast.makeText(downloadActivity, R.string.toast_ioException_ucsDownload, Toast.LENGTH_SHORT).show();
        } finally {
            if (p != null) {
                p.destroy();
            }

            if (fos != null) {
                try {
                    // 解凍したファイルを書き込むストリームをクローズ
                    fos.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイルダウンロード中に例外が発生した旨のトーストを出力
                    Toast.makeText(downloadActivity, R.string.toast_ioException_ucsDownload, Toast.LENGTH_SHORT).show();
                }
            }

            if (zis != null) {
                try {
                    // zipファイルを解凍するストリームをクローズ
                    zis.close();
                } catch (IOException e) {
                    // ログ出力
                    Log.e(TAG, e.getMessage(), e);

                    // ucsファイルダウンロード中に例外が発生した旨のトーストを出力
                    Toast.makeText(downloadActivity, R.string.toast_ioException_ucsDownload, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}