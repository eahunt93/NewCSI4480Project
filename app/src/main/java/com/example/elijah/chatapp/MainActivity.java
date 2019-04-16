package com.example.elijah.chatapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth Auth;
    private android.support.v7.widget.Toolbar toolbar;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout tabLayout;
    private DatabaseReference userRef;
    private DatabaseReference pubkey;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Auth = FirebaseAuth.getInstance();

        toolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("EllipticCurveChatApp");

        //If user is signed in get their user ID. Else send them to the start activity
        if(Auth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Auth.getCurrentUser().getUid());
        }else{
            sendToStart();
        }






        viewPager = (ViewPager)findViewById(R.id.viewpager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout = (TabLayout)findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = Auth.getCurrentUser();

        if(currentUser ==null){
            sendToStart();
        }else{
            userRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = Auth.getCurrentUser();

    }

    //Method to send user to start
    private void sendToStart() {
        Intent StartIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(StartIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.main_menu, menu);
      return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       super.onOptionsItemSelected(item);
       if(item.getItemId() == R.id.main_logout_button){
           FirebaseAuth.getInstance().signOut();
           sendToStart();
       }

       if(item.getItemId() == R.id.MainSettingsButton){
           Intent settngsIntent = new Intent(MainActivity.this,SettingsActivity.class);
           startActivity(settngsIntent);
        }

        if(item.getItemId() == R.id.MainAllButton){
           Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
           startActivity(usersIntent);
        }

       return true;
    }
}
