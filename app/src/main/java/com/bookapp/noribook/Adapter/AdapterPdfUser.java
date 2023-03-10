package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.TextDetailActivity;
import com.bookapp.noribook.Filter.FilterPdfUser;
import com.bookapp.noribook.Model.ModelBook;
import com.bookapp.noribook.databinding.RowPdfUserBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;

    public ArrayList<ModelBook> pdfArrayList, filterList;

    private RowPdfUserBinding binding;

    private static final String TAG = "Adapter Pdf User TAG";

    private FilterPdfUser filter;


    public AdapterPdfUser(Context context, ArrayList<ModelBook> pdfArrayList) {
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
        ModelBook model = pdfArrayList.get(position);
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
        holder.viewCountTv.setText("?????????: "+viewCount);

        Glide.with(context)
                .load(url)
                .into(binding.bookIv);


        setSubNum(title, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TextDetailActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle", title);
                context.startActivity(intent);
            }
        });

    }

    private void setSubNum(String title, HolderPdfUser holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks");
        reference.child(title).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long subNumCount = snapshot.getChildrenCount();
                holder.subNumTv.setText("??? ??????: "+subNumCount);
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


    class HolderPdfUser extends RecyclerView.ViewHolder{


        ImageView bookIv;
        TextView titleTv, descriptionTv, categoryTv,dateTv, viewCountTv, subNumTv ;


        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            subNumTv = binding.subNumTv;
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