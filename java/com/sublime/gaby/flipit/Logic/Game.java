package com.sublime.gaby.flipit.Logic;


import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Gaby on 2/9/2017.
 */


public class Game {


    private Board mBoard = new Board();
    private GamePiece mCurrentlyPlaying = GamePiece.BLACK; // PLayer with black pieces starts the game.
    private GameStatus mGameStatus = GameStatus.ONGOING;


    // Risk Zones from best to riskiest spots to play (for A.I. to make better moves):

    private static final Integer[] RISK_ZONE0 = new Integer[]{0,7,56,63}; // Corners.
    private static final Integer[] RISK_ZONE1 = new Integer[]{2,3,4,5,16,23,24,31,32,39,40,47,58,59,60,61}; // Margins (not including corners and spots around corners).
    private static final Integer[] RISK_ZONE2 = new Integer[]{18,19,20,21,26,27,28,29,34,35,36,37,42,43,44,45}; // Central area of the board.
    private static final Integer[] RISK_ZONE3 = new Integer[]{10,11,12,13,17,22,25,30,33,38,41,46,50,51,52,53}; // Near margins (not including spots around corners).
    private static final Integer[] RISK_ZONE4 = new Integer[]{1,6,8,15,48,55,57,62}; // Adjacent to Corners (but not diagonal).
    private static final Integer[] RISK_ZONE5 = new Integer[]{9,14,49,54}; // Adjacent and Diagonal to Corners.
    private static final Integer[] RISK_ZONE6 = new Integer[]{1,6,8,9,14,15,48,49,54,55,57,62}; // Around Corners (zones4+5).

    private static final int INFINITY = 1000000;


    public enum GameStatus{
        ONGOING, PLAYER_BLACK_WON, PLAYER_WHITE_WON, DRAW;

        @Override
        public String toString() {
            switch(this) {

                default:
                case ONGOING:
                    return "Ongoing";
                case PLAYER_BLACK_WON:
                    return "Player Black Won";
                case PLAYER_WHITE_WON:
                    return "Player White Won";
                case DRAW:
                    return "Draw";

            }
        }
    }


    public void playTile(int positionI, int positionJ) {

        // Trying to add a piece of game to the board:
        boolean playerPlayed = mBoard.addGamePiece(positionI, positionJ, mCurrentlyPlaying);

        if(playerPlayed) {

            if(mBoard.getmNumberOfEmptyTiles() == 0)
                endGame();

            else{

                // Determining opponent's game piece:
                GamePiece opponentPiece;
                if (mCurrentlyPlaying == GamePiece.BLACK) opponentPiece = GamePiece.WHITE;
                else opponentPiece = GamePiece.BLACK;

                // Checking If opponent can play:
                if(!mBoard.getPlayableTiles(opponentPiece).isEmpty())
                    toggleTurn();

                // Otherwise, if both players can't play
                else if(mBoard.getPlayableTiles(mCurrentlyPlaying).isEmpty())
                    endGame();

                // Otherwise, the same player will simply get another turn. :p

            }

        }


    }



    private void toggleTurn() {

        if(mCurrentlyPlaying == GamePiece.BLACK)
            mCurrentlyPlaying = GamePiece.WHITE;
        else
            mCurrentlyPlaying = GamePiece.BLACK;

    }



    private void endGame(){

        if(mBoard.getmNumberOfBlackPieces() > mBoard.getmNumberOfWhitePieces())
            mGameStatus = GameStatus.PLAYER_BLACK_WON;
        else if(mBoard.getmNumberOfBlackPieces() < mBoard.getmNumberOfWhitePieces())
            mGameStatus = GameStatus.PLAYER_WHITE_WON;
        else
            mGameStatus = GameStatus.DRAW;

    }




    // Getters:

    public Board getmBoard() {
        return mBoard;
    }

    public GamePiece getmCurrentlyPlaying() {
        return mCurrentlyPlaying;
    }

