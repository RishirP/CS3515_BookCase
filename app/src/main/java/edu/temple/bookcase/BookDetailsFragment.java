package edu.temple.bookcase;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {


    private static final String ARG_PARAM_BOOK_TITLE = "book_title";

    private String book_title;

    private TextView bookTitleTextView;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(String book_title) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_BOOK_TITLE, book_title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.book_title = getArguments().getString(ARG_PARAM_BOOK_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_details, container, false);

        this.bookTitleTextView = v.findViewById(R.id.bookTitleTextView);

        this.bookTitleTextView.setText( this.book_title );
        this.bookTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);

        return v;
    }

    public void displayBook(String title){

        if( this.bookTitleTextView != null ){
            this.bookTitleTextView.setText( title );
        }
        Bundle args = this.getArguments();
        if( args == null ){
            args = new Bundle();
        }
        args.putString(ARG_PARAM_BOOK_TITLE, title);
        this.setArguments(args);
    }

}
