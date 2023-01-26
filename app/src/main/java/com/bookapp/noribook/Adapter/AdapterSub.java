package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.PdfViewActivity2;
import com.bookapp.noribook.Model.ModelSub;
import com.bookapp.noribook.databinding.RowPdfSubBinding;

import java.util.ArrayList;

public class AdapterSub extends RecyclerView.Adapter<AdapterSub.HolderSub> {

    private Context context;

    private ArrayList<ModelSub> subArrayList;

    private RowPdfSubBinding binding;

    public AdapterSub(Context context, ArrayList<ModelSub> subArrayList) {
        this.context = context;
        this.subArrayList = subArrayList;
    }

    @NonNull
    @Override
    public HolderSub onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfSubBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderSub(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderSub holder, int position) {
        ModelSub model = subArrayList.get(position);
        String uid = model.getUid();
        String subNumber =model.getSubNumber();
        String subTitle = model.getSubTitle();
        String date = model.getDate();
        String bookTitle = model.getBookTitle();
        String url = model.getUrl();
        long viewCount = model.getViewCount();
        long recommendCount = model.getRecommendCount();

        holder.subNumberTv.setText(subNumber);
        holder.subTitleTv.setText(subTitle);
        holder.dateTv.setText(date);
        holder.viewCountTv.setText(""+viewCount);
        holder.recommendTv.setText(""+recommendCount);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfViewActivity2.class);
                intent.putExtra("bookTitle", bookTitle);
                intent.putExtra("subTitle", subTitle);
                intent.putExtra("subNumber",subNumber);
                context.startActivity(intent);

            }
        });


    }

    @Override
    public int getItemCount() {
        return subArrayList.size();
    }

    class HolderSub extends RecyclerView.ViewHolder{

        TextView subNumberTv, subTitleTv, dateTv, viewCountTv, recommendTv;

        public HolderSub(@NonNull View itemView) {
            super(itemView);

            viewCountTv = binding.viewCountTv;
            recommendTv = binding.recommendTv;
            dateTv = binding.dateTv;
            subTitleTv = binding.subTitleTv;
            subNumberTv = binding.subNumberTv;

        }
    }
}
