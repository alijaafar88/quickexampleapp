package com.ali.example;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.example.models.BagMovedEvent;
import com.ali.example.models.BagState;
import com.ali.example.models.GameState;
import com.ali.example.models.Rule;
import com.ali.example.models.Team;
import com.ali.example.views.FieldView;
import com.ali.example.views.GameScoreView;
import com.ali.example.views.RoundScoreView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Main Activity for Scoring, serves as the View
 */

public class ScoringActivity extends AppCompatActivity implements FieldView.BagListener, GameContract.View {

    private GameScoreView gameScoreView;
    private RoundScoreView redRoundScoreView;
    private RoundScoreView blueRoundScoreView;
    private TextView roundIndicator;
    private FieldView fieldView;
    GameContract.UserActionListener userActionListener;
    Team currentTeam;
    Rule currentRule;
    private MenuItem changeModeMenuItem;
    private GameState initGameState = null;
    private ArrayList<BagState> initBagStates = null;
    private SharedPreferences sharedPref;
    private float currentFieldWidth ;
    private float currentFieldHeight ;
    private float previousFieldWidth ;
    private float previousFieldHeight ;
    private String saveFileName = "SaveFile.txt";
    File saveFile;

    private Runnable restoreFieldRunnable = new Runnable() {
        @Override
        public void run() {
            currentFieldHeight = fieldView.getHeight();
            currentFieldWidth = fieldView.getWidth();
            if(initBagStates != null && initBagStates.size() > 0){
                fieldView.restoreBags(initBagStates, previousFieldWidth, previousFieldHeight);
                initBagStates = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        saveFile = new File(getFilesDir(),saveFileName);
        String savedRuleString = sharedPref.getString(getString(R.string.saved_rule_selection), Rule.OVER_21.name());
        if(Rule.EQUALS_21.name().equals(savedRuleString)){
            currentRule = Rule.EQUALS_21;
        } else {
            currentRule = Rule.OVER_21;
        }

        if(!readSavedSate() && savedInstanceState != null){
            initGameState = savedInstanceState.getParcelable(getString(R.string.saved_game_state_key));
            initBagStates = savedInstanceState.getParcelableArrayList(getString(R.string.saved_bags_state_key));
            previousFieldWidth = savedInstanceState.getFloat(getString(R.string.saved_previous_field_width));
            previousFieldHeight = savedInstanceState.getFloat(getString(R.string.saved_previous_field_height));
            currentTeam =  savedInstanceState.getParcelable(getString(R.string.saved_current_team));
        }
        if(initGameState != null) {
            currentRule = initGameState.currentRule();
        }
        setContentView(R.layout.activity_scoring);
        setupViews(GameEngine.BAGS_PER_ROUND);
        startNewGame();
    }

    private void setupViews(int bagsPerRound) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_scoring);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        gameScoreView = (GameScoreView) findViewById(R.id.game_score_view);
        redRoundScoreView = (RoundScoreView) findViewById(R.id.round_score_red);
        redRoundScoreView.setBagsPerRound(bagsPerRound);
        blueRoundScoreView = (RoundScoreView) findViewById(R.id.round_score_blue);
        blueRoundScoreView.setBagsPerRound(bagsPerRound);
        roundIndicator = (TextView) findViewById(R.id.tv_round);
        fieldView = (FieldView) findViewById(R.id.field_view);
        fieldView.post(restoreFieldRunnable);
        fieldView.setBagListener(this);
    }



