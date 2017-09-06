package com.sublime.gaby.flipit;


/**
 * Created by Gaby on 2/9/2017.
 */

public final class Constants {


    // Finals regarding shared preferences:

    public static final String DEFAULT = "N/A";
    public static final String OPTIONS_DATA = "OptionsData";
    public static final String CHOSEN_LANGUAGE = "ChosenLanguage";
    public static final String CHOSEN_SOUND_STATUS = "ChosenVoiceStatus";
    public static final String CHOSEN_VIBRATOR_STATUS = "ChosenVibratorStatus";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String[] LOCALES_ARRAY = {"en", "iw", "ru", "fr"};


    // Finals regarding durations:

    public static final long EMOTE_TIMEOUT = 6000; // 6 seconds
    public static final long EMOTE_DURATION = 5000; // 5 seconds
    public static final long EMOTE_POPUP_DURATION = 500; // half a second
    public static final long BOT_EMOTE_RESPONSE_DELAY = 2000; // 2 seconds

    public static final long FINALE_ANIMATION_DURATION = 1000; // 1 second

    public static final long VIBRATION_DURATION = 500; // half a second

    public static final long SHAKE_ANIMATION_DURATION = 1600; // 1.6 second
    public static final long SHAKE_TIMEOUT = 15000; // 15 seconds

    public static final long MAX_OPPONENT_SEARCH_TIME = 600000; // 10 minutes


    // Finals for Firebase database:

    public static final String PRIVATE_GAME_ROOMS = "PrivateGameRooms";
    public static final String QUICK_GAME_ROOMS = "QuickGameRooms";

    public static final String NUMBER_OF_ROOMS = "numberOfRooms";
    public static final String PASSWORD = "password";
    public static final String ARE_BOTH_CONNECTED = "areBothConnected";
    public static final String PLAYER_WHITE = "PlayerWhite";
    public static final String PLAYER_BLACK = "PlayerBlack";
    public static final String USER_ID = "userID";
    public static final String NAME = "name";
    public static final String PORTRAIT_URI_STRING = "portraitUriString";
    public static final String MOVE_POSITION = "movePosition";
    public static final String EMOTE_INDEX = "emoteIndex";
    public static final String IS_POKING = "isPoking";
    public static final String HAS_SURRENDERED = "hasSurrendered";
    public static final String IS_ASKING_FOR_REMATCH = "isAskingForRematch";

    public static final String NO_PICTURE = "No Picture";
    public static final String ROOM = "Room";


    // Ads:

    public static final double PROMO_AD_CHANCE = 0.2; // 20% chance


}
