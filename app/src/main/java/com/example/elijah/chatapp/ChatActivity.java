package com.example.elijah.chatapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String User;
    private Toolbar Toolbar;

    private DatabaseReference Ref;

    private TextView TitleView;
    private TextView LastSeenView;
    private CircleImageView ProfileImage;
    private FirebaseAuth Auth;
    private String CurrentUserId;

    private Button mChatAddBtn;
    private Button mChatSendBtn;
    private EditText mChatMessageView;


    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private PublicKey publicKey;

    String priveKey;

    PrivateKey privateKey;
    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;

    private String myPublicKey;
    private PublicKey mPublicKey;

    SecretKey uAesKey = null;



    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    private DatabaseHelper databaseHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(Toolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        Ref = FirebaseDatabase.getInstance().getReference();
        Auth = FirebaseAuth.getInstance();
        CurrentUserId = Auth.getCurrentUser().getUid();

        //Getting friend information from the FriendsFragment
        User = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        String PublicKey = getIntent().getStringExtra("pubKey");


        //front end stuff
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);



        //menu bar stuff
        TitleView = (TextView) findViewById(R.id.custom_bar_title);
        LastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        ProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        //getting variables for UI
        mChatAddBtn = (Button) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (Button) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        //creating message adapter
        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        //image storage.
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //setting the user seen status to true on firebase
        Ref.child("Chat").child(CurrentUserId).child(User).child("seen").setValue(true);

        //loads all messages between friend and user
        loadMessages();

        //Adds the friends name to the title
        TitleView.setText(userName);

        //After we get the friends public key from the FriendsFragment we need to put in back in public key form.
        publicKey = null;
        try {
            publicKey=   DHcryptography.loadPublicKey(PublicKey);
            Log.e(userName + "s public Key", publicKey.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        //We retrieve our private key from the private file we stored it in when we signed up
        FileInputStream fin = null;
        try {
            fin = openFileInput("PrivateKeyFile");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int c;
        String temp="";
        try {
            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //We  then load the private key back into its original format
        priveKey = temp;
        privateKey = null;
        try {
          privateKey =  DHcryptography.loadPrivateKey(temp);
          Log.e("My Private Key", privateKey.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        //we tpass our private key and the other users public key to create an AES key.
          uAesKey = DHcryptography.generateAESKey(privateKey, publicKey);
        Log.e("Generated AES key", uAesKey.toString());





        Ref.child("Users").child(User).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                String lastseen = dataSnapshot.child("image").getValue().toString();
                myPublicKey = dataSnapshot.child("PublicKey").getValue().toString();
                try {
                    mPublicKey = Cryptography.loadPublicKey(myPublicKey);
                    Log.e("Keyload Messaging",mPublicKey.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }


                //https://square.github.io/picasso/
                Picasso.with(getBaseContext()).load(image).placeholder(R.drawable.common_google_signin_btn_icon_light).into(ProfileImage);

                if(online.equals("true")) {

                    LastSeenView.setText("Online");

                } else  {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    LastSeenView.setText(lastSeenTime);

                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        Ref.child("Chat").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(User)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + CurrentUserId + "/" + User, chatAddMap);
                    chatUserMap.put("Chat/" + User + "/" + CurrentUserId, chatAddMap);

                    Ref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Pressing send button
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });



        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);


               startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);



            }
        });



        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();


            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){


            Uri imageUri = data.getData();

            Log.e("URI", imageUri.toString());



            final String current_user_ref = "messages/" + CurrentUserId + "/" + User;
            final String chat_user_ref = "messages/" + User + "/" + CurrentUserId;

            DatabaseReference user_message_push = Ref.child("messages")
                    .child(CurrentUserId).child(User).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();



                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", CurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);


                        mChatMessageView.setText("");

                        Ref.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = Ref.child("messages").child(CurrentUserId).child(User);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = Ref.child("messages").child(CurrentUserId).child(User);
        final DatabaseReference mPubKeyRef = Ref.child("Users").child("PublicKey");

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);



        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final Messages message = dataSnapshot.getValue(Messages.class);


                if(message.getType().equals("text")) {
                    message.setMessage(DHcryptography.decrypt(message.getMessage(), uAesKey));
                }

                itemPos++;
                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });//else


    }

    private void sendMessage() throws Exception {


        String message = mChatMessageView.getText().toString();
        String encrypted = DHcryptography.encrypt(message,uAesKey);



        if(!TextUtils.isEmpty(message)){
            Log.e("PlainText", message);
            String current_user_ref = "messages/" + CurrentUserId + "/" + User;
            String chat_user_ref = "messages/" + User + "/" + CurrentUserId;

            DatabaseReference user_message_push = Ref.child("messages")
                    .child(CurrentUserId).child(User).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", encrypted);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", CurrentUserId);



            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);



            mChatMessageView.setText("");

            Ref.child("Chat").child(CurrentUserId).child(User).child("seen").setValue(true);
            Ref.child("Chat").child(CurrentUserId).child(User).child("timestamp").setValue(ServerValue.TIMESTAMP);

            Ref.child("Chat").child(User).child(CurrentUserId).child("seen").setValue(false);
            Ref.child("Chat").child(User).child(CurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            Ref.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }
    }



}
