package com.editor.ucs.piu.unit;

import android.widget.ImageView;

import com.editor.ucs.piu.R;
import com.editor.ucs.piu.chart.NoteLayout;
import com.editor.ucs.piu.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 単ノートorホールドを扱うクラス
 */
public class UnitNote {
    /**
     * ノートの列番号
     * 範囲はSingle譜面のとき5~9、Double譜面のとき0~9
     */
    public byte column;

    /**
     * 単ノートorホールドの始点の行番号
     * 範囲は自然数全体
     * 2つの値が同じ場合は単ノート、違う場合はホールドに対応
     */
    public int start, goal;

    /**
     * 中抜きホールドの中間にかぶせる始点、終点の行番号のリスト
     * 範囲は自然数全体
     * 中抜きホールドではない場合は要素数0のリストにすること
     */
    public List<Integer> hollowStartList, hollowGoalList;

    /**
     * 単ノートorホールドの始点のイメージビュー
     */
    public ImageView startView;
    /**
     * ホールドor中抜きホールドの始点と終点との間のイメージビュー
     */
    public ImageView holdView;
    /**
     * 中抜きホールドの中間にかぶせる始点と終点との間のイメージビューのリスト
     */
    public List<ImageView> hollowViewList;
    /**
     * ホールドの終点のイメージビュー
     */
    public ImageView goalView;

