package com.sublime.gaby.flipit;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.sublime.gaby.flipit.Logic.Board;
import com.sublime.gaby.flipit.Logic.Game;
import com.sublime.gaby.flipit.Logic.GamePiece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Gaby on 3/7/2017.
 */

public class TutorialActivity extends AppCompatActivity {

    private boolean mIsAllowedToPress = false;
    private Board mBoard;

    private ArrayList<Integer> mPlayableTiles;
    private AnimatorSet mGuidelines;

    private int mPhaseNumber;
    private boolean mIsBasic;

    // Components:
    private GridView mGrid;
    private TextView mTextBubble;
    private Button mPreviousButton;
    private Button mNextButton;
    private Button mBackToMenuButton;

    // MediaPlayer:
    private MediaPlayer mPutPieceSound;
    private MediaPlayer mFlipPieceSound;
    private MediaPlayer mPopSound;

    private String mSoundStatus;

    private boolean mIsPopping;

    private boolean mIsTutorialFinished = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transition:
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Setting content view:
        setContentView(R.layout.activity_tutorial);

        // Getting text area:
        mTextBubble = (TextView) findViewById(R.id.text_bubble);

        // Extracting data from bundle:
        extractDataFromBundle();

        // Initialize sound media players:
        mPutPieceSound = MediaPlayer.create(this, R.raw.put_piece_sound);
        mFlipPieceSound = MediaPlayer.create(this, R.raw.flip_piece_sound);
        mPopSound = MediaPlayer.create(this, R.raw.pop_sound);

        // Setting sound status:
        setupSoundStatus();

        // Setting up the grid:
        setupGrid();

        // Set up buttons:
        setupPreviousButton();
        setupNextButton();
        setupBackToMenuButton();

