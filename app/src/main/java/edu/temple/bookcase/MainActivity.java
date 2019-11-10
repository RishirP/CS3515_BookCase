package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements
        BookListFragment.BookListFragmentInterface, QueryBooksTask.QueryBooksTaskInterface{

    private final String LIST_FRAG_TAG = "book_list";
    private final String PAGER_FRAG_TAG = "book_pager";
    private final String DETAIL_FRAG_TAG = "book_detail";
    private final String BOOKS_LOADED_KEY = "books_loaded";
    private final String BOOK_API_URL = "https://kamorris.com/lab/audlib/booksearch.php";
    private final String BOOK_SEARCH_PARAM = "search";

    private ArrayList<Book> books = new ArrayList<Book>();
    private BookDetailsFragment bookDetailFragment;
    private ViewPagerFragment viewPagerFragment;
    private BookListFragment bookListFragment;
    private EditText bookSearchEditText;

    //keeps track of book api query results
    private boolean booksLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if( savedInstanceState != null ) {
            booksLoaded = savedInstanceState.getBoolean(BOOKS_LOADED_KEY);
        }

        bookSearchEditText = findViewById(R.id.bookSearchEditText);

        int numPane = 1;

        View v = findViewById(R.id.bookDetailLayout);
        if( v != null ){
            numPane = 2;
        }

        FragmentManager fm = getSupportFragmentManager();

        viewPagerFragment = (ViewPagerFragment) fm.findFragmentByTag(PAGER_FRAG_TAG);
        bookListFragment = (BookListFragment) fm.findFragmentByTag(LIST_FRAG_TAG);
        bookDetailFragment = (BookDetailsFragment) fm.findFragmentByTag(DETAIL_FRAG_TAG);

        loadBooks();

        if( numPane == 1 ){

            loadViewPagerFragment(fm);
        }else{

            loadBookListFragment(fm);
            loadBookDetailFragment(fm);
        }

        findViewById(R.id.bookSearchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryBooks( bookSearchEditText.getText().toString() );
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(BOOKS_LOADED_KEY, booksLoaded);
    }

    private void loadViewPagerFragment(FragmentManager fm){
        if( viewPagerFragment  == null ) {
            viewPagerFragment = ViewPagerFragment.newInstance(books);
        }

        fm.beginTransaction()
                .replace(R.id.bookListLayout, viewPagerFragment, PAGER_FRAG_TAG)
                .commit();
    }

    private void loadBookListFragment(FragmentManager fm){
        if( bookListFragment  == null ) {
            bookListFragment = BookListFragment.newInstance(books);
            fm.beginTransaction()
                    .replace(R.id.bookListLayout, bookListFragment, LIST_FRAG_TAG)
                    .commit();
        }
    }

    private void loadBookDetailFragment(FragmentManager fm){
        if( bookDetailFragment  == null ) {
            bookDetailFragment = new BookDetailsFragment();
            fm.beginTransaction()
                    .add(R.id.bookDetailLayout, bookDetailFragment, DETAIL_FRAG_TAG)
                    .commit();
        }
    }

    //load books from fragments
    private void loadBooks(){

        if( !booksLoaded ){
            queryBooks("");
            return;
        }

        if(viewPagerFragment != null){
            books = viewPagerFragment.getBooks();
        }else if(bookListFragment != null){
            books = bookListFragment.getBooks();
        }
    }

    //query books api
    private void queryBooks(String search) {

        try {
            new QueryBooksTask(MainActivity.this).execute( makeQueryUrl(search) );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //query books api and wait for result
    private JSONArray queryBooksGet(String search) {
        JSONArray result = new JSONArray();
        try {
            result = new QueryBooksTask(MainActivity.this).execute( makeQueryUrl(search) ).get();

        } catch (ExecutionException | InterruptedException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    //make search query
    private String makeQueryUrl(String search) throws UnsupportedEncodingException {
        String url = BOOK_API_URL;

        if( ! (search.trim()).isEmpty() ){
            url += "?" + BOOK_SEARCH_PARAM + "=" + urlEncode(search);
        }

        return url;
    }

    private String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    //create book objects and update books array list
    //updates booksLoaded flag
    private void makeBooks(JSONArray result){
        if( result == null || containsError(result) ){
            booksLoaded = false;
            return;
        }

        books.clear();
        for(int i=0; i < result.length(); i++){
            try {
                books.add( new Book(result.getJSONObject(i)) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        booksLoaded = true;

        dataSetChanged();
    }

    //check if api result contains an error
    private Boolean containsError(JSONArray result){

        try {
            if( result != null && result.length() > 0 && result.getJSONObject(0).has("error") ){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    //update fragments as necessary
    private void dataSetChanged(){
        if(viewPagerFragment != null){
            viewPagerFragment.notifyDataChanged();
        }

        if(bookListFragment != null){
            bookListFragment.notifyDataChanged();
        }

        if(bookDetailFragment != null){

            Book book = bookDetailFragment.getBook();
            if( book != null ){
                if(bookInList(book.getId())){
                    return;
                }
                bookDetailFragment.clearBook();
            }
        }
    }

    //check if a book is present in the book array list
    private boolean bookInList(int id){
        for(int i = 0; i < books.size(); i++ ){
            if( id == books.get(i).getId() ){
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBookClicked(int index) {

        if( bookDetailFragment != null ){
            bookDetailFragment.displayBook( books.get(index) );
        }

    }

    @Override
    public void onQueryBooksTaskComplete(JSONArray result) {

        makeBooks(result);
    }
}