package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.TextDetailActivity;
import com.bookapp.noribook.Model.ModelBook;
import com.bookapp.noribook.databinding.RowHomeBookBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdapterHomeBook extends RecyclerView.Adapter<AdapterHomeBook.HolderHomeBook>  {

    private Context context;

    private ArrayList<ModelBook> pdfArrayList;

    private RowHomeBookBinding binding;

    public AdapterHomeBook(Context context, ArrayList<ModelBook> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }


    @NonNull
    @Override
    public HolderHomeBook onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowHomeBookBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderHomeBook(binding.getRoot());


    }

    @Override
    public void onBindViewHolder(@NonNull HolderHomeBook holder, int position) {
        ModelBook model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        String categoryTitle = model.getCategoryTitle();
        String date = model.getDate();
        String id = model.getId();
        String uid = model.getUid();
        long viewCount = model.getViewCount();
        long recommendCount = model.getRecommendCount();


        Glide.with(context)
                .load(pdfUrl)
                .into(binding.bookIv);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TextDetailActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle", title);
                context.startActivity(intent);
            }
        });


        holder.categoryTv.setText(categoryTitle);
        holder.titleTv.setText(title);
        holder.viewCountTv.setText("조회수:"+viewCount);
//        holder.favoriteCountTv.setText("선작수:"+recommendCount);



    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderHomeBook extends RecyclerView.ViewHolder{


        ImageView bookIv;

        TextView titleTv, categoryTv, viewCountTv, favoriteCountTv;


        public HolderHomeBook(@NonNull View itemView) {
            super(itemView);

            bookIv = binding.bookIv;

            titleTv = binding.titleTv;
            categoryTv = binding.categoryTv;
            viewCountTv = binding.viewCountTv;
//            favoriteCountTv = binding.favoriteCountTv;

        }
    }
}
