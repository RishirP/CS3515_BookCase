package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Book implements Parcelable, Serializable {

    public static final int  EMPTY = -1;

    private int id;
    private String title;
    private String author;
    private int published;
    private String coverUrl;
    private int duration;
    private String filename;
    private int progress;

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public Book(int id, String title, String author, int published, String coverUrl, int duration){
        this.id = id;
        this.title = title;
        this.author = author;
        this.published = published;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.progress = 0;
        makeFilename();
    }

    public Book(JSONObject args) throws JSONException {
        this.id = args.getInt("book_id");
        this.title = args.getString("title");
        this.author = args.getString("author");
        this.published = args.getInt("published");
        this.coverUrl = args.getString("cover_url");
        this.duration = args.getInt("duration");
        this.progress = 0;
        makeFilename();
    }

    // Parcelable implementation
    private Book(Parcel in){
        this.id = in.readInt();
        this.title = in.readString();
        this.author = in.readString();
        this.published = in.readInt();
        this.coverUrl = in.readString();
        this.duration = in.readInt();
        this.filename = in.readString();
        this.progress = in.readInt();
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
        dest.writeInt(duration);
        dest.writeString(filename);
        dest.writeInt(progress);
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
                ", duration='" + duration + '\'' +
                ", filename='" + filename + '\'' +
                ", progress='" + progress + '\'' +
                '}';
    }

    private void makeFilename(){
        filename = title.replaceAll("\\s+","_").toLowerCase() + ".mp3";
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

    public int getDuration(){ return duration; }

    public String getFilename(){ return filename; }

    public int getProgress(){ return progress; }

    //Setter
    public void setProgress(int progress){ this.progress = progress; }

}