        // Starting the tutorial:
        mPhaseNumber = 1;
        showProperPhase();

    }

    private void extractDataFromBundle() {

        // Getting the intent and bundle:
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(MainActivity.TUTORIAL_BUNDLE_KEY);

        mIsBasic = bundle.getBoolean(MainActivity.IS_BASIC_KEY);

    }

    private void setupSoundStatus(){

        // Using shared preferences:
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.OPTIONS_DATA, Context.MODE_PRIVATE);
        mSoundStatus = sharedPreferences.getString(Constants.CHOSEN_SOUND_STATUS, Constants.DEFAULT);

    }


    private void setupGrid() {

        // Creating a new board:
        mBoard = new Board();

        // Getting the grid view:
        mGrid = (GridView) findViewById(R.id.grid_view);

        // Setting number of columns in the grid:
        mGrid.setNumColumns(mBoard.getNumberOfCols());

        // Setting the adapter for the grid:
        mGrid.setAdapter(new TileAdapter(getApplicationContext(), mBoard));

        // Setting a click listener for the grid:
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // If player is allowed to press the board:
                if (mIsAllowedToPress) {

                    // If this tile is playable:
                    if (mPlayableTiles.contains(position)){

                        mIsAllowedToPress = false; // Disabling the player to play for now.

                        if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start(); // Playing proper sound.

                        // Cancel Guidelines:
                        cancelGuidelines();

                        // Making the play and updating grid:
                        mBoard.addGamePiece(position/mBoard.getNumberOfCols(), position%mBoard.getNumberOfCols(), GamePiece.BLACK);
                        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

                        // Playing flip sound in delay:
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


                                if(!mIsBasic && mPhaseNumber == 9 && mBoard.getmNumberOfEmptyTiles() != 0){
                                    mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showGuidelines();
                                        }
                                    });
                                    mIsAllowedToPress = true;
                                }

                                if(!mIsBasic && mPhaseNumber == 12 &&
                                        mBoard.getTile(1,1).getmOccupyingGamePiece() == GamePiece.BLACK){

                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start(); // Playing proper sound.

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            mBoard.addGamePiece(0,0,GamePiece.WHITE);
                                            ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

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

                                                }
                                            }).start();

                                        }
                                    });
                                }


                            }

                        }).start();

                    }

                }

            }
        });

    }

    private void cancelGuidelines(){

        if(mGuidelines != null) {
            mGuidelines.cancel();
            mGuidelines = null;
            // refresh tiles' color:
            for(int i = 0; i < mGrid.getChildCount(); i++){
                mGrid.getChildAt(i).setBackgroundResource(R.drawable.button_tile);
            }
        }

    }



    private void setupPreviousButton(){

        mPreviousButton = (Button) findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSoundStatus.equals(Constants.ON))
                    mFlipPieceSound.start(); // Playing proper sound.

                if(!mIsPopping) {
                    mPhaseNumber--;
                    cancelGuidelines();
                    showProperPhase();
                }

            }
        });

    }

    private void setupNextButton(){

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSoundStatus.equals(Constants.ON))
                    mFlipPieceSound.start(); // Playing proper sound.

                if(!mIsPopping) {
                    mPhaseNumber++;
                    cancelGuidelines();
                    showProperPhase();
                }

            }
        });


    }

    private void setupBackToMenuButton(){

        mBackToMenuButton = (Button) findViewById(R.id.back_to_menu_button);
        mBackToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSoundStatus.equals(Constants.ON))
                    mFlipPieceSound.start(); // Playing proper sound.

                if(mIsTutorialFinished){

                    // Go back to main menu:

                    // Creating an intent:
                    Intent mainIntent = new Intent(TutorialActivity.this , MainActivity.class);
                    // Starting the Main Activity:
                    startActivity(mainIntent);

                }

                else showReturnDialog();

            }
        });

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

    private void showAdvancedGuidelines(){

        mGuidelines = new AnimatorSet();
        ArrayList<ObjectAnimator> animatorsList = new ArrayList<ObjectAnimator>();

        if(mPhaseNumber == 5){

            for(Integer position : mPlayableTiles) {

                TileView tile = (TileView) mGrid.getChildAt(position);

                ObjectAnimator tileColorAnimator;
                if(position == 59){
                    tileColorAnimator = ObjectAnimator.ofObject(tile,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            ContextCompat.getColor(this, R.color.colorGreen),
                            ContextCompat.getColor(this, R.color.colorGreenLightest));
                }
                else {
                    tileColorAnimator = ObjectAnimator.ofObject(tile,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            ContextCompat.getColor(this, R.color.colorOrange),
                            ContextCompat.getColor(this, R.color.colorOrangeLight));
                }
                tileColorAnimator.setInterpolator(new DecelerateInterpolator());
                tileColorAnimator.setDuration(400);
                tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                animatorsList.add(tileColorAnimator);

            }

        }

        if(mPhaseNumber == 7 || mPhaseNumber == 15){

            // Animation for corners:

            ArrayList<Integer> cornerTiles = new ArrayList<Integer>(Arrays.asList(Game.getRiskZone0()));

            for(Integer position : cornerTiles) {

                TileView tile = (TileView) mGrid.getChildAt(position);

                ObjectAnimator tileColorAnimator = ObjectAnimator.ofObject(tile,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        ContextCompat.getColor(this, R.color.colorBlueDark),
                        ContextCompat.getColor(this, R.color.colorBlueLight));
                tileColorAnimator.setInterpolator(new DecelerateInterpolator());
                tileColorAnimator.setDuration(400);
                tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                animatorsList.add(tileColorAnimator);

            }

        }

        if(mPhaseNumber == 11 || mPhaseNumber == 15){

            // Animation for tiles around corners:

            ArrayList<Integer> aroundCornersTiles = new ArrayList<Integer>(Arrays.asList(Game.getRiskZone6()));

            for(Integer position : aroundCornersTiles) {

                TileView tile = (TileView) mGrid.getChildAt(position);

                ObjectAnimator tileColorAnimator = ObjectAnimator.ofObject(tile,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        ContextCompat.getColor(this, R.color.colorRedDark),
                        ContextCompat.getColor(this, R.color.colorRedLight));
                tileColorAnimator.setInterpolator(new DecelerateInterpolator());
                tileColorAnimator.setDuration(400);
                tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                animatorsList.add(tileColorAnimator);

            }

        }

        if(mPhaseNumber == 12){

            for(Integer position : mPlayableTiles) {

                TileView tile = (TileView) mGrid.getChildAt(position);

                ObjectAnimator tileColorAnimator;
                if(position == 9){
                    tileColorAnimator = ObjectAnimator.ofObject(tile,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            ContextCompat.getColor(this, R.color.colorOrange),
                            ContextCompat.getColor(this, R.color.colorOrangeLight));
                }
                else {
                    tileColorAnimator = ObjectAnimator.ofObject(tile,
                            "backgroundColor",
                            new ArgbEvaluator(),
                            ContextCompat.getColor(this, R.color.colorGreen),
                            ContextCompat.getColor(this, R.color.colorGreenLightest));
                }
                tileColorAnimator.setInterpolator(new DecelerateInterpolator());
                tileColorAnimator.setDuration(400);
                tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                animatorsList.add(tileColorAnimator);

            }

        }

        if(mPhaseNumber == 13 || mPhaseNumber == 15){

            // Animation for margins:

            ArrayList<Integer> marginTiles = new ArrayList<Integer>(Arrays.asList(Game.getRiskZone1()));

            for(Integer position : marginTiles) {

                TileView tile = (TileView) mGrid.getChildAt(position);

                ObjectAnimator tileColorAnimator = ObjectAnimator.ofObject(tile,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        ContextCompat.getColor(this, R.color.colorTeal),
                        ContextCompat.getColor(this, R.color.colorTealLight));
                tileColorAnimator.setInterpolator(new DecelerateInterpolator());
                tileColorAnimator.setDuration(400);
                tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                animatorsList.add(tileColorAnimator);

            }

        }

        if(mPhaseNumber == 14 || mPhaseNumber == 15){

            // Animation for tiles around margins:

            ArrayList<Integer> aroundMarginsTiles = new ArrayList<Integer>(Arrays.asList(Game.getRiskZone3()));

            for(Integer position : aroundMarginsTiles) {

                TileView tile = (TileView) mGrid.getChildAt(position);

                ObjectAnimator tileColorAnimator = ObjectAnimator.ofObject(tile,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        ContextCompat.getColor(this, R.color.colorOrange),
                        ContextCompat.getColor(this, R.color.colorOrangeLight));
                tileColorAnimator.setInterpolator(new DecelerateInterpolator());
                tileColorAnimator.setDuration(400);
                tileColorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                tileColorAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                animatorsList.add(tileColorAnimator);

            }

        }


        // Starting the guidelines animation:
        mGuidelines.playTogether((Collection) animatorsList);
        mGuidelines.start();

    }




    private void popTextBubble(){

        mIsPopping = true;

        // Playing the 'pop' sound:
        if(mSoundStatus.equals(Constants.ON)) mPopSound.start();

        // Animating the bubble's alpha (fade in):
        ObjectAnimator bubbleAlphaAnimator = ObjectAnimator.ofFloat(mTextBubble,
                "alpha", 0, 1);
        bubbleAlphaAnimator.setInterpolator(new DecelerateInterpolator());
        bubbleAlphaAnimator.setDuration(Constants.EMOTE_DURATION/2);
        bubbleAlphaAnimator.start();


        // Animating the bubble's size (pop up with overshoot):

        float scaleX = mTextBubble.getScaleX();
        ObjectAnimator bubbleScaleXAnimator = ObjectAnimator.ofFloat(mTextBubble,
                "scaleX", 0, scaleX);
        bubbleScaleXAnimator.setInterpolator(new OvershootInterpolator());
        bubbleScaleXAnimator.setDuration(Constants.EMOTE_POPUP_DURATION);

        float scaleY = mTextBubble.getScaleY();
        ObjectAnimator bubbleScaleYAnimator = ObjectAnimator.ofFloat(mTextBubble,
                "scaleY", 0, scaleY);
        bubbleScaleYAnimator.setInterpolator(new OvershootInterpolator());
        bubbleScaleYAnimator.setDuration(Constants.EMOTE_POPUP_DURATION);


        AnimatorSet scaleAnimatorSet = new AnimatorSet();
        scaleAnimatorSet.playTogether(bubbleScaleXAnimator, bubbleScaleYAnimator);
        scaleAnimatorSet.start();

        scaleAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                mIsPopping = false;
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });


    }


    private void showProperPhase(){

        // If it's a basic tutorial
        if(mIsBasic){

            switch (mPhaseNumber){

                default:
                case 1:
                    showPhase1();
                    break;
                case 2:
                    showPhase2();
                    break;
                case 3:
                    showPhase3();
                    break;
                case 4:
                    showPhase4();
                    break;
                case 5:
                    showPhase5();
                    break;
                case 6:
                    showPhase6();
                    break;
                case 7:
                    showPhase7();
                    break;
                case 8:
                    showPhase8();
                    break;
                case 9:
                    showPhase9();
                    break;
                case 10:
                    showPhase10();
                    break;
                case 11:
                    mIsTutorialFinished = true;
                    showPhase11();
                    break;

            }

        }

        // If it's an advanced tutorial
        else{

            switch (mPhaseNumber){

                default:
                case 1:
                    showAdvancedPhase1();
                    break;
                case 2:
                    showAdvancedPhase2();
                    break;
                case 3:
                    showAdvancedPhase3();
                    break;
                case 4:
                    showAdvancedPhase4();
                    break;
                case 5:
                    showAdvancedPhase5();
                    break;
                case 6:
                    showAdvancedPhase6();
                    break;
                case 7:
                    showAdvancedPhase7();
                    break;
                case 8:
                    showAdvancedPhase8();
                    break;
                case 9:
                    showAdvancedPhase9();
                    break;
                case 10:
                    showAdvancedPhase10();
                    break;
                case 11:
                    showAdvancedPhase11();
                    break;
                case 12:
                    showAdvancedPhase12();
                    break;
                case 13:
                    showAdvancedPhase13();
                    break;
                case 14:
                    showAdvancedPhase14();
                    break;
                case 15:
                    showAdvancedPhase15();
                    break;
                case 16:
                    showAdvancedPhase16();
                    break;
                case 17:
                    showAdvancedPhase17();
                    break;
                case 18:
                    showAdvancedPhase18();
                    break;
                case 19:
                    mIsTutorialFinished = true;
                    showAdvancedPhase19();
                    break;

            }

        }


    }



    private void showPhase1(){

        // Updating visibility:
        mPreviousButton.setVisibility(View.GONE);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase1);
        popTextBubble();

    }

    private void showPhase2(){

        // Updating visibility:
        mPreviousButton.setVisibility(View.VISIBLE);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase2);
        popTextBubble();

    }

    private void showPhase3(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase3);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board();
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

    }

    private void showPhase4(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase4);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(4,true);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
        showGuidelines();


    }

    private void showPhase5(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase5);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board();
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

    }

    private void showPhase6(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase6);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board();
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mIsAllowedToPress = false;

    }

    private void showPhase7(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase7);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(7,true);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
        showGuidelines();
        mIsAllowedToPress = true;

    }

    private void showPhase8(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase8);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(8,true);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
        showGuidelines();
        mIsAllowedToPress = true;

    }

    private void showPhase9(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase9);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board();
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mIsAllowedToPress = false;

    }

    private void showPhase10(){

        // Updating visibility:
        mNextButton.setVisibility(View.VISIBLE);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase10);
        popTextBubble();

    }

    private void showPhase11(){

        // Updating visibility:
        mNextButton.setVisibility(View.GONE);

        // Updating final backgrounds and styles for buttons:
        mBackToMenuButton.setBackgroundResource(R.drawable.button_green_alpha);
        mBackToMenuButton.setTypeface(null, Typeface.BOLD);
        mNextButton.setBackgroundResource(R.drawable.button_green_shadow);
        mNextButton.setTypeface(null, Typeface.NORMAL);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.basics_phase11);
        popTextBubble();

    }

    private void showAdvancedPhase1(){

        // Updating visibility:
        mPreviousButton.setVisibility(View.GONE);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase1);
        popTextBubble();

    }

    private void showAdvancedPhase2(){

        // Updating visibility:
        mPreviousButton.setVisibility(View.VISIBLE);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase2);
        popTextBubble();

    }

    private void showAdvancedPhase3(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase3);
        popTextBubble();

    }

    private void showAdvancedPhase4(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase4);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board();
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mIsAllowedToPress = false;

    }

    private void showAdvancedPhase5(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase5);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(5,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
        showAdvancedGuidelines();
        mIsAllowedToPress = true;

    }

    private void showAdvancedPhase6(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase6);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(6,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mIsAllowedToPress = false;

    }

    private void showAdvancedPhase7(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase7);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(7,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        showAdvancedGuidelines();

    }

    private void showAdvancedPhase8(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase8);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(8,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mIsAllowedToPress = false;

    }

    private void showAdvancedPhase9(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase9);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(9,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
        showGuidelines();
        mIsAllowedToPress = true;

    }

    private void showAdvancedPhase10(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase10);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(10,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mIsAllowedToPress = false;

    }

    private void showAdvancedPhase11(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase11);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(11,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        showAdvancedGuidelines();
        mIsAllowedToPress = false;

    }

    private void showAdvancedPhase12(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase12);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(12,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        mPlayableTiles = mBoard.getPlayableTiles(GamePiece.BLACK);
        showAdvancedGuidelines();
        mIsAllowedToPress = true;


    }

    private void showAdvancedPhase13(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase13);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(13,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();
        showAdvancedGuidelines();
        mIsAllowedToPress = false;

    }

    private void showAdvancedPhase14(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase14);
        popTextBubble();

        showAdvancedGuidelines();

    }

    private void showAdvancedPhase15(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase15);
        popTextBubble();

        showAdvancedGuidelines();

    }

    private void showAdvancedPhase16(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase16);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(16,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

    }

    private void showAdvancedPhase17(){

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase17);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(17,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

    }

    private void showAdvancedPhase18(){

        // Updating visibility:
        mNextButton.setVisibility(View.VISIBLE);

        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase18);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board(18,false);
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

    }

    private void showAdvancedPhase19(){

        // Updating visibility:
        mNextButton.setVisibility(View.GONE);

        // Updating final backgrounds and styles for buttons:
        mBackToMenuButton.setBackgroundResource(R.drawable.button_green_alpha);
        mBackToMenuButton.setTypeface(null, Typeface.BOLD);
        mNextButton.setBackgroundResource(R.drawable.button_green_shadow);
        mNextButton.setTypeface(null, Typeface.NORMAL);


        // Popping bubble with proper text:
        mTextBubble.setText(R.string.advanced_phase19);
        popTextBubble();

        // Setting a proper hypothetical board:
        mBoard = new Board();
        ((TileAdapter)mGrid.getAdapter()).setmBoard(mBoard);
        ((TileAdapter)mGrid.getAdapter()).notifyDataSetChanged();

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

                // Go back to main menu:

                // Creating an intent:
                Intent mainIntent = new Intent(TutorialActivity.this , MainActivity.class);
                // Starting the Main Activity:
                startActivity(mainIntent);

            }
        });

    }



    @Override
    public void onBackPressed() {

        if(mIsTutorialFinished){

            // Go back to main menu:

            // Creating an intent:
            Intent mainIntent = new Intent(TutorialActivity.this , MainActivity.class);
            // Starting the Main Activity:
            startActivity(mainIntent);

        }

        else showReturnDialog();

    }


}
