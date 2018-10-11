package com.editor.ucs.piu;

/**
 * 全パッケージの共通パラメータを定義する抽象staticクラス
 */
public abstract class CommonParameters {
    /**
     * AdMobのアプリID(全アクティビティで共通)
     */
    public static final String ADMOB_APP_ID = "ca-app-pub-2231903967147229~6088425420";

    /**
     * ダイアログタイトルのメッセージ情報を取得するためのタグ
     */
    public static final String BUNDLE_MESSAGE = "bundle_message";
    /**
     * ダイアログタイトルのバンドル情報を取得するためのタグ
     */
    public static final String BUNDLE_TITLE = "bundle_title";
    /**
     * ダイアログタイトルの種別情報を取得するためのタグ
     */
    public static final String BUNDLE_TYPE = "bundle_type";

    /**
     * MainActivityからDialogFragmentを表示するのに用いるタグ
     */
    public static final String DIALOG_FRAGMENT_FROM_MAIN_ACTIVITY = "MainActivityDialogFragment";
    /**
     * SettingActivityからDialogFragmentを表示するのに用いるタグ
     */
    public static final String DIALOG_FRAGMENT_FROM_SETTING_ACTIVITY = "SettingActivityDialogFragment";
    /**
     * DownloadActivityからDialogFragmentを表示するのに用いるタグ
     */
    public static final String DIALOG_FRAGMENT_FROM_DOWNLOAD_ACTIVITY = "DownloadActivityDialogFragment";

    /**
     * 偶数番目のブロックの色(R値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_EVEN_RED = "block_even_red";
    /**
     * 偶数番目のブロックの色(G値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_EVEN_GREEN = "block_even_green";
    /**
     * 偶数番目のブロックの色(B値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_EVEN_BLUE = "block_even_blue";

    /**
     * 奇数番目のブロックの色(R値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_ODD_RED = "block_odd_red";
    /**
     * 奇数番目のブロックの色(G値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_ODD_GREEN = "block_odd_green";
    /**
     * 奇数番目のブロックの色(B値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_ODD_BLUE = "block_odd_blue";

    /**
     * 枠線の色(R値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_FRAME_RED = "frame_red";
    /**
     * 枠線の色(G値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_FRAME_GREEN = "frame_green";
    /**
     * 枠線の色(B値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_FRAME_BLUE = "frame_blue";

    /**
     * ポインターが示すブロックの情報のテキストビューの色(R値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_TEXT_RED = "block_text_red";
    /**
     * ポインターが示すブロックの情報のテキストビューの色(G値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_TEXT_GREEN = "block_text_green";
    /**
     * ポインターが示すブロックの情報のテキストビューの色(B値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BLOCK_TEXT_BLUE = "block_text_blue";

    /**
     * ボタン群のレイアウトの場所が右かどうかのプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_BUTTONS_POSITION_RIGHT = "buttons_position_right";

    /**
     * 譜面再生時の確認ダイアログを今後表示しないかどうかのプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_PLAY_CONFIRMATION = "play_confirmation";

    /**
     * ポインターの色(R値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_POINTER_RED = "pointer_red";
    /**
     * ポインターの色(G値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_POINTER_GREEN = "pointer_green";
    /**
     * ポインターの色(B値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_POINTER_BLUE = "pointer_blue";
    /**
     * ポインターの色(ALPHA値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_POINTER_ALPHA = "pointer_alpha";

    /**
     * 領域の選択が有効になっているポインターの色(R値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_SELECTED_POINTER_RED = "selected_pointer_red";
    /**
     * 領域の選択が有効になっているポインターの色(G値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_SELECTED_POINTER_GREEN = "selected_pointer_green";
    /**
     * 領域の選択が有効になっているポインターの色(B値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_SELECTED_POINTER_BLUE = "selected_pointer_blue";
    /**
     * 領域の選択が有効になっているポインターの色(ALPHA値)のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_SELECTED_POINTER_ALPHA = "selected_pointer_alpha";

    /**
     * バイブレーションのプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_VIBRATION = "vibration";

    /**
     * 譜面倍率のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_ZOOM = "zoom";
    /**
     * 譜面倍率のラジオボタンの添字番号のプリファレンス情報を取得するためのタグ
     */
    public static final String PREFERENCE_ZOOM_INDEX = "zoom_index";

    /**
     * 内部ストレージからucsファイルを読み込むパーミッションが許可されてない場合、
     * その許可を求めるダイアログを識別するための8bit長のコード
     */
    public static final int PERMISSION_UCS_READ = 1;
    /**
     * 内部ストレージへucsファイルを書き込むパーミッションが許可されてない場合、
     * その許可を求めるダイアログを識別するための8bit長のコード
     */
    public static final int PERMISSION_UCS_SAVE = 2;
    /**
     * 内部ストレージの別ディレクトリへucsファイルを書き込むパーミッションが許可されてない場合、
     * その許可を求めるダイアログを識別するための8bit長のコード
     */
    public static final int PERMISSION_UCS_SAVE_AS = 3;
    /**
     * 内部ストレージへダウンロードしたサンプル用のファイルを書き込むパーミッションが許可されてない場合、
     * その許可を求めるダイアログを識別するための8bit長のコード
     */
    public static final int PERMISSION_SAMPLE_DOWNLOAD = 4;

    /**
     * サンプルのucsファイルをダウンロードするWebページを取得できるURL
     * 曲ごとのURLは、この文字列の後にPHPの引数を表す文字列を連結すること
     */
    public static final String URL_SAMPLE_UCS_DOWNLOAD = "http://www.piugame.com/bbs/piu.ucs.sample.download.php";
    /**
     * サンプルのucsファイルをダウンロードできる一覧のWebページを取得できるURL
     * バージョンごとのURLは、この文字列の後にPHPの引数を表す文字列を連結すること
     */
    public static final String URL_SAMPLE_UCS_OVERALL = "http://www.piugame.com/piu.ucs/ucs.sample/ucs.sample.alltunes.php";

    // スマートフォン用WebページをPC用として取得するユーザエージェントの指定
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";

    // 抽象staticクラスなのでコンストラクタはprivateにする
    private CommonParameters() {
    }
}
