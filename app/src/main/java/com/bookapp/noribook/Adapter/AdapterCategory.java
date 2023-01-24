package com.bookapp.noribook.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Filter.FilterCategory;
import com.bookapp.noribook.Model.ModelCategory;
import com.bookapp.noribook.Activities.PdfListAdminActivity;
import com.bookapp.noribook.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

// 2. extends  Adapter<holder> implements Filterable
public class AdapterCategory  extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {


    // 3. 인자 선언 / context, 자료 받을 arraylist, 필터 연결할 Filtercategory -> construct
    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterlist;
    // instance of filter class
    private FilterCategory filter ;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterlist = categoryArrayList;
    }

    private RowCategoryBinding binding; // 4. 카테고리 Row와 바인딩

    // 4-2. 카테고리 로우 바인딩 확장
    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategory(binding.getRoot()); // 바인딩하여 루트뷰 보여주는 홀드 카테고리 반환
    }

    // 5 데이터 바인딩 - 각종 기능 선언
    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        ModelCategory model = categoryArrayList.get(position); //카테고리 어레이 리스트의 위치 가져와 모델카테고리 인자와 연결하기;
        String id = model.getId();
        String category = model.getCategory();
        String date = model.getDate();
        String uid = model.getUid();

        //기능 선언 set Data
        holder.categoryTv.setText(category);

        // 기능선언 delete btn
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("삭제하겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context, "삭제 중입니다.", Toast.LENGTH_SHORT).show();
                                deleteCategory(model,holder); // model 변수와 holder 변수 가지고 새 method 생성
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        //handle item click, goto PdfListAdmin and pass category and categoryID
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);
            }
        });
    }

    //5- 2 delete
    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        String category = model.getCategory(); // 삭제할 모델 category 얻기
        //Firebase db 연결
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(category)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }



    // 6. 바인드 사이즈 = 데이터 사이즈
    @Override
    public int getItemCount() {
        return categoryArrayList.size();}


    //1. holder : 뷰 선언 - Row에 담을 내용 보여주기
    class HolderCategory extends RecyclerView.ViewHolder{

        TextView categoryTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;

        }
    }

    // 7. filter
    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategory(filterlist, this);
        }
        return filter;
    }
}
