package edu.temple.bookcase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookListViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private ArrayList<String> data;
    private OnItemClickListener clickListener;

    public static class BookListViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public BookListViewHolder( View v ){
            super(v);
            textView = v.findViewById(R.id.bookTitle);
        }
    }

    public BookListAdapter(ArrayList<String> data){
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BookListAdapter.BookListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);

        return new BookListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookListViewHolder holder, final int position) {
        holder.textView.setText( data.get(position) );
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
