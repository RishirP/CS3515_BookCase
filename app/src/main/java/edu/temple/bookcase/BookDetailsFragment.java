package edu.temple.bookcase;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    private static final String ARG_PARAM_BOOK = "book_detail";
    private Book book;

    private TextView bookTitleTextView;
    private TextView bookAuthorTextView;
    private TextView bookPublishedTextView;
    private ImageView bookCoverImageView;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(Book book){
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_BOOK, book);
        fragment.setArguments(args);
        return fragment;
    }

    public Book getBook(){
        return book;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.book = getArguments().getParcelable(ARG_PARAM_BOOK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_details, container, false);

        bookTitleTextView = v.findViewById(R.id.bookTitleTextView);
        bookAuthorTextView = v.findViewById(R.id.bookAuthorTextView);
        bookPublishedTextView = v.findViewById(R.id.bookPublishedTextView);
        bookCoverImageView = v.findViewById(R.id.bookImageView);

        if( book != null ) {
            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthor());
            bookPublishedTextView.setText(String.valueOf(book.getPublished()));
            Picasso.get().load(book.getCoverUrl()).into(bookCoverImageView);
        }

        bookTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,28);
        bookAuthorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        bookPublishedTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);

        return v;
    }

    public void displayBook(Book book){

        this.book = book;

        if( bookTitleTextView != null ){
            bookTitleTextView.setText( book.getTitle() );
        }

        if( bookAuthorTextView != null ){
            bookAuthorTextView.setText( book.getAuthor() );
        }

        if( bookPublishedTextView != null ){
            bookPublishedTextView.setText( String.valueOf(book.getPublished()) );
        }

        if( bookCoverImageView != null ){
            Picasso.get().load(book.getCoverUrl()).into(bookCoverImageView);
        }

        Bundle args = this.getArguments();
        if( args == null ){
            args = new Bundle();
        }
        args.putParcelable(ARG_PARAM_BOOK, book);
        this.setArguments(args);
    }

    public void clearBook(){

        this.book = null;

        if( bookTitleTextView != null ){
            bookTitleTextView.setText( "" );
        }

        if( bookAuthorTextView != null ){
            bookAuthorTextView.setText( "" );
        }

        if( bookPublishedTextView != null ){
            bookPublishedTextView.setText( "" );
        }

        if( bookCoverImageView != null ){
            bookCoverImageView.setImageResource(0);
        }

        Bundle args = this.getArguments();
        if( args != null ){
            args.remove(ARG_PARAM_BOOK);
            this.setArguments(args);
        }
    }

}
