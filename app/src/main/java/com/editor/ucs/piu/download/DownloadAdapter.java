package com.editor.ucs.piu.download;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.unit.UnitSample;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * サンプルのucsファイルをダウンロードできるリストビューのアダプターを表す内部クラス
 */
class DownloadAdapter extends ArrayAdapter<UnitSample> {
    /**
     * サンプルのucsファイルをダウンロードするアクティビティ
     */
    private DownloadActivity downloadActivity;

    private LayoutInflater layoutInflater;

    DownloadAdapter(DownloadActivity downloadActivity, int id, List<UnitSample> users) {
        super(downloadActivity, id, users);

        this.downloadActivity = downloadActivity;
        this.layoutInflater = (LayoutInflater) downloadActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        // アイテムを取得
        final UnitSample unitSample = getItem(position);
        if (unitSample == null) {
            throw new IllegalArgumentException("The UnitSample argument cannot be applied.");
        }

        // アイテムのビューをインフレートしてない場合はインフレートする
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_download, parent, false);
        }

        // サンプルの情報を各テキストビューにセット
        ((TextView) view.findViewById(R.id.downloadIndex)).setText(unitSample.index);
        ((TextView) view.findViewById(R.id.downloadSongName)).setText(unitSample.songName);
        ((TextView) view.findViewById(R.id.downloadSongArtist)).setText(unitSample.songArtist);
        ((TextView) view.findViewById(R.id.downloadSongBpm)).setText(downloadActivity.getString(R.string.textView_bpm_string, unitSample.songBpm));
        ((TextView) view.findViewById(R.id.downloadSongVersion)).setText(unitSample.songVersion);

        return view;
    }
}
