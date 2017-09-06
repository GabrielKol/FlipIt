package com.sublime.gaby.flipit;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sublime.gaby.flipit.Logic.Difficulty;
import com.sublime.gaby.flipit.Logic.Game;
import com.sublime.gaby.flipit.Logic.GamePiece;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Created by Gaby on 2/18/2017.
 */

public class OfflineGameActivity extends AppCompatActivity {


    private GamePiece mPlayerGamePiece;
    private Difficulty mDifficulty;
    private Game mGame;

    private ArrayList<Integer> mPlayableTiles;
    private AnimatorSet mGuidelines;


    // Components:

    private GridView mGrid;
    private Button mShakeButton;
    private ListView mEmotePickerList;
    private Button mEmoteButton;
    private ImageView mDummy;


    private TextView mBlackCounter;
    private TextView mWhiteCounter;

    private LinearLayout mPlayerProfileArea;
    private LinearLayout mOpponentProfileArea;
    private TextView mPlayerUsername;
    private TextView mOpponentUsername;

    private TextView mPlayerEmoteBubble;
    private TextView mOpponentEmoteBubble;


    // MediaPlayer:

    private MediaPlayer mPutPieceSound;
    private MediaPlayer mFlipPieceSound;
    private MediaPlayer mPopSound;
    private MediaPlayer mShakeSound;

    private String mSoundStatus;
    private String mVibratorStatus;


    // Booleans:

    private boolean mIsAllowedToPlay = false;
    private boolean mIsEmotePlayable = true;
    private boolean mIsShakePlayable = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transition:
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Setting content view:
        setContentView(R.layout.activity_game);

        // Initialize sound media players:
        mPutPieceSound = MediaPlayer.create(this, R.raw.put_piece_sound);
        mFlipPieceSound = MediaPlayer.create(this, R.raw.flip_piece_sound);
        mPopSound = MediaPlayer.create(this, R.raw.pop_sound);
        mShakeSound = MediaPlayer.create(this, R.raw.shake_sound);

        // Extracting data from bundle:
        extractDataFromBundle();

        // Setting sound status and vibrator status:
        checkChosenOptions();

        // Creating a game:
        mGame = new Game();

        // Setting up the components:
        setupComponents();

        // Setting up the grid:
        setupGrid();

        // Setting up list for emotes:
        setupEmotePickerList();

        // Setting up remaining buttons:
        setupShakeButton();
        setupEmoteButton();
        setupReturnButton();
        setupRestartButton();

