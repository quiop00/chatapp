package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TopBarAdapter extends FragmentPagerAdapter {
    public TopBarAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:{
                ChatsFragment chats=new ChatsFragment();
                return chats;
            }
            case 1:{
                ContactsFragment contact=new ContactsFragment();
                return contact;
            }
            case 2:{
                ProfileFragment profile=new ProfileFragment();
                return profile;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chat";
            case 1:
                return "Find friends";
            case 2:
                return  "Profile";
        }
        return null;
    }
}
