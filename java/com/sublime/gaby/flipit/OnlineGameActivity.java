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
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.Toast;

import com.sublime.gaby.flipit.Logic.Game;
import com.sublime.gaby.flipit.Logic.GamePiece;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Created by Gaby on 3/27/2017.
 */

public class OnlineGameActivity extends AppCompatActivity {


    // Field used by MainActivity:
    public static final String NO_OPPONENT_KEY = "NoOpponent";

    // Firebase:
    private DatabaseReference mRoomRef;
    private DatabaseReference mPlayerRef;
    private DatabaseReference mOpponentRef;
    private ValueEventListener mOpponentNameListener;
    private ValueEventListener mOpponentPortraitListener;
    private ValueEventListener mOpponentMovePositionListener;
    private ValueEventListener mOpponentEmoteIndexListener;
    private ValueEventListener mOpponentPokeListener;
    private ValueEventListener mOpponentHasSurrenderedListener;
    private ValueEventListener mRematchListener;
    private boolean mHasOpponentLeft = false;
    private boolean mHasPlayerLeft = false; // to prevent memory leak.
    private boolean mIsOpponentAskingForRematch = false;
    private boolean mIsPlayerAskingForRematch = false;

    private String mRoomName;
    private String mOpponentName = "";


    // Game related:

    private GamePiece mPlayerGamePiece;
    private GamePiece mOpponentGamePiece;
    private Game mGame;

    private ArrayList<Integer> mPlayableTiles;
    private AnimatorSet mGuidelines;


    // Components:

    private GridView mGrid;
    private ListView mEmotePickerList;
    private Button mEmoteButton;
    private Button mShakeButton;
    private ImageView mDummy;


    private TextView mBlackCounter;
    private TextView mWhiteCounter;

    private LinearLayout mPlayerProfileArea;
    private LinearLayout mOpponentProfileArea;
    private TextView mPlayerUsername;
    private TextView mOpponentUsername;

    private TextView mPlayerEmoteBubble;
    private TextView mOpponentEmoteBubble;

    // Fields regarding opponent search (if this is a quick match):
    private Dialog mOpponentSearchDialog;
    private TextView mTimerText;
    private CountDownTimer mTimer;
    private long mTimePassed = 0;
    private boolean mIsPaused;
    private boolean mOpponentNotFound = false;


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
        setupConcedeButton();

        // Setting up a listener for opponent's actions:
        setupOpponentListeners();

        // Starting the game:
        // (unless the player is the room creator, which in that case he needs to wait for the opponent).
        if(mPlayerGamePiece == GamePiece.WHITE)
            startGame();
        else
            mIsPlayerAskingForRematch = true;

        // Removing the room from the real-time database when player disconnects from the room:
        // (Note that we'll also have to cancel that in case the opponent already disconnected,
        // because if other 2 players will create a room with the same name and start playing,
        // then in that case we don't want to erase their room).
        mRoomRef.onDisconnect().removeValue();

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



