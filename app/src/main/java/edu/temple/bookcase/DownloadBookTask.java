package edu.temple.bookcase;

import android.content.Context;
import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class DownloadBookTask extends AsyncTask<String, Void, String> {

    private DownloadBookTaskInterface mCallback;

    public DownloadBookTask(Context context){
        this.mCallback = (DownloadBookTaskInterface) context;
    }

    protected String doInBackground(String... params) {

        String result = "Download Complete";

        try {
            String url = params[0];
            String filepath = params[1];
            String filename = params[2];
            URL api = new URL(url);

            DataInputStream in = new DataInputStream( api.openStream() );

            String line;
            byte[] data;
            data = new byte[1024];
            int datalen;

            File file = new File(filepath,filename);
            FileOutputStream writer = new FileOutputStream(file);

            while( (datalen = in.read(data)) != -1 ){
                writer.write(data, 0, datalen);
            }

            writer.flush();
            writer.close();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            result = "Error: Could not download file.";
        }

        return result;
    }

    protected void onPostExecute(String result) {

        mCallback.onDownloadBookTaskComplete(result);
    }

    public interface DownloadBookTaskInterface {

        void onDownloadBookTaskComplete(String result);

    }
}