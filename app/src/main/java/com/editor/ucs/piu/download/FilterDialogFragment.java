package com.editor.ucs.piu.download;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.unit.UnitSample;

import java.util.ArrayList;
import java.util.List;

/**
 * リストビューをフィルタリングするダイアログを扱うクラス
 */
public class FilterDialogFragment extends DialogFragment {
    // デバッグ用のタグ
    private static final String TAG = "FilterDialogFragment";

    /**
     * サンプルのucsファイルをダウンロードするアクティビティ
     */
    private static DownloadActivity downloadActivity;

    /**
     * リストビューをフィルタリングするダイアログのインスタンスを生成する
     *
     * @param downloadActivity サンプルのucsファイルをダウンロードするアクティビティ
     * @return リストビューをフィルタリングするダイアログのインスタンス
     */
    static FilterDialogFragment newInstance(DownloadActivity downloadActivity) {
        FilterDialogFragment.downloadActivity = downloadActivity;
        return new FilterDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        LayoutInflater inflater = (LayoutInflater) downloadActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new NullPointerException("inflater is null.");
        }

        // 「フィルター」のダイアログのビューを取得
        final View downloadFilterView = inflater.inflate(R.layout.dialog_download_filter, (ViewGroup) downloadActivity.findViewById(R.id.downloadFilterLayout));

        // フィルタリング条件のバージョン名を選択させるスピナーを取得
        Spinner version = downloadFilterView.findViewById(R.id.downloadFilterVersion);

        // スピナーにアダプターをセット
        ArrayAdapter<String> adapter = new ArrayAdapter<>(downloadActivity, android.R.layout.simple_spinner_item, downloadActivity.versionList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        version.setAdapter(adapter);

        // ダイアログを生成して返す
        return new AlertDialog.Builder(downloadActivity)
                .setTitle(R.string.button_download_filter)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(downloadFilterView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // サンプル情報をフィルタリングする
                        filter(downloadFilterView);
                    }
                }).setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    /**
     *  「フィルター」のダイアログのビューでの入力情報をもとに、サンプル情報をフィルタリングする
     *
     * @param downloadFilterView  「フィルター」のダイアログのビュー
     */
    private void filter(View downloadFilterView) {
        // サンプルのucsファイルをダウンロードできるサンプル情報のリストビューを取得
        ListView listView = downloadActivity.findViewById(R.id.downloadListView);

        // フィルタリング条件の曲名を取得
        String songName = ((EditText) downloadFilterView.findViewById(R.id.downloadFilterSongName)).getText().toString();
        // ログ出力
        Log.d(TAG, "filter:songName=" + songName);

        // フィルタリング条件のアーティスト名を取得
        String artistName = ((EditText) downloadFilterView.findViewById(R.id.downloadFilterArtistName)).getText().toString();
        // ログ出力
        Log.d(TAG, "filter:artistName=" + artistName);

        // フィルタリング条件のBPMの最小値を取得してチェック(無指定時は-1をセット)
        float bpmFrom = -1;
        String bpmFromStr = ((EditText) downloadFilterView.findViewById(R.id.downloadFilterBpmFrom)).getText().toString();
        if (!bpmFromStr.equals("")) {
            try {
                bpmFrom = Float.parseFloat(bpmFromStr);
                if (bpmFrom < 0.1f || bpmFrom > 999.0f) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                // 「BPM」に異常値を入力した旨のトーストを出力
                Toast.makeText(downloadActivity, downloadActivity.getString(R.string.toast_formatError_bpm), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // ログ出力
        Log.d(TAG, "filter:bpmFrom=" + bpmFrom);

        // フィルタリング条件のBPMの最大値を取得してチェック(無指定時は-1をセット)
        float bpmTo = -1;
        String bpmToStr = ((EditText) downloadFilterView.findViewById(R.id.downloadFilterBpmTo)).getText().toString();
        if (!bpmToStr.equals("")) {
            try {
                bpmTo = Float.parseFloat(bpmToStr);
                if (bpmTo < 0.1f || bpmTo > 999.0f) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                // 「BPM」に異常値を入力した旨のトーストを出力
                Toast.makeText(downloadActivity, downloadActivity.getString(R.string.toast_formatError_bpm), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // ログ出力
        Log.d(TAG, "filter:bpmTo=" + bpmTo);

        // フィルタリング条件のバージョン名を取得
        String version = ((String) ((Spinner) downloadFilterView.findViewById(R.id.downloadFilterVersion)).getSelectedItem());
        // ログ出力
        Log.d(TAG, "filter:version=" + version);

        // フィルタリング条件を満たすサンプル情報のみリストに格納する
        List<UnitSample> sampleList = new ArrayList<>();
        for (UnitSample unitSample : downloadActivity.unitSampleMap.values()) {
            // 曲名のフィルタリングを行う
            if (!songName.equals("") && checkStringFromSubString(unitSample.songName, songName)) {
                continue;
            }

            // アーティスト名のフィルタリングを行う
            if (!artistName.equals("") && checkStringFromSubString(unitSample.songArtist, artistName)) {
                continue;
            }

            // BPMの最小値のフィルタリングを行う
            if (bpmFrom != -1 && unitSample.songBpmMin < bpmFrom) {
                continue;
            }

            // BPMの最大値のフィルタリングを行う
            if (bpmTo != -1 && unitSample.songBpmMax > bpmTo) {
                continue;
            }

            // バージョン名のフィルタリングを行う
            if (!version.equals(downloadActivity.getString(R.string.textView_filter_versionUnspecified)) && !unitSample.songVersion.equals(version)) {
                continue;
            }

            sampleList.add(unitSample);
        }

        // ログ出力
        Log.d(TAG, "filter:sampleList.size=" + sampleList.size());
        // フィルタリング条件を満たすサンプル情報が1つも存在しない場合は、1つも存在しない旨のトーストを出力
        if (sampleList.size() == 0) {
            Toast.makeText(downloadActivity, downloadActivity.getString(R.string.toast_filteringError_notFound), Toast.LENGTH_SHORT).show();
            return;
        }

        // リストビューにアダプターを再セット
        listView.setAdapter(new DownloadAdapter(downloadActivity, 0, sampleList));
    }

    /**
     * 指定した文字列が部分一致しないかどうかチェックする
     *
     * @param str 文字列
     * @param subStr 走査する部分文字列
     * @return 部分一致しない場合はtrue、部分一致する場合はfalse
     */
    private static boolean checkStringFromSubString(String str, String subStr) {
        for (int i = str.length() - subStr.length(); i >= 0; i--) {
            if (str.regionMatches(true, i, subStr, 0, subStr.length())) {
                // 部分一致したのでfalseを返す
                return false;
            }
        }
        // 最後までループしても部分一致しなかったのでtrueを返す
        return true;
    }
}
