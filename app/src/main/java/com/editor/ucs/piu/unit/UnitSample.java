package com.editor.ucs.piu.unit;

/**
 * ダウンロードできるサンプル情報を表すクラス
 */
public class UnitSample {
    /**
     * サンプル情報のインデックス名
     */
    public String index;

    /**
     * サンプルの曲名
     */
    public String songName;

    /**
     * サンプルの曲のアーティスト名
     */
    public String songArtist;

    /**
     * サンプルの曲のBPM値の文字列
     */
    public String songBpm;
    /**
     * サンプルの曲のBPMの最小値
     */
    public float songBpmMin;
    /**
     * サンプルの曲のBPMの最大値
     */
    public float songBpmMax;

    /**
     * サンプルの曲のバージョン名
     */
    public String songVersion;

    /**
     * サンプルのzipファイルをダウンロードできるURLの文字列
     */
    public String downloadUrl;

    /**
     * コンストラクタ
     *
     * @param index サンプル情報のインデックス名
     * @param songName サンプルの曲名
     * @param songArtist サンプルの曲のアーティスト名
     * @param songBpm サンプルの曲のBPM値の文字列
     * @param songVersion サンプルの曲のバージョン名
     * @param downloadUrl サンプルをダウンロードできるURLの文字列
     */
    public UnitSample(String index, String songName, String songArtist, String songBpm, String songVersion, String downloadUrl) {
        this.index = index;
        this.songName = songName;
        this.songArtist = songArtist;
        this.songBpm = songBpm;
        this.songVersion = songVersion;
        this.downloadUrl = downloadUrl;

        // BPM値の文字列が「xxx-yyy」の表記になっているかどうかチェック
        if (songBpm.contains("-")) {
            // 「-」のインデックスを取得
            int idx = songBpm.indexOf("-");

            // サンプルの曲のBPMの最小値を取得
            try {
                songBpmMin = Float.parseFloat(songBpm.substring(0, idx));
            } catch (NumberFormatException e) {
                // 小数に変換できない場合は-1とする
                songBpmMin = -1;
            }

            // サンプルの曲のBPMの最大値を取得
            try {
                songBpmMax = Float.parseFloat(songBpm.substring(idx + 1));
            } catch (NumberFormatException e) {
                // 小数に変換できない場合は-1とする
                songBpmMax = -1;
            }
        } else {
            // サンプルの曲のBPMの最小値と最大値を取得
            try {
                songBpmMin = Float.parseFloat(songBpm);
                songBpmMax = Float.parseFloat(songBpm);
            } catch (NumberFormatException e) {
                // 小数に変換できない場合は-1とする
                songBpmMin = -1;
                songBpmMax = -1;
            }
        }
    }
}
