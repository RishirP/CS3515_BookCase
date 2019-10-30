package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment implements BookListAdapter.OnItemClickListener{

    private static final String ARG_PARAM_BOOK_NAME = "book_names";

    private ArrayList<String> books;

    private BookListFragmentInterface mActivity;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(ArrayList<String> book_names) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM_BOOK_NAME, book_names);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.books = getArguments().getStringArrayList(ARG_PARAM_BOOK_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);

        RecyclerView bookRecyclerView = v.findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager( new LinearLayoutManager((Context)mActivity));

        BookListAdapter bookAdapter = new BookListAdapter(this.books);
        bookAdapter.setOnItemClickListener(this);
        bookRecyclerView.setAdapter( bookAdapter );

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookListFragmentInterface) {
            mActivity = (BookListFragmentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookListFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onItemClick(int position) {
        mActivity.onBookClicked( position );
    }

    public interface BookListFragmentInterface {

        void onBookClicked(int index);
    }

}
