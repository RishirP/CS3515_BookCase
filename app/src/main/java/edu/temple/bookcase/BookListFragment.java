package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment implements BookListAdapter.OnItemClickListener{

    private static final String ARG_PARAM_BOOK_NAME = "book_names";

    private Library library;

    private BookListFragmentInterface mActivity;

    private RecyclerView bookRecyclerView;
    private BookListAdapter bookAdapter;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(Library library) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_BOOK_NAME, library);
        fragment.setArguments(args);
        return fragment;
    }

    public Library getBooks(){
        return library;
    }

    public void notifyDataChanged(){
        if(bookRecyclerView != null){
            bookRecyclerView.removeAllViews();
            bookAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.library = getArguments().getParcelable(ARG_PARAM_BOOK_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);

        bookRecyclerView = v.findViewById(R.id.bookRecyclerView);
        bookRecyclerView.setLayoutManager( new LinearLayoutManager((Context)mActivity));

        bookAdapter = new BookListAdapter(library.getBooks());
        bookAdapter.setOnItemClickListener(this);
        bookRecyclerView.setAdapter( bookAdapter );

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
