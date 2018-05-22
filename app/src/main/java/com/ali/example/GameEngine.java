package com.ali.example;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.ali.example.models.BagStatus;
import com.ali.example.models.BagMovedEvent;
import com.ali.example.models.GameState;
import com.ali.example.models.Rule;
import com.ali.example.models.Team;

import java.util.EnumMap;
import java.util.List;

/**
 * The "presenter", that holds state and makes decisions
 *
 * Created by pdv on 2/28/17.
 */

class GameEngine implements GameContract.UserActionListener {

    static final int BAGS_PER_ROUND = 4;
    private static final int SCORE_TO_WIN = 21;
    private static final int WIN_BY = 2;
    private static final int DECREASED_TO = 11;
    private static final EnumMap<BagStatus, Integer> POINT_VALUES;
    static {
        POINT_VALUES = new EnumMap<>(BagStatus.class);
        POINT_VALUES.put(BagStatus.OFF_BOARD, 0);
        POINT_VALUES.put(BagStatus.ON_BOARD, 1);
        POINT_VALUES.put(BagStatus.IN_HOLE, 3);
    }

    @NonNull private final GameContract.View view;
    private GameState state;

    GameEngine(@NonNull GameContract.View view) {
        this.view = view;
    }

    @Override
    public void init(@NonNull GameState initialState) {
        this.state = initialState;
        view.updateScoreboard(state);
    }

    @Override
    public void initNewGame(@NonNull Rule initialRule) {
        this.state = GameState.freshState(BAGS_PER_ROUND,initialRule);
        view.updateScoreboard(state);
    }

    @Override
    public void onBagMoved(@NonNull BagMovedEvent event) {
        state = processEvent(state, event);
        if (state.roundOver()) {
            onRoundOver();
        }

        view.updateScoreboard(state);
    }

    private static Pair<Team, Integer> roundWinnerAndDelta(GameState state) {
        int redScore = state.currentRoundScore(Team.RED);
        int blueScore = state.currentRoundScore(Team.BLUE);
        Team roundWinner = null;
        if (redScore != blueScore) {
            roundWinner = redScore > blueScore ? Team.RED : Team.BLUE;
        }

        int resultScore = Math.abs(redScore - blueScore);

        if(state.currentRule() == Rule.EQUALS_21) {
            int redTotal = state.totalScore(Team.RED);
            int blueTotal = state.totalScore(Team.BLUE);
            int winnerTotalScore = redTotal;
            if (roundWinner == Team.BLUE) {
                winnerTotalScore = blueTotal;
            }

            int resultTotalScore = resultScore + winnerTotalScore;

            if (resultTotalScore > SCORE_TO_WIN) {
                resultScore = DECREASED_TO - winnerTotalScore;
            }
        }

        return new Pair<>(roundWinner, resultScore);
    }

    private void onRoundOver() {
        Pair<Team, Integer> winnerAndDelta = roundWinnerAndDelta(state);
        view.showRoundOverDialog(winnerAndDelta.first, state.round(), winnerAndDelta.second);
    }

    @Nullable
    private static Team getWinnerOrNull(GameState state) {
        int redTotal = state.totalScore(Team.RED);
        int blueTotal = state.totalScore(Team.BLUE);
        Rule currentRule = state.currentRule();

        if(currentRule == Rule.OVER_21) {
            if (redTotal >= SCORE_TO_WIN && redTotal - blueTotal >= WIN_BY) {
                return Team.RED;
            } else if (blueTotal >= SCORE_TO_WIN && blueTotal - redTotal >= WIN_BY) {
                return Team.BLUE;
            }
        } else if ( currentRule == Rule.EQUALS_21 ){
            if (redTotal == SCORE_TO_WIN) {
                return Team.RED;
            } else if (blueTotal == SCORE_TO_WIN ) {
                return Team.BLUE;
            }

        }
        return null;
    }

    @Override
    public void onNewRoundClicked() {
        Pair<Team, Integer> winnerAndDelta = roundWinnerAndDelta(state);

        state = GameState.modifyTotalScore(state, winnerAndDelta.first, winnerAndDelta.second);


        Team winner = getWinnerOrNull(state);
        if (winner != null) {
            view.showGameOverDialog(winner, state.totalScore(Team.RED), state.totalScore(Team.BLUE));
        } else {
            state = GameState.endRound(state, BAGS_PER_ROUND);
            view.clearBags();
            view.updateScoreboard(state);
        }
    }

    @Override
    public void onRuleChanged(Rule rule) {
        state = GameState.modifyRule(state, rule);
    }

    @Override
    public GameState getSaveState() {
        return state;
    }

    @Override
    public boolean endRoundIfOver() {
        if (state.roundOver()) {
            onRoundOver();
            return true;
        }
        return false;
    }

    @Override
    public boolean endGameIfOver() {
        Team winner = getWinnerOrNull(state);
        if (winner != null) {
            view.showGameOverDialog(winner, state.totalScore(Team.RED), state.totalScore(Team.BLUE));
            return true;
        }
        return false;
    }

    private static GameState processOrigin(GameState before, Team team, BagStatus origin) {
        switch (origin) {
            case IN_HAND:
                return GameState.decrementRemaining(before, team);
            default:
                return GameState.modifyCurrentRoundScore(before, team, -1 * POINT_VALUES.get(origin));
        }
    }

    private static GameState processResult(GameState before, Team team, BagStatus result) {
        switch (result) {
            case IN_HAND:
                throw new IllegalArgumentException("You can't pick the bags up until the round is over, silly");
            default:
                return GameState.modifyCurrentRoundScore(before, team, POINT_VALUES.get(result));
        }
    }

    private static GameState processEvent(@NonNull GameState before, @NonNull BagMovedEvent event) {
        GameState intermediate = processOrigin(before, event.team, event.origin);
        return processResult(intermediate, event.team, event.result);
    }

    static GameState processEvents(@NonNull GameState before, @NonNull List<BagMovedEvent> events) {
        GameState state = before;
        for (BagMovedEvent event : events) {
            state = processEvent(state, event);
        }
        return state;
    }

}
