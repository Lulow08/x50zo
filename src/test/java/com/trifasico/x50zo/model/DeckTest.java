package com.trifasico.x50zo.model;

import com.trifasico.x50zo.exceptions.EmptyDeckException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Deck}.
 *
 * Covers:
 *  - Initialization: 52 cards, not empty
 *  - draw(): decrements the deck and throws EmptyDeckException when empty
 *  - addToBottom(): the card ends up last in draw order
 *  - reshuffleFromPile(): loads the given cards and makes them drawable
 *
 *   * @author Yostin Ramirez
 *  * @author Lesly Zapata
 *  * @author Joseph Terreros
 *  * @version 1.0
 */
class DeckTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    // ------------------------------------------------------------------ //
    //  Initialization                                                      //
    // ------------------------------------------------------------------ //

    @Test
    void deck_initiallyHas52Cards() {
        assertEquals(52, deck.remainingCards());
    }

    @Test
    void deck_initiallyNotEmpty() {
        assertFalse(deck.isEmpty());
    }

    @Test
    void deck_containsAllFourSuits() throws EmptyDeckException {
        // Drain the full deck and verify all 4 suits are present
        List<String> suits = new ArrayList<>();
        while (!deck.isEmpty()) {
            suits.add(deck.draw().suit());
        }
        assertTrue(suits.contains(Card.SUIT_HEARTS));
        assertTrue(suits.contains(Card.SUIT_DIAMONDS));
        assertTrue(suits.contains(Card.SUIT_CLUBS));
        assertTrue(suits.contains(Card.SUIT_SPADES));
    }

    @Test
    void deck_containsAllThirteenRanks() throws EmptyDeckException {
        List<Rank> ranks = new ArrayList<>();
        while (!deck.isEmpty()) {
            ranks.add(deck.draw().rank());
        }
        for (Rank r : Rank.values()) {
            assertTrue(ranks.contains(r), "Missing rank: " + r);
        }
    }

    // ------------------------------------------------------------------ //
    //  draw()                                                              //
    // ------------------------------------------------------------------ //

    @Test
    void draw_returnsCard() throws EmptyDeckException {
        Card card = deck.draw();
        assertNotNull(card);
    }

    @Test
    void draw_reducesRemainingByOne() throws EmptyDeckException {
        deck.draw();
        assertEquals(51, deck.remainingCards());
    }

    @Test
    void draw_emptyDeck_throwsEmptyDeckException() throws EmptyDeckException {
        // Drain the deck
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertThrows(EmptyDeckException.class, () -> deck.draw());
    }

    @Test
    void draw_until_empty_exactlyZeroRemaining() throws EmptyDeckException {
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertEquals(0, deck.remainingCards());
        assertTrue(deck.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  addToBottom()                                                       //
    // ------------------------------------------------------------------ //

    @Test
    void addToBottom_increasesRemainingByOne() {
        int before = deck.remainingCards();
        deck.addToBottom(new Card(Rank.ACE, Card.SUIT_HEARTS));
        assertEquals(before + 1, deck.remainingCards());
    }

    @Test
    void addToBottom_cardEndsUpLast() throws EmptyDeckException {
        // Build a controlled sequence: drain, then add two known cards,
        // and verify they come out in insertion order
        Card bottom = new Card(Rank.ACE, Card.SUIT_SPADES);

        // Drain the deck to control draw order
        while (!deck.isEmpty()) deck.draw();

        Card first = new Card(Rank.TWO, Card.SUIT_CLUBS);
        Card second = new Card(Rank.THREE, Card.SUIT_DIAMONDS);
        deck.addToBottom(first);
        deck.addToBottom(second);

        assertEquals(first,  deck.draw());
        assertEquals(second, deck.draw());

    }

    @Test
    void addToBottom_nullCardThrows() {
        assertThrows(NullPointerException.class, () -> deck.addToBottom(null));
    }

    // ------------------------------------------------------------------ //
    //  reshuffleFromPile()                                                 //
    // ------------------------------------------------------------------ //

    @Test
    void reshuffleFromPile_loadsPileCardsIntoDeck() throws EmptyDeckException {
        List<Card> pile = List.of(
                new Card(Rank.FIVE,  Card.SUIT_HEARTS),
                new Card(Rank.KING,  Card.SUIT_SPADES),
                new Card(Rank.THREE, Card.SUIT_CLUBS)
        );

        // Drain first to confirm reshuffleFromPile replenishes the deck
        while (!deck.isEmpty()) deck.draw();

        deck.reshuffleFromPile(pile);

        assertEquals(3, deck.remainingCards());
        assertFalse(deck.isEmpty());
    }

    @Test
    void reshuffleFromPile_allPileCardsAreDrawable() throws EmptyDeckException {
        List<Card> pile = List.of(
                new Card(Rank.ACE,  Card.SUIT_DIAMONDS),
                new Card(Rank.JACK, Card.SUIT_HEARTS)
        );

        while (!deck.isEmpty()) deck.draw();
        deck.reshuffleFromPile(pile);

        List<Card> drawn = new ArrayList<>();
        while (!deck.isEmpty()) {
            drawn.add(deck.draw());
        }

        assertTrue(drawn.containsAll(pile));
    }

    @Test
    void reshuffleFromPile_emptyListThrowsEmptyDeckException() {
        assertThrows(EmptyDeckException.class,
                () -> deck.reshuffleFromPile(List.of()));
    }

    @Test
    void reshuffleFromPile_nullListThrowsEmptyDeckException() {
        assertThrows(EmptyDeckException.class,
                () -> deck.reshuffleFromPile(null));
    }

    @Test
    void reshuffleFromPile_replacesPreviousContents() throws EmptyDeckException {
        // Deck starts with 52 cards; after reshuffle it only contains the pile cards
        List<Card> pile = List.of(new Card(Rank.SEVEN, Card.SUIT_CLUBS));
        deck.reshuffleFromPile(pile);
        assertEquals(1, deck.remainingCards());
    }
}