    private void checkChosenOptions() {

        // Using shared preferences:
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.OPTIONS_DATA, Context.MODE_PRIVATE);
        mSoundStatus = sharedPreferences.getString(Constants.CHOSEN_SOUND_STATUS, Constants.DEFAULT);
        mVibratorStatus = sharedPreferences.getString(Constants.CHOSEN_VIBRATOR_STATUS, Constants.DEFAULT);

    }


    private void extractDataFromBundle() {

        // Getting the intent:
        Intent intent = getIntent();

        // If private match:
        if(intent.hasExtra(GameRoomActivity.PRIVATE_MATCH_BUNDLE_KEY)){

            // Getting the bundle:
            Bundle bundle = intent.getBundleExtra(GameRoomActivity.PRIVATE_MATCH_BUNDLE_KEY);

            mRoomName = bundle.getString(GameRoomActivity.ROOM_NAME_KEY);
            mPlayerGamePiece = (GamePiece) bundle.getSerializable(GameRoomActivity.PIECE_TYPE_KEY);

            // Setting mRoomRef:
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            mRoomRef = rootRef.child(Constants.PRIVATE_GAME_ROOMS).child(mRoomName);

        }

        // If quick match:
        else{

            // Show loading dialog:
            showOpponentSearchDialog();

            // Getting the bundle:
            Bundle bundle = intent.getBundleExtra(MainActivity.QUICK_MATCH_BUNDLE_KEY);

            mRoomName = bundle.getString(MainActivity.ROOM_NAME_KEY);
            mPlayerGamePiece = (GamePiece) bundle.getSerializable(MainActivity.PIECE_TYPE_KEY);

            // Setting mRoomRef:
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            mRoomRef = rootRef.child(Constants.QUICK_GAME_ROOMS).child(mRoomName);

        }

        // Setting mOpponentGamePiece, mPlayerRef and mOpponentRef:
        if(mPlayerGamePiece == GamePiece.BLACK) {
            mOpponentGamePiece = GamePiece.WHITE;
            mPlayerRef = mRoomRef.child(Constants.PLAYER_BLACK);
            mOpponentRef = mRoomRef.child(Constants.PLAYER_WHITE);
        }
        else {
            mOpponentGamePiece = GamePiece.BLACK;
            mPlayerRef = mRoomRef.child(Constants.PLAYER_WHITE);
            mOpponentRef = mRoomRef.child(Constants.PLAYER_BLACK);
        }

    }


    private void showOpponentSearchDialog() {

        // Creating a loading dialog and getting the right views:
        mOpponentSearchDialog = new Dialog(this, R.style.DialogTheme);
        mOpponentSearchDialog.setContentView(R.layout.dialog_opponent_search);
        mOpponentSearchDialog.setCanceledOnTouchOutside(false);
        mOpponentSearchDialog.setCancelable(false);
        mTimerText = (TextView) mOpponentSearchDialog.findViewById(R.id.waiting_timer_text);
        Button stopWaitingButton = (Button) mOpponentSearchDialog.findViewById(R.id.stop_waiting_button);
        mOpponentSearchDialog.show();

        // Setting up and starting the timer:
        setupTimer();
        mTimer.start();

        // Setting a listener for the button:
        stopWaitingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Playing a proper sound:
                if (mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Shwing the return dialog:
                showReturnDialog();

            }
        });

    }

    private void setupTimer() {

        mTimer = new CountDownTimer(Constants.MAX_OPPONENT_SEARCH_TIME - mTimePassed, 1000) {

            @Override
            public void onTick(long millisUntilFinished) { //Ticks every second.

                mTimePassed += 1000;
                int seconds = (int) (mTimePassed / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                //Updating the text presented to the user as a timer (add one second):
                mTimerText.setText(String.format("%02d:%02d", minutes, seconds));

            }

            @Override
            public void onFinish() {

                // Closing loading dialog:
                if (mOpponentSearchDialog != null)
                    mOpponentSearchDialog.dismiss();

                // cancel onDisconnect listener (to prevent deleting the room a second time):
                mRoomRef.onDisconnect().cancel();

                // Delete the room from Firebase database:
                mRoomRef.removeValue();

                // If activity is not paused:
                if(!mIsPaused) {
                    // Go back to main menu:
                    Intent mainIntent = new Intent(OnlineGameActivity.this, MainActivity.class);
                    mainIntent.putExtra(NO_OPPONENT_KEY, new Bundle());
                    startActivity(mainIntent);
                    finish();
                }
                else mOpponentNotFound = true;

            }

        };

    }

    private void setupComponents() {

        // Initializing components regarding profile areas for both players:
        mPlayerProfileArea = (LinearLayout) findViewById(R.id.profile_area_player);
        mPlayerUsername = (TextView) findViewById(R.id.username_player);
        mOpponentProfileArea = (LinearLayout) findViewById(R.id.profile_area_opponent);
        mOpponentUsername = (TextView) findViewById(R.id.username_opponent);

        // If player plays with black pieces (also starts the game):
        if (mPlayerGamePiece == GamePiece.BLACK) {

            // Setting black and white counters:
            mBlackCounter = (TextView) findViewById(R.id.piece_counter_player);
            mWhiteCounter = (TextView) findViewById(R.id.piece_counter_opponent);

            // Setting player's profile area color to green:
            mPlayerProfileArea.setBackgroundResource(R.drawable.user_area_green);
            mPlayerUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);

        }

        // Otherwise:
        else {

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

        // Setting the concede button look:
        Button concedeButton = (Button) findViewById(R.id.concede_button);
        concedeButton.setBackgroundResource(R.drawable.flag_icon);

        // Setting player's name tag:
        FirebaseApp app = FirebaseApp.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance(app);
        mPlayerUsername.setText(auth.getCurrentUser().getDisplayName());

        // Setting player's portrait:
        Uri playerProfileUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        ImageView playerPortrait = (ImageView) findViewById(R.id.profile_pic_player);

        // Set portrait with real profile picture only if there is one:
        if(playerProfileUrl != null)
            Picasso.with(this).load(playerProfileUrl).into(playerPortrait);

        // Setting opponent's temporary portrait and name:
        ImageView opponentPortrait = (ImageView) findViewById(R.id.profile_pic_opponent);
        opponentPortrait.setImageResource(R.drawable.noob_icon_red);
        mOpponentUsername.setText(R.string.temp_opponent_name);

        // Setting emote bubbles components:
        mPlayerEmoteBubble = (TextView) findViewById(R.id.emote_player);
        mOpponentEmoteBubble = (TextView) findViewById(R.id.emote_opponent);

        mEmoteButton = (Button) findViewById(R.id.emote_button);

        mShakeButton = (Button) findViewById(R.id.shake_button);

    }


    @Override
    protected void onPause() {
        super.onPause();

        // Removing listeners (besides name listener):
        // [Also not stopping the timer]

        if(mOpponentPortraitListener != null) {
            mOpponentRef.removeEventListener(mOpponentPortraitListener);
            mOpponentRef.child(Constants.PORTRAIT_URI_STRING).removeEventListener(mOpponentPortraitListener);
        }

        if(mOpponentMovePositionListener != null) {
            mOpponentRef.removeEventListener(mOpponentMovePositionListener);
            mOpponentRef.child(Constants.MOVE_POSITION).removeEventListener(mOpponentMovePositionListener);
        }

        if(mOpponentPokeListener != null) {
            mOpponentRef.removeEventListener(mOpponentPokeListener);
            mOpponentRef.child(Constants.IS_POKING).removeEventListener(mOpponentPokeListener);
        }

        if(mOpponentEmoteIndexListener != null) {
            mOpponentRef.removeEventListener(mOpponentEmoteIndexListener);
            mOpponentRef.child(Constants.EMOTE_INDEX).removeEventListener(mOpponentEmoteIndexListener);
        }

        if(mOpponentHasSurrenderedListener != null) {
            mOpponentRef.removeEventListener(mOpponentHasSurrenderedListener);
            mOpponentRef.child(Constants.HAS_SURRENDERED).removeEventListener(mOpponentHasSurrenderedListener);
        }

        if(mRematchListener != null) {
            mOpponentRef.removeEventListener(mRematchListener);
            mOpponentRef.child(Constants.IS_ASKING_FOR_REMATCH).removeEventListener(mRematchListener);

        }

        mIsPaused = true;

    }



    @Override
    protected void onResume() {
        super.onResume();

        // If opponent not found:
        if(mOpponentNotFound) {
            // Go back to main menu:
            Intent mainIntent = new Intent(OnlineGameActivity.this, MainActivity.class);
            mainIntent.putExtra(NO_OPPONENT_KEY, new Bundle());
            startActivity(mainIntent);
        }

        else {

            // Adding the listeners (besides name listener):

            if (mOpponentPortraitListener != null)
                mOpponentRef.child(Constants.PORTRAIT_URI_STRING).addValueEventListener(mOpponentPortraitListener);

            if (mOpponentMovePositionListener != null)
                mOpponentRef.child(Constants.MOVE_POSITION).addValueEventListener(mOpponentMovePositionListener);

            if (mOpponentPokeListener != null)
                mOpponentRef.child(Constants.IS_POKING).addValueEventListener(mOpponentPokeListener);

            if (mOpponentEmoteIndexListener != null)
                mOpponentRef.child(Constants.EMOTE_INDEX).addValueEventListener(mOpponentEmoteIndexListener);

            if (mOpponentHasSurrenderedListener != null)
                mOpponentRef.child(Constants.HAS_SURRENDERED).addValueEventListener(mOpponentHasSurrenderedListener);

            if (mRematchListener != null)
                mOpponentRef.child(Constants.IS_ASKING_FOR_REMATCH).addValueEventListener(mRematchListener);

            mIsPaused = false;

        }

    }





    private void setupOpponentListeners(){


        // Setting up the opponent name listener:
        mOpponentNameListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If exists:
                if(dataSnapshot.exists()) {

                    // Getting the opponent's name:
                    String name = dataSnapshot.getValue(String.class);

                    // If opponent username is not already set:
                    if (!name.equals("")) {

                        // Canceling timer and closing loading dialog (if its a quick match):

                        if(mTimer != null){
                            mTimer.cancel();
                            mTimer = null;
                        }

                        if (mOpponentSearchDialog != null)
                            mOpponentSearchDialog.dismiss();

                        // Setting opponent name:
                        mOpponentName = name;
                        mOpponentUsername.setText(name);

                        // If the player is the creator, show toast that the opponent is now connected:
                        if (mPlayerGamePiece == GamePiece.BLACK)
                            Toast.makeText(OnlineGameActivity.this, R.string.opponent_connected, Toast.LENGTH_LONG).show();

                        // Remove listener:
                        mOpponentRef.removeEventListener(this);
                        mOpponentRef.child(Constants.NAME).removeEventListener(this);
                        mOpponentNameListener = null;

                    }

                }

                // Otherwise, if data doesn't exist:
                else{

                    // Opponent has left.

                    if(!mHasOpponentLeft) {
                        mRoomRef.onDisconnect().cancel();
                        mHasOpponentLeft = true;
                        if (!mHasPlayerLeft) showOpponentLeftDialog();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };

        // Adding the listener:
        mOpponentRef.child(Constants.NAME).addValueEventListener(mOpponentNameListener);


        // Setting up the opponent portrait listener:
        mOpponentPortraitListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If exists:
                if(dataSnapshot.exists()) {

                    // Getting the opponent's picture Uri String:
                    String profileUrlString = dataSnapshot.getValue(String.class);

                    // If opponent entered the room:
                    if (!profileUrlString.equals("")) {

                        // If there is a real profile picture:
                        if(!profileUrlString.equals(Constants.NO_PICTURE)) {

                            // Set portrait with real profile picture:
                            ImageView portraitView = (ImageView) findViewById(R.id.profile_pic_opponent);
                            Picasso.with(OnlineGameActivity.this).load(Uri.parse(profileUrlString)).into(portraitView);
                        }

                        // Remove listener:
                        mOpponentRef.removeEventListener(this);
                        mOpponentRef.child(Constants.PORTRAIT_URI_STRING).removeEventListener(this);
                        mOpponentPortraitListener = null;

                    }

                }

                // Otherwise, if data doesn't exist:
                else{

                    // Opponent has left.

                    if(!mHasOpponentLeft) {
                        mRoomRef.onDisconnect().cancel();
                        mHasOpponentLeft = true;
                        if (!mHasPlayerLeft) showOpponentLeftDialog();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };


        // Setting up the mOpponentHasSurrenderedListener:
        mOpponentHasSurrenderedListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If exists:
                if(dataSnapshot.exists()){

                    // Getting hasSurrendered from Firebase database:
                    boolean hasSurrendered = dataSnapshot.getValue(Boolean.class);

                    // If opponent surrendered:
                    if (hasSurrendered){

                        // Update Firebase database:
                        mOpponentRef.child(Constants.HAS_SURRENDERED).setValue(false);

                        // End Game:
                        if(mPlayerGamePiece == GamePiece.BLACK)
                            mGame.setmGameStatus(Game.GameStatus.PLAYER_BLACK_WON);
                        else
                            mGame.setmGameStatus(Game.GameStatus.PLAYER_WHITE_WON);

                        // Handle game's ending:
                        handleGameEnding();

                    }

                }

                // Otherwise, if data doesn't exist:
                else{

                    // Opponent has left.

                    if(!mHasOpponentLeft) {
                        mRoomRef.onDisconnect().cancel();
                        mHasOpponentLeft = true;
                        if (!mHasPlayerLeft) showOpponentLeftDialog();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };


        // Setting up the opponent poke listener:
        mOpponentPokeListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If exists:
                if(dataSnapshot.exists()){

                    // Getting the poke state:
                    boolean isPoking = dataSnapshot.getValue(Boolean.class);

                    // If poked:
                    if (isPoking) {

                        // Vibrate:
                        if (mVibratorStatus.equals(Constants.ON)) {
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(Constants.SHAKE_ANIMATION_DURATION);
                        }

                        // Showing the poke animation:
                        showShakeAnimation(mPlayerProfileArea);

                        // Updating Firebase database with default opponent emote index:
                        mOpponentRef.child(Constants.IS_POKING).setValue(false);

                    }

                }

                // Otherwise, if data doesn't exist:
                else{

                    // Opponent has left.

                    if(!mHasOpponentLeft) {
                        mRoomRef.onDisconnect().cancel();
                        mHasOpponentLeft = true;
                        if (!mHasPlayerLeft) showOpponentLeftDialog();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };


        // Setting up the opponent emote index listener:
        mOpponentEmoteIndexListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If exists:
                if(dataSnapshot.exists()){

                    // Getting the emote index:
                    int emoteIndex = dataSnapshot.getValue(Integer.class);

                    // If it's not the default position:
                    if (emoteIndex != -1) {

                        // Updating the bubble with the right text:
                        final String[] emotesArray = getResources().getStringArray(R.array.emotes_array);
                        mOpponentEmoteBubble.setText(emotesArray[emoteIndex]);

                        // Showing the emote:
                        showEmote(mOpponentEmoteBubble);

                        // Updating Firebase database with default opponent emote index:
                        mOpponentRef.child(Constants.EMOTE_INDEX).setValue(-1);

                    }

                }

                // Otherwise, if data doesn't exist:
                else{

                    // Opponent has left.

                    if(!mHasOpponentLeft) {
                        mRoomRef.onDisconnect().cancel();
                        mHasOpponentLeft = true;
                        if (!mHasPlayerLeft) showOpponentLeftDialog();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };


        // Setting up the opponent move position listener:
        mOpponentMovePositionListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If exists:
                if(dataSnapshot.exists()) {

                    // Getting the move position:
                    int position = dataSnapshot.getValue(Integer.class);

                    // If not default position:
                    if (position != -1) {

                        // If opponent made a move
                        if (mGame.getmBoard().getTile(position / mGame.getmBoard().getNumberOfCols(), position % mGame.getmBoard().getNumberOfCols()).getmOccupyingGamePiece() == null) {

                            // Making the play and updating the grid:
                            mGame.playTile(position / mGame.getmBoard().getNumberOfCols(), position % mGame.getmBoard().getNumberOfCols());
                            ((TileAdapter) mGrid.getAdapter()).notifyDataSetChanged();

                            // Playing proper sound:
                            if (mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

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

                                    if (mSoundStatus.equals(Constants.ON))
                                        mFlipPieceSound.start(); // Playing proper sound.

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
                                            if (mGame.getmGameStatus() == Game.GameStatus.ONGOING) {

                                                // If turn was toggled:
                                                if (mGame.getmCurrentlyPlaying() != mOpponentGamePiece) {

                                                    // Updating colors for profile areas:
                                                    mPlayerProfileArea.setBackgroundResource(R.drawable.user_area_green);
                                                    mPlayerUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);
                                                    mOpponentProfileArea.setBackgroundResource(R.drawable.user_area_gray);
                                                    mOpponentUsername.setBackgroundResource(R.color.colorGrayDarkAlpha);

                                                    // Now Player should make a move:
                                                    showGuidelines();
                                                    mIsAllowedToPlay = true;

                                                }

                                            }

                                            // Otherwise (game has ended)
                                            else handleGameEnding();

                                        }
                                    });

                                }

                            }).start();

                        }

                        // Updating Firebase database with opponent default move:
                        mOpponentRef.child(Constants.MOVE_POSITION).setValue(-1);

                    }

                }

                // Otherwise, if data doesn't exist:
                else{

                    // Opponent has left.

                    if(!mHasOpponentLeft) {
                        mRoomRef.onDisconnect().cancel();
                        mHasOpponentLeft = true;
                        if (!mHasPlayerLeft) showOpponentLeftDialog();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        };


        // If the player starts the game, he'll need to wait for the other opponent to be ready before
        // he is allowed to make the first move.
        if(mPlayerGamePiece == GamePiece.BLACK) {

            // Setting up the rematch listener:
            mRematchListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // If exists:
                    if (dataSnapshot.exists()) {

                        // Getting isAskingForRematch from Firebase database:
                        boolean isAskingForRematch = dataSnapshot.getValue(Boolean.class);

                        // If opponent wants a rematch:
                        if (isAskingForRematch) {

                            if(mIsPlayerAskingForRematch){
                                startGame();
                                mIsPlayerAskingForRematch = false;
                            }
                            else mIsOpponentAskingForRematch = true;

                            mOpponentRef.child(Constants.IS_ASKING_FOR_REMATCH).setValue(false);

                        }

                    }

                    // Otherwise, if data doesn't exist:
                    else {

                        // Opponent has left.

                        if (!mHasOpponentLeft) {
                            mRoomRef.onDisconnect().cancel();
                            mHasOpponentLeft = true;
                            if (!mHasPlayerLeft) showOpponentLeftDialog();
                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            };

        }


    }



    private void startGame() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                // If player goes first, initialize the playable tiles and show guidelines:
                if (mPlayerGamePiece == mGame.getmCurrentlyPlaying()) {
                    mPlayableTiles = mGame.getmBoard().getPlayableTiles(mPlayerGamePiece);
                    showGuidelines();
                    mIsAllowedToPlay = true;
                }

                // Otherwise, opponent begins:
                else mIsAllowedToPlay = false;


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

                    Log.d("Playable Tiles", mPlayableTiles.toString());
                    Log.d("Position", "" + position);

                    // If this tile is playable:
                    if (mPlayableTiles.contains(position)) {

                        mIsAllowedToPlay = false; // Disabling the player to play for now.

                        if (mSoundStatus.equals(Constants.ON))
                            mPutPieceSound.start(); // Playing proper sound.

                        // Cancel Guidelines:
                        if (mGuidelines != null) {
                            mGuidelines.cancel();
                            mGuidelines = null;
                            // Refresh tiles' color:
                            for (int i = 0; i < mGrid.getChildCount(); i++) {
                                mGrid.getChildAt(i).setBackgroundResource(R.drawable.button_tile);
                            }
                            Log.d("Guidelines", "have been canceled.");
                        }

                        // Making the play and updating grid:
                        mGame.playTile(position / mGame.getmBoard().getNumberOfCols(), position % mGame.getmBoard().getNumberOfCols());
                        ((TileAdapter) mGrid.getAdapter()).notifyDataSetChanged();

                        // Update Firebase database with the move:
                        mPlayerRef.child(Constants.MOVE_POSITION).setValue(position);

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

                                if (mSoundStatus.equals(Constants.ON))
                                    mFlipPieceSound.start(); // Playing proper sound.


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
                                        if (mGame.getmGameStatus() == Game.GameStatus.ONGOING) {

                                            // If turn was toggled:
                                            if (mGame.getmCurrentlyPlaying() != mPlayerGamePiece) {

                                                // Updating colors for profile areas:
                                                mOpponentProfileArea.setBackgroundResource(R.drawable.user_area_green);
                                                mOpponentUsername.setBackgroundResource(R.color.colorGreenDarkAlpha);
                                                mPlayerProfileArea.setBackgroundResource(R.drawable.user_area_gray);
                                                mPlayerUsername.setBackgroundResource(R.color.colorGrayDarkAlpha);

                                            }

                                            // Otherwise, player gets another turn:
                                            else {

                                                // But, before we let him play again, we need to make sure his opponent
                                                // saw the move.

                                                mPlayerRef.child(Constants.MOVE_POSITION).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        // Getting the position:
                                                        int position = dataSnapshot.getValue(Integer.class);

                                                        // If opponent saw the move:
                                                        if(position == -1){

                                                            // Removing the listener:
                                                            mPlayerRef.removeEventListener(this);
                                                            mPlayerRef.child(Constants.MOVE_POSITION).removeEventListener(this);

                                                            // Showing the guidelines and letting the player play again:
                                                            mPlayableTiles = mGame.getmBoard().getPlayableTiles(mPlayerGamePiece);
                                                            showGuidelines();
                                                            mIsAllowedToPlay = true;

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {}

                                                });


                                            }

                                        } else { // Otherwise, game has ended.
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



    private void showGuidelines() {

        mGuidelines = new AnimatorSet();

        ArrayList<ObjectAnimator> animatorsList = new ArrayList<ObjectAnimator>();


        // Preparing an animation for each playable tile:

        for (Integer position : mPlayableTiles) {

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

        mIsAllowedToPlay = false;


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
        if (mGame.getmGameStatus() == Game.GameStatus.PLAYER_BLACK_WON) {

            // If Player Won:
            if (mPlayerGamePiece == GamePiece.BLACK) {
                conclusionText.setText(R.string.victory_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorBlue));
                MediaPlayer.create(this, R.raw.victory_sound).start(); // Playing the proper sound.
            }
            // If Player Lost:
            else {
                conclusionText.setText(R.string.defeat_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
                MediaPlayer.create(this, R.raw.defeat_sound).start(); // Playing the proper sound.
                // Vibrate:
                if (mVibratorStatus.equals(Constants.ON)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(Constants.VIBRATION_DURATION);
                }
            }

        }

        // If the player WHITE the white pieces won:
        else if (mGame.getmGameStatus() == Game.GameStatus.PLAYER_WHITE_WON) {

            // If Player Won:
            if (mPlayerGamePiece == GamePiece.WHITE) {
                conclusionText.setText(R.string.victory_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorBlue));
                MediaPlayer.create(this, R.raw.victory_sound).start(); // Playing the proper sound.
            }
            // If Player Lost:
            else {
                conclusionText.setText(R.string.defeat_text);
                conclusionText.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
                MediaPlayer.create(this, R.raw.defeat_sound).start(); // Playing the proper sound.
                // Vibrate:
                if (mVibratorStatus.equals(Constants.ON)) {
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
        ObjectAnimator conclusionTextScaleXAnimator = ObjectAnimator.ofFloat(conclusionText, "scaleX", 4 * scaleX, scaleX);
        conclusionTextScaleXAnimator.setInterpolator(new OvershootInterpolator());
        conclusionTextScaleXAnimator.setDuration(Constants.FINALE_ANIMATION_DURATION);
        conclusionTextScaleXAnimator.start();

        float scaleY = conclusionText.getScaleY();
        ObjectAnimator conclusionTextScaleYAnimator = ObjectAnimator.ofFloat(conclusionText, "scaleY", 4 * scaleY, scaleY);
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
                if (mSoundStatus.equals(Constants.ON))
                    mFlipPieceSound.start(); // Playing a proper sound.
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
                if (mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Creating a new game and reinitializing UI components:
                mGame = new Game();
                setupGrid();
                mBlackCounter.setText("2");
                mWhiteCounter.setText("2");

                if(mPlayerGamePiece == GamePiece.BLACK){

                    // If opponents also wants a rematch, start the game:
                    if(mIsOpponentAskingForRematch) {
                        startGame();
                        mIsOpponentAskingForRematch = false;
                    }
                    // Else, only this player wants a rematch currently:
                    else mIsPlayerAskingForRematch = true;

                }

                else{
                    mPlayerRef.child(Constants.IS_ASKING_FOR_REMATCH).setValue(true);
                    startGame();
                }


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

                // If opponent is not connected yet:
                if(mOpponentName.equals(""))
                    Toast.makeText(OnlineGameActivity.this, R.string.no_opponent_yet, Toast.LENGTH_SHORT).show();

                // Otherwise, If shake is playable at the moment:
                else if(mIsShakePlayable) {

                    // Making button unavailable for a while:
                    mIsShakePlayable = false;
                    mShakeButton.setBackgroundResource(R.color.colorGrayDark);

                    // Updating the Firebase database with the poke:
                    mPlayerRef.child(Constants.IS_POKING).setValue(true);

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



    private void setupEmoteButton() {

        // Setting listener for the button:
        mEmoteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // If opponent is not connected yet:
                if(mOpponentName.equals(""))
                    Toast.makeText(OnlineGameActivity.this, R.string.no_opponent_yet, Toast.LENGTH_SHORT).show();

                // Otherwise, If emotes are playable at the moment:
                else if (mIsEmotePlayable) {
                    // Make emote picker list visible:
                    mEmotePickerList.setVisibility(View.VISIBLE);
                    mDummy.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void setupEmotePickerList() {

        // Getting arrays of emotes:
        final String[] emotesArray = getResources().getStringArray(R.array.emotes_array);

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


                // Updating the Firebase database with the new emote index:
                mPlayerRef.child(Constants.EMOTE_INDEX).setValue(position);


                // Making emotes unavailable for a while:
                mIsEmotePlayable = false;
                mEmoteButton.setBackgroundResource(R.color.colorGrayDark);


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


    private void showEmote(TextView emoteBubble) {

        // Playing the 'pop' sound:
        if (mSoundStatus.equals(Constants.ON)) mPopSound.start();

        // Animating the bubble's alpha (fade in, fade out):

        ObjectAnimator emoteAlphaAnimator = ObjectAnimator.ofFloat(emoteBubble,
                "alpha", 0, 1);
        emoteAlphaAnimator.setInterpolator(new DecelerateInterpolator());
        emoteAlphaAnimator.setRepeatCount(1);
        emoteAlphaAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        emoteAlphaAnimator.setDuration(Constants.EMOTE_DURATION / 2);


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

    private void setupReturnButton() {

        Button returnButton = (Button) findViewById(R.id.return_button);

        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showReturnDialog();

            }
        });

    }

    private void setupConcedeButton() {

        Button concedeButton = (Button) findViewById(R.id.concede_button);

        concedeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // If opponent is not connected yet:
                if(mOpponentName.equals(""))
                    Toast.makeText(OnlineGameActivity.this, R.string.no_opponent_yet, Toast.LENGTH_SHORT).show();

                // Otherwise:
                else showConcedeDialog();

            }
        });
    }

    private void showReturnDialog() {

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
                if (mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                verificationDialog.dismiss();

            }
        });

        // Setting up a listener for the Done Button:
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if (mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                verificationDialog.dismiss();

                // Canceling timer and closing loading dialog (if its a quick match and they still exist):

                if(mTimer != null){
                    mTimer.cancel();
                    mTimer = null;
                }

                if (mOpponentSearchDialog != null)
                    mOpponentSearchDialog.dismiss();


                // Go back to main menu:

                // Creating an intent:
                Intent mainIntent = new Intent(OnlineGameActivity.this, MainActivity.class);
                // Starting the Main Activity:
                startActivity(mainIntent);

                finish();

            }
        });

    }

    private void showConcedeDialog() {

        // Creating a dialog and getting the right views:
        final Dialog verificationDialog = new Dialog(this, R.style.DialogTheme);
        verificationDialog.setContentView(R.layout.dialog_verification);
        TextView verificationText = (TextView) verificationDialog.findViewById(R.id.verification_text);
        Button cancelButton = (Button) verificationDialog.findViewById(R.id.cancel_button);
        Button acceptButton = (Button) verificationDialog.findViewById(R.id.accept_button);
        verificationDialog.show();

        verificationText.setText(R.string.verification_concede);

        // Setting up a listener for the Cancel Button:
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if (mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                verificationDialog.dismiss();

            }
        });

        // Setting up a listener for the Done Button:
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if (mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // End Game:
                if(mPlayerGamePiece == GamePiece.BLACK)
                    mGame.setmGameStatus(Game.GameStatus.PLAYER_WHITE_WON);
                else
                    mGame.setmGameStatus(Game.GameStatus.PLAYER_BLACK_WON);

                // Update Firebase database:
                mPlayerRef.child(Constants.HAS_SURRENDERED).setValue(true);

                // Handle game's ending:
                handleGameEnding();

                // Dismiss dialog:
                verificationDialog.dismiss();

            }
        });

    }


    private void showOpponentLeftDialog() {

        // Creating a dialog and getting the right views:
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setContentView(R.layout.dialog_opponent_left);
        dialog.setCancelable(false);
        Button leaveButton = (Button) dialog.findViewById(R.id.leave_button);
        dialog.show();

        // Setting up a listener for the  leaveButton:
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Play a proper sound:
                if (mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Go back to main menu:

                // Creating an intent:
                Intent mainIntent = new Intent(OnlineGameActivity.this, MainActivity.class);
                // Starting the Main Activity:
                startActivity(mainIntent);

            }
        });

    }


    @Override
    public void onBackPressed() {

        // If opponent has left:
        if(mHasOpponentLeft){

            // Go back to main menu without showing any dialog:

            // Creating an intent:
            Intent mainIntent = new Intent(OnlineGameActivity.this, MainActivity.class);
            // Starting the Main Activity:
            startActivity(mainIntent);

        }

        else showReturnDialog(); // (same as the UI's return button)

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If you leave the room first:
        if(!mHasOpponentLeft) {

            mHasPlayerLeft = true; // to prevent memory leak.

            // cancel onDisconnect listener (to prevent deleting the room a second time):
            mRoomRef.onDisconnect().cancel();

            // Delete the room from Firebase database:
            mRoomRef.removeValue();

        }

    }



}