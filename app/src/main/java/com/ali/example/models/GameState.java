package com.ali.example.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable pojo representing the current state of the game
 *
 * Created by pdv on 2/28/17.
 */

public class GameState implements Parcelable , Serializable {


    private Rule currentRule;
    private Scores bagsRemaining;
    private Scores totalScores;
    private List<Scores> roundScores;

    //Auto-Generated Code
    private GameState(Parcel in) {
        currentRule = in.readParcelable(Rule.class.getClassLoader());
        bagsRemaining = in.readParcelable(Scores.class.getClassLoader());
        totalScores = in.readParcelable(Scores.class.getClassLoader());
        if(roundScores == null){
            roundScores = new ArrayList<>();
        }
        in.readList(roundScores, Scores.class.getClassLoader());
    }

    public static final Creator<GameState> CREATOR = new Creator<GameState>() {
        @Override
        public GameState createFromParcel(Parcel in) {
            return new GameState(in);
        }

        @Override
        public GameState[] newArray(int size) {
            return new GameState[size];
        }
    };
    //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.v("", "writeToParcel..." + flags);
        dest.writeParcelable(currentRule,flags);
        dest.writeParcelable(bagsRemaining,flags);
        dest.writeParcelable(totalScores,flags);
        dest.writeList(roundScores);
    }


    private GameState(Scores totalScores,
                      List<Scores> roundScores,
                      Scores bagsRemaining, Rule currentRule) {
        this.totalScores = totalScores;
        this.roundScores = roundScores;
        this.bagsRemaining = bagsRemaining;
        this.currentRule = currentRule;
    }

    public static GameState freshState(int bagsPerRound, Rule rule) {
        Scores totalScores = new Scores();
        Scores firstRoundScores = new Scores();
        Scores bagsRemaining = new Scores();
        for (Team team : Team.values()) {
            totalScores.put(team, 0);
            firstRoundScores.put(team, 0);
            bagsRemaining.put(team, bagsPerRound);
        }
        return new GameState(totalScores, Arrays.asList(firstRoundScores), bagsRemaining, rule);
    }
    public static GameState freshState(int bagsPerRound) {
        return freshState(bagsPerRound, Rule.OVER_21);
    }

    public Rule currentRule(){
        return currentRule;
    }

    // 0-indexed!
    public int round() {
        return roundScores.size() - 1;
    }

    public int totalScore(Team team) {
        return totalScores.get(team);
    }

    public int currentRoundScore(Team team) {
        return roundScore(round(), team);
    }

    private int roundScore(int round, Team team) {
        if (round > round()) {
            throw new IllegalArgumentException("Round " + round + " hasn't happened yet");
        }
        // Fix for Bug where Scores is changed into a Hashmap after being created and read from a parcel
        // Only happens if app closes unexpectedly , Rotatation works fine.
        // Caused by: java.lang.ClassCastException: java.util.HashMap cannot be cast to com.ali.example.models.Scores
        //https://medium.com/the-wtf-files/the-mysterious-case-of-the-bundle-and-the-map-7b15279a794e
        int result ;
        try {
            result = roundScores.get(round).get(team);
        }
        catch (Exception e) {
            for(int i = 0 ; i < roundScores.size(); i++){

                Scores validScores = new Scores(new HashMap<>(roundScores.get(i)));
                roundScores.set(i,validScores);
            }
            result = roundScores.get(round).get(team);// shouldn't crash now
        }

        return result;
    }

    public int bagsRemaining(Team team) {
        return bagsRemaining.get(team);
    }

    public boolean roundOver() {
        for (Team team : Team.values()) {
            if (bagsRemaining(team) != 0) {
                return false;
            }
        }
        return true;
    }

    // Manipulation - static "pure" methods

    public static GameState decrementRemaining(final GameState previous, final Team team) {
        GameState newState = copyOf(previous);
        int remainingBefore = newState.bagsRemaining(team);
        if (remainingBefore == 0) {
            throw new IllegalStateException(String.format("Team %s has thrown too many bags", team.toString()));
        }
        newState.bagsRemaining.put(team, remainingBefore - 1);
        return newState;
    }
    public static GameState modifyRule(final GameState previous, final Rule rule) {
        if(rule == previous.currentRule){
            return previous;
            //rule is already set so no change is required
        }
        GameState newState = copyOf(previous);
        newState.currentRule = rule;
        return newState;
    }
    public static GameState modifyCurrentRoundScore(final GameState previous, final Team team, int delta) {
        GameState newState = copyOf(previous);
        int oldScore = newState.currentRoundScore(team);
        newState.roundScores.get(newState.round()).put(team, oldScore + delta);
        return newState;
    }

    public static GameState modifyTotalScore(final GameState previous, final Team team, int delta) {
        GameState newState = copyOf(previous);
        int oldScore = newState.totalScore(team);
        newState.totalScores.put(team, oldScore + delta);
        return newState;
    }

    public static GameState endRound(final GameState previous, int bagsPerRound) {
        GameState newState = copyOf(previous);
        Scores newRound = new Scores();
        for (Team team : Team.values()) {
            newRound.put(team, 0);
            newState.bagsRemaining.put(team, bagsPerRound);
        }
        newState.roundScores.add(newRound);
        return newState;
    }

    private static Rule copyOf(Rule original) {
        return Rule.valueOf(original.name());
    }

    private static Scores copyOf(Scores original) {
        Scores ret = new Scores();
        for (Map.Entry<Team, Integer> entry : original.entrySet()) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    private static GameState copyOf(GameState originalState) {
        Scores totalScoresCopy = copyOf(originalState.totalScores);
        List<Scores> roundScoresCopy = new ArrayList<>();
        for (Scores roundScore : originalState.roundScores) {
            roundScoresCopy.add(copyOf(roundScore));
        }
        Scores bagsRemainingCopy = copyOf(originalState.bagsRemaining);
        Rule currentRuleCopy = copyOf(originalState.currentRule);
        return new GameState(totalScoresCopy, roundScoresCopy, bagsRemainingCopy, currentRuleCopy);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GameState)) {
            return false;
        }
        GameState otherState = (GameState) other;
        if (this.round() != otherState.round()) {
            return false;
        }
        for (Team team : Team.values()) {
            if (this.totalScore(team) != otherState.totalScore(team)) {
                return false;
            }
            for (int round = 0; round < round(); round++) {
                if (!this.roundScores.get(round).get(team).equals(otherState.roundScores.get(round).get(team))) {
                    return false;
                }
            }
            if (!this.bagsRemaining.get(team).equals(otherState.bagsRemaining.get(team))) {
                return false;
            }
        }
        return true;
    }

}
