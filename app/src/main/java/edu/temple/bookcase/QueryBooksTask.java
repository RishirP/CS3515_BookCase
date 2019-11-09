package edu.temple.bookcase;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class QueryBooksTask extends AsyncTask<String, Void, JSONArray>{

    private QueryBooksTaskInterface mCallback;

    public QueryBooksTask(Context context){
        this.mCallback = (QueryBooksTaskInterface) context;
    }

    protected JSONArray doInBackground(String... urls) {

        StringBuilder result = new StringBuilder();
        JSONArray arrResult = null;

        try {
            URL api = new URL(urls[0]);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            api.openStream(), StandardCharsets.UTF_8
                    )
            );

            String line;

            while( (line = reader.readLine()) != null ){
                result.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            result.setLength(0);
            result.append( "[{\"error\":\"Could not query api.\"}]" );
        }

        try {
            arrResult = new JSONArray(result.toString());
        }catch (JSONException e){
            e.printStackTrace();
        }

        return arrResult;
    }

    protected void onPostExecute(JSONArray result) {

        mCallback.onQueryBooksTaskComplete(result);
    }

    public interface QueryBooksTaskInterface {

        void onQueryBooksTaskComplete(JSONArray result);

    }
}
