package com.editor.ucs.piu.chart;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.buttons.ButtonsLayout;
import com.editor.ucs.piu.main.MainActivity;
import com.editor.ucs.piu.unit.UnitBlock;
import com.mixiaoxiao.fastscroll.FastScrollScrollView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * メイン画面の譜面のスクロールビューを表すクラス
 */
public class ChartScrollView extends FastScrollScrollView {
    // デバッグ用のタグ
    private static final String TAG = "ChartScrollView";

    /**
     * 譜面のスクロールビューでの再生動作を行うタイマー
     * 再生動作を行わない場合はnullをセットしておくこと
     */
    private CountDownTimer playTimer = null;

    /**
     * BGMを再生するためのメディアプレーヤー
     * BPMを再生しない場合はnullをセットしておくこと
     */
    private MediaPlayer mediaPlayer = null;

    /**
     * ノート音を鳴らすためのサウンドプール
     */
    private SoundPool noteSoundPool;

    /**
     * ビューがスクロール可能かどうかのフラグ
     * 再生中はスクロール不可のためfalseにセットすること
     */
    public boolean isScrolled = true;

    public ChartScrollView(Context context) {
        super(context);
    }

    public ChartScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 指定された位置を示す行番号から最後の行番号まで再生する
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param startRow 再生したい位置を示す行番号
     */
    public void play(final MainActivity mainActivity, int startRow) {
        // ログ出力
        Log.d(TAG, "play:startRow=" + startRow);

        // ボタン群のレイアウトを取得
        ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);
        // 譜面のレイアウトを取得
        ChartLayout chartLayout = mainActivity.findViewById(R.id.chartLayout);
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        // サイドバーのボタンの内容を示すスピナーを無効にする
        buttonsLayout.spinner.setEnabled(false);

        // 「最初の位置から再生」、「ポインターの位置から再生」、「譜面倍率変更」、「詳細設定」ボタンを非表示にする
        buttonsLayout.buttonOtherPlayInitially.setVisibility(GONE);
        buttonsLayout.buttonOtherPlayCurrently.setVisibility(GONE);
        buttonsLayout.buttonOtherZoom.setVisibility(GONE);
        buttonsLayout.buttonOtherSetting.setVisibility(GONE);

        // 「譜面の再生を中断」ボタンを表示する
        buttonsLayout.buttonOtherInterrupt.setVisibility(VISIBLE);

        // 譜面のブロックごとの開始地点のY座標からの相対距離を初期化
        List<Float> distances = new ArrayList<>();
        distances.add(0f);
        // 譜面のブロックごとの開始地点からの時刻を初期化
        List<Float> milestones = new ArrayList<>();
        milestones.add(0f);
        // ノート音を鳴らすタイミングを初期化
        Set<Float> timings = new TreeSet<>();

        // 再生したい位置での位置情報を計算
        ChartLayout.PositionInformation information = chartLayout.calcPositionInformation(startRow);

        /*
         * BGMを最初からシークする時間、BGMを再生する動作の待機時間を計算する(すべて単位はms)
         * NOTE : 最初以外の譜面のブロックのdelay値は、ゲームの仕様通り無視する
         */
        float waitBGMPeriod = 0f;
        float seekPeriod = Math.max(0f, information.seekPeriod + chartLayout.blockList.get(0).delay);
        if (chartLayout.blockList.get(0).delay < 0) {
            waitBGMPeriod = Math.max(0f, - information.seekPeriod - chartLayout.blockList.get(0).delay);
        }

        // ログ出力
        Log.d(TAG, "play:seekPeriod=" + seekPeriod);
        Log.d(TAG, "play:waitBGMPeriod=" + waitBGMPeriod);

        // 再生したい位置でのY座標を取得(この地点ではBGMを再生する動作の待機時間を考慮していない)
        float initialY = information.coordinate;
        // 再生したい位置における、譜面のブロックのインスタンスを取得
        UnitBlock unitBlock = chartLayout.blockList.get(information.idxBlock);

