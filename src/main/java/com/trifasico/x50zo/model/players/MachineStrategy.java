package com.trifasico.x50zo.model.players;

import com.trifasico.x50zo.model.Card;
import com.trifasico.x50zo.model.GameState;
import com.trifasico.x50zo.model.Rank;

import java.util.List;

/**
 * A scoring-based implementation that evaluates each
 * card in the machine's hand and picks the one with the highest score.
 *
 * <h2>Scoring rules (applied in order, scores are additive)</h2>
 * <ol>
 *   <li><strong>Unplayable card</strong> → score set to
 *       {@link #SCORE_UNPLAYABLE} (−1000). Immediately skipped; no other
 *       rules apply.</li>
 *   <li><strong>Aggressive play</strong>: the card's resolved value is the
 *       highest possible among all playable cards in hand → {@link #SCORE_AGGRESSIVE}
 *       (+30). Aims to push the sum as high as possible, cornering the next player.</li>
 *   <li><strong>Neutral nine</strong>: rank is {@link Rank#NINE} (value 0) and
 *       no additive card exists → {@link #SCORE_NINE_NEUTRAL} (+20). Preserves
 *       the sum when no safe positive card is available.</li>
 *   <li><strong>Subtractive play</strong>: rank is J/Q/K (value −10) and no
 *       additive or neutral option is playable → {@link #SCORE_SUBTRACT} (+10).
 *       Last resort; lowers the sum but also gives opponents more room.</li>
 * </ol>
 *
 * <p>Ties are broken by the order in which cards appear in the hand (first
 * highest-scoring card wins). The algorithm is O(n²) in hand size, which is
 * negligible given the maximum hand size of 4.</p>
 *
 * @author Yostin Ramirez
 * @author Lesly Zapata
 * @author Joseph Terreros
 * @version 1.0
 * @see MachinePlayer
 */
public class MachineStrategy {

    static final int SCORE_UNPLAYABLE   = -1000;
    static final int SCORE_AGGRESSIVE   =    30;
    static final int SCORE_NINE_NEUTRAL =    20;
    static final int SCORE_SUBTRACT     =    10;

    /**
     * Constructs a {@code MachineStrategy} with default scoring weights.
     */
    public MachineStrategy() {}

    /**
     * {@inheritDoc}
     *
     * <p>Scores every card in {@code hand} according to the class-level
     * scoring rules and returns the index of the card with the highest score.</p>
     */

    public int chooseCardIndex(List<Card> hand, GameState gameState) {
        int tableSum      = gameState.tableSum();
        int highestValue  = findHighestPlayableValue(hand, tableSum);
        boolean hasAdditive  = hasPlayableAdditive(hand, tableSum);
        boolean hasNeutral   = hasPlayableNeutral(hand, tableSum);

        int bestIndex = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < hand.size(); i++) {
            Card card  = hand.get(i);
            int  score = scoreCard(card, tableSum, highestValue, hasAdditive, hasNeutral);

            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    /**
     * Computes the score for a single card given the current game context.
     *
     * @param card        the card being evaluated
     * @param tableSum    the current table sum
     * @param highestPlayableValue the highest resolved value among all playable cards
     * @param hasAdditive whether any card in hand has a positive resolved value and is playable
     * @param hasNeutral  whether the nine is in hand and playable
     * @return the computed score
     */
    private int scoreCard(Card card, int tableSum, int highestPlayableValue,
                          boolean hasAdditive, boolean hasNeutral) {
        if (!card.isPlayable(tableSum)) {
            return SCORE_UNPLAYABLE;
        }

        int resolvedValue = card.getValue(tableSum);
        int score         = 0;

        if (resolvedValue == highestPlayableValue && resolvedValue > 0) {
            score += SCORE_AGGRESSIVE;
        }

        if (card.rank() == Rank.NINE && !hasAdditive) {
            score += SCORE_NINE_NEUTRAL;
        }

        if (resolvedValue < 0 && !hasAdditive && !hasNeutral) {
            score += SCORE_SUBTRACT;
        }

        return score;
    }

    /**
     * Returns the highest resolved value among all cards in {@code hand}
     * that are playable at the current sum. Returns {@link Integer#MIN_VALUE}
     * if no card is playable (should not happen during normal play).
     */
    private int findHighestPlayableValue(List<Card> hand, int tableSum) {
        int max = Integer.MIN_VALUE;
        for (Card card : hand) {
            if (card.isPlayable(tableSum)) {
                int v = card.getValue(tableSum);
                if (v > max) max = v;
            }
        }
        return max;
    }

    /**
     * Returns {@code true} if at least one card in hand is playable and has
     * a strictly positive resolved value at the current sum.
     */
    private boolean hasPlayableAdditive(List<Card> hand, int tableSum) {
        for (Card card : hand) {
            if (card.isPlayable(tableSum) && card.getValue(tableSum) > 0) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the hand contains a {@link Rank#NINE} that is
     * playable at the current sum (it always is, since it adds 0).
     */
    private boolean hasPlayableNeutral(List<Card> hand, int tableSum) {
        for (Card card : hand) {
            if (card.rank() == Rank.NINE && card.isPlayable(tableSum)) return true;
        }
        return false;
    }
}