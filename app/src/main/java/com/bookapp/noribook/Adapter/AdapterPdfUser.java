package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.PdfDetailActivity;
import com.bookapp.noribook.Filter.FilterPdfUser;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.RowPdfUserBinding;
import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;

    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private RowPdfUserBinding binding;

    private static final String TAG = "Adapter Pdf User TAG";

    private FilterPdfUser filter;


    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }



    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        // get data
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String url = model.getUrl();
        String categoryId = model.getCategoryId();
        String categoryTitle = model.getCategoryTitle();
        String date = model.getDate();
        String id = model.getId();
        String uid = model.getUid();
        long viewCount = model.getViewCount();

        // set Data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);
        holder.categoryTv.setText(categoryTitle);
        holder.viewCountTv.setText(""+viewCount);

        Glide.with(context)
                .load(url)
                .into(binding.bookIv);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle", title);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }


    class HolderPdfUser extends RecyclerView.ViewHolder{


        ImageView bookIv;
        TextView titleTv, descriptionTv, categoryTv,dateTv, viewCountTv ;


        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            viewCountTv = binding.viewCountTv;
            bookIv = binding.bookIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv =  binding.categoryTv;
            dateTv = binding.dateTv;
        }
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }
}