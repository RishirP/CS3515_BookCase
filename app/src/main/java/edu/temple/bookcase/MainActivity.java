package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListFragmentInterface {

    private final String LIST_FRAG_TAG = "book_list";
    private final String PAGER_FRAG_TAG = "book_pager";
    private final String DETAIL_FRAG_TAG = "book_detail";

    private ArrayList<String> books;
    private BookDetailsFragment bookDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        String[] book_names = res.getStringArray(R.array.book_names_array);
        books = new ArrayList<String>(Arrays.asList(book_names));

        int numPane = 1;

        View v = findViewById(R.id.bookDetailLayout);
        if( v != null ){
            numPane = 2;
        }

        FragmentManager fm = getSupportFragmentManager();

        if( numPane == 1 ){
            ViewPagerFragment viewPagerFragment = (ViewPagerFragment) fm.findFragmentByTag(PAGER_FRAG_TAG);
            if( viewPagerFragment  == null ) {
                viewPagerFragment = ViewPagerFragment.newInstance(books);
            }

            fm.beginTransaction()
                    .replace(R.id.bookListLayout, viewPagerFragment, PAGER_FRAG_TAG)
                    .commit();
        }else{

            BookListFragment bookListFragment = (BookListFragment) fm.findFragmentByTag(LIST_FRAG_TAG);
            if( bookListFragment  == null ) {
                bookListFragment = BookListFragment.newInstance(books);
                fm.beginTransaction()
                        .replace(R.id.bookListLayout, bookListFragment, LIST_FRAG_TAG)
                        .commit();
            }

            bookDetailFragment = (BookDetailsFragment) fm.findFragmentByTag(DETAIL_FRAG_TAG);
            if( bookDetailFragment  == null ) {
                bookDetailFragment = BookDetailsFragment.newInstance("");
                fm.beginTransaction()
                        .add(R.id.bookDetailLayout, bookDetailFragment, DETAIL_FRAG_TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onBookClicked(int index) {

        if( bookDetailFragment != null ){
            bookDetailFragment.displayBook( books.get(index) );
        }

    }
}