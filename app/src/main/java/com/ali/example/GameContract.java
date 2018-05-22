package com.ali.example;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ali.example.models.BagMovedEvent;
import com.ali.example.models.GameState;
import com.ali.example.models.Rule;
import com.ali.example.models.Team;

/**
 * A contract that defines the interfaces for the view and user action listener.
 *
 * Created by pdv on 3/2/17.
 */

interface GameContract {

    interface View {

        /**
         * Update the scoreboard UI to reflect the current state of the game
         * @param state The current state
         */
        void updateScoreboard(@NonNull GameState state);

        /**
         * Show a dialog prompting the user to roll over the round. Can be dismissed.
         * Also locks the field so that no new bags can be thrown.
         * @param roundWinner Who won, null if tie
         * @param roundNumber Which round it is
         * @param victoryMargin How many points are going to be added to their score
         */
        void showRoundOverDialog(@Nullable Team roundWinner, int roundNumber, int victoryMargin);

        /**
         * Clears the thrown bags off the field
         */
        void clearBags();

        /**
         * Show a dialog with the winner
         * @param winner The winning team
         */
        void showGameOverDialog(@NonNull Team winner, int redScore, int blueScore);

    }

    interface UserActionListener {

        /**
         * Starts the engine
         * @param initialState The state to start in
         */
        void init(@NonNull GameState initialState);

        /**
         * Starts the engine with a new game
         * @param initialRule The rule ot be applied to the new game being started
         *
         */
        void initNewGame(@NonNull Rule initialRule);


        /**
         * Called when the user moves a bag, or throws a new one
         * @param event The event
         */
        void onBagMoved(@NonNull BagMovedEvent event);

        /**
         * Called when the user accepts the round over dialog
         */
        void onNewRoundClicked();

        /**
         * Called when the user changes the rules for winning game
         */
        void onRuleChanged(Rule rule);

        /**
         * Called when the application needs to access the game state for saving the state
         * @return GameState
         */
        GameState getSaveState();

        /**
         * Called when the application needs to check if the Round is Over
         * @return boolean True if the round is Over , False if it isn't
         */
        boolean endRoundIfOver();
        /**
         * Called when the application needs to check if the Game is Over
         * @return boolean True if the game is Over , False if it isn't
         */
        boolean endGameIfOver();

    }

}
