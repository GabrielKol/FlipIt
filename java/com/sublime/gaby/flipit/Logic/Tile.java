package com.sublime.gaby.flipit.Logic;

/**
 * Created by Gaby on 2/8/2017.
 */


public class Tile {


    private boolean mRecentlyFlipped = false;
    private GamePiece mOccupyingGamePiece = null;


    // All methods are simply getters and setters:

    public boolean ismRecentlyFlipped() {
        return mRecentlyFlipped;
    }

    public void setmRecentlyFlipped(boolean mRecentlyFlipped) {
        this.mRecentlyFlipped = mRecentlyFlipped;
    }

    public GamePiece getmOccupyingGamePiece() {
        return mOccupyingGamePiece;
    }

    public void setmOccupyingGamePiece(GamePiece mOccupyingGamePiece) {
        this.mOccupyingGamePiece = mOccupyingGamePiece;
    }


}