        if (waitBGMPeriod > 0f) {
            // BGMを再生する動作を待機する場合は、開始地点のY座標を加算し、1番目の譜面のブロックにおける開始地点のY座標からの相対距離と、その時刻を計算
            float initialPeriod = 0f;
            for (int i = 0; i < chartLayout.blockList.size(); i++) {
                unitBlock = chartLayout.blockList.get(i);

                // 「i < information.idxBlock」の場合は何もしない
                if (i == information.idxBlock) {
                    initialPeriod += 60000 * (unitBlock.rowLength - startRow + information.offsetRowLength + 1) / ((unitBlock.split + 1) * unitBlock.bpm);
                    if (waitBGMPeriod <= initialPeriod) {
                        // 開始地点のY座標、1番目の譜面のブロックにおける開始地点のY座標からの相対距離、その時刻の最終決定
                        initialY += waitBGMPeriod * chartLayout.noteLength * chartLayout.zoom * unitBlock.bpm / 30000f;
                        distances.add((initialPeriod - waitBGMPeriod) * chartLayout.noteLength * chartLayout.zoom * unitBlock.bpm / 30000f);
                        milestones.add(initialPeriod - waitBGMPeriod);
                        break;
                    } else {
                        // 開始地点のY座標をインクリメント
                        initialY += 2 * chartLayout.noteLength * chartLayout.zoom * (unitBlock.rowLength - startRow + information.offsetRowLength + 1) / (unitBlock.split + 1);
                        // 位置情報を更新
                        information.offsetRowLength += unitBlock.rowLength;
                    }
                } else if (i > information.idxBlock) {
                    initialPeriod += 60000 * unitBlock.rowLength / ((unitBlock.split + 1) * unitBlock.bpm);
                    if (waitBGMPeriod <= initialPeriod) {
                        // 開始地点のY座標、1番目の譜面のブロックにおける開始地点のY座標からの相対距離、その時刻の最終決定
                        initialY += (waitBGMPeriod - initialPeriod + 60000 * unitBlock.rowLength / ((unitBlock.split + 1) * unitBlock.bpm)) * chartLayout.noteLength * chartLayout.zoom * unitBlock.bpm / 30000f;
                        distances.add((initialPeriod - waitBGMPeriod) * chartLayout.noteLength * chartLayout.zoom * unitBlock.bpm / 30000f);
                        milestones.add(initialPeriod - waitBGMPeriod);

                        // 位置情報を更新
                        information.idxBlock = i;

                        break;
                    } else {
                        // 開始地点のY座標をインクリメント
                        initialY += unitBlock.height;
                        // 位置情報を更新
                        information.offsetRowLength += unitBlock.rowLength;
                    }
                }
            }
        } else {
            // BPMを再生する動作を待機しない場合は、1番目の譜面のブロックにおける開始地点のY座標からの相対距離と、その時刻のみ計算
            distances.add(2 * chartLayout.noteLength * chartLayout.zoom * (information.offsetRowLength + unitBlock.rowLength - startRow + 1) / (unitBlock.split + 1));
            milestones.add(60000 * (unitBlock.rowLength - startRow + information.offsetRowLength + 1) / ((unitBlock.split + 1) * unitBlock.bpm));
        }

        // 開始地点のY座標に移動
        scrollTo(0, (int) initialY);

        // ログ出力
        Log.d(TAG, "play:initialY=" + initialY);
        Log.d(TAG, "play:distances[" + (distances.size() - 1) + "]=" + distances.get(distances.size() - 1));
        Log.d(TAG, "play:milestones[" + (milestones.size() - 1) + "]=" + milestones.get(milestones.size() - 1));

        // 1番目の譜面のブロックに属するノートのマップから、そのノート音のタイミングをセット
        for (Integer key : noteLayout.noteMap.subMap(startRow * 10, (information.offsetRowLength + unitBlock.rowLength + 1) * 10).keySet()) {
            timings.add((milestones.get(milestones.size() - 1) + waitBGMPeriod) * (key / 10 - startRow) / (information.offsetRowLength + unitBlock.rowLength - startRow + 1f) - waitBGMPeriod);

            // ログ出力
            Log.d(TAG, "play:timings<-" + ((milestones.get(milestones.size() - 1) + waitBGMPeriod) * (key / 10 - startRow) / (information.offsetRowLength + unitBlock.rowLength - startRow + 1f) - waitBGMPeriod));
        }

