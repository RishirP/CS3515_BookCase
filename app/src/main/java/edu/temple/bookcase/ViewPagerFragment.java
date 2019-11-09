package edu.temple.bookcase;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPagerFragment extends Fragment {

    private static final String ARG_PARAM_BOOKS = "books";

    private ArrayList<Book> books;
    private PagerAdapter pagerAdapter;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    public static ViewPagerFragment newInstance(ArrayList<Book> books) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM_BOOKS, books);
        fragment.setArguments(args);
        return fragment;
    }

    public ArrayList<Book> getBooks(){
        return books;
    }

    public void notifyDataChanged(){
        if( pagerAdapter != null ){
            pagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.books = getArguments().getParcelableArrayList(ARG_PARAM_BOOKS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_pager, container, false);

        ViewPager viewPager = v.findViewById(R.id.bookDetailViewPager);
        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), this.books);
        viewPager.setAdapter( pagerAdapter );

        return v;
    }
}
