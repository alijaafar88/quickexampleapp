package com.ali.example.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ali.example.R;

/**
 * Created by pdv on 2/28/17.
 */

public class RoundScoreView extends LinearLayout {

    @DimenRes private static final int BAG_SIZE = R.dimen.xsmall;
    @DimenRes private static final int BAG_HORIZONTAL_MARGIN = R.dimen.xxxsmall;

    @DrawableRes int bgRes;

    View turnIndicator;
    TextView score;
    ViewGroup remainingBagsContainer;

    public RoundScoreView(Context context) {
        this(context, null);
    }

    public RoundScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.partial_round_score, this);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundScoreView);
        bgRes = attributes.getResourceId(R.styleable.RoundScoreView_indicatorBackground, -1);
        attributes.recycle();

        turnIndicator = findViewById(R.id.iv_turn_indicator);
        score = (TextView) findViewById(R.id.tv_round_score);
        remainingBagsContainer = (LinearLayout) findViewById(R.id.group_remaining_bags);
    }

    private LayoutParams bagLayoutParams() {
        @Px int sideLength = getResources().getDimensionPixelSize(BAG_SIZE);
        @Px int horizontalMargin = getResources().getDimensionPixelSize(BAG_HORIZONTAL_MARGIN);
        LayoutParams layoutParams = new LayoutParams(sideLength, sideLength);
        layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        return layoutParams;
    }

    public void setBagsPerRound(int bagsPerRound) {
        remainingBagsContainer.removeAllViews();
        for (int i = 0; i < bagsPerRound; i++) {
            View bag = new View(getContext());
            bag.setBackground(ContextCompat.getDrawable(getContext(), bgRes));
            bag.setLayoutParams(bagLayoutParams());
            remainingBagsContainer.addView(bag);
        }
    }

    public void setBagsRemaining(int bagsRemaining) {
        for (int i = 0; i < remainingBagsContainer.getChildCount(); i++) {
            remainingBagsContainer.getChildAt(i).setSelected(i < bagsRemaining);
        }
    }

    public void setRoundScore(int roundScore) {
        this.score.setText(String.valueOf(roundScore));
    }

    public void setTurnIndicatorVisible(boolean visible) {
        turnIndicator.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

}
