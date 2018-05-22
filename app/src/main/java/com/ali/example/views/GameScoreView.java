package com.ali.example.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ali.example.R;

/**
 * Created by pdv on 2/28/17.
 */

public class GameScoreView extends LinearLayout {

    TextView redTeamScore;
    TextView blueTeamScore;

    public GameScoreView(Context context) {
        this(context, null);
    }

    public GameScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.partial_game_score, this);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        redTeamScore = (TextView) findViewById(R.id.tv_red_game_score);
        blueTeamScore = (TextView) findViewById(R.id.tv_blue_game_score);
        setRedTeamScore(0);
        setBlueTeamScore(0);
    }

    public void setRedTeamScore(int redTeamScore) {
        this.redTeamScore.setText(String.valueOf(redTeamScore));
    }

    public void setBlueTeamScore(int blueTeamScore) {
        this.blueTeamScore.setText(String.valueOf(blueTeamScore));
    }

}