        // 位置情報を更新
        information.offsetRowLength += unitBlock.rowLength;
        information.idxBlock++;

        for (; information.idxBlock < chartLayout.blockList.size(); information.idxBlock++) {
            // 2番目以降の譜面の各ブロックにおける、開始地点のY座標からの相対距離と、開始地点からの時刻をセット
            unitBlock = chartLayout.blockList.get(information.idxBlock);
            distances.add(distances.get(distances.size() - 1) + unitBlock.height);
            milestones.add(milestones.get(milestones.size() - 1) + 60000 * unitBlock.rowLength / ((unitBlock.split + 1) * unitBlock.bpm));
            // ログ出力
            Log.d(TAG, "play:distances[" + (distances.size() - 1) + "]=" + distances.get(distances.size() - 1));
            Log.d(TAG, "play:milestones[" + (milestones.size() - 1) + "]=" + milestones.get(milestones.size() - 1));

            // 2番目以降の譜面の各ブロックに属するノートのマップから、そのノート音のタイミングをセット
            for (Integer key : noteLayout.noteMap.subMap((information.offsetRowLength + 1) * 10, (information.offsetRowLength + unitBlock.rowLength + 1) * 10).keySet()) {
                timings.add(milestones.get(milestones.size() - 2) + (milestones.get(milestones.size() - 1) - milestones.get(milestones.size() - 2)) * (key / 10 - information.offsetRowLength - 1) / unitBlock.rowLength);
                // ログ出力
                Log.d(TAG, "play:timings=" + (milestones.get(milestones.size() - 2) + (milestones.get(milestones.size() - 1) - milestones.get(milestones.size() - 2)) * (key / 10 - information.offsetRowLength - 1) / unitBlock.rowLength));
            }

            information.offsetRowLength += unitBlock.rowLength;
        }

        // BGMを再生するためのメディアプレーヤーを生成
        mediaPlayer = new MediaPlayer();
        try {
            // mp3ファイルの絶対パスをセット
            mediaPlayer.setDataSource(mainActivity.ucs.fileDir + "/" + mainActivity.ucs.fileName + ".mp3");

            // 再生可能状態になるまで待機
            mediaPlayer.prepare();

            // BGMをシーク
            mediaPlayer.seekTo((int) seekPeriod);
        } catch (IOException e) {
            // 対応するmp3ファイルが存在しない場合は、BGMのデータを開放し、nullを代入して再生しないようにする
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // ノート音を鳴らすためのサウンドプールを生成
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            noteSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        } else {
            noteSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setMaxStreams(1)
                    .build();
        }

        // 再生動作を行うタイマーをセット
        setPlayTimer(mainActivity, initialY, distances, milestones, new ArrayList<>(timings));