        // Starting the game:
        startGame();


    }




    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Getting the screen height and width:
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;

        // Setting a proper position for the emote picker list:
        ListView listView = (ListView) findViewById(R.id.emote_picker_list);
        listView.setY(height/2.2f);

    }

    private void checkChosenOptions(){

        // Using shared preferences:
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.OPTIONS_DATA, Context.MODE_PRIVATE);
        mSoundStatus = sharedPreferences.getString(Constants.CHOSEN_SOUND_STATUS, Constants.DEFAULT);
        mVibratorStatus = sharedPreferences.getString(Constants.CHOSEN_VIBRATOR_STATUS, Constants.DEFAULT);

    }



    private void extractDataFromBundle() {

        // Getting the intent and bundle:
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(MainActivity.PRACTICE_GAME_BUNDLE_KEY);

        mDifficulty = (Difficulty)  bundle.getSerializable(MainActivity.DIFFICULTY_KEY);
        mPlayerGamePiece = (GamePiece) bundle.getSerializable(MainActivity.GAME_PIECE_KEY);

    }


    private void setupComponents() {

        // Initializing components regarding profile areas for both players:
        mPlayerProfileArea = (LinearLayout) findViewById(R.id.profile_area_player);
        mPlayerUsername = (TextView) findViewById(R.id.username_player);
        mOpponentProfileArea = (LinearLayout) findViewById(R.id.profile_area_opponent);
        mOpponentUsername = (TextView) findViewById(R.id.username_opponent);

        // If player plays with black pieces (also starts the game):
        if(mPlayerGamePiece == GamePiece.BLACK){

            // Setting black and white counters:
            mBlackCounter = (TextView) findViewById(R.id.piece_counter_player);
            mWhiteCounter = (TextView) findViewById(R.id.piece_counter_opponent);

            // Setting player's profile area color to green:
            mPlayerProfileArea.setBackgroundResource(R.drawable.user_area_green);
            mPlayerUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);

        }

        // Otherwise:
        else{

            // Setting black and white counters:
            mWhiteCounter = (TextView) findViewById(R.id.piece_counter_player);
            mBlackCounter = (TextView) findViewById(R.id.piece_counter_opponent);

            // Setting opponents's profile area color to green:
            mOpponentProfileArea.setBackgroundResource(R.drawable.user_area_green);
            mOpponentUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);

        }

        // Setting colors for counters:
        mBlackCounter.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        mBlackCounter.setBackgroundResource(R.drawable.piece_counter_black);
        mWhiteCounter.setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
        mWhiteCounter.setBackgroundResource(R.drawable.piece_counter_white);

        // Setting bot name tag:
        if(mDifficulty == Difficulty.BEGINNER) mOpponentUsername.setText(R.string.bot_name_beginner);
        else if (mDifficulty == Difficulty.EASY) mOpponentUsername.setText(R.string.bot_name_easy);
        else if (mDifficulty == Difficulty.MEDIUM) mOpponentUsername.setText(R.string.bot_name_medium);
        else if (mDifficulty == Difficulty.HARD) mOpponentUsername.setText(R.string.bot_name_hard);
        else if (mDifficulty == Difficulty.EXPERT) mOpponentUsername.setText(R.string.bot_name_expert);

        // Setting emote bubbles components:
        mPlayerEmoteBubble = (TextView) findViewById(R.id.emote_player);
        mOpponentEmoteBubble = (TextView) findViewById(R.id.emote_opponent);

        mEmoteButton = (Button) findViewById(R.id.emote_button);

        mShakeButton = (Button) findViewById(R.id.shake_button);

    }


    private void startGame(){

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                // If player goes first, initialize the playable tiles and show guidelines:
                if(mPlayerGamePiece == mGame.getmCurrentlyPlaying()){
                    mPlayableTiles = mGame.getmBoard().getPlayableTiles(mPlayerGamePiece);
                    showGuidelines();
                    mIsAllowedToPlay = true;
                }
                // Otherwise, opponent begins:
                else{
                    mIsAllowedToPlay = false;
                    makeAIMove();
                }

            }
        }, 600); // Delay that gives enough time to initialize ImageView.

    }


    private void setupGrid() {

        // Getting the grid view:
        mGrid = (GridView) findViewById(R.id.grid_view);

        // Setting number of columns in the grid:
        mGrid.setNumColumns(mGame.getmBoard().getNumberOfCols());

        // Setting the adapter for the grid:
        mGrid.setAdapter(new TileAdapter(getApplicationContext(), mGame.getmBoard()));

        // Setting a click listener for the grid:
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Making sure the game hasn't ended and that its a proper timing to let the player make his turn.
                if (mGame.getmGameStatus() == Game.GameStatus.ONGOING && mIsAllowedToPlay) {

                    Log.d("Playable Tiles" , mPlayableTiles.toString());
                    Log.d("Position" , "" + position);

                    // If this tile is playable:
                    if (mPlayableTiles.contains(position)){

                        mIsAllowedToPlay = false; // Disabling the player to play for now.

                        if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start(); // Playing proper sound.

                        // Cancel Guidelines:
                        if(mGuidelines != null) {
                            mGuidelines.cancel();
                            mGuidelines = null;
                            // refresh tiles' color:
                            for(int i = 0; i < mGrid.getChildCount(); i++){
                                mGrid.getChildAt(i).setBackgroundResource(R.drawable.button_tile);
                            }
                        }

                        // Making the play and updating grid:
                        mGame.playTile(position/mGame.getmBoard().getNumberOfCols(),position%mGame.getmBoard().getNumberOfCols());
                        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

                        // Updating Counters:
                        mBlackCounter.setText("" + mGame.getmBoard().getmNumberOfBlackPieces());
                        mWhiteCounter.setText("" + mGame.getmBoard().getmNumberOfWhitePieces());

                        // While the UI changes, make a proper sound for flipping pieces (after a short delay):
                        // And then move on (update UI, let opponent play next, end game, etc.)
                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start(); // Playing proper sound.


                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // Now back to UI thread in order to update UI:
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        // If game is still ongoing:
                                        if(mGame.getmGameStatus() == Game.GameStatus.ONGOING){

                                            // If turn was toggled:
                                            if(mGame.getmCurrentlyPlaying() != mPlayerGamePiece) {

                                                // Updating colors for profile areas:
                                                mOpponentProfileArea.setBackgroundResource(R.drawable.user_area_green);
                                                mOpponentUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);
                                                mPlayerProfileArea.setBackgroundResource(R.drawable.user_area_gray);
                                                mPlayerUsername.setBackgroundResource(R.color.colorGrayDarkAlpha);

                                                // Now A.I. opponent should make a move:
                                                makeAIMove();

                                            }

                                            // Otherwise, player gets another turn:
                                            else{
                                                mPlayableTiles = mGame.getmBoard().getPlayableTiles(mPlayerGamePiece);
                                                showGuidelines();
                                                mIsAllowedToPlay = true;
                                            }

                                        }

                                        else{ // Otherwise, game has ended.
                                            handleGameEnding();
                                        }

                                    }
                                });


                            }

                        }).start();

                    }

                }

            }
        });

    }

    private void makeAIMove(){


        // Getting A.I.'s game peace type:
        final GamePiece opponentPiece = mGame.getmCurrentlyPlaying();

        // Creating new thread for A.I. move calculation, just in case the calculation would be too long.
        // (So the A.I. move calculation won't freeze the screen).
        new Thread(new Runnable() {

            @Override
            public void run() {

                // Making the play:
                mGame.makeAIMove(mDifficulty);

                // Now back to UI thread:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Updating the grid:
                        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

                        // Playing proper sound:
                        if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                        // Updating Counters:
                        mBlackCounter.setText("" + mGame.getmBoard().getmNumberOfBlackPieces());
                        mWhiteCounter.setText("" + mGame.getmBoard().getmNumberOfWhitePieces());

                        // Update playableTiles:
                        mPlayableTiles = mGame.getmBoard().getPlayableTiles(mPlayerGamePiece);

                        // While the UI changes, make a proper sound for flipping pieces (after a short delay):
                        // And then move on (update UI, let player play next, end game, etc.)
                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start(); // Playing proper sound.

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // Now back to UI thread in order to update UI:
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        // If game is still ongoing:
                                        if(mGame.getmGameStatus() == Game.GameStatus.ONGOING){

                                            // If turn was toggled:
                                            if(mGame.getmCurrentlyPlaying() != opponentPiece) {

                                                // Updating colors for profile areas:
                                                mPlayerProfileArea.setBackgroundResource(R.drawable.user_area_green);
                                                mPlayerUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);
                                                mOpponentProfileArea.setBackgroundResource(R.drawable.user_area_gray);
                                                mOpponentUsername.setBackgroundResource(R.color.colorGrayDarkAlpha);

                                                // Now Player should make a move:
                                                showGuidelines();
                                                mIsAllowedToPlay = true;

                                            }

                                            // Otherwise, opponent gets another turn:
                                            else{
                                                makeAIMove();
                                            }

                                        }

                                        else{ // Otherwise (game has ended)
                                            handleGameEnding();
                                        }

                                    }
                                });


                            }

                        }).start();


                    }
                });


            }
        }).start();

    }

    private void showGuidelines(){

        mGuidelines = new AnimatorSet();

        ArrayList<ObjectAnimator> animatorsList = new ArrayList<ObjectAnimator>();


        // Preparing an animation for each playable tile:

        for(Integer position : mPlayableTiles) {

            TileView tile = (TileView) mGrid.getChildAt(position);
            ObjectAnimator tileColorAnimator = ObjectAnimator.ofObject(tile,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    ContextCompat.getColor(this, R.color.colorGreen),
                    ContextCompat.getColor(this, R.color.colorGreenLightest));
            tileColorAnimator.setInterpolator(new DecelerateInterpolator());
            tileColorAnimator.setDuration(400);
            tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            animatorsList.add(tileColorAnimator);

        }

        // Starting the guidelines animation:
        mGuidelines.playTogether((Collection) animatorsList);
        mGuidelines.start();

    }



    private void handleGameEnding() {


        // Shadow screen animation:

        final ImageView shadowScreen = (ImageView) findViewById(R.id.shadow_screen);
        shadowScreen.setVisibility(View.VISIBLE);
        ObjectAnimator shadowScreenAnimator = ObjectAnimator.ofFloat(shadowScreen, "alpha", 0, 1);
        shadowScreenAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        shadowScreenAnimator.start();


        // Preparing Conclusion Text:

        final TextView conclusionText = (TextView) findViewById(R.id.conclusion_text);
        conclusionText.setVisibility(View.VISIBLE);
        conclusionText.setText(R.string.defeat_text);
        conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorRed));


        // If the player with the BLACK pieces won:
        if(mGame.getmGameStatus() == Game.GameStatus.PLAYER_BLACK_WON){

            // If Player Won:
            if(mPlayerGamePiece == GamePiece.BLACK){
                conclusionText.setText(R.string.victory_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorBlue));
                MediaPlayer.create(this, R.raw.victory_sound).start(); // Playing the proper sound.
            }
            // If Player Lost:
            else{
                conclusionText.setText(R.string.defeat_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
                MediaPlayer.create(this, R.raw.defeat_sound).start(); // Playing the proper sound.
                // Vibrate:
                if(mVibratorStatus.equals(Constants.ON)){
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(Constants.VIBRATION_DURATION);
                }
            }

        }

        // If the player WHITE the white pieces won:
        else if (mGame.getmGameStatus() == Game.GameStatus.PLAYER_WHITE_WON) {

            // If Player Won:
            if(mPlayerGamePiece == GamePiece.WHITE){
                conclusionText.setText(R.string.victory_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorBlue));
                MediaPlayer.create(this, R.raw.victory_sound).start(); // Playing the proper sound.
            }
            // If Player Lost:
            else{
                conclusionText.setText(R.string.defeat_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
                MediaPlayer.create(this, R.raw.defeat_sound).start(); // Playing the proper sound.
                // Vibrate:
                if(mVibratorStatus.equals(Constants.ON)){
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(Constants.VIBRATION_DURATION);
                }
            }

        }

        // If Draw:
        else {
            conclusionText.setText(R.string.draw_text);
            conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
            MediaPlayer.create(this, R.raw.victory_sound).start(); // Playing the proper sound.
        }


        // Conclusion Text alpha animation:

        ObjectAnimator conclusionTextAlphaAnimator = ObjectAnimator.ofFloat(conclusionText, "alpha", 0, 1);
        conclusionTextAlphaAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        conclusionTextAlphaAnimator.start();

        // Conclusion Text scale animations:

        float scaleX = conclusionText.getScaleX();
        ObjectAnimator conclusionTextScaleXAnimator = ObjectAnimator.ofFloat(conclusionText, "scaleX", 4*scaleX, scaleX);
        conclusionTextScaleXAnimator.setInterpolator(new OvershootInterpolator());
        conclusionTextScaleXAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        conclusionTextScaleXAnimator.start();

        float scaleY = conclusionText.getScaleY();
        ObjectAnimator conclusionTextScaleYAnimator = ObjectAnimator.ofFloat(conclusionText, "scaleY", 4*scaleY, scaleY);
        conclusionTextScaleYAnimator.setInterpolator(new OvershootInterpolator());
        conclusionTextScaleYAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        conclusionTextScaleYAnimator.start();



        // Back to Menu button:

        final Button returnButton = (Button) findViewById(R.id.return_button2);
        returnButton.setVisibility(View.VISIBLE);
        ObjectAnimator returnButtonAnimator = ObjectAnimator.ofFloat(returnButton, "alpha", 0, 1);
        returnButtonAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        returnButtonAnimator.start();


        // Setting a listener for the Play Again button:
        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start(); // Playing a proper sound.
                showReturnDialog(); // (same as the back button, and the other UI return button)
            }
        });



        // Play Again button:

        final Button playAgainButton = (Button) findViewById(R.id.play_again_button);
        playAgainButton.setVisibility(View.VISIBLE);
        ObjectAnimator playAgainButtonAnimator = ObjectAnimator.ofFloat(playAgainButton, "alpha", 0, 1);
        playAgainButtonAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        playAgainButtonAnimator.start();


        // Setting a listener for the Play Again button:
        playAgainButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Playing a proper sound:
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Creating a new game and reinitializing UI components:
                mGame = new Game();
                setupGrid();
                mBlackCounter.setText("2");
                mWhiteCounter.setText("2");
                startGame();

                // Making the "Finale Views" disappear:
                shadowScreen.setVisibility(View.GONE);
                conclusionText.setVisibility(View.GONE);
                returnButton.setVisibility(View.GONE);
                playAgainButton.setVisibility(View.GONE);

            }
        });


        // Preventing the user from touching anything behind the shadow screen:
        shadowScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        // Showing a promo ad by chance:
        showPromoAdByChance();

    }


    private void showPromoAdByChance() {

        if(new Random().nextDouble() <= Constants.PROMO_AD_CHANCE){

            // Setting up a promo ad:
            AdRequest adRequest = new AdRequest.Builder().build();
            final InterstitialAd interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId(getString(R.string.promo_ad_unit_id));
            interstitial.loadAd(adRequest);

            // Setting up the Interstitial Ad Listener:
            interstitial.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    if (interstitial.isLoaded()) {
                        interstitial.show();
                    }
                }
            });

        }

    }



    private void setupShakeButton() {

        // Setting listener for the button:
        mShakeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // If shake is playable at the moment:
                if(mIsShakePlayable) {

                    // Making button unavailable for a while:
                    mIsShakePlayable = false;
                    mShakeButton.setBackgroundResource(R.color.colorGrayDark);

                    // Animating the profile area:
                    showShakeAnimation(mOpponentProfileArea);

                    Handler EmoteAvailabilityHandler = new Handler();
                    EmoteAvailabilityHandler.postDelayed(new Runnable() {
                        public void run() {

                            // Making button available once again:
                            mShakeButton.setBackgroundResource(R.drawable.button_red);
                            mIsShakePlayable = true;

                        }
                    }, Constants.SHAKE_TIMEOUT);

                }

            }
        });

    }

    private void showShakeAnimation(View profileArea){

        // Playing the shake sound:
        if(mSoundStatus.equals(Constants.ON)) mShakeSound.start();

        final View finalProfileArea = profileArea;
        final int numberOfRotations = 30;
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(profileArea,
                "rotation", -3, 3);
        shakeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shakeAnimator.setRepeatCount(numberOfRotations - 1);
        shakeAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        shakeAnimator.setDuration(Constants.SHAKE_ANIMATION_DURATION / numberOfRotations);

        shakeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                finalProfileArea.setRotation(0);
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        shakeAnimator.start();

    }



    private void setupEmoteButton(){

        // Setting listener for the button:
        mEmoteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // If emotes are playable at the moment:
                if(mIsEmotePlayable) {
                    // Make emote picker list visible:
                    mEmotePickerList.setVisibility(View.VISIBLE);
                    mDummy.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void setupEmotePickerList(){

        // Getting arrays of emotes:
        final String[] emotesArray = getResources().getStringArray(R.array.emotes_array);
        final String[] botEmotesArray = getResources().getStringArray(R.array.bot_emotes_array);

        //Getting the listView:
        mEmotePickerList = (ListView) findViewById(R.id.emote_picker_list);

        //Setting the adapter for the listView:
        mEmotePickerList.setAdapter(new EmoteChoiceAdapter(getApplicationContext()));

        //Setting a click listener for the listView:
        mEmotePickerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Setting the right text in the bubble:
                mPlayerEmoteBubble.setText(emotesArray[position]);

                // Animating the bubble:
                showEmote(mPlayerEmoteBubble);

                // Making emotes unavailable for a while:
                mIsEmotePlayable = false;
                mEmoteButton.setBackgroundResource(R.color.colorGrayDark);



                Handler botEmoteHandler = new Handler();
                botEmoteHandler.postDelayed(new Runnable() {
                    public void run() {

                        // Animating A.I. emote bubble with random bot-like text to make it 'polite':
                        mOpponentEmoteBubble.setText(botEmotesArray[new Random().nextInt(botEmotesArray.length)]);
                        showEmote(mOpponentEmoteBubble);

                    }
                }, Constants.BOT_EMOTE_RESPONSE_DELAY);


                Handler EmoteAvailabilityHandler = new Handler();
                EmoteAvailabilityHandler.postDelayed(new Runnable() {
                    public void run() {

                        // Making emotes available once again:
                        mEmoteButton.setBackgroundResource(R.drawable.button_yellow);
                        mIsEmotePlayable = true;

                    }
                }, Constants.EMOTE_TIMEOUT);


                // Making emote picker list unavailable:
                mEmotePickerList.setVisibility(View.GONE);
                mDummy.setVisibility(View.GONE);

            }
        });


        // Setting up the dummy, so when the user clicks on the dummy, which is everywhere outside
        // of the list, the list will disappear:

        mDummy = (ImageView) findViewById(R.id.dummy);
        mDummy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEmotePickerList.setVisibility(View.GONE);
                mDummy.setVisibility(View.GONE);
                return true;
            }
        });


        // Making the list disappear (we don't want it to appear as the game starts).
        mEmotePickerList.setVisibility(View.GONE);
        mDummy.setVisibility(View.GONE);


    }



    private void showEmote(TextView emoteBubble){

        // Playing the 'pop' sound:
        if(mSoundStatus.equals(Constants.ON)) mPopSound.start();

        // Animating the bubble's alpha (fade in, fade out):

        ObjectAnimator emoteAlphaAnimator = ObjectAnimator.ofFloat(emoteBubble,
                "alpha", 0, 1);
        emoteAlphaAnimator.setInterpolator(new DecelerateInterpolator());
        emoteAlphaAnimator.setRepeatCount(1);
        emoteAlphaAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        emoteAlphaAnimator.setDuration(Constants.EMOTE_DURATION/2);


        // Animating the bubble's size (pop up with overshoot):

        float scaleX = emoteBubble.getScaleX();
        ObjectAnimator emoteScaleXAnimator = ObjectAnimator.ofFloat(emoteBubble,
                "scaleX", 0, scaleX);
        emoteScaleXAnimator.setInterpolator(new OvershootInterpolator());
        emoteScaleXAnimator.setDuration(Constants.EMOTE_POPUP_DURATION);

        float scaleY = emoteBubble.getScaleY();
        ObjectAnimator emoteScaleYAnimator = ObjectAnimator.ofFloat(emoteBubble,
                "scaleY", 0, scaleY);
        emoteScaleYAnimator.setInterpolator(new OvershootInterpolator());
        emoteScaleYAnimator.setDuration(Constants.EMOTE_POPUP_DURATION);


        // Start animations:

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(emoteAlphaAnimator, emoteScaleXAnimator, emoteScaleYAnimator);
        animatorSet.start();


    }

    private void setupReturnButton(){

        Button returnButton = (Button) findViewById(R.id.return_button);

        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showReturnDialog();

            }
        });

    }

    private void setupRestartButton(){

        Button restartButton = (Button) findViewById(R.id.concede_button);

        restartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showRestartDialog();

            }
        });
    }

    private void showReturnDialog(){

        // Creating a dialog and getting the right views:
        final Dialog verificationDialog = new Dialog(this, R.style.DialogTheme);
        verificationDialog.setContentView(R.layout.dialog_verification);
        TextView verificationText = (TextView) verificationDialog.findViewById(R.id.verification_text);
        Button cancelButton = (Button) verificationDialog.findViewById(R.id.cancel_button);
        Button acceptButton = (Button) verificationDialog.findViewById(R.id.accept_button);
        verificationDialog.show();

        verificationText.setText(R.string.verification_return);

        // Setting up a listener for the Cancel Button:
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                verificationDialog.dismiss();

            }
        });

        // Setting up a listener for the Done Button:
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                verificationDialog.dismiss();

                // Go back to main menu:

                // Creating an intent:
                Intent mainIntent = new Intent(OfflineGameActivity.this , MainActivity.class);
                // Starting the Main Activity:
                startActivity(mainIntent);

            }
        });

    }

    private void showRestartDialog(){

        // Creating a dialog and getting the right views:
        final Dialog verificationDialog = new Dialog(this, R.style.DialogTheme);
        verificationDialog.setContentView(R.layout.dialog_verification);
        TextView verificationText = (TextView) verificationDialog.findViewById(R.id.verification_text);
        Button cancelButton = (Button) verificationDialog.findViewById(R.id.cancel_button);
        Button acceptButton = (Button) verificationDialog.findViewById(R.id.accept_button);
        verificationDialog.show();

        verificationText.setText(R.string.verification_restart);

        // Setting up a listener for the Cancel Button:
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                verificationDialog.dismiss();

            }
        });

        // Setting up a listener for the Done Button:
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Creating a new game and reinitializing UI components:
                mGame = new Game();
                setupGrid();
                mBlackCounter.setText("2");
                mWhiteCounter.setText("2");
                startGame();

                // Dismiss dialog:
                verificationDialog.dismiss();

                // Showing a promo ad by chance:
                showPromoAdByChance();

            }
        });

    }





    @Override
    public void onBackPressed() {

        showReturnDialog(); // (same as the UI's return button)

    }






}
