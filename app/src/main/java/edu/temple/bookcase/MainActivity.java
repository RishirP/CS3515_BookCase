package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListFragmentInterface {

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

        if( numPane == 1 ){
            ViewPagerFragment viewPagerFragment = (ViewPagerFragment) getSupportFragmentManager().findFragmentByTag("book_pager");
            if( viewPagerFragment  == null ) {
                viewPagerFragment = ViewPagerFragment.newInstance(books);
            }

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.bookListLayout, viewPagerFragment, "book_pager")
                    .commit();
        }else{

            FragmentManager fm = getSupportFragmentManager();

            BookListFragment bookListFragment = (BookListFragment) getSupportFragmentManager().findFragmentByTag("book_list");
            if( bookListFragment  == null ) {
                bookListFragment = BookListFragment.newInstance(books);
                fm.beginTransaction()
                        .replace(R.id.bookListLayout, bookListFragment, "book_list")
                        .commit();
            }

            bookDetailFragment = (BookDetailsFragment) getSupportFragmentManager().findFragmentByTag("book_detail");
            if( bookDetailFragment  == null ) {
                bookDetailFragment = BookDetailsFragment.newInstance("");
                fm.beginTransaction()
                        .add(R.id.bookDetailLayout, bookDetailFragment, "book_detail")
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