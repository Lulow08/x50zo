package com.trifasico.x50zo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Card} and its delegation to {@link Rank}.
 *
 * Covers:
 *  - Rank.resolveValue / Rank.isPlayable for fixed-value ranks
 *  - ACE edge cases: prefers 10 when it fits, falls back to 1 otherwise
 *  - Card.isPlayable correctly delegating to Rank
 *  - Card.getValue correctly delegating to Rank
 */
class CardTest {

    // ------------------------------------------------------------------ //
    //  Rank – fixed values                                                //
    // ------------------------------------------------------------------ //

    @Test
    void rank_two_hasValue2() {
        assertEquals(2, Rank.TWO.resolveValue(0));
    }

    @Test
    void rank_nine_hasValue0() {
        // NINE is worth 0: never changes the running sum
        assertEquals(0, Rank.NINE.resolveValue(30));
    }

    @Test
    void rank_ten_hasValue10() {
        assertEquals(10, Rank.TEN.resolveValue(0));
    }

    @Test
    void rank_jack_hasValueMinus10() {
        assertEquals(-10, Rank.JACK.resolveValue(20));
    }

    @Test
    void rank_queen_hasValueMinus10() {
        assertEquals(-10, Rank.QUEEN.resolveValue(50));
    }

    @Test
    void rank_king_hasValueMinus10() {
        assertEquals(-10, Rank.KING.resolveValue(10));
    }

    // ------------------------------------------------------------------ //
    //  Rank – isPlayable for fixed-value ranks                           //
    // ------------------------------------------------------------------ //

    @Test
    void rank_isPlayable_trueWhenResultExactly50() {
        // sum=48, TWO(2) → exactly 50, legal
        assertTrue(Rank.TWO.isPlayable(48));
    }

    @Test
    void rank_isPlayable_falseWhenResultAbove50() {
        // sum=49, TWO(2) → 51, illegal
        assertFalse(Rank.TWO.isPlayable(49));
    }

    @Test
    void rank_nine_alwaysPlayable() {
        // NINE is worth 0 → never exceeds 50
        assertTrue(Rank.NINE.isPlayable(50));
    }

    @Test
    void rank_negativeValue_alwaysPlayable() {
        // JACK/QUEEN/KING subtract from the sum, never exceed 50
        assertTrue(Rank.JACK.isPlayable(50));
        assertTrue(Rank.QUEEN.isPlayable(50));
        assertTrue(Rank.KING.isPlayable(50));
    }

    // ------------------------------------------------------------------ //
    //  ACE – resolveValue: prefers 10, falls back to 1                   //
    // ------------------------------------------------------------------ //

    @Test
    void ace_resolves10_whenSumIs40() {
        // 40 + 10 = 50 → fits exactly, chooses 10
        assertEquals(10, Rank.ACE.resolveValue(40));
    }

    @Test
    void ace_resolves10_whenSumIs0() {
        // 0 + 10 = 10 ≤ 50, chooses 10
        assertEquals(10, Rank.ACE.resolveValue(0));
    }

    @Test
    void ace_resolves1_whenSumIs41() {
        // 41 + 10 = 51 > 50 → falls back to 1
        assertEquals(1, Rank.ACE.resolveValue(41));
    }

    @Test
    void ace_resolves1_whenSumIs50() {
        // 50 + 10 = 60 > 50 → falls back to 1
        assertEquals(1, Rank.ACE.resolveValue(50));
    }

    @Test
    void ace_isPlayable_trueAtSum49() {
        // 49 + 1 = 50 ≤ 50 → legal
        assertTrue(Rank.ACE.isPlayable(49));
    }

    @Test
    void ace_isPlayable_falseAtSum50WithOnly10Option() {
        // sum=50: chooses 1 → 50+1=51 > 50 → illegal
        assertFalse(Rank.ACE.isPlayable(50));
    }

    // ------------------------------------------------------------------ //
    //  Card – delegation to Rank                                          //
    // ------------------------------------------------------------------ //

    @Test
    void card_getValue_delegatesToRank() {
        Card card = new Card(Rank.SEVEN, Card.SUIT_CLUBS);
        assertEquals(7, card.getValue(10));
    }

    @Test
    void card_isPlayable_trueWhenLegal() {
        Card card = new Card(Rank.FIVE, Card.SUIT_HEARTS);
        // sum=45 + 5 = 50 → legal
        assertTrue(card.isPlayable(45));
    }

    @Test
    void card_isPlayable_falseWhenIllegal() {
        Card card = new Card(Rank.FIVE, Card.SUIT_DIAMONDS);
        // sum=46 + 5 = 51 → illegal
        assertFalse(card.isPlayable(46));
    }

    @Test
    void card_aceIsPlayable_uses1WhenNeeded() {
        Card ace = new Card(Rank.ACE, Card.SUIT_SPADES);
        // sum=49: chooses 1 → 50, legal
        assertTrue(ace.isPlayable(49));
    }

    @Test
    void card_aceIsPlayable_falseAtSum50() {
        Card ace = new Card(Rank.ACE, Card.SUIT_HEARTS);
        // sum=50: 50+1=51 > 50, illegal
        assertFalse(ace.isPlayable(50));
    }

    // ------------------------------------------------------------------ //
    //  Card – equals, toString, nulls                                     //
    // ------------------------------------------------------------------ //

    @Test
    void card_equalsSameRankAndSuit() {
        Card a = new Card(Rank.KING, Card.SUIT_SPADES);
        Card b = new Card(Rank.KING, Card.SUIT_SPADES);
        assertEquals(a, b);
    }

    @Test
    void card_notEqualsDifferentSuit() {
        Card a = new Card(Rank.KING, Card.SUIT_SPADES);
        Card b = new Card(Rank.KING, Card.SUIT_HEARTS);
        assertNotEquals(a, b);
    }

    @Test
    void card_toStringFormat() {
        Card card = new Card(Rank.ACE, Card.SUIT_HEARTS);
        assertEquals("A-hearts", card.toString());
    }

    @Test
    void card_nullRankThrows() {
        assertThrows(NullPointerException.class,
                () -> new Card(null, Card.SUIT_CLUBS));
    }

    @Test
    void card_nullSuitThrows() {
        assertThrows(NullPointerException.class,
                () -> new Card(Rank.TWO, null));
    }
}
