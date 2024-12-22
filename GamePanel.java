package Chess.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.util.ArrayList;
import java.awt.RenderingHints;

import java.util.Stack;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;

import Chess.piece.Bishop;
import Chess.piece.King;
import Chess.piece.Knight;
import Chess.piece.Pawn;
import Chess.piece.Piece;
import Chess.piece.Queen;
import Chess.piece.Rook;

public class GamePanel extends JPanel implements Runnable{

    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;

    Board board = new Board();
    Mouse mouse = new Mouse();

    // Pieces
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>(); 
    Piece activePiece, checkingPiece;
    public static Piece castlingPiece;

    //Legal Moves
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    boolean stalemate;

    // Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    private Stack<GameState> undoStack = new Stack<>();
    private Stack<GameState> redoStack = new Stack<>();

    private Set<Piece> whiteCapturedPieces = new HashSet<>();
    private Set<Piece> blackCapturedPieces = new HashSet<>();
    
    // UI Components
    private JButton undoButton;
    private JButton redoButton;

    public GamePanel(){
        // Panel settings
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        setLayout(null);

        // Create and configure undo button
        undoButton = new JButton("Undo");
        undoButton.setBounds(900, 350, 100, 40);
        undoButton.setFont(new Font("Arial", Font.BOLD, 16));
        undoButton.setFocusable(false);
        undoButton.addActionListener(_ -> {
            undo();
            repaint();
        });
        add(undoButton);

        // Create and configure redo button
        redoButton = new JButton("Redo");
        redoButton.setBounds(900, 400, 100, 40);
        redoButton.setFont(new Font("Arial", Font.BOLD, 16));
        redoButton.setFocusable(false);
        redoButton.addActionListener(_ -> {
            redo();
            repaint();
        });
        add(redoButton);

        // Mouse settings/actions
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }
    
    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces(){
        // White team
        for(int i = 0; i < 8; i++){
            pieces.add(new Pawn(WHITE, i, 6));
        }
        pieces.add(new Rook(WHITE, 0,7));
        pieces.add(new Rook(WHITE, 7,7));
        pieces.add(new Knight(WHITE, 1,7));
        pieces.add(new Knight(WHITE, 6,7));
        pieces.add(new Bishop(WHITE, 2,7));
        pieces.add(new Bishop(WHITE, 5,7));
        pieces.add(new Queen(WHITE, 3,7));
        pieces.add(new King(WHITE, 4,7));

        // Balck team
        for(int i = 0; i < 8; i++){
            pieces.add(new Pawn(BLACK, i, 1));
        }
        pieces.add(new Rook(BLACK, 0,0));
        pieces.add(new Rook(BLACK, 7,0));
        pieces.add(new Knight(BLACK, 1,0));
        pieces.add(new Knight(BLACK, 6,0));
        pieces.add(new Bishop(BLACK, 2,0));
        pieces.add(new Bishop(BLACK, 5,0));
        pieces.add(new Queen(BLACK, 3,0));
        pieces.add(new King(BLACK, 4,0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for(int i = 0; i < source.size(); i++){
            target.add(source.get(i));
        }
    }

    @Override
    public void run() {
        // Create Game loop
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null){
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if(delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    private void updateButtonStates() {
        undoButton.setEnabled(!undoStack.isEmpty() && !gameover && !stalemate);
        redoButton.setEnabled(!redoStack.isEmpty() && !gameover && !stalemate);
    }

    // Update saveGameState method
    private void saveGameState() {
        if (gameover || stalemate) {
            return;
        }
        GameState currentState = new GameState(pieces, currentColor, whiteCapturedPieces, blackCapturedPieces);
        undoStack.push(currentState);
        redoStack.clear();
        updateButtonStates();
    }
    
    public void undo() {
        if (!undoStack.isEmpty() && !gameover && !stalemate) {
            // Save current state to redo stack
            GameState currentState = new GameState(pieces, currentColor, whiteCapturedPieces, blackCapturedPieces);
            redoStack.push(currentState);
            
            // Restore previous state
            GameState previousState = undoStack.pop();
            
            // Update pieces
            pieces.clear();
            pieces.addAll(previousState.getPieces());
            
            // Update color
            currentColor = previousState.getCurrentColor();
            
            // Update captured pieces
            whiteCapturedPieces.clear();
            whiteCapturedPieces.addAll(previousState.getWhiteCapturedPieces());
            blackCapturedPieces.clear();
            blackCapturedPieces.addAll(previousState.getBlackCapturedPieces());
            
            // Update simulation pieces
            copyPieces(pieces, simPieces);
            
            // Reset piece states
            activePiece = null;
            promotion = false;
            checkingPiece = null;
            castlingPiece = null;
            
            updateButtonStates();
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty() && !gameover && !stalemate) {
            // Save current state to undo stack
            GameState currentState = new GameState(pieces, currentColor, whiteCapturedPieces, blackCapturedPieces);
            undoStack.push(currentState);
            
            // Restore next state
            GameState nextState = redoStack.pop();
            
            // Update pieces
            pieces.clear();
            pieces.addAll(nextState.getPieces());
            
            // Update color
            currentColor = nextState.getCurrentColor();
            
            // Update captured pieces
            whiteCapturedPieces.clear();
            whiteCapturedPieces.addAll(nextState.getWhiteCapturedPieces());
            blackCapturedPieces.clear();
            blackCapturedPieces.addAll(nextState.getBlackCapturedPieces());
            
            // Update simulation pieces
            copyPieces(pieces, simPieces);
            
            // Reset piece states
            activePiece = null;
            promotion = false;
            checkingPiece = null;
            castlingPiece = null;
            
            updateButtonStates();
        }
    }

    public void update() {
        if(promotion) {
            promoting();
        }
        else if(gameover == false && stalemate == false) {
            if(mouse.pressed) {
                if(activePiece == null) {
                    for(Piece piece : simPieces) {
                        if(
                            piece.color == currentColor &&
                            piece.col == mouse.x/Board.SQUARE_SIZE &&
                            piece.row == mouse.y/Board.SQUARE_SIZE
                        ) {
                            activePiece = piece;
                        }
                    }
                }
                else {
                    simulate();
                }
            }
    
            if(mouse.pressed == false) {
                if(activePiece != null) {
                    if(validSquare) {
                        // Save the state before making the move
                        saveGameState();
                        
                        copyPieces(simPieces, pieces);
                        activePiece.updatePositions();
    
                        if(castlingPiece != null) {
                            castlingPiece.updatePositions();
                        }

                        if(isKingInCheck() && isCheckmate()) {
                            gameover = true;
                            updateButtonStates();
                        }
                        else if(isStalemate() && isKingInCheck() == false) {
                            stalemate = true;
                            updateButtonStates();
                        }
                        else {
                            if(canPromote()) {
                                promotion = true;
                            }
                            else {
                                changePlayer();
                            }
                        }
                    }
                    else {
                        copyPieces(pieces, simPieces);
                        activePiece.resetPosition();
                        activePiece = null;
                    }
                }
            }
        }
    }

    private void simulate() {
        canMove = false;
        validSquare = false;

        copyPieces(pieces, simPieces);

        if(castlingPiece != null) {
            castlingPiece.col = castlingPiece.preCol;
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }

        activePiece.x = mouse.x - Board.HALF_SQAURE_SIZE;
        activePiece.y = mouse.y - Board.HALF_SQAURE_SIZE;

        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        if(activePiece.canMove(activePiece.col, activePiece.row)) {
            canMove = true;

            if(activePiece.hittingPiece != null) {
                // HashSet will automatically handle duplicates
                if(activePiece.hittingPiece.color == WHITE) {
                    whiteCapturedPieces.add(activePiece.hittingPiece);
                } else {
                    blackCapturedPieces.add(activePiece.hittingPiece);
                }
                simPieces.remove(activePiece.hittingPiece.getIndex());
            }

            checkCastling();

            if(isIllegal(activePiece) == false && opponentCanCaptureKing() == false) {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king){
        if(king.type == Type.KING){
            for(Piece piece : simPieces){
                if(piece != king && piece.color != king.color && piece.canMove(king.col, king.row)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(){
        Piece king = getKing(false);

        for(Piece piece : simPieces){
            if(piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck(){

        Piece king = getKing(true);

        if(activePiece.canMove(king.col, king.row)){
            checkingPiece = activePiece;
            return true;
        }
        else{
            checkingPiece = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent){
        Piece king = null;

        for(Piece piece : simPieces){
            if(opponent){
                if(piece.type == Type.KING && piece.color != currentColor){
                    king = piece;
                }
            }
            else{
                if(piece.type == Type.KING && piece.color == currentColor){
                    king = piece;
                }
            }
        }

        return king;
    }

    private boolean isCheckmate(){

        Piece king = getKing(true);

        if(kingCanMove(king)){
            return false;
        }
        else{
            int colDiff = Math.abs(checkingPiece.col - king.col);
            int rwoDiff = Math.abs(checkingPiece.row - king.row);

            if(colDiff == 0){
                //vertical attacking
                if(checkingPiece.row < king.row){
                    //piece is above king
                    for(int row = checkingPiece.row; row < king.row; row++){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingPiece.row > king.row){
                    //piece is below the king
                    for(int row = checkingPiece.row; row > king.row; row--){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)){
                                return false;
                            }
                        }
                    }
                }
            }
            if(rwoDiff == 0){
                //Horizontal attacking
                if(checkingPiece.col < king.col){
                    //Piece is to left
                    for(int col = checkingPiece.col; col < king.col; col++){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingPiece.col > king.col){
                    //Piece is to right
                    for(int col = checkingPiece.col; col > king.col; col--){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)){
                                return false;
                            }
                        }
                    }
                }
            }
            if(colDiff == rwoDiff){
                //diagonal attacking
                if(checkingPiece.row < king.row){
                    //above
                    if(checkingPiece.col < king.col){
                        //upper left
                        for(int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row++){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingPiece.col > king.col){
                        //upper right
                        for(int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row++){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingPiece.row > king.row){
                    //below
                    if(checkingPiece.col < king.col){
                        //lower left
                        for(int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row--){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingPiece.col > king.col){
                        //lower right
                        for(int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row--){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            else{
                //knight is attacking -> cannot be blocked as it is a jumping attack
            }
        }

        return true;

    }

    private boolean kingCanMove(Piece king){

        if(isValidMove(king, -1, -1)){return true;}
        if(isValidMove(king, 0, -1)){return true;}
        if(isValidMove(king, 1, -1)){return true;}
        if(isValidMove(king, -1, 0)){return true;}
        if(isValidMove(king, 1, 0)){return true;}
        if(isValidMove(king, -1, 0)){return true;}
        if(isValidMove(king, 0, 1)){return true;}
        if(isValidMove(king, 1, 1)){return true;}

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus){

        boolean isValidMove = false;

        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col, king.row)){
            if(king.hittingPiece != null){
                simPieces.remove(king.hittingPiece.getIndex());
            }
            if(isIllegal(king) == false){
                isValidMove = true;
            }
        }

        king.resetPosition();
        copyPieces(pieces, simPieces);
        
        return isValidMove;

    }

    private boolean isStalemate(){
        int count = 0;

        for(Piece piece : simPieces){
            if(piece.color != currentColor){
                count++;
            }
        }

        if(count == 1){
            if(kingCanMove(getKing(true)) == false){
                return true;
            }
        }

        return false;
    }

    private void checkCastling(){
        if(castlingPiece != null){
            if(castlingPiece.col == 0){
                castlingPiece.col += 3;
            }
            else if(castlingPiece.col == 7){
                castlingPiece.col -= 2;
            }
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
        }
    }

    private void changePlayer(){
        if(currentColor == WHITE){
            currentColor = BLACK;

            //Reset Black's two stepped status
            for(Piece piece : pieces){
                if(piece.color == BLACK){
                    piece.twoStepped = false;
                }
            }
        }
        else{
            currentColor = WHITE;

            //Reset White's two stepped status
            for(Piece piece : pieces){
                if(piece.color == WHITE){
                    piece.twoStepped = false;
                }
            }
        }
        activePiece = null;
    }

    private boolean canPromote(){
        if(activePiece.type == Type.PAWN){
            if(currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7){
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9,2));
                promoPieces.add(new Knight(currentColor, 9,3));
                promoPieces.add(new Bishop(currentColor, 9,4));
                promoPieces.add(new Queen(currentColor, 9,5));
                return true;
            }
        }

        return false;
    }

    public void promoting(){
        if(mouse.pressed){
            for(Piece piece : promoPieces){
                if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch(piece.type){
                        case ROOK : simPieces.add(new Rook(currentColor, activePiece.col, activePiece.row)); break;
                        case KNIGHT : simPieces.add(new Knight(currentColor, activePiece.col, activePiece.row)); break;
                        case BISHOP : simPieces.add(new Bishop(currentColor, activePiece.col, activePiece.row)); break;
                        case QUEEN : simPieces.add(new Queen(currentColor, activePiece.col, activePiece.row)); break;
                        default : break;
                    }
                    simPieces.remove(activePiece.getIndex());
                    copyPieces(simPieces, pieces);
                    activePiece = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        // Draw board
        board.draw(g2);

        // Draw pieces
        for(Piece p : simPieces){
            p.draw(g2);
        }

        if(activePiece != null){
            if(canMove){
                if(isIllegal(activePiece) || opponentCanCaptureKing()){
                    g2.setColor(Color.GRAY);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col*Board.SQUARE_SIZE, activePiece.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
                else{
                    g2.setColor(Color.WHITE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col*Board.SQUARE_SIZE, activePiece.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }

            activePiece.draw(g2);
        }

        //Status Message
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        g2.setColor(Color.WHITE);

        if(promotion){
            g2.drawString("PROMOTE TO:", 840, 150);
            for(Piece piece : promoPieces){
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        }
        else{
            if(currentColor == WHITE){
                g2.drawString("WHITE'S TURN", 880, 770);
                if(checkingPiece != null && checkingPiece.color == BLACK){
                    g2.setColor(Color.RED);
                    g2.drawString("The King", 840, 650);
                    g2.drawString("is in Check", 840, 700);
                }
            }
            else{
                g2.drawString("BLACK'S TURN", 880, 30);
                if(checkingPiece != null && checkingPiece.color == WHITE){
                    g2.setColor(Color.RED);
                    g2.drawString("The King", 840, 200);
                    g2.drawString("is in Check", 840, 250);
                }
            }
        }
        if(gameover){
            String s = "";
            if(currentColor == WHITE){
                s = "White Wins";
            }
            else{
                s = "Balck Wins";
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.GREEN);
            g2.drawString(s, 200, 420);
        }
        if(stalemate){
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.lightGray);
            g2.drawString("Stalemate", 200, 420);
        }

        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        
        // White's captured pieces (at the bottom)
        g2.setColor(Color.WHITE);
        g2.drawString("White's Captures:", 870, 500);
        int x = 820;
        int y = 520;
        for(Piece piece : blackCapturedPieces) {
            g2.drawImage(piece.image, x, y, Board.SQUARE_SIZE/2, Board.SQUARE_SIZE/2, null);
            x += Board.SQUARE_SIZE/2;
            if(x > 1040) {
                x = 820;
                y += Board.SQUARE_SIZE/2;
            }
        }

        // Black's captured pieces (at the top)
        g2.drawString("Black's Captures:", 870, 310);
        x = 820;
        y = 50;
        for(Piece piece : whiteCapturedPieces) {
            g2.drawImage(piece.image, x, y, Board.SQUARE_SIZE/2, Board.SQUARE_SIZE/2, null);
            x += Board.SQUARE_SIZE/2;
            if(x > 1040) {
                x = 820;
                y += Board.SQUARE_SIZE/2;
            }
        }
    }
    
}
