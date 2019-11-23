package com.example.chatapp2;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
public  class MainActivity extends AppCompatActivity {
   private Toolbar mToolbar;
   private ViewPager mViewPager;
   private TabLayout mTabLayout;
    TopBarAdapter topBarAdapter;
    private FirebaseUser mCurrentUser;
    private DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat");
        topBarAdapter=new TopBarAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(topBarAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    //init all element
    public void init(){
        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        mViewPager=findViewById(R.id.main_viewpager);
        mTabLayout=findViewById(R.id.main_tabs);
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
                startActivity(new Intent(MainActivity.this,StartActivity.class));
                finish();
                return true;
    }
}
        return false;
    }
}
