package com.sublime.gaby.flipit.Logic;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Gaby on 2/8/2017.
 */


public class Board {


    // Number of cols and rows would be the same (the board is NxN):
    private static final int NUMBER_OF_COLS = 8;
    private static final int NUMBER_OF_TILES = NUMBER_OF_COLS*NUMBER_OF_COLS;

    private Tile mTiles[][] = new Tile[NUMBER_OF_COLS][NUMBER_OF_COLS];

    // At the beginning there are 2 pieces on the board for each type of piece (4 pieces in total):
    private int mNumberOfEmptyTiles;
    private int mNumberOfBlackPieces;
    private int mNumberOfWhitePieces;

    // Constructor:
    public Board() {

        // Initializing the array of tiles:
        for(int i = 0; i < NUMBER_OF_COLS; i++) {
            for(int j = 0; j < NUMBER_OF_COLS; j++) {
                mTiles[i][j] = new Tile();
            }
        }

        // Placing 4 pieces in the middle of the board:
        mTiles[NUMBER_OF_COLS/2 - 1][NUMBER_OF_COLS/2 - 1].setmOccupyingGamePiece(GamePiece.WHITE);
        mTiles[NUMBER_OF_COLS/2 - 1][NUMBER_OF_COLS/2].setmOccupyingGamePiece(GamePiece.BLACK);
        mTiles[NUMBER_OF_COLS/2][NUMBER_OF_COLS/2 - 1].setmOccupyingGamePiece(GamePiece.BLACK);
        mTiles[NUMBER_OF_COLS/2][NUMBER_OF_COLS/2].setmOccupyingGamePiece(GamePiece.WHITE);

        mNumberOfEmptyTiles = NUMBER_OF_TILES - 4;
        mNumberOfBlackPieces = 2;
        mNumberOfWhitePieces = 2;

    }

    // Copy Constructor:
    public Board(Board other) {

        // Copy tiles:
        for(int i = 0; i < NUMBER_OF_COLS; i++) {
            for(int j = 0; j < NUMBER_OF_COLS; j++) {
                mTiles[i][j] = new Tile();
                mTiles[i][j].setmOccupyingGamePiece(other.getTile(i,j).getmOccupyingGamePiece());
                mTiles[i][j].setmRecentlyFlipped(other.getTile(i,j).ismRecentlyFlipped());
            }
        }

        // Copy counters:
        mNumberOfEmptyTiles = other.getmNumberOfEmptyTiles();
        mNumberOfBlackPieces = other.getmNumberOfBlackPieces();
        mNumberOfWhitePieces = other.getmNumberOfWhitePieces();

    }

