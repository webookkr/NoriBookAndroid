package com.bookapp.noribook.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Activities.SubAddActivity;
import com.bookapp.noribook.Filter.FilterPdfAdmin;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.Activities.PdfDetailActivity;
import com.bookapp.noribook.Activities.PdfEditActivity;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.RowPdfAdminBinding;
import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

// 2. extend
public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

// 3. 변수 선언

    private Context context;

    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private FilterPdfAdmin filter;

    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private RowPdfAdminBinding binding;


    // 5. holder view
    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    // 6. model 위치 / holder 기능
    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String uid = model.getUid();
        String id = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String date = model.getDate();
        String category = model.getCategoryTitle();
        String url = model.getUrl();
        long viewCount = model.getViewCount();
        long recommendCount = model.getRecommendCount();


        holder.viewCountTv.setText("조회수:"+viewCount);
        holder.recommendCountTv.setText("추천수:"+recommendCount);
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);
        holder.categoryTv.setText(category);

        Glide.with(context)
                .load(url)
                .into(binding.bookIv);

        MyApplication.loadPdfSize(""+url,
                                  holder.sizeTv);

        // 7. more btn 1)edit 2)delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(model, holder);
            }
        });

        // addPdfFab : subtitle추가로 옮김
        holder.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SubAddActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle",title);
                context.startActivity(intent);
            }
        });

        // 8. book pdf 클릭 시 detail로 이동
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", id);
                intent.putExtra("bookTitle",title);
                context.startActivity(intent);
            }
        });

        // featuredBtn (오너 추천)
        holder.featuredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFeaturedBook(model, holder);
            }
        });

    }

    // 오너 추천 버튼
    private void addFeaturedBook(ModelPdf model, HolderPdfAdmin holder) {
        String bookTitle = model.getTitle();
        Boolean featured = model.isFeatured();
        if ( featured == false) {
            Boolean newFeatured = true ;

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("featured", newFeatured);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
            reference.child(bookTitle)
                    .updateChildren(hashMap);

            binding.featuredBtn.setImageResource(R.drawable.ic_star_black);

        } else {
            Boolean newFeatured = false ;
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("featured", newFeatured);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
            reference.child(bookTitle)
                    .updateChildren(hashMap);
            binding.featuredBtn.setImageResource(R.drawable.ic_star_border_black);
        }

    }

    // 7-2 more btn
    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookTitle = model.getTitle();
        String bookUrl = model.getUrl();

        //option to show in dialog
        String[] options = {"수정", "삭제"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("옵션 선택")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){ //Edit 클릭시 책 정보 수정
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId); // model 에서 받은 book Id 값을 전달
                            intent.putExtra("bookTitle",bookTitle); // book title 값 전달
                            context.startActivity(intent);
                        }
                        else if (i ==1){// delete
                            MyApplication.deleteBook(context,
                                    ""+bookId,
                                    ""+bookTitle,
                                    ""+bookUrl);
//                            deleteBook(model,holder);
                        }
                    }
                })
                .show();
    }


    // 7. 어레이 사이즈만큼
    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }


    // 4. filter되지 않으면 그대로 자료형 보여주기 filterlist / Filter되면 된 값인 filtercategory 반환
    @Override
    public Filter getFilter() {
        if (filter ==null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }


    // 1. 홀더 선언
    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        ImageView bookIv;
        TextView titleTv, descriptionTv, categoryTv, sizeTv,dateTv, viewCountTv, recommendCountTv ;
        ImageButton moreBtn, featuredBtn;

        FloatingActionButton addPdfFab;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            bookIv = binding.bookIv;

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv =  binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
            addPdfFab = binding.addPdfFab;

            recommendCountTv = binding.recommendCountTv;
            viewCountTv = binding.viewCountTv;
            featuredBtn = binding.featuredBtn;


        }
    }
}
