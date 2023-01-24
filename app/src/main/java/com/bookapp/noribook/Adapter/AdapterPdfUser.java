package com.bookapp.noribook.Adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPdfUser extends RecyclerView.Adapter implements Filterable {


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class HolderPdfUSer extends RecyclerView.ViewHolder{

        public HolderPdfUSer(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public Filter getFilter() {
        return null;
    }

}
