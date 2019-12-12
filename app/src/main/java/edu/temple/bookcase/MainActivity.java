package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements
        BookListFragment.BookListFragmentInterface, BookDetailsFragment.BookDetailsFragmentInterface,
        QueryBooksTask.QueryBooksTaskInterface, DownloadBookTask.DownloadBookTaskInterface{

    private final String LIST_FRAG_TAG = "book_list";
    private final String PAGER_FRAG_TAG = "book_pager";
    private final String DETAIL_FRAG_TAG = "book_detail";
    private final String BOOKS_LOADED_KEY = "books_loaded";
    private final String LIBRARY_KEY = "books_library";
    private final String SEARCHED_BOOKS_FILENAME = "books.dat";
    private final String SEARCH_STRING_KEY = "search_string";
    private final String AUDIOBOOK_KEY = "audiobook_playing";
    private final String AUDIOBOOK_DOWNLOAD_STATUS_KEY = "audiobook_download_status";
    private final String BOOK_API_URL = "https://kamorris.com/lab/audlib/booksearch.php";
    private final String BOOK_DOWNLOAD_API_URL = "https://kamorris.com/lab/audlib/download.php";
    private final String BOOK_SEARCH_PARAM = "search";

    private Library library;
    private BookDetailsFragment bookDetailFragment;
    private ViewPagerFragment viewPagerFragment;
    private BookListFragment bookListFragment;
    private EditText bookSearchEditText;
    private TextView nowPlayingTextView;
    private SeekBar bookSeekBar;
    private Intent serviceIntent;
    private String lastSearch;
    private BookDetailsFragment actionDetailFragment;

    //keeps track of current download status
    private boolean downloading = false;

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
                double percent = ((bookProgress.getProgress() / (audioBook.getDuration() + 0.0)) * 100.0);
                bookSeekBar.setProgress( (int)percent );
                audioBook.setProgress( bookProgress.getProgress() );
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
            downloading = savedInstanceState.getBoolean(AUDIOBOOK_DOWNLOAD_STATUS_KEY);
            library = savedInstanceState.getParcelable(LIBRARY_KEY);
            lastSearch = savedInstanceState.getString(SEARCH_STRING_KEY);
        }else{
            retrieveLibrary();
            retrieveSavedPreferences();
            loadBooks();
        }

        serviceIntent = new Intent(MainActivity.this, AudiobookService.class);
        bindService( serviceIntent, audiobookConnection, BIND_AUTO_CREATE);

        bookSearchEditText = findViewById(R.id.bookSearchEditText);
        bookSeekBar = findViewById(R.id.bookSeekBar);
        bookSeekBar.setMax(100);
        nowPlayingTextView = findViewById(R.id.nowPlayingTextView);
        nowPlayingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        nowPlayingTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        bookSearchEditText.setText(lastSearch);

        int numPane = 1;

        View v = findViewById(R.id.bookDetailLayout);
        if( v != null ){
            numPane = 2;
        }

        FragmentManager fm = getSupportFragmentManager();

        viewPagerFragment = (ViewPagerFragment) fm.findFragmentByTag(PAGER_FRAG_TAG);
        bookListFragment = (BookListFragment) fm.findFragmentByTag(LIST_FRAG_TAG);
        bookDetailFragment = (BookDetailsFragment) fm.findFragmentByTag(DETAIL_FRAG_TAG);

        if( numPane == 1 ){

            loadViewPagerFragment(fm);
        }else{

            loadBookListFragment(fm);
            loadBookDetailFragment(fm);
        }

        findViewById(R.id.bookSearchButton).setOnClickListener(v1 -> queryBooks( bookSearchEditText.getText().toString() ));
        findViewById(R.id.bookPauseButton).setOnClickListener(v1 -> pauseBook() );
        findViewById(R.id.bookStopButton).setOnClickListener(v1 -> stopBook() );
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
    protected void onDestroy(){
        super.onDestroy();
        unbindService(audiobookConnection);
        saveLibrary();
        savePreferences();
        savePreferences();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(BOOKS_LOADED_KEY, booksLoaded);
        outState.putParcelable(AUDIOBOOK_KEY, audioBook);
        outState.putBoolean(AUDIOBOOK_DOWNLOAD_STATUS_KEY, downloading);
        outState.putParcelable(LIBRARY_KEY, library);
        outState.putString(SEARCH_STRING_KEY,lastSearch);
    }

    private void pauseBook(){
        if( audioBook != null ) {
            Log.d("audiobook", "paused at " + audioBook.getProgress() + " seconds");
        }
        audiobookInterface.pause();
    }

    private void playBook(Book book){
        if( audiobookInterface != null ){
            audioBook = book;
            startService(serviceIntent);
            File file = getAudioBookFile( book.getFilename() );
            if( file.exists() ){
                Log.d("audiobook","playing from file");
                int resume = Math.max(book.getProgress() - 10,0);
                Log.d("audiobook","playing from " + resume + " seconds");
                audiobookInterface.play( file, resume );
            }else{
                audiobookInterface.play( book.getId() );
            }

            setPlayingBook();
        }
    }

    private void stopBook(){
        audiobookInterface.stop();
        stopService(serviceIntent);
        audioBook.setProgress( 0 );
        audioBook = null;
    }

    private void setPlayingBook(){
        if( nowPlayingTextView != null && audioBook != null ) {
            nowPlayingTextView.setText("Now playing " + audioBook.getTitle());
        }
    }

    private void loadViewPagerFragment(FragmentManager fm){
        if( viewPagerFragment  == null ) {
            viewPagerFragment = ViewPagerFragment.newInstance(library);
        }

        fm.beginTransaction()
                .replace(R.id.bookListLayout, viewPagerFragment, PAGER_FRAG_TAG)
                .commit();
    }

    private void loadBookListFragment(FragmentManager fm){
        if( bookListFragment  == null ) {
            bookListFragment = BookListFragment.newInstance(library);
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

        if( !booksLoaded && lastSearch.equals("") ){
            queryBooks("");
            return;
        }
    }

    //query books api
    private void queryBooks(String search) {

        lastSearch = search;

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

    //query download api and save resulting audio data
    private void downloadBook(Book book){
        try {
            Log.d("audiobook","Starting download");
            downloading = true;
            new DownloadBookTask(MainActivity.this).execute(
                    makeDownloadUrl(book)
                    , getExternalFilesDir(Environment.DIRECTORY_AUDIOBOOKS).getAbsolutePath()
                    , book.getFilename() );
        } catch (Exception e) {
            downloading = false;
            e.printStackTrace();
        }
    }

    //make search query
    private String makeQueryUrl(String search) throws UnsupportedEncodingException {
        String url = BOOK_API_URL;

        if( ! (search.trim()).isEmpty() ){
            url += "?" + BOOK_SEARCH_PARAM + "=" + urlEncode(search);
        }

        return url;
    }

    private String makeDownloadUrl(Book book) throws UnsupportedEncodingException {
        String url = BOOK_DOWNLOAD_API_URL;

        url += "?id=" + book.getId();

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

        library.clear();

        for(int i=0; i < result.length(); i++){
            try {
                library.addBook( new Book(result.getJSONObject(i)) );
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
            Log.d("downloadTask","starting download");
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
                if( library.getBookWithId(book.getId()) != null ){
                    return;
                }
                bookDetailFragment.clearBook();
            }
        }
    }

    private File getAudioBookFile(String filename){
        return new File(getExternalFilesDir(Environment.DIRECTORY_AUDIOBOOKS), filename);
    }

    private void saveLibrary(){
        File file = new File(getFilesDir(),SEARCHED_BOOKS_FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream( fos );
            oos.writeObject( library.getBooks() );
            oos.flush();
            oos.close();
            fos.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void retrieveLibrary(){
        File file = new File(getFilesDir(),SEARCHED_BOOKS_FILENAME);
        ArrayList<Book> books = new ArrayList<>();

        try {

            if( file.exists() ) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);

                books = (ArrayList<Book>) ois.readObject();
                ois.close();
                fis.close();

                library = new Library(books);
            }else{
                library = new Library();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void savePreferences(){
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(SEARCH_STRING_KEY, lastSearch);
        editor.commit();
    }

    private void retrieveSavedPreferences(){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        lastSearch = prefs.getString(SEARCH_STRING_KEY, "");
    }

    @Override
    public void onBookClicked(int index) {

        if( bookDetailFragment != null ){
            bookDetailFragment.displayBook( library.getBookAt(index) );
        }

    }

    @Override
    public void onPlayClicked(Book book){
        playBook(book);
    }

    @Override
    public void onDownloadClicked(Book book, BookDetailsFragment frag) {
        if( downloading ){
            Toast.makeText( this, "An audio book is already being downloaded.", Toast.LENGTH_SHORT).show();
        }else{
            actionDetailFragment = frag;
            downloadBook(book);
        }
    }

    @Override
    public void onDeleteClicked(Book book, BookDetailsFragment frag) {
        File file = getAudioBookFile(book.getFilename());

        if( file.delete() ){
            frag.storageChanged();
        }else{
            Toast.makeText( this, "could not delete audiobook", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQueryBooksTaskComplete(JSONArray result) {

        makeBooks(result);
    }

    @Override
    public void onDownloadBookTaskComplete(String result) {

        downloading = false;
        if( actionDetailFragment != null ) {
            actionDetailFragment.storageChanged();
            actionDetailFragment = null;
        }
        Toast.makeText( this, result, Toast.LENGTH_SHORT).show();
    }
}