package com.example.elijah.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private RecyclerView recyclerView;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.UsersBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        recyclerView = (RecyclerView)findViewById(R.id.UsersList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                databaseReference

        ) {

            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumb_image(), getApplicationContext());

                final String user_id = getRef(position).getKey();


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent ProfileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        ProfileIntent.putExtra("user_id", user_id);
                        startActivity(ProfileIntent);

                    }
                });

            }
        };

       firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){

            TextView usernameView = (TextView) mView.findViewById(R.id.profilename);
            usernameView.setText(name);
        }

        public void setStatus(String status){
            TextView userStatusView = (TextView)mView.findViewById(R.id.userprofile);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image, Context context) {
            CircleImageView pic = (CircleImageView)mView.findViewById(R.id.image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.ic_launcher_background).into(pic);
        }
    }
}
