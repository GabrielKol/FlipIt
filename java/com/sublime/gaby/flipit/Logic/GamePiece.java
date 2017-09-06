package com.sublime.gaby.flipit.Logic;

import java.io.Serializable;

/**
 * Created by Gaby on 2/9/2017.
 */


public enum GamePiece implements Serializable {
    BLACK, WHITE;

    @Override
    public String toString() {
        switch(this) {

            case BLACK:
                return "Black";
            case WHITE:
                return "White";
            default:
                return "None";

        }
    }
}