    public GameStatus getmGameStatus() {
        return mGameStatus;
    }

    // Getters used by advanced tutorial:

    public static Integer[] getRiskZone0() {
        return RISK_ZONE0;
    }

    public static Integer[] getRiskZone1() {
        return RISK_ZONE1;
    }

    public static Integer[] getRiskZone2() {
        return RISK_ZONE2;
    }

    public static Integer[] getRiskZone3() {
        return RISK_ZONE3;
    }

    public static Integer[] getRiskZone4() {
        return RISK_ZONE4;
    }

    public static Integer[] getRiskZone5() {
        return RISK_ZONE5;
    }

    public static Integer[] getRiskZone6() {
        return RISK_ZONE6;
    }


    // Setter that used only in online game when a player concedes:

    public void setmGameStatus(GameStatus gameStatus) {
        mGameStatus = gameStatus;
    }





    // The rest of the methods are useful for A.I. :


    public void makeAIMove(Difficulty difficulty){

        // If Difficulty is BEGINNER:
        // Make a random move.

        // If Difficulty is EASY:
        // Make a random move with a basic strategy that attempts to capture the corners.

        // If Difficulty is MEDIUM:
        // Attempt to capture the corners while making the move that results with more flipped pieces.

        // If Difficulty is HARD:
        // Picking the best zone to play out of all legal moves and making the move
        // that results with more flipped pieces within that zone.

        // If Difficulty is EXPERT:
        // Using the alpha beta pruning algorithm to calculate a few steps ahead, while relying on
        // an advanced zone-based strategy.

        if (difficulty == Difficulty.BEGINNER) makeRandomMove();
        else if (difficulty == Difficulty.EASY)makeCornerStrategyMove();
        else if (difficulty == Difficulty.MEDIUM) makeCornerMaximalStrategyMove();
        else if (difficulty == Difficulty.HARD) makeZoneMaximalStrategyMove();
        else if (difficulty == Difficulty.EXPERT) makeOptimalMove();

    }


    private void makeRandomMove(){


        // Playable Tiles:
        ArrayList<Integer> playableTiles = mBoard.getPlayableTiles(mCurrentlyPlaying);

        // Making a random legal move:
        int index = new Random().nextInt(playableTiles.size());
        int position = playableTiles.get(index);
        playTile(position/mBoard.getNumberOfCols(),position%mBoard.getNumberOfCols());


    }


    private void makeCornerStrategyMove(){


        // Playable Tiles:
        ArrayList<Integer> playableTiles = mBoard.getPlayableTiles(mCurrentlyPlaying);

        // Trying to capture one of the corners:
        for(int i = 0; i < RISK_ZONE0.length; i++){
            if(playableTiles.contains(RISK_ZONE0[i])){
                playTile(RISK_ZONE0[i]/mBoard.getNumberOfCols(), RISK_ZONE0[i]%mBoard.getNumberOfCols());
                return;
            }
        }

        // Making a random legal move:
        int index = new Random().nextInt(playableTiles.size());
        int position = playableTiles.get(index);
        playTile(position/mBoard.getNumberOfCols(),position%mBoard.getNumberOfCols());


    }


