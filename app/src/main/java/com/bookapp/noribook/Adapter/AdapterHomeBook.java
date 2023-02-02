package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.PdfDetailActivity;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.RowHomeBookBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterHomeBook extends RecyclerView.Adapter<AdapterHomeBook.HolderHomeBook>  {

    private Context context;

    private ArrayList<ModelPdf> pdfArrayList;

    private RowHomeBookBinding binding;

    public AdapterHomeBook(Context context, ArrayList<ModelPdf> pdfArrayList) {
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
        ModelPdf model = pdfArrayList.get(position);
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




        MyApplication.loadPdfFromUrl(""+pdfUrl,holder.pdfView,holder.progressBar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle", title);
                context.startActivity(intent);
            }
        });

        holder.pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle", title);
                context.startActivity(intent);
            }
        });

        holder.categoryTv.setText(categoryTitle);
        holder.titleTv.setText(title);
        holder.viewCountTv.setText(""+viewCount);



    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderHomeBook extends RecyclerView.ViewHolder{

        PDFView pdfView;

        ProgressBar progressBar;

        TextView titleTv, categoryTv, viewCountTv;


        public HolderHomeBook(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;

            titleTv = binding.titleTv;
            categoryTv = binding.categoryTv;
            viewCountTv = binding.viewCountTv;

        }
    }
}
