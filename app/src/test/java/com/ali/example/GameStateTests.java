package com.ali.example;

import com.ali.example.models.GameState;
import com.ali.example.models.Team;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by pdv on 2/28/17.
 */

public class GameStateTests {

    @Test
    public void testFreshState() {
        int bagsPerRound = 4;
        GameState state = GameState.freshState(bagsPerRound);

        assertEquals(bagsPerRound, state.bagsRemaining(Team.BLUE));
        assertEquals(bagsPerRound, state.bagsRemaining(Team.RED));
        assertEquals(0, state.currentRoundScore(Team.BLUE));
        assertEquals(0, state.currentRoundScore(Team.RED));
        assertEquals(0, state.totalScore(Team.BLUE));
        assertEquals(0, state.totalScore(Team.RED));
    }

    @Test
    public void testImmutable() {
        int bagsPerRound = 4;
        GameState before = GameState.freshState(bagsPerRound);
        GameState after = GameState.modifyCurrentRoundScore(before, Team.RED, 3);

        assertEquals(0, before.currentRoundScore(Team.RED));
        assertEquals(3, after.currentRoundScore(Team.RED));
    }

    @Test
    public void testPositiveBagsAssertion() {
        int bagsPerRound = 0;
        GameState before = GameState.freshState(bagsPerRound);
        try {
            GameState after = GameState.decrementRemaining(before, Team.RED);
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testDecrement() {
        int bagsPerRound = 3;
        GameState before = GameState.freshState(bagsPerRound);
        GameState after1 = GameState.decrementRemaining(before, Team.RED);
        GameState after2 = GameState.decrementRemaining(after1, Team.RED);
        GameState after3 = GameState.decrementRemaining(after2, Team.RED);

        assertEquals(3, before.bagsRemaining(Team.RED));
        assertEquals(2, after1.bagsRemaining(Team.RED));
        assertEquals(1, after2.bagsRemaining(Team.RED));
        assertEquals(0, after3.bagsRemaining(Team.RED));
    }

    @Test
    public void testRoundScore() {
        int bagsPerRound = 3;
        GameState before = GameState.freshState(bagsPerRound);
        GameState after1 = GameState.modifyCurrentRoundScore(before, Team.RED, 2);
        GameState after2 = GameState.modifyCurrentRoundScore(after1, Team.RED, 3);
        GameState after3 = GameState.modifyCurrentRoundScore(after2, Team.RED, -1);

        assertEquals(0, before.currentRoundScore(Team.RED));
        assertEquals(2, after1.currentRoundScore(Team.RED));
        assertEquals(5, after2.currentRoundScore(Team.RED));
        assertEquals(4, after3.currentRoundScore(Team.RED));
    }

    @Test
    public void testTotalScore() {
        int bagsPerRound = 3;
        GameState before = GameState.freshState(bagsPerRound);
        GameState after1 = GameState.modifyTotalScore(before, Team.RED, 2);
        GameState after2 = GameState.modifyTotalScore(after1, Team.RED, 3);
        GameState after3 = GameState.modifyTotalScore(after2, Team.RED, -1);

        assertEquals(0, before.totalScore(Team.RED));
        assertEquals(2, after1.totalScore(Team.RED));
        assertEquals(5, after2.totalScore(Team.RED));
        assertEquals(4, after3.totalScore(Team.RED));
    }

}
