package com.ali.example.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ali.example.models.BagMovedEvent;
import com.ali.example.models.BagState;
import com.ali.example.models.BagStatus;
import com.ali.example.R;
import com.ali.example.models.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pdv on 2/28/17.
 */

public class FieldView extends RelativeLayout {

    public interface BagListener {
        Team getCurrentTeam();
        void onBagMoved(BagMovedEvent bagMovedEvent);
    }

    @DrawableRes private static final int BOARD_RES = R.drawable.board;
    @DrawableRes private static final int BG_RES = R.drawable.bg_sunny;
    @Px private static final int GROUND_Z = 0;
    @Px private static final int BOARD_Z = 1;
    @Px private static final int FLOATING_Z = 2;

    // The shadows are baked into the board asset; proportions of their respective dimensions
    private static final double LEFT_SHADOW = 0.0827;
    private static final double RIGHT_SHADOW = 0.147;
    private static final double TOP_SHADOW = 0.0339;
    private static final double BOTTOM_SHADOW = 0.0823;
    private static final double HOLE_TOP_OFFSET = 0.2085;

    ImageView bgView;
    ImageView boardView;
    BagListener bagListener;

    Rect boardBounds;
    Rect holeBounds;

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);

        bgView = makeBackground(context, BG_RES);
        boardView = makeBoard(context, BOARD_RES);
        boardView.setZ(BOARD_Z);

        addView(bgView);
        addView(boardView);

        post(() -> {
            boardBounds = getBoardBounds(boardView);
            holeBounds = getHole(boardBounds);
        });
    }

    public void setBagListener(BagListener bagListener) {
        this.bagListener = bagListener;
    }

    private static ImageView makeBackground(Context context, @DrawableRes int drawableRes) {
        ImageView bgView = new ImageView(context);
        bgView.setImageResource(drawableRes);
        bgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bgView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return bgView;
    }

    private static ImageView makeBoard(Context context, @DrawableRes int drawableRes) {
        ImageView boardView = new ImageView(context);
        boardView.setImageResource(drawableRes);
        boardView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Layout with "large" top and bottom margins
        LayoutParams boardLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        @Px int verticalMargin = context.getResources().getDimensionPixelSize(R.dimen.large);
        boardLayoutParams.setMargins(0, verticalMargin, 0, verticalMargin);
        boardLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        boardView.setLayoutParams(boardLayoutParams);

        // The image resource is slightly off-center because of the shadow, this centers it
        @Px int leftPadding = context.getResources().getDimensionPixelSize(R.dimen.small);
        boardView.setPadding(leftPadding, 0, 0, 0);

        return boardView;
    }

    private static Rect getBoardBounds(View boardView) {
        int rawWidth = boardView.getWidth();
        int rawHeight = boardView.getHeight();
        int leftShadow = (int) (LEFT_SHADOW * rawWidth) + boardView.getPaddingLeft();
        int topShadow = (int) (TOP_SHADOW * rawHeight);
        int x = (int) boardView.getX() + leftShadow;
        int y = (int) boardView.getY() + topShadow;
        int width = rawWidth - leftShadow - (int) (RIGHT_SHADOW * rawWidth);
        int height = rawHeight - topShadow - (int) (BOTTOM_SHADOW * rawHeight);

        return new Rect(x, y, x + width, y + height);
    }

    // Yeah, technically the hole is square
    private static Rect getHole(Rect board) {
        int centerX = board.centerX();
        int centerY = board.top + (int) (board.height() * HOLE_TOP_OFFSET);
        int radius = board.width() / 6;
        return new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    /**
     * Returns the location of the bag - on the board, off the board, or in the hole
     */
    private BagStatus getStatus(Point point) {
        if (holeBounds.contains(point.x, point.y)) {
            return BagStatus.IN_HOLE;
        } else if (boardBounds.contains(point.x, point.y)) {
            return BagStatus.ON_BOARD;
        }
        return BagStatus.OFF_BOARD;
    }

    // If we recieve a touch event, there must not've been any bags in the way, so we should create
    // a new bag and add it to this view. We continue to catch the remainder of the touch event and
    // dispatch it to the new bag, until it can receive its own.

    boolean newBagsAllowed = true;
    List<BagView> bags = new ArrayList<>();

    public ArrayList<BagState> getBagsSaveState(){
        ArrayList<BagState> bagStates = new ArrayList<>(bags.size());
        for(int i = 0; i < bags.size(); i++){
            bagStates.add(bags.get(i).getSavedState());
        }
        return bagStates;
    }


    public void restoreBags(List<BagState> bagStates, float previousFieldWidth, float previousFieldHeight) {
        float currentFieldWidth = getWidth();
        float currentFieldHeight = getHeight();
        float newPostionX ;
        float newPostionY ;

        for(int i = 0 ; i < bagStates.size(); i++){
            newPostionX = (bagStates.get(i).positionX/previousFieldWidth) * currentFieldWidth;
            newPostionY = (bagStates.get(i).positionY/previousFieldHeight) * currentFieldHeight;
            BagView restoredBag = new BagView(getContext(), bagStates.get(i).team, bagStates.get(i).rotation,
                    newPostionX, newPostionY);
            addView(restoredBag);
            bags.add(restoredBag);
        }


    }


    public void disallowNewBags() {
        newBagsAllowed = false;
    }

    public void clearBags() {
        for (BagView bag : bags) {
            removeView(bag);
        }
        bags.clear();
        newBagsAllowed = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!newBagsAllowed) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                BagView newBag = new BagView(getContext(), bagListener.getCurrentTeam());
                newBag.centerAround(event.getX(), event.getY());
                newBag.setOnTouchListener(bagTouchListener());
                addView(newBag);
                bags.add(newBag);
                break;
        }
        // The latest bag added, still in drag mode
        return bags.get(bags.size() - 1).dispatchTouchEvent(event);
    }

    private OnTouchListener bagTouchListener() {
        return new OnTouchListener() {
            private BagStatus origin;
            private float dX, dY;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (origin == null) {
                            origin = BagStatus.IN_HAND;
                        }
                        view.setZ(FLOATING_Z);
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;
                    case MotionEvent.ACTION_UP:
                        BagView bagView = (BagView) view;
                        BagStatus destination = getStatus(bagView.getCenter());
                        view.setZ(destination == BagStatus.ON_BOARD ? FLOATING_Z : GROUND_Z);
                        bagListener.onBagMoved(new BagMovedEvent(origin, destination, bagView.getTeam()));
                        origin = destination;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        };
    }

}
