package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

public class BlogActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        bottomNavigationView = findViewById(R.id.blog_bottom_nav);
        viewPager = findViewById(R.id.view_pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(),0);
        viewPager.setAdapter(adapter);



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Log.i("ID",item.getItemId()+"");
                switch (item.getItemId()){
                    case R.id.home:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.favorite:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.add_post:
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.account:
                        viewPager.setCurrentItem(3);
                        return true;
                    case R.id.chats:
                        viewPager.setCurrentItem(4);
                        return true;

                    default:
                        return false;
                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.favorite).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.add_post).setChecked(true);
                        break;
                    case 3:
                        bottomNavigationView.getMenu().findItem(R.id.account).setChecked(true);
                        break;
                    case 4:
                        bottomNavigationView.getMenu().findItem(R.id.chats).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}