# PIU ucs Editor
![icon](icon.png)  
既存Windows版ソフトウェアの[StepEdit Lite](http://www.piugame.com/piu.ucs/ucs.intro/ucs.intro.php)のAndroid版アプリを制作

## インストール
[こちら](https://github.com/infhyroyage/PIUucsEditor/raw/master/app/release/app-release.apk)からapkファイルをダウンロードし、**Android OS 2.2以上**のAndroid端末上でapkファイルを展開。

## StepEdit Liteとの同等機能
* 譜面編集
    - 設置したい列でのブロックの上をタップすると、ポインター上に単ノートを設置
    - 設置したい列でのブロックの上を長押しすると、ポインター上にホールドの1点を設置
* 譜面再生(ハンドクラップ)
* 譜面のブロック編集
* 譜面倍率設定
* ucsファイル読み込み、保存
* 中抜きホールドを含むucsファイルの表示
    - 中抜きホールドの編集には未対応

## StepEdit Liteには存在しない固有機能
* ポインターの概念
    * オブジェクトの設置はポインターが示す位置を基準とする
    * スクロールビューで見えている位置と、ポインターが示す位置は異なる点に注意
* 複数ノートに対する選択範囲の概念
    * Stepmaniaの譜面編集機能のイメージと同等
    * コピー、切り取り、貼り付け、回転、削除機能を実装
* サンプルucsファイルのダウンロード

## TODO
* 枠線の色変更の設定
* 譜面停止対応
* ノートスキン変更の設定