    private void makeCornerMaximalStrategyMove(){


        // Playable Tiles:
        ArrayList<Integer> playableTiles = mBoard.getPlayableTiles(mCurrentlyPlaying);

        // The play with maximum flipped pieces:
        int maxPlayFlippedAmount = -1;
        int maxPlayPosition = -1;


        // Trying to capture one of the corners (with a preference for flipping maximum pieces):

        for(int i = 0; i < RISK_ZONE0.length; i++){
            if(playableTiles.contains(RISK_ZONE0[i])){

                int flippedAmount = predictOneTurnFlippedAmount(RISK_ZONE0[i]);

                if(flippedAmount > maxPlayFlippedAmount){
                    maxPlayFlippedAmount = flippedAmount;
                    maxPlayPosition = RISK_ZONE0[i];
                }

            }
        }


        // If one of the corners or more are playable:
        if(maxPlayPosition!=-1) {
            // Play the corner that results with more flipped pieces:
            playTile(maxPlayPosition / mBoard.getNumberOfCols(), maxPlayPosition % mBoard.getNumberOfCols());
            return;
        }

        // Otherwise, make another move that results with maximum flipped pieces:

        for(int i = 0; i < playableTiles.size(); i++){

            int flippedAmount = predictOneTurnFlippedAmount(playableTiles.get(i));

            if(flippedAmount > maxPlayFlippedAmount){
                maxPlayFlippedAmount = flippedAmount;
                maxPlayPosition = playableTiles.get(i);
            }

        }

        // Play a tile that results with more flipped pieces:
        playTile(maxPlayPosition / mBoard.getNumberOfCols(), maxPlayPosition % mBoard.getNumberOfCols());


    }


    private void makeZoneMaximalStrategyMove(){

        // Better Playable Tiles:
        ArrayList<Integer> betterPlayableTiles = getBetterPlayableTiles(mBoard.getPlayableTiles(mCurrentlyPlaying), false, null);

        // The play with maximum flipped pieces:
        int maxPlayFlippedAmount = -1;
        int maxPlayPosition = -1;

        // Going through all better playable tiles:
        for(Integer position : betterPlayableTiles){

            int flippedAmount = predictOneTurnFlippedAmount(position);

            if(flippedAmount > maxPlayFlippedAmount){
                maxPlayFlippedAmount = flippedAmount;
                maxPlayPosition = position;
            }

        }

        // Play a tile within the best playable zone that results with more flipped pieces:
        playTile(maxPlayPosition / mBoard.getNumberOfCols(), maxPlayPosition % mBoard.getNumberOfCols());

    }




