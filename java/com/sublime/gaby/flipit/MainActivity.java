package com.sublime.gaby.flipit;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Arrays;
import java.util.Locale;

import android.content.res.Configuration;
import android.widget.TextView;
import android.widget.Toast;

import com.sublime.gaby.flipit.Logic.Difficulty;
import com.sublime.gaby.flipit.Logic.GamePiece;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9630;

    // Fields used by the OfflineGameActivity:
    public static final String PRACTICE_GAME_BUNDLE_KEY = "PracticeGameBundle";
    public static final String DIFFICULTY_KEY = "Difficulty";
    public static final String GAME_PIECE_KEY = "GamePiece";
    private Difficulty mChosenDifficulty;
    private GamePiece mChosenPiece;

    // Fields used by the OnlineGameActivity if its a quick match:
    public static final String QUICK_MATCH_BUNDLE_KEY = "QuickMatchBundle";
    public static final String ROOM_NAME_KEY = "RoomName";
    public static final String PIECE_TYPE_KEY = "PieceType";
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mGameRoomsRef = mRootRef.child(Constants.QUICK_GAME_ROOMS);

    // Fields used by TutorialActivity:
    public static final String TUTORIAL_BUNDLE_KEY = "TutorialBundle";
    public static final String IS_BASIC_KEY = "IsBasic";

    // Fields used by a new activity of the same type:
    public static final String LOCALE_CHANGE_KEY = "LocaleChange";

    // Media Players:
    private MediaPlayer mPutPieceSound;
    private MediaPlayer mFlipPieceSound;

    // Options:
    private String mSoundStatus;
    private String mVibratorStatus;
    private String mChosenLanguage;

    // Firebase:
    private boolean mIsLoggedIn;
    private FirebaseApp mApp = FirebaseApp.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance(mApp);

    private Button mConnectionButton;

    private ProgressDialog mRoomLoadingDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transition:
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Checking previously saved options:
        checkSavedOptions();

        // Setting content view:
        setContentView(R.layout.activity_main);


        // Checking if user is logged in or not:
        if (mAuth.getCurrentUser() != null) mIsLoggedIn = true;
        else mIsLoggedIn = false;

        // Setting up a listener for user login/logout:
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {

                // If User logged in:
                if (firebaseAuth.getCurrentUser() != null) mIsLoggedIn = true;

                // If User logged out:
                else mIsLoggedIn = false;

            }
        });


        // Initialize the sound players:
        mPutPieceSound = MediaPlayer.create(this, R.raw.put_piece_sound);
        mFlipPieceSound = MediaPlayer.create(this, R.raw.flip_piece_sound);

        //Setting up Buttons:
        setupOptionsButton();
        setupTutorialButton();
        setupPlayOfflineButton();
        setupPlayOnlineButton();

        // Getting intent:
        Intent intent = getIntent();

        // If the activity was created due to locale change:
        if(intent.hasExtra(LOCALE_CHANGE_KEY)){
            // Reopen the options dialog:
            createOptionsDialog();
        }

        // If the activity was created due to not finding an opponent in quick match:
        else if(intent.hasExtra(OnlineGameActivity.NO_OPPONENT_KEY)){
            // Show a proper toast:
            Toast.makeText(MainActivity.this, R.string.opponent_not_found, Toast.LENGTH_LONG).show();
        }

        // Setting up a banner ad:
        AdView adView = (AdView) findViewById(R.id.main_ad_view_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }



    private void setupOptionsButton(){

        Button button = (Button) findViewById(R.id.options_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();
                createOptionsDialog();
            }
        });

    }

    private void setupTutorialButton(){

        Button button = (Button) findViewById(R.id.tutorial_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();
                createTutorialDialog();
            }
        });

    }

    private void setupPlayOfflineButton(){

        Button button = (Button) findViewById(R.id.play_offline_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();
                createDifficultyDialog();
            }
        });

    }

    private void setupPlayOnlineButton(){

        Button button = (Button) findViewById(R.id.play_online_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                if(!mIsLoggedIn) startLoginProcess();
                else createOnlineGameDialog();

            }
        });

    }

    private void createOnlineGameDialog(){

        // Creating a dialog that shows the online game types and getting the right views:
        final Dialog onlineGameDialog = new Dialog(this, R.style.DialogTheme);
        onlineGameDialog.setContentView(R.layout.dialog_online_game);
        Button quickMatchButton = (Button) onlineGameDialog.findViewById(R.id.quick_match_button);
        Button privateMatchButton = (Button) onlineGameDialog.findViewById(R.id.private_match_button);
        onlineGameDialog.show();


        // Setting listeners for the buttons:

        quickMatchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Dismiss dialog:
                onlineGameDialog.dismiss();

                // Show loading dialog:
                mRoomLoadingDialog = ProgressDialog.show(MainActivity.this,
                        getResources().getString(R.string.queue_title), getResources().getString(R.string.room_search), true);

                // Seeking a quick match:
                seekQuickMatch();

            }
        });

        privateMatchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Dismiss dialog:
                onlineGameDialog.dismiss();

                // Starting Game Room Activity:
                Intent intent = new Intent(MainActivity.this, GameRoomActivity.class);
                startActivity(intent);

            }
        });

    }



    private void seekQuickMatch() {

        // Adding a listener to the rooms list in order to read data only once:
        mGameRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Getting number of quick match rooms:
                long numberOfRooms = dataSnapshot.child(Constants.NUMBER_OF_ROOMS).getValue(Long.class);

                // If the last room still exists:
                if(dataSnapshot.child(Constants.ROOM + numberOfRooms).exists()){

                    // If the last room got one more place:
                    boolean areBothConnected = dataSnapshot.child(Constants.ROOM+numberOfRooms).child(Constants.ARE_BOTH_CONNECTED).getValue(Boolean.class);
                    if(areBothConnected == false) {

                        // Entering an existing room:
                        enterExistingRoom(numberOfRooms);

                    }

                    // If the last room is occupied:
                    else{

                        // Entering a new room
                        enterNewRoom(numberOfRooms + 1);

                    }

                }

                // If the last room doesn't exist anymore:
                else{

                    // Entering a new room
                    enterNewRoom(numberOfRooms + 1);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}


        });

    }

    private void enterNewRoom(final long roomNumber){

        // Creating a new room:
        mGameRoomsRef.child(Constants.NUMBER_OF_ROOMS).setValue(roomNumber);
        mGameRoomsRef.child(Constants.ROOM + roomNumber).child(Constants.ARE_BOTH_CONNECTED).setValue(false);

        Uri profilePicture = mAuth.getCurrentUser().getPhotoUrl();

        final DatabaseReference playerRef = mGameRoomsRef.child(Constants.ROOM + roomNumber).child(Constants.PLAYER_BLACK);
        playerRef.child(Constants.USER_ID).setValue(mAuth.getCurrentUser().getUid());
        playerRef.child(Constants.NAME).setValue(mAuth.getCurrentUser().getDisplayName());
        if(profilePicture != null) playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(profilePicture.toString());
        else playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(Constants.NO_PICTURE);
        playerRef.child(Constants.MOVE_POSITION).setValue(-1);
        playerRef.child(Constants.EMOTE_INDEX).setValue(-1);
        playerRef.child(Constants.IS_POKING).setValue(false);
        playerRef.child(Constants.HAS_SURRENDERED).setValue(false);

        DatabaseReference opponentRef = mGameRoomsRef.child(Constants.ROOM + roomNumber).child(Constants.PLAYER_WHITE);
        opponentRef.child(Constants.USER_ID).setValue("");
        opponentRef.child(Constants.NAME).setValue("");
        opponentRef.child(Constants.PORTRAIT_URI_STRING).setValue("");
        opponentRef.child(Constants.MOVE_POSITION).setValue(-1);
        opponentRef.child(Constants.EMOTE_INDEX).setValue(-1);
        opponentRef.child(Constants.IS_POKING).setValue(false);
        opponentRef.child(Constants.HAS_SURRENDERED).setValue(false);
        opponentRef.child(Constants.IS_ASKING_FOR_REMATCH).setValue(false);


        // Verifying no one else connected to the same room as the same player type at the same time:
        // (We need that because Firebase is asynchronous)

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                playerRef.child(Constants.USER_ID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // If there is no one else that occupied the same room as the same player type:
                        if(dataSnapshot.getValue(String.class).equals(mAuth.getCurrentUser().getUid())){
                            // Start the quick match (enter the room):
                            startQuickMatch(Constants.ROOM + roomNumber, GamePiece.BLACK);
                        }
                        // Otherwise:
                        else{
                            // Keep seeking a match:
                            seekQuickMatch();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}

                });

            }
        }, 600); // Delay that gives enough time for Firebase database to update.

    }

    private void enterExistingRoom(final long roomNumber){

        mGameRoomsRef.child(Constants.ROOM + roomNumber).child(Constants.ARE_BOTH_CONNECTED).setValue(true);

        Uri profilePicture = mAuth.getCurrentUser().getPhotoUrl();

        final DatabaseReference playerRef = mGameRoomsRef.child(Constants.ROOM + roomNumber).child(Constants.PLAYER_WHITE);
        playerRef.child(Constants.USER_ID).setValue(mAuth.getCurrentUser().getUid());
        playerRef.child(Constants.NAME).setValue(mAuth.getCurrentUser().getDisplayName());
        if(profilePicture != null) playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(profilePicture.toString());
        else playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(Constants.NO_PICTURE);
        playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(mAuth.getCurrentUser().getPhotoUrl().toString());
        playerRef.child(Constants.IS_ASKING_FOR_REMATCH).setValue(true);


        // Verifying no one else connected to the same room as the same player type at the same time:
        // (We need that because Firebase is asynchronous)

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {


                playerRef.child(Constants.USER_ID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // If there is no one else that occupied the same room as the same player type:
                        if(dataSnapshot.getValue(String.class).equals(mAuth.getCurrentUser().getUid())){
                            // Start the quick match (enter the room):
                            startQuickMatch(Constants.ROOM + roomNumber, GamePiece.WHITE);
                        }
                        // Otherwise:
                        else{
                            // Keep seeking a match:
                            seekQuickMatch();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}

                });

            }
        }, 600); // Delay that gives enough time for Firebase database to update.

    }

    private void startQuickMatch(String roomName, GamePiece gamePiece) {

        // Stop showing the loading bar:
        mRoomLoadingDialog.dismiss();

        // Creating an intent:
        Intent gameIntent = new Intent(this, OnlineGameActivity.class);

        // Creating a bundle for the intent and setting up the bundle:
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_NAME_KEY, roomName);
        bundle.putSerializable(PIECE_TYPE_KEY, gamePiece);

        // Put bundle in intent:
        gameIntent.putExtra(QUICK_MATCH_BUNDLE_KEY, bundle);

        // Starting the Game Activity:
        startActivity(gameIntent);

    }

    private void createOptionsDialog(){

        // Creating a dialog that shows the options and getting the right views:
        final Dialog optionsDialog = new Dialog(this, R.style.DialogTheme);
        optionsDialog.setContentView(R.layout.dialog_options);
        mConnectionButton = (Button) optionsDialog.findViewById(R.id.login_logout_button);
        Switch soundSwitch = (Switch) optionsDialog.findViewById(R.id.sound_switch);
        Switch vibratorSwitch = (Switch) optionsDialog.findViewById(R.id.vibrator_switch);
        Spinner languageSpinner = (Spinner) optionsDialog.findViewById(R.id.language_spinner);


        if(mIsLoggedIn) mConnectionButton.setText(R.string.log_out);
        else mConnectionButton.setText(R.string.log_in);

        if(mSoundStatus.equals(Constants.ON))
            soundSwitch.setChecked(true);

        if(mVibratorStatus.equals(Constants.ON))
            vibratorSwitch.setChecked(true);


        // Setting the right chosen language in languageSpinner:
        for(int i = 0; i < Constants.LOCALES_ARRAY.length; i++){
            if(mChosenLanguage.equals(Constants.LOCALES_ARRAY[i])){
                languageSpinner.setSelection(i);
                i = Constants.LOCALES_ARRAY.length; // return.
            }
        }


        optionsDialog.show();


        // Using shared preferences:
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.OPTIONS_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();


        // Listener for the button:
        mConnectionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mIsLoggedIn) createLogoutDialog();
                else startLoginProcess();

            }
        });


        // Listener for the sound switch:
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    mSoundStatus = Constants.ON;
                    editor.putString(Constants.CHOSEN_SOUND_STATUS, Constants.ON);
                    editor.commit();
                }
                else{
                    mSoundStatus = Constants.OFF;
                    editor.putString(Constants.CHOSEN_SOUND_STATUS, Constants.OFF);
                    editor.commit();
                }
            }
        });


        // Listener for the vibrator switch:
        vibratorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    mVibratorStatus = Constants.ON;
                    editor.putString(Constants.CHOSEN_VIBRATOR_STATUS, Constants.ON);
                    editor.commit();
                }
                else{
                    mVibratorStatus = Constants.OFF;
                    editor.putString(Constants.CHOSEN_VIBRATOR_STATUS, Constants.OFF);
                    editor.commit();
                }
            }
        });


        // Listener for the language spinner:
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                // If a different language was chosen:
                if(!Constants.LOCALES_ARRAY[position].equals(mChosenLanguage)){

                    // Update language:
                    mChosenLanguage = Constants.LOCALES_ARRAY[position];
                    editor.putString(Constants.CHOSEN_LANGUAGE, Constants.LOCALES_ARRAY[position]);
                    editor.commit();

                    // Restarting the activity::
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra(LOCALE_CHANGE_KEY, new Bundle());
                    startActivity(intent);

                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });



    }

    public void createLogoutDialog(){

        // Creating a dialog and getting the right views:
        final Dialog verificationDialog = new Dialog(this, R.style.DialogTheme);
        verificationDialog.setContentView(R.layout.dialog_verification);
        TextView verificationText = (TextView) verificationDialog.findViewById(R.id.verification_text);
        Button cancelButton = (Button) verificationDialog.findViewById(R.id.cancel_button);
        Button acceptButton = (Button) verificationDialog.findViewById(R.id.accept_button);
        verificationDialog.show();

        verificationText.setText(R.string.verification_logout);

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

                // Signing out:
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                mConnectionButton.setText(R.string.log_in);
                                Toast.makeText(MainActivity.this, R.string.log_out_succeeded, Toast.LENGTH_LONG).show();
                            }
                        });

                // Dismiss dialog:
                verificationDialog.dismiss();

            }
        });



    }



    public void createTutorialDialog(){


        // Creating a dialog that shows the tutorial types and getting the right views:
        final Dialog tutorialDialog = new Dialog(this, R.style.DialogTheme);
        tutorialDialog.setContentView(R.layout.dialog_tutorial);
        Button basicRulesButton = (Button) tutorialDialog.findViewById(R.id.basic_rules_button);
        Button advancedStrategiesButton = (Button) tutorialDialog.findViewById(R.id.advanced_strategies_button);
        tutorialDialog.show();


        // Setting listeners for the buttons:

        basicRulesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Play proper sound:
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Dismiss dialog:
                tutorialDialog.dismiss();

                // Start tutorial:
                startTutorialActivity(true);

            }
        });

        advancedStrategiesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Play proper sound:
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();

                // Dismiss dialog:
                tutorialDialog.dismiss();

                // Start tutorial:
                startTutorialActivity(false);

            }
        });


    }



    public void checkSavedOptions() {

        Locale locale;

        // Using shared preferences:
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.OPTIONS_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        mChosenLanguage = sharedPreferences.getString(Constants.CHOSEN_LANGUAGE, Constants.DEFAULT);
        mSoundStatus = sharedPreferences.getString(Constants.CHOSEN_SOUND_STATUS, Constants.DEFAULT);
        mVibratorStatus = sharedPreferences.getString(Constants.CHOSEN_VIBRATOR_STATUS, Constants.DEFAULT);

        // If language was never chosen before:
        if(mChosenLanguage.equals(Constants.DEFAULT)){
            // Set default language to English:
            editor.putString(Constants.CHOSEN_LANGUAGE, Constants.LOCALES_ARRAY[0]);
            editor.commit();
            locale = new Locale(Constants.LOCALES_ARRAY[0]);
        }
        // Otherwise, get the chosen language:
        else{
            locale = new Locale(mChosenLanguage);
        }

        // Setting configuration with the proper locale:
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());


        // If sound status was never chosen before:
        if(mSoundStatus.equals(Constants.DEFAULT)){
            // Set default sound to 'On':
            editor.putString(Constants.CHOSEN_SOUND_STATUS, Constants.ON);
            editor.commit();
            mSoundStatus = Constants.ON;
        }


        // If vibrator status was never chosen before:
        if(mVibratorStatus.equals(Constants.DEFAULT)){
            // Set default vibrator status to 'On':
            editor.putString(Constants.CHOSEN_VIBRATOR_STATUS, Constants.ON);
            editor.commit();
            mVibratorStatus = Constants.ON;
        }


    }



    private void createDifficultyDialog(){

        // Creating a dialog for the user so he could choose a difficulty, and getting the right views:
        final Dialog difficultyDialog = new Dialog(this, R.style.DialogTheme);
        difficultyDialog.setContentView(R.layout.dialog_difficulty);
        LinearLayout beginnerLayout = (LinearLayout) difficultyDialog.findViewById(R.id.beginner_layout);
        LinearLayout easyLayout = (LinearLayout) difficultyDialog.findViewById(R.id.easy_layout);
        LinearLayout mediumLayout = (LinearLayout) difficultyDialog.findViewById(R.id.medium_layout);
        LinearLayout hardLayout = (LinearLayout) difficultyDialog.findViewById(R.id.hard_layout);
        LinearLayout expertLayout = (LinearLayout) difficultyDialog.findViewById(R.id.expert_layout);
        difficultyDialog.show();

        // Setting up listeners for the buttons in the dialog:

        beginnerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();
                mChosenDifficulty = Difficulty.BEGINNER;
                createGamePieceDialog();
                difficultyDialog.dismiss();
            }
        });

        easyLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();
                mChosenDifficulty = Difficulty.EASY;
                createGamePieceDialog();
                difficultyDialog.dismiss();
            }
        });

        mediumLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();
                mChosenDifficulty = Difficulty.MEDIUM;
                createGamePieceDialog();
                difficultyDialog.dismiss();
            }
        });

        hardLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();
                mChosenDifficulty = Difficulty.HARD;
                createGamePieceDialog();
                difficultyDialog.dismiss();
            }
        });

        expertLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSoundStatus.equals(Constants.ON)) mFlipPieceSound.start();
                mChosenDifficulty = Difficulty.EXPERT;
                createGamePieceDialog();
                difficultyDialog.dismiss();
            }
        });


    }


    private void createGamePieceDialog() {

        // Creating a dialog for the user so he could choose a type of game piece, and getting the right views:
        final Dialog gamePieceDialog = new Dialog(this, R.style.DialogTheme);
        gamePieceDialog.setContentView(R.layout.dialog_game_piece);
        ImageButton blackButton = (ImageButton) gamePieceDialog.findViewById(R.id.black_piece);
        ImageButton whiteButton = (ImageButton) gamePieceDialog.findViewById(R.id.white_piece);
        gamePieceDialog.show();

        // Setting up listeners for the buttons in the dialog:

        blackButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Play proper sound:
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                gamePieceDialog.dismiss();

                // Start game v.s. A.i.:
                mChosenPiece = GamePiece.BLACK;
                startOfflineGame();

            }
        });

        whiteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Play proper sound:
                if(mSoundStatus.equals(Constants.ON)) mPutPieceSound.start();

                // Dismiss dialog:
                gamePieceDialog.dismiss();

                // Start game v.s. A.i.:
                mChosenPiece = GamePiece.WHITE;
                startOfflineGame();

            }
        });

    }


    private void startTutorialActivity(boolean isBasic){

        // Creating an intent:
        Intent intent = new Intent(this, TutorialActivity.class);

        // Creating a bundle for the intent and setting up the bundle:
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_BASIC_KEY, isBasic);

        // Put bundle in intent:
        intent.putExtra(TUTORIAL_BUNDLE_KEY, bundle);

        // Starting the Tutorial Activity:
        startActivity(intent);

    }


    private void startOfflineGame() {

        // Creating an intent:
        Intent gameIntent = new Intent(this, OfflineGameActivity.class);

        // Creating a bundle for the intent and setting up the bundle:
        Bundle bundle = new Bundle();
        bundle.putSerializable(DIFFICULTY_KEY, mChosenDifficulty);
        bundle.putSerializable(GAME_PIECE_KEY, mChosenPiece);

        // Put bundle in intent:
        gameIntent.putExtra(PRACTICE_GAME_BUNDLE_KEY, bundle);

        // Starting the Game Activity:
        startActivity(gameIntent);

    }


    private void startLoginProcess() {

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.AppTheme)
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .build(),
                RC_SIGN_IN);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the sign in response is a known one:
        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            // If successfully signed in:
            if (resultCode == ResultCodes.OK) {
                Toast.makeText(this, R.string.sign_in_succeeded, Toast.LENGTH_LONG).show();
                if(mConnectionButton != null) mConnectionButton.setText(R.string.log_out);
                return;
            }
            // Otherwise (if sign in failed):
            else {
                // If sign in cancelled (user pressed back button):
                if (response == null) {
                    Toast.makeText(this, R.string.sign_in_cancelled, Toast.LENGTH_LONG).show();
                    return;
                }
                // If there is no network connection:
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    return;
                }
                // If there is an unknown error:
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
                    return;
                }
            }

        }

        // Otherwise, the sign in response is unknown:
        Toast.makeText(this, R.string.unknown_sign_in_response, Toast.LENGTH_LONG).show();

    }







    @Override
    public void onBackPressed() {
        // Do nothing.
    }



}
