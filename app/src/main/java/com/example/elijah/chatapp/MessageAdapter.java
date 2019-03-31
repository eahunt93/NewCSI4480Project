package com.example.elijah.chatapp;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by elija on 3/23/2019.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private List<Messages> mMessageList;
    private FirebaseAuth currentUser;

    private ImageView messageImage;
    DatabaseReference databaseReference;


    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_message);
            profileImage = (CircleImageView) view.findViewById(R.id.message_img);
            messageImage = (ImageView)view.findViewById(R.id.message_image_layout);


        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        currentUser = FirebaseAuth.getInstance();
        if(currentUser.getCurrentUser() != null) {

            Messages c = mMessageList.get(i);
            viewHolder.messageText.setText(c.getMessage());


            String fromUser = c.getFrom();
            String message_type = c.getType();

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUser);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();


                    Picasso.with(viewHolder.profileImage.getContext()).load(image).into(viewHolder.profileImage);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(message_type.equals("text")){
                viewHolder.messageText.setVisibility(View.VISIBLE);
                viewHolder.messageText.setText(c.getMessage());
                viewHolder.messageImage.setVisibility(View.INVISIBLE);

            }else if(message_type.equals("image")){
                viewHolder.messageText.setVisibility(View.INVISIBLE);
                viewHolder.messageImage.setVisibility(View.VISIBLE);



                Picasso.with(viewHolder.messageImage.getContext()).load(c.getMessage()).into(viewHolder.messageImage);
            }
            } else {
                Log.e("Current user equas null", "logout and log back in");
            }



    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



}
