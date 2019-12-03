package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements
        BookListFragment.BookListFragmentInterface, BookDetailsFragment.BookDetailsFragmentInterface,
        QueryBooksTask.QueryBooksTaskInterface{

    private final String LIST_FRAG_TAG = "book_list";
    private final String PAGER_FRAG_TAG = "book_pager";
    private final String DETAIL_FRAG_TAG = "book_detail";
    private final String BOOKS_LOADED_KEY = "books_loaded";
    private final String AUDIOBOOK_KEY = "audiobook_playing";
    private final String BOOK_API_URL = "https://kamorris.com/lab/audlib/booksearch.php";
    private final String BOOK_SEARCH_PARAM = "search";

    private ArrayList<Book> books = new ArrayList<Book>();
    private BookDetailsFragment bookDetailFragment;
    private ViewPagerFragment viewPagerFragment;
    private BookListFragment bookListFragment;
    private EditText bookSearchEditText;
    private TextView nowPlayingTextView;
    private SeekBar bookSeekBar;


    //keeps track of book api query results
    private boolean booksLoaded = false;

    //keeps track of currently playing audiobook
    private Book audioBook;

    private AudiobookService.MediaControlBinder  audiobookInterface;

    private Handler audiobookHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message mssg){
            AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) mssg.obj;
            if( bookProgress != null ) {
                int percent = (int)((bookProgress.getProgress() / (audioBook.getDuration() + 0.0)) * 100.0);
                bookSeekBar.setProgress( percent );
            }
        }
    };

    private ServiceConnection audiobookConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audiobookInterface = (AudiobookService.MediaControlBinder)service;
            audiobookInterface.setProgressHandler(audiobookHandler);
            if( audiobookInterface.isPlaying() ){
                setPlayingBook();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audiobookInterface = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if( savedInstanceState != null ) {
            booksLoaded = savedInstanceState.getBoolean(BOOKS_LOADED_KEY);
            audioBook = savedInstanceState.getParcelable(AUDIOBOOK_KEY);
        }

        bookSearchEditText = findViewById(R.id.bookSearchEditText);
        bookSeekBar = findViewById(R.id.bookSeekBar);
        bookSeekBar.setMax(100);
        nowPlayingTextView = findViewById(R.id.nowPlayingTextView);
        nowPlayingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        nowPlayingTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        bindService(new Intent(MainActivity.this, AudiobookService.class), audiobookConnection, BIND_AUTO_CREATE);

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

        findViewById(R.id.bookSearchButton).setOnClickListener(v1 -> queryBooks( bookSearchEditText.getText().toString() ));
        findViewById(R.id.bookPauseButton).setOnClickListener(v1 -> audiobookInterface.pause() );
        findViewById(R.id.bookStopButton).setOnClickListener(v1 -> {audiobookInterface.stop(); audioBook = null;} );
        bookSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if( fromUser && audioBook != null && audiobookInterface.isPlaying() ){
                    int skipTo = (int)((progress / 100.0) * audioBook.getDuration());
                    audiobookInterface.seekTo( skipTo );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(BOOKS_LOADED_KEY, booksLoaded);
        outState.putParcelable(AUDIOBOOK_KEY, audioBook);
    }

    private void setPlayingBook(){
        if( nowPlayingTextView != null && audioBook != null ) {
            nowPlayingTextView.setText("Now playing " + audioBook.getTitle());
        }
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
        Book book;
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
    public void onPlayClicked(Book book){
        if( audiobookInterface != null ){
            audioBook = book;
            audiobookInterface.play( book.getId() );
            setPlayingBook();
        }
    }

    @Override
    public void onQueryBooksTaskComplete(JSONArray result) {

        makeBooks(result);
    }
}