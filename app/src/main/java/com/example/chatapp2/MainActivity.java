package com.example.chatapp2;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public  class MainActivity extends AppCompatActivity {
   private Toolbar mToolbar;
   private ViewPager mViewPager;
   private TabLayout mTabLayout;
    TopBarAdapter topBarAdapter;
    private FirebaseUser mCurrentUser;
    private DatabaseReference reference;
    CircleImageView profileImage;
    TextView userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user;
                user=dataSnapshot.getValue(User.class);
                userName.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    //Set image from firebase storage
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        topBarAdapter=new TopBarAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(topBarAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    //init all element
    public void init(){
        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        mViewPager=findViewById(R.id.main_viewpager);
        mTabLayout=findViewById(R.id.main_tabs);
        profileImage=findViewById(R.id.profile_image);
        userName=findViewById(R.id.username);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:{
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            }
         }
        return false;
    }
    /*Set status online/offline of user into firebase*/
    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUser.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