    private ArrayList<Integer> getBetterPlayableTiles(ArrayList<Integer> playableTiles ,
                                                      boolean applyAdvancedStrategy, Board board){


        // Better Playable Tiles:
        ArrayList<Integer> betterPlayableTiles = new ArrayList<Integer>();


        // Checking if can capture RISK_ZONE0:
        for(int i = 0; i < RISK_ZONE0.length; i++){
            if(playableTiles.contains(RISK_ZONE0[i])){
                betterPlayableTiles.add(RISK_ZONE0[i]);
            }
        }
        if(!betterPlayableTiles.isEmpty())
            return betterPlayableTiles;



        // Using a more stable strategy:
        if(applyAdvancedStrategy){


            // Out of all playable tiles, we want to ignore those that gives the opponent an opportunity
            // to capture the corners next turn (if we can):

            GamePiece currentlyNotPlaying;
            if(mCurrentlyPlaying == GamePiece.BLACK) currentlyNotPlaying = GamePiece.WHITE;
            else currentlyNotPlaying = GamePiece.BLACK;

            ArrayList<Integer> preventCornerPlayableTiles = new ArrayList<Integer>();

            for(int i = 0; i < playableTiles.size(); i++){

                Board newBoard = new Board(board);
                newBoard.addGamePiece(playableTiles.get(i) / board.getNumberOfCols(),
                        playableTiles.get(i) % board.getNumberOfCols(),
                        mCurrentlyPlaying);

                ArrayList<Integer> opponentNextTurnPlayable = newBoard.getPlayableTiles(currentlyNotPlaying);

                // If opponent won't be able to capture a corner next turn:

                boolean canOpponentCapture = false;
                for(int k = 0; k < RISK_ZONE0.length; k++){
                    if (opponentNextTurnPlayable.contains(RISK_ZONE0[k])){
                        canOpponentCapture = true;
                        k = RISK_ZONE0.length;
                    }
                }

                if(!canOpponentCapture) preventCornerPlayableTiles.add(playableTiles.get(i));

            }

            if(!preventCornerPlayableTiles.isEmpty())
                playableTiles = preventCornerPlayableTiles;



            // To make it even smarter, w'ell try to prevent the opponent from capturing RISK_ZONE1 next turn:

            ArrayList<Integer> preventMarginPlayableTiles = new ArrayList<Integer>();

            for(int i = 0; i < playableTiles.size(); i++){

                Board newBoard = new Board(board);
                newBoard.addGamePiece(playableTiles.get(i) / board.getNumberOfCols(),
                        playableTiles.get(i) % board.getNumberOfCols(),
                        mCurrentlyPlaying);

                ArrayList<Integer> opponentNextTurnPlayable = newBoard.getPlayableTiles(currentlyNotPlaying);

                // If opponent won't be able to capture the margins next turn:

                boolean canOpponentCapture = false;
                for(int k = 0; k < RISK_ZONE1.length; k++){
                    if (opponentNextTurnPlayable.contains(RISK_ZONE1[k])){
                        canOpponentCapture = true;
                        k = RISK_ZONE1.length;
                    }
                }

                if(!canOpponentCapture) preventMarginPlayableTiles.add(playableTiles.get(i));

            }

            if(!preventMarginPlayableTiles.isEmpty())
                playableTiles = preventMarginPlayableTiles;


            // In some cases, capturing RISK_ZONE4 might be actually great.
            // If the opponent can't capture the corners nor the margins, RISK_ZONE4 will simply give
            // the player more board control.
            if(!preventCornerPlayableTiles.isEmpty() && !preventMarginPlayableTiles.isEmpty()){

                // Checking if can capture RISK_ZONE4:
                for(int i = 0; i < RISK_ZONE4.length; i++){
                    if(playableTiles.contains(RISK_ZONE4[i])){
                        betterPlayableTiles.add(RISK_ZONE4[i]);
                    }
                }
                if(!betterPlayableTiles.isEmpty())
                    return betterPlayableTiles;

            }




        }


        // Checking if can capture RISK_ZONE1:
        for(int i = 0; i < RISK_ZONE1.length; i++){
            if(playableTiles.contains(RISK_ZONE1[i])){
                betterPlayableTiles.add(RISK_ZONE1[i]);
            }
        }
        if(!betterPlayableTiles.isEmpty())
            return betterPlayableTiles;


        // Checking if can capture RISK_ZONE2:
        for(int i = 0; i < RISK_ZONE2.length; i++){
            if(playableTiles.contains(RISK_ZONE2[i])){
                betterPlayableTiles.add(RISK_ZONE2[i]);
            }
        }
        if(!betterPlayableTiles.isEmpty())
            return betterPlayableTiles;


        // Checking if can capture RISK_ZONE3:
        for(int i = 0; i < RISK_ZONE3.length; i++){
            if(playableTiles.contains(RISK_ZONE3[i])){
                betterPlayableTiles.add(RISK_ZONE3[i]);
            }
        }
        if(!betterPlayableTiles.isEmpty())
            return betterPlayableTiles;


        return playableTiles;


    }



