package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.PdfDetailActivity;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.RowPdfFavoriteBinding;
import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterFavorite extends RecyclerView.Adapter<AdapterFavorite.HolderFavorite> {

    private Context context;

    private RowPdfFavoriteBinding binding;

    private ArrayList<ModelPdf> pdfArrayList;

    public AdapterFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFavorite holder, int position) {

        ModelPdf model = pdfArrayList.get(position);


        loadBookDetails(model, holder);

        // item view -> pdf detail view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                intent.putExtra("bookTitle", model.getTitle());
                context.startActivity(intent);
            }
        });

        // remove fab btn
        holder.removeFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context, model.getId(), model.getTitle());
            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderFavorite holder) {
        String bookTitle = model.getTitle();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String categoryTitle = ""+snapshot.child("categoryTitle").getValue();
                        String date = ""+snapshot.child("date").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        long viewCount = Long.parseLong(""+snapshot.child("viewCount").getValue());
                        String url = ""+snapshot.child("url").getValue();
                        String bookid = ""+snapshot.child("id").getValue();

                        // long viewCount = Long.parseLong(viewCounts);
                        model.setFavorite(true);
                        model.setCategoryTitle(categoryTitle);
                        model.setDate(date);
                        model.setDescription(description);
                        model.setTitle(title);
                        model.setViewCount(viewCount);
                        model.setUrl(url);
                        model.setId(bookid);

                        Glide.with(context)
                                .load(url)
                                .into(binding.bookIv);


                        holder.titleTv.setText(title);
                        holder.dateTv.setText(date);
                        holder.descriptionTv.setText(description);
                        holder.categoryTv.setText(categoryTitle);
                        holder.viewCountTv.setText(""+viewCount);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderFavorite extends RecyclerView.ViewHolder {

        ImageView bookIv;

        TextView titleTv, descriptionTv, dateTv, viewCountTv, recommendCountTv, categoryTv ;

        ImageButton removeFabBtn;

        public HolderFavorite(@NonNull View itemView) {
            super(itemView);

            bookIv = binding.bookIv;
            dateTv = binding.dateTv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            viewCountTv = binding.viewCountTv;
            recommendCountTv = binding.recommendCountTv;
            categoryTv = binding.categoryTv;
            removeFabBtn = binding.removeFavBtn;

        }
    }
}