    private boolean writeSavedSate() {
        boolean didWriteFile = false;
        try {

            if(!saveFile.exists()||(saveFile.exists() && saveFile.delete())) {
                saveFile.createNewFile();
            }
            FileOutputStream f = new FileOutputStream(saveFile);
            ObjectOutputStream o = new ObjectOutputStream(f);

            // Write objects to file
            o.writeObject(userActionListener.getSaveState());
            o.writeObject(fieldView.getBagsSaveState());
            o.writeObject(currentTeam);
            o.writeFloat(currentFieldWidth);
            o.writeFloat(currentFieldHeight);

            o.close();
            f.close();
            didWriteFile = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return didWriteFile;
    }

    private boolean readSavedSate(){
        boolean didReadFile = false;
        try {
            File saveFile = new File(getFilesDir(),saveFileName);
            if(saveFile.exists()) {
                FileInputStream fi = new FileInputStream(saveFile);
                ObjectInputStream oi = new ObjectInputStream(fi);

                // Read objects
                initGameState = (GameState) oi.readObject();
                initBagStates = (ArrayList<BagState>) oi.readObject();
                currentTeam = (Team) oi.readObject();
                previousFieldWidth = oi.readFloat();
                previousFieldHeight = oi.readFloat();

                oi.close();
                fi.close();
                didReadFile = true;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return didReadFile;
    }

    private void startNewGame() {
        userActionListener = new GameEngine(this);
        if(initGameState != null) {
            userActionListener.init(initGameState);
            if(!userActionListener.endGameIfOver()) {
                //this will skip the round popup if the game is over
                //and the state is restored without the user need
                //click through the round popup , I consider this a "feature" not a bug.
                userActionListener.endRoundIfOver();
            }
            initGameState = null;

        } else {
            //delete old file if new game has started
            saveFile.delete();
            fieldView.clearBags();
            currentTeam = Team.RED;
            userActionListener.initNewGame(Rule.OVER_21);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void updateScoreboard(@NonNull GameState gameState) {
        gameScoreView.setBlueTeamScore(gameState.totalScore(Team.BLUE));
        gameScoreView.setRedTeamScore(gameState.totalScore(Team.RED));
        redRoundScoreView.setRoundScore(gameState.currentRoundScore(Team.RED));
        redRoundScoreView.setBagsRemaining(gameState.bagsRemaining(Team.RED));
        blueRoundScoreView.setRoundScore(gameState.currentRoundScore(Team.BLUE));
        blueRoundScoreView.setBagsRemaining(gameState.bagsRemaining(Team.BLUE));
        roundIndicator.setText(getString(R.string.round, gameState.round() + 1));
        writeSavedSate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.saved_game_state_key),userActionListener.getSaveState());
        outState.putParcelableArrayList(getString(R.string.saved_bags_state_key),fieldView.getBagsSaveState());
        outState.putFloat(getString(R.string.saved_previous_field_width),currentFieldWidth);
        outState.putFloat(getString(R.string.saved_previous_field_height),currentFieldHeight);
        outState.putParcelable(getString(R.string.saved_current_team),currentTeam);


    }

    @Override
    public Team getCurrentTeam() {
        if (currentTeam == Team.RED) {
            currentTeam = Team.BLUE;
            return Team.RED;
        } else {
            currentTeam = Team.RED;
            return Team.BLUE;
        }
    }

    @Override
    public void onBagMoved(BagMovedEvent event) {
        userActionListener.onBagMoved(event);
    }

    @Override
    public void clearBags() {
        fieldView.clearBags();
    }

    @Override
    public void showRoundOverDialog(@Nullable Team roundWinner, int roundNumber, int scoreDelta) {
        fieldView.disallowNewBags();
        String title, message;
        if (roundWinner != null) {
            String teamName = getString(roundWinner.getDisplayNameRes());
            title = getString(R.string.dialog_title_round_win, teamName, roundNumber + 1);

            if(scoreDelta > 0) {
                message = getResources().getQuantityString(R.plurals.dialog_msg_round_win, scoreDelta, scoreDelta);
            } else {
                scoreDelta = Math.abs(scoreDelta);
                message = getResources().getQuantityString(R.plurals.dialog_msg_round_over_max_score, scoreDelta, scoreDelta);
            }
        } else {
            title = getString(R.string.dialog_title_round_tie, roundNumber + 1);
            message = getString(R.string.dialog_msg_round_tie);
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.start_next_round, (di, i) -> userActionListener.onNewRoundClicked())
                .show();
    }

    @Override
    public void showGameOverDialog(@NonNull Team winner, int redScore, int blueScore) {
        String teamName = getString(winner.getDisplayNameRes());
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_game_over, teamName))
                .setPositiveButton(R.string.start_new_game, (di, i) -> startNewGame())
                .setMessage(getString(R.string.final_score, redScore, blueScore))
                .setCancelable(false)
                .show();
    }

    private void showRules() {
        startActivity(new Intent(this, RulesActivity.class));
    }

    private void changeRules() {
        if(currentRule == Rule.EQUALS_21) {
            currentRule = Rule.OVER_21;
            userActionListener.onRuleChanged(currentRule);
            changeModeMenuItem.setTitle(getString(Rule.EQUALS_21.ruleChangeTextResource));
        } else {
            currentRule = Rule.EQUALS_21;
            userActionListener.onRuleChanged(currentRule);
            changeModeMenuItem.setTitle(getString(Rule.OVER_21.ruleChangeTextResource));
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_rule_selection), currentRule.name());
        editor.apply();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scoring, menu);
        changeModeMenuItem = menu.findItem(R.id.menu_item_change);
        if(currentRule != null && currentRule == Rule.EQUALS_21) {
            changeModeMenuItem.setTitle(getString(Rule.OVER_21.ruleChangeTextResource));
        } else {
            changeModeMenuItem.setTitle(getString(Rule.EQUALS_21.ruleChangeTextResource));
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_rules:
                showRules();
                return true;
            case R.id.menu_item_restart:
                startNewGame();
                return true;
            case R.id.menu_item_change:
                changeRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
