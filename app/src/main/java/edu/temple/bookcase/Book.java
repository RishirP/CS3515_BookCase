package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Book implements Parcelable{

    public static final int  EMPTY = -1;

    private int id;
    private String title;
    private String author;
    private int published;
    private String coverUrl;

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public Book(int id, String title, String author, int published, String coverUrl){
        this.id = id;
        this.title = title;
        this.author = author;
        this.published = published;
        this.coverUrl = coverUrl;
    }

    public Book(JSONObject args) throws JSONException {
        this.id = args.getInt("book_id");
        this.title = args.getString("title");
        this.author = args.getString("author");
        this.published = args.getInt("published");
        this.coverUrl = args.getString("cover_url");
    }

    // Parcelable implementation
    private Book(Parcel in){
        this.id = in.readInt();
        this.title = in.readString();
        this.author = in.readString();
        this.published = in.readInt();
        this.coverUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeInt(this.published);
        dest.writeString(this.coverUrl);
    }

    @NonNull
    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", published='" + published + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                '}';
    }

    // Getters
    public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public int getPublished(){
        return published;
    }

    public String getCoverUrl(){
        return coverUrl;
    }

}