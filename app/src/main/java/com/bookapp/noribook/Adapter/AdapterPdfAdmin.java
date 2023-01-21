package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Filter.FilterCategory;
import com.bookapp.noribook.Filter.FilterPdfAdmin;
import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.databinding.RowCategoryBinding;
import com.bookapp.noribook.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// 2. extend
public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdf> implements Filterable {

// 3. 변수 선언

    private Context context;

    public ArrayList<ModelPdf> pdfArrayList, filterlist;

    private FilterPdfAdmin filter;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList, ArrayList<ModelPdf> filterlist) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterlist = filterlist;
    }

    private RowPdfAdminBinding binding;

    // 4. filter되지 않으면 그대로 자료형 보여주기 filterlist / Filter되면 된 값인 filtercategory 반환
    @Override
    public Filter getFilter() {
        if (filter ==null){
            filter = new FilterPdfAdmin(filterlist, this);
        }
        return filter;
    }

    // 5. holder view
    @NonNull
    @Override
    public HolderPdf onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdf(binding.getRoot());
    }

    // 6. model 위치 / holder 기능
    @Override
    public void onBindViewHolder(@NonNull HolderPdf holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String uid = model.getUid();
        String id = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String date = model.getDate();
        String category = model.getCategory();

        // 기능선언
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        // pdf 북에는 정보에는 없는 카테고리, pdf(url로 받아오기) , size 찾기
        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);
        loadPdfSize(model, holder);

    }

    //6-1 category를 받아오기 firebase database
    private void loadCategory(ModelPdf model, HolderPdf holder) {
        String category = model.getCategory();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child("category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get category
                String category = ""+snapshot.child("category").getValue();
                //set category
                binding.categoryTv.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdf holder) {

    }

    private void loadPdfSize(ModelPdf model, HolderPdf holder) {

    }

    // 7. 어레이 사이즈만큼
    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }


    // 1. 홀더 선언
    class HolderPdf extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv,dateTv ;
        ImageButton moreBtn;

        public HolderPdf(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv =  binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;


        }
    }
}
