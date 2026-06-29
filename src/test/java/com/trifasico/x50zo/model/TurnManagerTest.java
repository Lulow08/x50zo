package com.trifasico.x50zo;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.exceptions.InvalidPlayException;
import com.trifasico.x50zo.exceptions.InvalidPlayerCountException;
import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.TurnManager;
import com.trifasico.x50zo.model.players.IPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TurnManager}, verifying game setup, turn flow,
 * player elimination, and win condition detection.
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */
@DisplayName("TurnManager — setup, turns, elimination and win condition")
class TurnManagerTest {

    private TurnManager manager;

    @BeforeEach
    void setUp() throws EmptyDeckException {
        manager = new TurnManager("Human", 1);
        manager.startGame();
    }

    // ------------------------------------------------------------------ //
    //  Constructor validation                                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("0 machine players throws InvalidPlayerCountException")
    void constructor_zeroMachines_throwsInvalidPlayerCountException() {
        assertThrows(InvalidPlayerCountException.class,
                () -> new TurnManager("Human", 0));
    }

    @Test
    @DisplayName("4 machine players throws InvalidPlayerCountException")
    void constructor_fourMachines_throwsInvalidPlayerCountException() {
        assertThrows(InvalidPlayerCountException.class,
                () -> new TurnManager("Human", 4));
    }

    @Test
    @DisplayName("1 to 3 machine players are valid")
    void constructor_validMachineCounts_doesNotThrow() {
        assertDoesNotThrow(() -> new TurnManager("Human", 1));
        assertDoesNotThrow(() -> new TurnManager("Human", 2));
        assertDoesNotThrow(() -> new TurnManager("Human", 3));
    }

    // ------------------------------------------------------------------ //
    //  Setup — startGame                                                   //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("After startGame each player has exactly 4 cards")
    void startGame_eachPlayerHasFourCards() {
        for (IPlayer player : manager.getActivePlayers()) {
            assertEquals(4, player.handSize());
        }
    }

    @Test
    @DisplayName("After startGame deck has 52 - 4*players - 1 cards")
    void startGame_deckSizeIsCorrect() {
        // 2 players * 4 cards + 1 initial table card = 9 drawn
        assertEquals(43, manager.remainingDeckCards());
    }

    @Test
    @DisplayName("After startGame top table card is not null")
    void startGame_topTableCardIsNotNull() {
        assertNotNull(manager.getTopCard());
    }

    @Test
    @DisplayName("After startGame human is the current player")
    void startGame_humanIsFirstPlayer() {
        assertEquals("Human", manager.currentPlayer().getName());
    }

    @Test
    @DisplayName("After startGame game is not over")
    void startGame_gameIsNotOver() {
        assertFalse(manager.isGameOver());
        assertNull(manager.getWinner());
    }

    @Test
    @DisplayName("After startGame there are 2 active players")
    void startGame_twoActivePlayers() {
        assertEquals(2, manager.getActivePlayers().size());
    }

    // ------------------------------------------------------------------ //
    //  Turn flow                                                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("humanPlayCard with playable card advances turn to machine")
    void humanPlayCard_playableCard_advancesTurnToMachine()
            throws InvalidPlayException, EmptyDeckException {
        IPlayer human = manager.currentPlayer();
        int sum = manager.getTableSum();

        for (int i = 0; i < human.getHand().size(); i++) {
            Card card = human.getHand().get(i);
            if (card.isPlayable(sum)) {
                manager.humanPlayCard(i);
                assertNotEquals("Human", manager.currentPlayer().getName());
                return;
            }
        }
        assertTrue(true, "No playable card available — edge case skipped");
    }

    @Test
    @DisplayName("humanPlayCard with playable card keeps hand at 4 cards")
    void humanPlayCard_handRemainsAtFourCards()
            throws InvalidPlayException, EmptyDeckException {
        IPlayer human = manager.currentPlayer();
        int sum = manager.getTableSum();

        for (int i = 0; i < human.getHand().size(); i++) {
            Card card = human.getHand().get(i);
            if (card.isPlayable(sum)) {
                manager.humanPlayCard(i);
                assertEquals(4, manager.getHuman().handSize());
                return;
            }
        }
        assertTrue(true, "No playable card available — edge case skipped");
    }

    @Test
    @DisplayName("humanPlayCard updates the table sum correctly")
    void humanPlayCard_updatesTableSum()
            throws InvalidPlayException, EmptyDeckException {
        IPlayer human = manager.currentPlayer();
        int sumBefore = manager.getTableSum();

        for (int i = 0; i < human.getHand().size(); i++) {
            Card card = human.getHand().get(i);
            if (card.isPlayable(sumBefore)) {
                int expectedSum = sumBefore + card.getValue(sumBefore);
                manager.humanPlayCard(i);
                assertEquals(expectedSum, manager.getTableSum());
                return;
            }
        }
        assertTrue(true, "No playable card available — edge case skipped");
    }

    // ------------------------------------------------------------------ //
    //  Elimination                                                         //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("eliminateCurrentPlayer removes player from active list")
    void eliminateCurrentPlayer_removesFromActivePlayers() {
        int before = manager.getActivePlayers().size();
        manager.eliminateCurrentPlayer();
        assertEquals(before - 1, manager.getActivePlayers().size());
    }

    @Test
    @DisplayName("eliminateCurrentPlayer sends cards back to deck")
    void eliminateCurrentPlayer_cardsReturnToDeck() {
        int deckBefore = manager.remainingDeckCards();
        int handSize   = manager.currentPlayer().handSize();
        manager.eliminateCurrentPlayer();
        assertEquals(deckBefore + handSize, manager.remainingDeckCards());
    }

    // ------------------------------------------------------------------ //
    //  Win condition                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Game is over when only one player remains")
    void gameOver_whenOnePlayerRemains() {
        while (manager.getActivePlayers().size() > 1) {
            manager.eliminateCurrentPlayer();
        }
        assertTrue(manager.isGameOver());
        assertNotNull(manager.getWinner());
    }

    @Test
    @DisplayName("Winner is the last remaining active player")
    void winner_isLastRemainingPlayer() {
        IPlayer lastPlayer = manager.getActivePlayers().get(
                manager.getActivePlayers().size() - 1
        );
        while (manager.getActivePlayers().size() > 1) {
            lastPlayer = manager.getActivePlayers().get(
                    manager.getActivePlayers().size() - 1
            );
            manager.eliminateCurrentPlayer();
        }
        assertEquals(lastPlayer, manager.getWinner());
    }

    @Test
    @DisplayName("Game is not over when multiple players remain")
    void gameNotOver_withMultiplePlayers() {
        assertFalse(manager.isGameOver());
    }
}