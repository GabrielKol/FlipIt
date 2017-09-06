package com.sublime.gaby.flipit;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sublime.gaby.flipit.Logic.GamePiece;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Gaby on 3/27/2017.
 */

public class GameRoomActivity extends AppCompatActivity {

    // Fields used by the OnlineGameActivity if its a private match:
    public static final String PRIVATE_MATCH_BUNDLE_KEY = "PrivateMatchBundle";
    public static final String ROOM_NAME_KEY = "RoomName";
    public static final String PIECE_TYPE_KEY = "PieceType";

    // Components:
    private TextView mTitle;
    private TextView mRoomNameTitle;
    private TextView mRoomPasswordTitle;
    private EditText mRoomName;
    private EditText mRoomPassword;
    private Button mDoneButton;

    // Firebase fields:
    private FirebaseApp mApp = FirebaseApp.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance(mApp);
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance(mApp).getReference();
    private DatabaseReference mGameRoomsRef = mRootRef.child(Constants.PRIVATE_GAME_ROOMS);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transition:
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Setting content view:
        setContentView(R.layout.activity_game_room);

        // Setting up the components:
        setupComponents();

        // Setting up a banner ad:
        AdView adView = (AdView) findViewById(R.id.game_room_ad_view_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Setting up the keyboardListener:
        setUpKeyboardListener();

    }


    private void setUpKeyboardListener(){

        final View activityRootView = findViewById(R.id.room_layout);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();

                activityRootView.getWindowVisibleDisplayFrame(r);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;

                int heightDiff = height - (r.bottom - r.top);

                if (heightDiff > 100) {
                    // Keyboard is shown.
                    mTitle.setVisibility(View.GONE);
                    mDoneButton.setVisibility(View.GONE);
                    if(mRoomName.isFocused()){
                        mRoomPasswordTitle.setVisibility(View.GONE);
                        mRoomPassword.setVisibility(View.GONE);
                    }
                    if(mRoomPassword.isFocused()){
                        mRoomNameTitle.setVisibility(View.GONE);
                        mRoomName.setVisibility(View.GONE);
                    }
                }
                else{
                    // Keyboard is hidden.
                    mTitle.setVisibility(View.VISIBLE);
                    mRoomNameTitle.setVisibility(View.VISIBLE);
                    mRoomName.setVisibility(View.VISIBLE);
                    mRoomPasswordTitle.setVisibility(View.VISIBLE);
                    mRoomPassword.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                }
            }
        });


    }



    private void setupComponents(){

        mTitle = (TextView) findViewById(R.id.enter_room_title);
        mRoomNameTitle = (TextView) findViewById(R.id.room_name_title);
        mRoomPasswordTitle = (TextView) findViewById(R.id.room_password_title);
        mRoomName = (EditText) findViewById(R.id.room_name_editable);
        mRoomPassword = (EditText) findViewById(R.id.room_password_editable);
        mDoneButton = (Button) findViewById(R.id.done_button);

        // Setting up a listener for the Done Button:
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String roomName = mRoomName.getText().toString();

                if(roomName.length() < 3){
                    Toast.makeText(GameRoomActivity.this, R.string.room_name_too_short, Toast.LENGTH_SHORT).show();
                    return;
                }

                final String roomPassword = mRoomPassword.getText().toString();

                if(roomPassword.length() < 3){
                    Toast.makeText(GameRoomActivity.this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Preparing for a match:
                prepareForMatch(roomName, roomPassword);

            }
        });

    }

    private void prepareForMatch(final String roomName, final String roomPassword){

        // Adding a listener to the room with the chosen name in order to read data only once:
        mGameRoomsRef.child(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If room exists:
                if (dataSnapshot.exists()) {

                    // If password is correct:
                    String correctPassword = dataSnapshot.child(Constants.PASSWORD).getValue(String.class);
                    if(roomPassword.equals(correctPassword)) {

                        // If that room got one more place:
                        boolean areBothConnected = dataSnapshot.child(Constants.ARE_BOTH_CONNECTED).getValue(Boolean.class);
                        if(areBothConnected == false) {

                            mGameRoomsRef.child(roomName).child(Constants.ARE_BOTH_CONNECTED).setValue(true);

                            Uri profilePicture = mAuth.getCurrentUser().getPhotoUrl();

                            final DatabaseReference playerRef = mGameRoomsRef.child(roomName).child(Constants.PLAYER_WHITE);
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
                                                // Start the private match (enter the room):
                                                startOnlineGame(roomName, GamePiece.WHITE);
                                            }
                                            // Otherwise:
                                            else{
                                                // This room is already full:
                                                Toast.makeText(GameRoomActivity.this, R.string.full_room, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}

                                    });

                                }
                            }, 600); // Delay that gives enough time for Firebase database to update.

                        }

                        // Otherwise, if this room is already full:
                        else Toast.makeText(GameRoomActivity.this, R.string.full_room, Toast.LENGTH_SHORT).show();

                    }

                    // Otherwise, if password is not correct:
                    else Toast.makeText(GameRoomActivity.this, R.string.wrong_password, Toast.LENGTH_SHORT).show();

                }

                // Otherwise, if room doesn't exist:
                else{

                    mGameRoomsRef.child(roomName).child(Constants.PASSWORD).setValue(roomPassword);
                    mGameRoomsRef.child(roomName).child(Constants.ARE_BOTH_CONNECTED).setValue(false);

                    Uri profilePicture = mAuth.getCurrentUser().getPhotoUrl();

                    final DatabaseReference playerRef = mGameRoomsRef.child(roomName).child(Constants.PLAYER_BLACK);
                    playerRef.child(Constants.USER_ID).setValue(mAuth.getCurrentUser().getUid());
                    playerRef.child(Constants.NAME).setValue(mAuth.getCurrentUser().getDisplayName());
                    if(profilePicture != null) playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(profilePicture.toString());
                    else playerRef.child(Constants.PORTRAIT_URI_STRING).setValue(Constants.NO_PICTURE);
                    playerRef.child(Constants.MOVE_POSITION).setValue(-1);
                    playerRef.child(Constants.EMOTE_INDEX).setValue(-1);
                    playerRef.child(Constants.IS_POKING).setValue(false);
                    playerRef.child(Constants.HAS_SURRENDERED).setValue(false);

                    DatabaseReference opponentRef = mGameRoomsRef.child(roomName).child(Constants.PLAYER_WHITE);
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
                                        // Start the private match (enter the room):
                                        startOnlineGame(roomName, GamePiece.BLACK);
                                    }
                                    // Otherwise:
                                    else{
                                        // Keep trying:
                                        prepareForMatch(roomName, roomPassword);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}

                            });

                        }
                    }, 600); // Delay that gives enough time for Firebase database to update.

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

    }


    private void startOnlineGame(String roomName, GamePiece gamePiece){

        // Creating an intent:
        Intent gameIntent = new Intent(this, OnlineGameActivity.class);

        // Creating a bundle for the intent and setting up the bundle:
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_NAME_KEY, roomName);
        bundle.putSerializable(PIECE_TYPE_KEY, gamePiece);

        // Put bundle in intent:
        gameIntent.putExtra(PRIVATE_MATCH_BUNDLE_KEY, bundle);

        // Starting the Game Activity:
        startActivity(gameIntent);

    }




    @Override
    public void onBackPressed() {

        // Go back to main menu:

        // Creating an intent:
        Intent mainIntent = new Intent(this, MainActivity.class);
        // Starting the Main Activity:
        startActivity(mainIntent);

    }



}