    // Constructor for tutorial purposes:
    public Board(int phaseNumber, boolean isBasic) {

        // Initializing the array of tiles:
        for(int i = 0; i < NUMBER_OF_COLS; i++) {
            for(int j = 0; j < NUMBER_OF_COLS; j++) {
                mTiles[i][j] = new Tile();
            }
        }

        // If it's a basic tutorial:
        if(isBasic){

            if(phaseNumber == 4){
                int k=0;
                for(int i = 0; i <NUMBER_OF_COLS; i++){
                    for(int j = 0; j < NUMBER_OF_COLS; j++){
                        if(k < 51)  mTiles[i][j].setmOccupyingGamePiece(GamePiece.BLACK);
                        else mTiles[i][j].setmOccupyingGamePiece(GamePiece.WHITE);
                        k++;
                    }
                }
                mNumberOfEmptyTiles = 0;
                mNumberOfBlackPieces = 51;
                mNumberOfWhitePieces = 13;
            }

            if(phaseNumber == 7){
                mTiles[2][3].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[3][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mNumberOfEmptyTiles = NUMBER_OF_TILES - 4;
                mNumberOfBlackPieces = 1;
                mNumberOfWhitePieces = 3;
            }

            if(phaseNumber == 8){
                mTiles[1][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[4][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[0][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][3].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[1][6].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][4].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mNumberOfEmptyTiles = NUMBER_OF_TILES - 12;
                mNumberOfBlackPieces = 4;
                mNumberOfWhitePieces = 8;
            }

        }

        // If it's an advanced tutorial:
        else {

            if(phaseNumber == 5){
                mTiles[2][3].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][2].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][6].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[1][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][4].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][4].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[6][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[6][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[7][4].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[7][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mNumberOfEmptyTiles = NUMBER_OF_TILES - 15;
                mNumberOfBlackPieces = 3;
                mNumberOfWhitePieces = 12;
            }

            else if(phaseNumber == 8){
                mTiles[0][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[0][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mNumberOfEmptyTiles = NUMBER_OF_TILES - 4;
                mNumberOfBlackPieces = 4;
                mNumberOfWhitePieces = 0;
            }

            else if(phaseNumber == 9){
                for(int i = 0; i <NUMBER_OF_COLS; i++){
                    for(int j = 0; j < NUMBER_OF_COLS; j++){
                        mTiles[i][j].setmOccupyingGamePiece(GamePiece.WHITE);
                    }
                }
                mTiles[4][4].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[0][0].setmOccupyingGamePiece(null);
                mTiles[0][7].setmOccupyingGamePiece(null);
                mTiles[7][0].setmOccupyingGamePiece(null);
                mTiles[7][7].setmOccupyingGamePiece(null);
                mNumberOfEmptyTiles = 4;
                mNumberOfBlackPieces = 1;
                mNumberOfWhitePieces = 59;
            }

            else if(phaseNumber == 12){
                mTiles[4][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[5][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[5][1].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[2][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mNumberOfEmptyTiles = NUMBER_OF_TILES - 10;
                mNumberOfBlackPieces = 3;
                mNumberOfWhitePieces = 7;
            }

            else if(phaseNumber == 17 || phaseNumber == 18){
                mTiles[7][0].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][1].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][2].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][3].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][4].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][5].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][6].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[7][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][2].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][3].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][4].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][5].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][6].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[6][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[5][4].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[5][5].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[5][6].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[5][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[4][3].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[4][4].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[4][5].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[4][6].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[4][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[3][4].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[3][7].setmOccupyingGamePiece(GamePiece.BLACK);
                mTiles[2][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][4].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][6].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[2][7].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][5].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[3][6].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[4][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][0].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][1].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][2].setmOccupyingGamePiece(GamePiece.WHITE);
                mTiles[5][3].setmOccupyingGamePiece(GamePiece.WHITE);
                mNumberOfEmptyTiles = NUMBER_OF_TILES - 40;
                mNumberOfBlackPieces = 25;
                mNumberOfWhitePieces = 15;
            }

            else{
                // Return an empty board:
                mNumberOfEmptyTiles = NUMBER_OF_TILES;
                mNumberOfBlackPieces = 0;
                mNumberOfWhitePieces = 0;
            }


        }

    }


    public boolean addGamePiece(int positionI, int positionJ, GamePiece gamePiece){

        // Checking if the tile is already occupied:
        if(mTiles[positionI][positionJ].getmOccupyingGamePiece() != null){
            return false;
        }

        refreshTiles();

        // Placing the piece:
        mTiles[positionI][positionJ].setmOccupyingGamePiece(gamePiece);
        increasePieceAmountByOne(gamePiece);
        mNumberOfEmptyTiles--;

        // Flipping opponent's pieces:

        // Going through all the rows and cols with tiles that might surround the chosen spot:
        for(int i = positionI-1; i <= positionI+1; i++){
            for(int j = positionJ-1; j <= positionJ+1; j++){

                // Now we need to make sure for each of these tiles
                // that it actually exists within the borders of the board:
                if(i>=0 && i < NUMBER_OF_COLS && j>=0 && j < NUMBER_OF_COLS){
                    //We also want to skip the chosen spot itself:
                    if(i!=positionI || j!=positionJ){

                        // If there is an opponent's piece, we continue to check tiles in this direction
                        // until we encounter a piece of the same type as the currently playing.
                        if(mTiles[i][j].getmOccupyingGamePiece() != gamePiece &&
                                mTiles[i][j].getmOccupyingGamePiece() != null){

                            int sequentialPositionI = i;
                            int sequentialPositionJ = j;

                            boolean continueChecking = true;

                            while(continueChecking) {

                                // Advancing the position in the correct direction:
                                if (i < positionI) sequentialPositionI--;
                                if (i > positionI) sequentialPositionI++;
                                if (j < positionJ) sequentialPositionJ--;
                                if (j > positionJ) sequentialPositionJ++;

                                // Make sure that the new position exists within the borders of the board:
                                if(sequentialPositionI >= 0 && sequentialPositionI < NUMBER_OF_COLS &&
                                        sequentialPositionJ >= 0 && sequentialPositionJ < NUMBER_OF_COLS){

                                    // If the tile is occupied by a piece of the currently playing type:
                                    if(mTiles[sequentialPositionI][sequentialPositionJ].getmOccupyingGamePiece() == gamePiece){

                                        // Flipping the tiles (the checkup is finished for this direction):
                                        while(sequentialPositionI != i || sequentialPositionJ != j){

                                            // Going back:
                                            if (i < positionI) sequentialPositionI++;
                                            if (i > positionI) sequentialPositionI--;
                                            if (j < positionJ) sequentialPositionJ++;
                                            if (j > positionJ) sequentialPositionJ--;

                                            // Flipping the piece:
                                            increasePieceAmountByOne(gamePiece);
                                            decreasePieceAmountByOne(mTiles[sequentialPositionI][sequentialPositionJ].getmOccupyingGamePiece());
                                            mTiles[sequentialPositionI][sequentialPositionJ].setmOccupyingGamePiece(gamePiece);
                                            mTiles[sequentialPositionI][sequentialPositionJ].setmRecentlyFlipped(true);

                                        }

                                        // We found and flipped all the pieces that needed to be flipped
                                        // (in this direction).
                                        continueChecking = false; //(the checkup is finished).
                                    }

                                    // If the tile is empty:
                                    else if (mTiles[sequentialPositionI][sequentialPositionJ].getmOccupyingGamePiece() == null) {
                                        // There are no pieces needed to be flipped (in this direction).
                                        continueChecking = false; //(the checkup is finished).
                                    }

                                    // Otherwise we found another opponent's piece and the checkup will simply continue.

                                }

                                // Otherwise there is no point to continue to check.
                                else {
                                    // There are no pieces needed to be flipped (in this direction).
                                    continueChecking = false; //(the checkup is finished).
                                }

                            }
                        }
                    }
                }
            }
        }

        return true; // The game piece has been placed successfully.

    }




    public ArrayList<Integer> getPlayableTiles (GamePiece gamePiece){

        // If the input is not a logical one:
        if(gamePiece == null)
            return null;


        // Preparing a set for the playable tiles (a collection without duplicates):
        HashSet<Integer> playableTiles = new HashSet<Integer>();

        // Going through all tiles:
        for(int positionI = 0; positionI < NUMBER_OF_COLS; positionI++) {
            for(int positionJ = 0; positionJ < NUMBER_OF_COLS; positionJ++) {

                // If we found a matching piece we can continue to check from that spot to all directions:
                if(mTiles[positionI][positionJ].getmOccupyingGamePiece() == gamePiece){

                    // Going through all the rows and cols with tiles that might surround the chosen spot:
                    for(int i = positionI-1; i <= positionI+1; i++) {
                        for (int j = positionJ - 1; j <= positionJ + 1; j++) {

                            // Now we need to make sure for each of these tiles
                            // that they actually exist within the borders of the board:
                            if (i >= 0 && i < NUMBER_OF_COLS && j >= 0 && j < NUMBER_OF_COLS) {
                                //We also want to skip the chosen spot itself:
                                if (i != positionI || j != positionJ) {

                                    // If there is an opponent's piece, we continue to check tiles in this direction
                                    // until we encounter an empty tile.
                                    if(mTiles[i][j].getmOccupyingGamePiece() != gamePiece &&
                                            mTiles[i][j].getmOccupyingGamePiece() != null){

                                        int sequentialPositionI = i;
                                        int sequentialPositionJ = j;

                                        boolean continueChecking = true;

                                        while(continueChecking) {

                                            // Advancing the position in the correct direction:
                                            if (i < positionI) sequentialPositionI--;
                                            if (i > positionI) sequentialPositionI++;
                                            if (j < positionJ) sequentialPositionJ--;
                                            if (j > positionJ) sequentialPositionJ++;

                                            // Make sure that the new position exists within the borders of the board:
                                            if(sequentialPositionI >= 0 && sequentialPositionI < NUMBER_OF_COLS &&
                                                    sequentialPositionJ >=0 && sequentialPositionJ < NUMBER_OF_COLS){

                                                // If the tile is empty (the checkup is finished for this direction):
                                                if(mTiles[sequentialPositionI][sequentialPositionJ].getmOccupyingGamePiece() == null){

                                                    // Adding this tile's position to the set:
                                                    playableTiles.add(sequentialPositionI*NUMBER_OF_COLS + sequentialPositionJ);

                                                    // We found a playable tile (in this direction).
                                                    continueChecking = false;

                                                }

                                                // If the tile has a piece of the same type as the currently playing:
                                                else if (mTiles[sequentialPositionI][sequentialPositionJ].getmOccupyingGamePiece() == gamePiece) {
                                                    // Playable tile was not found (in this direction).
                                                    continueChecking = false;
                                                }

                                                // Otherwise we found another opponents's piece and the checkup will simply continue.

                                            }

                                            // Otherwise there is no point to continue to check.
                                            else {
                                                // Playable tile was not found (in this direction).
                                                continueChecking = false;
                                            }

                                        }
                                    }


                                }
                            }

                        }
                    }


                }


            }
        }

        // Returning an ArrayList of the playable tiles' positions:
        return new ArrayList<Integer>(playableTiles);

    }





    // The following function is useful in case the apps include animations.
    // After all the recently-flipped tiles will finish to flip and the animation will end,
    // this function should be used somewhere before the next animation (relevant UI change)
    // will take place so the next animation would be a proper one.
    // [This function is used only in addGamePiece() before a new piece is added to the board].

    private void refreshTiles(){

        // Enough time has passed, so none of the tiles should be seen as "recently flipped":
        for(int i = 0; i < NUMBER_OF_COLS; i++) {
            for(int j = 0; j < NUMBER_OF_COLS; j++) {
                mTiles[i][j].setmRecentlyFlipped(false);
            }
        }

    }



    // The following 2 functions are used to add/remove piece type to/from the board:

    private void increasePieceAmountByOne(GamePiece gamePiece){
        if(gamePiece == GamePiece.BLACK)
            mNumberOfBlackPieces++;
        if(gamePiece == GamePiece.WHITE)
            mNumberOfWhitePieces++;
    }

    private void decreasePieceAmountByOne(GamePiece gamePiece){
        if(gamePiece == GamePiece.BLACK)
            mNumberOfBlackPieces--;
        if(gamePiece == GamePiece.WHITE)
            mNumberOfWhitePieces--;
    }


    // Getters:

    public static int getNumberOfCols() {
        return NUMBER_OF_COLS;
    }

    public static int getNumberOfTiles() {
        return NUMBER_OF_TILES;
    }

    public Tile getTile(int positionI, int positionJ) {
        return mTiles[positionI][positionJ];
    }

    public int getmNumberOfEmptyTiles() {
        return mNumberOfEmptyTiles;
    }

    public int getmNumberOfBlackPieces() {
        return mNumberOfBlackPieces;
    }

    public int getmNumberOfWhitePieces() {
        return mNumberOfWhitePieces;
    }


}
