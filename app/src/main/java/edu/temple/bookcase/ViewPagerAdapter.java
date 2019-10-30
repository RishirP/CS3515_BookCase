package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> books;

    public ViewPagerAdapter(FragmentManager fm, ArrayList<String> books ){
        super(fm);
        this.books = books;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return BookDetailsFragment.newInstance( this.books.get(position) );
    }

    @Override
    public int getCount() {
        return books.size();
    }
}
