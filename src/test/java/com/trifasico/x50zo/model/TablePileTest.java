package com.trifasico.x50zo;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.Rank;
import com.trifasico.x50zo.model.TablePile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TablePile}, verifying push, peek, sum tracking,
 * and the reshuffle collection behaviour.
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 */
@DisplayName("TablePile — push, peek, sum and reshuffle")
class TablePileTest {

    private TablePile pile;

    @BeforeEach
    void setUp() {
        pile = new TablePile();
    }

    // ------------------------------------------------------------------ //
    //  Initial state                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("A new TablePile is empty and has sum 0")
    void newPile_isEmptyAndSumIsZero() {
        assertTrue(pile.isEmpty());
        assertEquals(0, pile.getCurrentSum());
        assertEquals(0, pile.size());
    }

    @Test
    @DisplayName("peek on empty pile throws IllegalStateException")
    void peek_emptyPile_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> pile.peek());
    }

    // ------------------------------------------------------------------ //
    //  push                                                                //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("push adds card to top and updates size")
    void push_updatesSizeByOne() {
        pile.push(new Card(Rank.FIVE, "hearts"));
        assertEquals(1, pile.size());
        assertFalse(pile.isEmpty());
    }

    @Test
    @DisplayName("push updates sum correctly for numeric card")
    void push_updatesSumForNumericCard() {
        pile.push(new Card(Rank.SEVEN, "spades"));
        assertEquals(7, pile.getCurrentSum());
    }

    @Test
    @DisplayName("push with J/Q/K subtracts 10 from sum")
    void push_subtractsTenForFaceCard() {
        pile.push(new Card(Rank.FIVE, "hearts"));   // sum = 5
        pile.push(new Card(Rank.KING, "spades"));   // sum = 5 - 10 = -5
        assertEquals(-5, pile.getCurrentSum());
    }

    @Test
    @DisplayName("push with NINE does not change sum")
    void push_nineDoesNotChangeSum() {
        pile.push(new Card(Rank.SIX, "clubs"));     // sum = 6
        pile.push(new Card(Rank.NINE, "diamonds")); // sum = 6
        assertEquals(6, pile.getCurrentSum());
    }

    @Test
    @DisplayName("push with ACE adds 10 when sum + 10 <= 50")
    void push_aceAddsTenWhenSafe() {
        pile.push(new Card(Rank.TEN, "hearts"));    // sum = 10
        pile.push(new Card(Rank.ACE, "spades"));    // 10+10=20 <= 50 → adds 10
        assertEquals(20, pile.getCurrentSum());
    }

    @Test
    @DisplayName("push with ACE adds 1 when sum + 10 would exceed 50")
    void push_aceAddsOneWhenTenWouldExceed() {
        pile.push(new Card(Rank.TEN, "hearts"));    // sum = 10
        pile.push(new Card(Rank.TEN, "spades"));    // sum = 20
        pile.push(new Card(Rank.TEN, "clubs"));     // sum = 30
        pile.push(new Card(Rank.TEN, "diamonds"));  // sum = 40
        pile.push(new Card(Rank.TEN, "hearts"));    // sum = 50
        pile.push(new Card(Rank.ACE, "spades"));    // 50+10=60 > 50 → adds 1
        assertEquals(51, pile.getCurrentSum());
    }

    @Test
    @DisplayName("push null card throws NullPointerException")
    void push_nullCard_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> pile.push(null));
    }

    // ------------------------------------------------------------------ //
    //  peek                                                                //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("peek returns the most recently pushed card")
    void peek_returnsTopCard() {
        pile.push(new Card(Rank.THREE, "clubs"));
        pile.push(new Card(Rank.KING, "hearts"));
        Card top = pile.peek();
        assertEquals(Rank.KING, top.rank());
        assertEquals("hearts", top.suit());
    }

    @Test
    @DisplayName("peek does not remove the card from the pile")
    void peek_doesNotRemoveCard() {
        pile.push(new Card(Rank.ACE, "diamonds"));
        pile.peek();
        assertEquals(1, pile.size());
    }

    // ------------------------------------------------------------------ //
    //  collectForReshuffle                                                 //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("collectForReshuffle returns all cards except the top one")
    void collectForReshuffle_returnsAllButTop() throws EmptyDeckException {
        pile.push(new Card(Rank.TWO,   "hearts"));
        pile.push(new Card(Rank.THREE, "spades"));
        pile.push(new Card(Rank.FOUR,  "clubs"));   // top card

        List<Card> collected = pile.collectForReshuffle();

        assertEquals(2, collected.size());
        assertEquals(1, pile.size());
        assertEquals(Rank.FOUR, pile.peek().rank());
    }

    @Test
    @DisplayName("collectForReshuffle preserves current sum")
    void collectForReshuffle_preservesSum() throws EmptyDeckException {
        pile.push(new Card(Rank.TEN,  "hearts"));   // sum = 10
        pile.push(new Card(Rank.FIVE, "spades"));   // sum = 15
        int sumBefore = pile.getCurrentSum();

        pile.collectForReshuffle();

        assertEquals(sumBefore, pile.getCurrentSum());
    }

    @Test
    @DisplayName("collectForReshuffle on pile with one card throws EmptyDeckException")
    void collectForReshuffle_onePileCard_throwsEmptyDeckException() {
        pile.push(new Card(Rank.ACE, "hearts"));
        assertThrows(EmptyDeckException.class, () -> pile.collectForReshuffle());
    }

    @Test
    @DisplayName("collectForReshuffle on empty pile throws EmptyDeckException")
    void collectForReshuffle_emptyPile_throwsEmptyDeckException() {
        assertThrows(EmptyDeckException.class, () -> pile.collectForReshuffle());
    }
}