        // 再生動作を開始する
        playTimer.start();
    }

    /**
     * 譜面のスクロールビューの再生動作を行うタイマーをセットする
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param initialY 開始地点のY座標(px単位)
     * @param distances 0、譜面のブロックごとの開始地点のY座標からの相対距離、開始地点のY座標から終了地点までの相対距離(px単位、昇順)
     * @param milestones 0、開始地点からの譜面のブロックごとの時刻、終了地点の時刻(ms単位、昇順)
     * @param timings ノート音を鳴らす再生開始からの時刻(ms単位)
     * @throws IllegalArgumentException distancesとmilestonesの要素数が不正な場合
     */
    public void setPlayTimer(final MainActivity mainActivity, final float initialY, final List<Float> distances, final List<Float> milestones, final List<Float> timings) {
        // 要素数のチェック
        if (distances.size() != milestones.size()) {
            throw new IllegalArgumentException("distances.size=" + distances.size() + ",milestones.size=" + milestones.size());
        }

        // ボタン群のレイアウトを取得
        final ButtonsLayout buttonsLayout = mainActivity.findViewById(R.id.buttonsLayout);

        // BGM再生時間、ノート音の動作時間を取得
        final float playPeriod = milestones.get(milestones.size() - 1);

        // ノート音のサウンドデータを読み込む
        final int noteSound = noteSoundPool.load(mainActivity, R.raw.note_sound, 1);

        // 譜面のスクロールビューのスクロール動作を行わないようにする
        isScrolled = false;

        // 10ミリ秒ごとに更新する再生動作を行うタイマーを定義する
        final int[] timingIdx = {0};
        final int[] distanceIdx = {1};
        playTimer = new CountDownTimer((long) playPeriod, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                // オフセットの分だけ待機し、まだBGMを再生していない場合、BGMを再生を開始する
                if (mediaPlayer != null && millisUntilFinished <= playPeriod && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }

                // ノート音を鳴らす時刻を少しでも超えた場合、ノート音を鳴らしてインデックスを更新する
                if (timingIdx[0] != timings.size() && millisUntilFinished <= playPeriod - timings.get(timingIdx[0])) {
                    if (buttonsLayout.toggleButtonOtherNoteSound.isChecked()) {
                        noteSoundPool.play(noteSound, 1.0f, 1.0f, 0, 0, 1.0f);
                    }
                    timingIdx[0]++;
                }

                // 譜面のブロックが変化した場合、インデックスを更新する
                if (distanceIdx[0] != distances.size() - 1 && millisUntilFinished <= playPeriod - milestones.get(distanceIdx[0])) {
                    distanceIdx[0]++;
                }
                scrollTo(0, (int) (initialY + distances.get(distanceIdx[0] - 1) + (distances.get(distanceIdx[0]) - distances.get(distanceIdx[0] - 1)) * (playPeriod - millisUntilFinished - milestones.get(distanceIdx[0] - 1)) / (milestones.get(distanceIdx[0]) - milestones.get(distanceIdx[0] - 1))));
            }
            @Override
            public void onFinish() {
                // 「譜面の再生を中断」ボタンを非表示にする
                buttonsLayout.buttonOtherInterrupt.setVisibility(View.GONE);

                // 「最初の位置から再生」、「ポインターの位置から再生」、「譜面倍率変更」、「詳細設定」ボタンを表示する
                buttonsLayout.buttonOtherSetting.setVisibility(View.VISIBLE);
                buttonsLayout.buttonOtherZoom.setVisibility(View.VISIBLE);
                buttonsLayout.buttonOtherPlayCurrently.setVisibility(View.VISIBLE);
                buttonsLayout.buttonOtherPlayInitially.setVisibility(View.VISIBLE);

                // サイドバーのボタンの内容を示すスピナーを有効にする
                buttonsLayout.spinner.setEnabled(true);

                // 譜面のスクロールビューのスクロール動作を行うことができるようにする
                isScrolled = true;

                if (playPeriod == timings.get(timings.size() - 1)) {
                    // スクロール動作が終了した座標にノートがある場合、onTick()ではノート音を鳴らせないので、このタイミングで鳴らす
                    if (buttonsLayout.toggleButtonOtherNoteSound.isChecked()) {
                        noteSoundPool.play(noteSound, 1.0f, 1.0f, 0, 0, 1.0f);
                    }
                    /*
                     * ノート音が鳴り終わる前にクラップ音のサウンドプールのデータを開放すると
                     * 開放したタイミングでノート音が途切れてしまうため、500ミリ秒だけsleepしてから開放する
                     */
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    noteSoundPool.release();
                } else {
                    // 上記以外の場合は、即座にノート音のサウンドプールのデータを開放する
                    noteSoundPool.release();
                }

                // BGMのデータを開放する
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        };
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        // ビューがスクロール可能でない場合は、強制的にfalseを返す
        return isScrolled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // ビューがスクロール可能でない場合は、強制的にfalseを返す
        return isScrolled && super.onInterceptTouchEvent(event);
    }

    /**
     * 再生動作中の場合、再生動作を中断する
     * 再生動作中ではない場合、何もしない
     */
    public void interrupt() {
        if (playTimer != null) {
            // 再生動作のタイマーを終了する
            playTimer.cancel();
            // 譜面のスクロールビューのスクロール動作を行うことができるようにする
            isScrolled = true;
            // ノート音のサウンドプールのデータを開放する
            noteSoundPool.release();
            // BGMのデータを開放する
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
}