package com.example.elijah.chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView mDisplayID, mProfileName, mProfileStatus, ProfileFriendCount;
    private ImageView mProfileImageView;
    private Button mFriendRequestBtn, declineFriendReqBtn;

    private DatabaseReference currentReference;
    private ProgressDialog progressDialog;
    private DatabaseReference FriendsReqReference;
    private DatabaseReference FriendsReference;
    private DatabaseReference notificationDB;

    private FirebaseUser currentUser;

    private String current_state;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        rootRef = FirebaseDatabase.getInstance().getReference();

        currentReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        FriendsReqReference = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationDB = FirebaseDatabase.getInstance().getReference().child("notifications");

        mProfileName = (TextView)findViewById(R.id.DisplayName);
        mProfileStatus = (TextView)findViewById(R.id.ProfileStatus);
        ProfileFriendCount = (TextView)findViewById(R.id.TotalFriends);
        mProfileImageView = (ImageView)findViewById(R.id.ProfileImage);
        mFriendRequestBtn = (Button)findViewById(R.id.FriendRequestbtn);
        declineFriendReqBtn = (Button)findViewById(R.id.DeclineFriendRequestbtn);



        current_state = "not_friends";

        declineFriendReqBtn.setVisibility(View.INVISIBLE);
        declineFriendReqBtn.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("User information loading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        currentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.common_google_signin_btn_icon_light).into(mProfileImageView);

                if(currentUser.getUid().equals(user_id)){

                    declineFriendReqBtn.setEnabled(false);
                    declineFriendReqBtn.setVisibility(View.INVISIBLE);

                    mFriendRequestBtn.setEnabled(false);
                    mFriendRequestBtn.setVisibility(View.INVISIBLE);

                }


                //--------------- FRIENDS LIST / REQUEST FEATURE -----

                FriendsReqReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            Log.e("Request Type", req_type);

                            if(req_type.equals("received")){

                                current_state = "req_received";
                                mFriendRequestBtn.setText("Accept Friend Request");

                                declineFriendReqBtn.setVisibility(View.VISIBLE);
                                declineFriendReqBtn.setEnabled(true);


                            } else if(req_type.equals("sent")) {

                                current_state = "req_sent";
                                mFriendRequestBtn.setText("Cancel Friend Request");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);

                            }

                            progressDialog.dismiss();


                        } else {
                            FriendsReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        current_state = "friends";
                                        mFriendRequestBtn.setText("Unfriend this Person");

                                        declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                        declineFriendReqBtn.setEnabled(false);

                                    }

                                    progressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();
                                    Log.e("Current state", current_state);

                                }
                            });

                        }




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.e("Current state", current_state);
        mFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFriendRequestBtn.setEnabled(false);

                // --------------- NOT FRIENDS STATE ------------

                if(current_state.equals("not_friends")){


                    DatabaseReference newNotificationref = rootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + currentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            }

                                current_state = "req_sent";
                                mFriendRequestBtn.setText("Cancel Friend Request");
                                mFriendRequestBtn.setEnabled(true);




                        }
                    });

                }


                // - -------------- CANCEL REQUEST STATE ------------

                if(current_state.equals("req_sent")){

                    FriendsReqReference.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            FriendsReqReference.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendRequestBtn.setEnabled(true);
                                    current_state = "not_friends";
                                    mFriendRequestBtn.setText("Send Friend Request");

                                    declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                    declineFriendReqBtn.setEnabled(false);


                                }
                            });

                        }
                    });

                }


                // ------------ REQ RECEIVED STATE ----------

                if(current_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + currentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + currentUser.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + currentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + currentUser.getUid(), null);


                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mFriendRequestBtn.setEnabled(true);
                                current_state = "friends";
                                mFriendRequestBtn.setText("Unfriend this Person");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });

                }


                // ------------ UNFRIENDS ---------

                if(current_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + currentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + currentUser.getUid(), null);

                    rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                current_state = "not_friends";
                                mFriendRequestBtn.setText("Send Friend Request");

                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                declineFriendReqBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            mFriendRequestBtn.setEnabled(true);
                            Log.e("current state",current_state);

                        }
                    });

                }


            }

        });

    }

}
