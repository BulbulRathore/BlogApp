package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                //home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                //fav
                FavoriteFragment favoriteFragment = new FavoriteFragment();
                return favoriteFragment;
            case 2:
                //account
                AddPostFragment addPostFragment = new AddPostFragment();
                return addPostFragment;

            case 3:
                //account
                AccountFragment accountFragment = new AccountFragment();
                return accountFragment;

            case 4:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}
