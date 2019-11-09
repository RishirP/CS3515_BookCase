package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Book> books;

    public ViewPagerAdapter(FragmentManager fm, ArrayList<Book> books ){
        super(fm);
        this.books = books;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return BookDetailsFragment.newInstance( this.books.get(position) );
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public int getCount() {
        return books.size();
    }
}
