package com.ali.example.views;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ali.example.R;
import com.ali.example.models.BagState;
import com.ali.example.models.Team;

import java.util.EnumMap;

/**
 * Created by pdv on 3/1/17.
 */

public class BagView extends ImageView {

    @DimenRes
    private static final int BAG_SIZE = R.dimen.large;

    private static final EnumMap<Team, Integer> SOURCES;
    private BagState savedState = new BagState();
    static {
        SOURCES = new EnumMap<>(Team.class);
        SOURCES.put(Team.BLUE, R.drawable.bag_blue);
        SOURCES.put(Team.RED, R.drawable.bag_red);
    }


    public BagView(Context context) {
        this(context, Team.RED);
    }

    public BagView(Context context, @NonNull Team team) {
        this(context, team,(float) Math.random() * 180);
    }

    public BagView(Context context, @NonNull Team team, float rotation){
        super(context);
        this.savedState.team = team;
        setImageResource(SOURCES.get(savedState.team));
        @Px int bagSize = getResources().getDimensionPixelSize(BAG_SIZE);
        setLayoutParams(new ViewGroup.LayoutParams(bagSize, bagSize));
        savedState.rotation = rotation;
        setRotation(savedState.rotation);
    }

   public BagView(Context context, @NonNull Team team, float rotation , float positionX, float positionY) {
        this(context,team, rotation);
        setRotation(savedState.rotation);
        centerAround(positionX,positionY);
        savedState.positionX = positionX;
        savedState.positionY = positionY;
        savedState.rotation = rotation;

    }

    public Team getTeam() {
        return savedState.team;
    }

    public BagState getSavedState() {
        return savedState;
    }

    public void centerAround(float x, float y) {
        setX(x - getLayoutParams().width / 2);
        setY(y - getLayoutParams().width / 2);
        savedState.positionX = x;
        savedState.positionY = y;
    }

    public Point getCenter() {
        int halfWidth = getLayoutParams().width / 2;
        return new Point((int) getX() + halfWidth, (int) getY() + halfWidth);
    }

}