    private void makeOptimalMove(){


        final int MAX_MOVES_AHEAD = 3;


        // Better Playable Tiles:
        ArrayList<Integer> betterPlayableTiles = getBetterPlayableTiles(mBoard.getPlayableTiles(mCurrentlyPlaying), true, mBoard);


        int maxPieceAmount = -INFINITY;
        int optimalPiecePosition = -1;


        // Going through all possible plays:
        for(int i = 0; i < betterPlayableTiles.size(); i++){

            // Creating new board:
            Board newBoard = new Board(mBoard);

            // Simulating a move:
            newBoard.addGamePiece(betterPlayableTiles.get(i) / mBoard.getNumberOfCols(),
                    betterPlayableTiles.get(i) % mBoard.getNumberOfCols(),
                    mCurrentlyPlaying);

            int newPieceAmount;
            if(mCurrentlyPlaying == GamePiece.BLACK) newPieceAmount = newBoard.getmNumberOfBlackPieces();
            else newPieceAmount = newBoard.getmNumberOfWhitePieces();

            // If the new board is full:
            if(newBoard.getmNumberOfEmptyTiles() == 0){
                if(newPieceAmount > maxPieceAmount){
                    maxPieceAmount = newPieceAmount;
                    optimalPiecePosition = betterPlayableTiles.get(i);
                }
            }
            else{

                // Determining opponent's game piece:
                GamePiece currentlyNotPlaying;
                if (mCurrentlyPlaying == GamePiece.BLACK) currentlyNotPlaying = GamePiece.WHITE;
                else currentlyNotPlaying = GamePiece.BLACK;

                // Checking If opponent can play:
                if(!newBoard.getPlayableTiles(currentlyNotPlaying).isEmpty()){

                    newPieceAmount = getMinimalPosition(new Board(newBoard), MAX_MOVES_AHEAD, -1000, 1000, true);
                    if(newPieceAmount > maxPieceAmount){
                        maxPieceAmount = newPieceAmount;
                        optimalPiecePosition = betterPlayableTiles.get(i);
                    }

                }

                // Otherwise, if both players can't play
                else if(newBoard.getPlayableTiles(mCurrentlyPlaying).isEmpty()){

                    if(newPieceAmount > maxPieceAmount){
                        maxPieceAmount = newPieceAmount;
                        optimalPiecePosition = betterPlayableTiles.get(i);
                    }

                }


                // Otherwise (same player gets another turn):
                else{
                    newPieceAmount = getMaximalPosition(new Board(newBoard), MAX_MOVES_AHEAD, -1000, 1000, false);
                    if(newPieceAmount > maxPieceAmount){
                        maxPieceAmount = newPieceAmount;
                        optimalPiecePosition = betterPlayableTiles.get(i);
                    }
                }

            }

        }


        // Making the optimal move:
        playTile(optimalPiecePosition / mBoard.getNumberOfCols(), optimalPiecePosition % mBoard.getNumberOfCols());


    }







    private int getMaximalPosition(Board board, int movesAhead, int alpha, int beta, boolean isAncestorMin){


        // Better Playable Tiles:
        ArrayList<Integer> betterPlayableTiles = getBetterPlayableTiles(board.getPlayableTiles(mCurrentlyPlaying), true, board);


        int maxPieceAmount = -INFINITY;


        // Going through all possible plays:
        for(int i = 0; i < betterPlayableTiles.size(); i++){

            // Creating new board:
            Board newBoard = new Board(board);

            // Simulating a move:
            newBoard.addGamePiece(betterPlayableTiles.get(i) / mBoard.getNumberOfCols(),
                    betterPlayableTiles.get(i) % mBoard.getNumberOfCols(),
                    mCurrentlyPlaying);

            int newPieceAmount;
            if(mCurrentlyPlaying == GamePiece.BLACK) newPieceAmount = newBoard.getmNumberOfBlackPieces();
            else newPieceAmount = newBoard.getmNumberOfWhitePieces();

            // If the new board is full or we went through enough moves:
            if(newBoard.getmNumberOfEmptyTiles() == 0 || movesAhead == 0){
                if(newPieceAmount > maxPieceAmount){
                    maxPieceAmount = newPieceAmount;
                }
            }
            else{

                // Determining opponent's game piece:
                GamePiece currentlyNotPlaying;
                if (mCurrentlyPlaying == GamePiece.BLACK) currentlyNotPlaying = GamePiece.WHITE;
                else currentlyNotPlaying = GamePiece.BLACK;

                // Checking If opponent can play:
                if(!newBoard.getPlayableTiles(currentlyNotPlaying).isEmpty()){

                    newPieceAmount = getMinimalPosition(new Board(newBoard), movesAhead - 1, maxPieceAmount, beta, true);
                    if(newPieceAmount > maxPieceAmount){
                        maxPieceAmount = newPieceAmount;
                    }

                }

                // Otherwise, if both players can't play
                else if(newBoard.getPlayableTiles(mCurrentlyPlaying).isEmpty()){

                    if(newPieceAmount > maxPieceAmount){
                        maxPieceAmount = newPieceAmount;
                    }

                }


                // Otherwise (same player gets another turn):
                else{
                    newPieceAmount = getMaximalPosition(new Board(newBoard), movesAhead - 1, maxPieceAmount, beta, false);
                    if(newPieceAmount > maxPieceAmount){
                        maxPieceAmount = newPieceAmount;
                    }
                }

            }


            // Pruning:
            if(isAncestorMin && maxPieceAmount >= beta)
                return maxPieceAmount;


        }


        return maxPieceAmount;


    }



