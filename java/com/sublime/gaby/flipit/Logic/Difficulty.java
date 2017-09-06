package com.sublime.gaby.flipit.Logic;

import java.io.Serializable;

/**
 * Created by Gaby on 2/28/2017.
 */

// This is valid only for games versus A.I. :

public enum Difficulty implements Serializable {
    BEGINNER, EASY, MEDIUM, HARD, EXPERT;

    @Override
    public String toString() {
        switch(this) {

            case BEGINNER:
                return "Beginner";
            case EASY:
                return "Easy";
            case MEDIUM:
                return "Medium";
            case HARD:
                return "Hard";
            case EXPERT:
                return "Expert";
            default:
                return "None";

        }
    }
}
