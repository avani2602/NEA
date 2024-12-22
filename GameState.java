package Chess.main;

import java.util.ArrayList;
import Chess.piece.Piece;
import java.util.HashSet;
import java.util.Set;

// Class to represent a game state (keep this outside GamePanel)
class GameState {
    private final ArrayList<Piece> pieces;
    private final int currentColor;
    private final Set<Piece> whiteCapturedPieces;
    private final Set<Piece> blackCapturedPieces;

    public GameState(ArrayList<Piece> pieces, int currentColor, 
                    Set<Piece> whiteCaptured, Set<Piece> blackCaptured) {
        // Deep copy the pieces
        this.pieces = new ArrayList<>();
        for (Piece piece : pieces) {
            this.pieces.add(piece.clone());
        }
        
        this.currentColor = currentColor;
        
        // Deep copy white captured pieces
        this.whiteCapturedPieces = new HashSet<>();
        for (Piece piece : whiteCaptured) {
            this.whiteCapturedPieces.add(piece.clone());
        }
        
        // Deep copy black captured pieces
        this.blackCapturedPieces = new HashSet<>();
        for (Piece piece : blackCaptured) {
            this.blackCapturedPieces.add(piece.clone());
        }
    }
    
    public ArrayList<Piece> getPieces() {
        return new ArrayList<>(pieces);
    }
    
    public int getCurrentColor() {
        return currentColor;
    }
    
    public Set<Piece> getWhiteCapturedPieces() {
        return new HashSet<>(whiteCapturedPieces);
    }
    
    public Set<Piece> getBlackCapturedPieces() {
        return new HashSet<>(blackCapturedPieces);
    }
}