    private int getMinimalPosition(Board board, int movesAhead, int alpha, int beta, boolean isAncestorMax){


        // Determining opponent's game piece:
        GamePiece currentlyNotPlaying;
        if (mCurrentlyPlaying == GamePiece.BLACK) currentlyNotPlaying = GamePiece.WHITE;
        else currentlyNotPlaying = GamePiece.BLACK;


        // Better Playable Tiles:
        ArrayList<Integer> betterPlayableTiles = getBetterPlayableTiles(board.getPlayableTiles(currentlyNotPlaying), true, board);


        int minPieceAmount = INFINITY;


        // Going through all possible plays:
        for(int i = 0; i < betterPlayableTiles.size(); i++){

            // Creating new board:
            Board newBoard = new Board(board);

            // Simulating a move:
            newBoard.addGamePiece(betterPlayableTiles.get(i) / mBoard.getNumberOfCols(),
                    betterPlayableTiles.get(i) % mBoard.getNumberOfCols(),
                    currentlyNotPlaying);

            int newPieceAmount;
            if(mCurrentlyPlaying == GamePiece.BLACK) newPieceAmount = newBoard.getmNumberOfBlackPieces();
            else newPieceAmount = newBoard.getmNumberOfWhitePieces();

            // If the new board is full or we went through enough moves:
            if(newBoard.getmNumberOfEmptyTiles() == 0 || movesAhead == 0){
                if(newPieceAmount < minPieceAmount){
                    minPieceAmount = newPieceAmount;
                }
            }
            else{

                // Checking If opponent can play:
                if(!newBoard.getPlayableTiles(mCurrentlyPlaying).isEmpty()){

                    newPieceAmount = getMaximalPosition(new Board(newBoard), movesAhead - 1, alpha, minPieceAmount, true);
                    if(newPieceAmount < minPieceAmount){
                        minPieceAmount = newPieceAmount;
                    }

                }

                // Otherwise, if both players can't play:
                else if(newBoard.getPlayableTiles(currentlyNotPlaying).isEmpty()){

                    if(newPieceAmount < minPieceAmount){
                        minPieceAmount = newPieceAmount;
                    }

                }


                // Otherwise (same player gets another turn):
                else{
                    newPieceAmount = getMinimalPosition(new Board(newBoard), movesAhead - 1, alpha, minPieceAmount, false);
                    if(newPieceAmount < minPieceAmount){
                        minPieceAmount = newPieceAmount;
                    }
                }

            }


            // Pruning:
            if(isAncestorMax && minPieceAmount <= alpha)
                return minPieceAmount;


        }


        return minPieceAmount;


    }




    private int predictOneTurnFlippedAmount(int position){

        Board newBoard = new Board(mBoard);

        newBoard.addGamePiece(position/mBoard.getNumberOfCols(),
                position%mBoard.getNumberOfCols(),
                mCurrentlyPlaying);


        int flippedAmount = 0;

        if(mCurrentlyPlaying == GamePiece.BLACK)
            flippedAmount = newBoard.getmNumberOfBlackPieces() - mBoard.getmNumberOfBlackPieces();
        else
            flippedAmount = newBoard.getmNumberOfWhitePieces() - mBoard.getmNumberOfWhitePieces();


        return flippedAmount;

    }




}
