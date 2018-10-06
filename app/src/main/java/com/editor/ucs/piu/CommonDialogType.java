package com.editor.ucs.piu;

/**
 * 全アクティビティのダイアログ種別を指定する列挙型
 */
public enum CommonDialogType {
	/**
	 * アラートダイアログを認識するコード
	 */
	ALERT,

	/**
	 * ボタン群のレイアウトにある「ブロック追加」ボタンを押したタイミングを認識するコード
	 */
	BUTTON_BLOCK_ADD,

    /**
     * ボタン群のレイアウトにある「ブロック削除」ボタンを押したタイミングを認識するコード
     */
    BUTTON_BLOCK_DELETE,

    /**
     * ボタン群のレイアウトにある「ブロック結合」ボタンを押したタイミングを認識するコード
     */
    BUTTON_BLOCK_MERGE,

    /**
	 * ボタン群のレイアウトにある「ブロック設定」ボタンを押したタイミングを認識するコード
	 */
	BUTTON_BLOCK_SETTING,

	/**
	 * ボタン群のレイアウトにある「ブロック分割」ボタンを押したタイミングを認識するコード
	 */
	BUTTON_BLOCK_SPLIT,

	/**
	 * 譜面のレイアウトに新規作成するタイミングを認識するコード
	 */
	BUTTON_FILE_NEW,

	/**
	 * ボタン群のレイアウトにある「UCSファイル新規作成」にて、譜面データが1回でも変更した状態のタイミングを認識するコード
	 */
	BUTTON_FILE_NEW_CHANGED,

	/**
	 * ボタン群のレイアウトにある「UCSファイルを開く」にて、譜面データが1回でも変更した状態のタイミングを認識するコード
	 */
	BUTTON_FILE_OPEN_CHANGED,

	/**
	 * ボタン群のレイアウトにある「ucsファイル名前変更」にて、譜面データが存在する状態でのタイミングを認識するコード
	 */
	BUTTON_FILE_RENAME_EXISTED,

    /**
     * ボタン群のレイアウトにある「最初の位置から再生」ボタンを押したタイミングを認識するコード
     */
    BUTTON_OTHER_PLAY_INITIALLY,

    /**
     * ボタン群のレイアウトにある「ポインターの位置から再生」ボタンを押したタイミングを認識するコード
     */
    BUTTON_OTHER_PLAY_CURRENTLY,

    /**
     * ボタン群のレイアウトにある「譜面倍率変更」ボタンを押したタイミングを認識するコード
     */
    BUTTON_OTHER_ZOOM,

    /**
	 * 詳細設定画面の「偶数番目のブロックの色」を押したタイミングを認識するコード
	 */
	LIST_BLOCK_EVEN_COLOR,

	/**
	 * 詳細設定画面の「奇数番目のブロックの色」を押したタイミングを認識するコード
	 */
	LIST_BLOCK_ODD_COLOR,

	/**
	 * 詳細設定画面の「ブロック情報のテキストの色」を押したタイミングを認識するコード
	 */
	LIST_BLOCK_TEXT_COLOR,

	/**
	 * 詳細設定画面の「作業ボタン群の表示位置」を押したタイミングを認識するコード
	 */
	LIST_BUTTONS_POSITION,

    /**
     * 詳細設定画面の「ライセンス表記」を押したタイミングを認識するコード
     */
    LIST_LICENSE,

    /**
     * 詳細設定画面の「ポインターの色(通常)」を押したタイミングを認識するコード
     */
    LIST_POINTER_COLOR,

    /**
	 * 詳細設定画面の「ポインターの色(譜面選択)」を押したタイミングを認識するコード
	 */
	LIST_POINTER_SELECTED_COLOR,

    /**
     * 詳細設定画面の「開発情報」を押したタイミングを認識するコード
     */
    LIST_VERSION,

    /**
     * 詳細設定画面の「バイブレーション」を押したタイミングを認識するコード
     */
    LIST_VIBRATION,

    /**
	 * 譜面データが1回でも変更した状態で「戻る」ボタンを押したタイミングを認識するコード
	 */
	ON_BACK_PRESSED,

	/**
	 * コピーしたノーツのデータを貼り付けるタイミングを認識するコード
	 */
	PASTE_COPIED_NOTES,
}