    /**
     * 単ノートorホールドの1点目のコンストラクタ
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param column       単ノートorホールドの列番号
     * @param row          単ノートorホールドの行番号
     */
    public UnitNote(MainActivity mainActivity, byte column, int row) {
        this.column = column;
        this.start = row;
        this.goal = row;
        this.hollowStartList = new ArrayList<>();
        this.hollowGoalList = new ArrayList<>();

        // イメージビューの生成
        startView = new ImageView(mainActivity);
        startView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    /**
     * 単ノートorホールドのコンストラクタ
     *
     * @param mainActivity メイン画面のアクティビティ
     * @param column       単ノートorホールドの列番号
     * @param start        単ノートorホールドの始点の行番号
     * @param goal         単ノートorホールドの終点の行番号
     */
    public UnitNote(MainActivity mainActivity, byte column, int start, int goal) {
        this.column = column;
        this.start = start;
        this.goal = goal;
        this.hollowStartList = new ArrayList<>();
        this.hollowGoalList = new ArrayList<>();

        // イメージビューの生成
        startView = new ImageView(mainActivity);
        startView.setScaleType(ImageView.ScaleType.FIT_XY);

        // ホールドかどうか判断
        if (start != goal) {
            holdView = new ImageView(mainActivity);
            holdView.setScaleType(ImageView.ScaleType.FIT_XY);
            goalView = new ImageView(mainActivity);
            goalView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    /**
     * 単ノートorホールドor中抜きホールドのコンストラクタ
     *
     * @param mainActivity    メイン画面のアクティビティ
     * @param column          単ノートorホールドor中抜きホールドの列番号
     * @param start           単ノートorホールドor中抜きホールドの始点の行番号
     * @param goal            単ノートorホールドor中抜きホールドの終点の行番号
     * @param hollowStartList 中抜きホールドの中間にかぶせる始点の行番号のリスト
     * @param hollowGoalList  中抜きホールドの中間にかぶせる終点の行番号のリスト
     */
    public UnitNote(MainActivity mainActivity, byte column, int start, int goal, List<Integer> hollowStartList, List<Integer> hollowGoalList) {
        this.column = column;
        this.start = start;
        this.goal = goal;
        this.hollowStartList = new ArrayList<>();
        this.hollowStartList.addAll(hollowStartList);
        this.hollowGoalList = new ArrayList<>();
        this.hollowGoalList.addAll(hollowGoalList);

        // イメージビューの生成
        startView = new ImageView(mainActivity);
        startView.setScaleType(ImageView.ScaleType.FIT_XY);

        // ホールドかどうか判断
        if (start != goal) {
            holdView = new ImageView(mainActivity);
            holdView.setScaleType(ImageView.ScaleType.FIT_XY);
            goalView = new ImageView(mainActivity);
            goalView.setScaleType(ImageView.ScaleType.FIT_XY);

            // 中抜きホールドかどうか判断
            if (hollowStartList.size() == hollowGoalList.size() && hollowStartList.size() > 0) {
                hollowViewList = new ArrayList<>();
                for (int i = 0; i < hollowStartList.size(); i++) {
                    ImageView hollowView = new ImageView(mainActivity);
                    hollowView.setScaleType(ImageView.ScaleType.FIT_XY);
                    hollowViewList.add(hollowView);
                }
            }
        }
    }

    /**
     * リソースのpng画像をセットして、イメージビューを更新する
     * イメージビューはAlpha値を128にセットせず、Alpha値128のpngファイルを直接用意する
     *
     * @param mainActivity メイン画面のアクティビティ
     */
    public void updateAllViews(MainActivity mainActivity) {
        // ノートのレイアウトを取得
        NoteLayout noteLayout = mainActivity.findViewById(R.id.noteLayout);

        switch (column % 5) {
            // 左下の場合
            case 0:
                if (noteLayout.holdEdge[column] > 0) {
                    startView.setImageResource(R.drawable.edge0);
                } else {
                    startView.setImageResource(R.drawable.note0);
                }
                break;
            // 左上の場合
            case 1:
                if (noteLayout.holdEdge[column] > 0) {
                    startView.setImageResource(R.drawable.edge1);
                } else {
                    startView.setImageResource(R.drawable.note1);
                }
                break;
            // 真中の場合
            case 2:
                if (noteLayout.holdEdge[column] > 0) {
                    startView.setImageResource(R.drawable.edge2);
                } else {
                    startView.setImageResource(R.drawable.note2);
                }
                break;
            // 右上の場合
            case 3:
                if (noteLayout.holdEdge[column] > 0) {
                    startView.setImageResource(R.drawable.edge3);
                } else {
                    startView.setImageResource(R.drawable.note3);
                }
                break;
            // 右下の場合
            case 4:
                if (noteLayout.holdEdge[column] > 0) {
                    startView.setImageResource(R.drawable.edge4);
                } else {
                    startView.setImageResource(R.drawable.note4);
                }
                break;
        }

        // ホールドかどうか判断
        if (start != goal) {
            // 中抜きホールドかどうかのフラグ
            boolean isHollow = hollowStartList.size() == hollowGoalList.size() && hollowStartList.size() > 0;
            switch (column % 5) {
                // 左下の場合
                case 0:
                    goalView.setImageResource(R.drawable.note0);
                    if (isHollow) {
                        holdView.setImageResource(R.drawable.hollow0);
                        for (ImageView hollowView : hollowViewList) {
                            hollowView.setImageResource(R.drawable.hold0);
                        }
                    } else {
                        holdView.setImageResource(R.drawable.hold0);
                    }
                    break;
                // 左上の場合
                case 1:
                    goalView.setImageResource(R.drawable.note1);
                    if (isHollow) {
                        holdView.setImageResource(R.drawable.hollow1);
                        for (ImageView hollowView : hollowViewList) {
                            hollowView.setImageResource(R.drawable.hold1);
                        }
                    } else {
                        holdView.setImageResource(R.drawable.hold1);
                    }
                    break;
                // 真中の場合
                case 2:
                    goalView.setImageResource(R.drawable.note2);
                    if (isHollow) {
                        holdView.setImageResource(R.drawable.hollow2);
                        for (ImageView hollowView : hollowViewList) {
                            hollowView.setImageResource(R.drawable.hold2);
                        }
                    } else {
                        holdView.setImageResource(R.drawable.hold2);
                    }
                    break;
                // 右上の場合
                case 3:
                    goalView.setImageResource(R.drawable.note3);
                    if (isHollow) {
                        holdView.setImageResource(R.drawable.hollow3);
                        for (ImageView hollowView : hollowViewList) {
                            hollowView.setImageResource(R.drawable.hold3);
                        }
                    } else {
                        holdView.setImageResource(R.drawable.hold3);
                    }
                    break;
                // 右下の場合
                case 4:
                    goalView.setImageResource(R.drawable.note4);
                    if (isHollow) {
                        holdView.setImageResource(R.drawable.hollow4);
                        for (ImageView hollowView : hollowViewList) {
                            hollowView.setImageResource(R.drawable.hold4);
                        }
                    } else {
                        holdView.setImageResource(R.drawable.hold4);
                    }
                    break;
            }
        }
    }

    /**
     * このノートのインスタンスのコピーを返す
     *
     * @param mainActivity メイン画面のアクティビティ
     * @return ノートのインスタンスのコピー
     */
    public UnitNote copy(MainActivity mainActivity) {
        return new UnitNote(mainActivity, column, start, goal, hollowStartList, hollowGoalList);
    }
}
