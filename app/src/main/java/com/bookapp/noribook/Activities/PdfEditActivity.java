package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bookapp.noribook.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;

    // bookid get from AdapterPdfAdmin
    private String bookid, booktitle;

    // ProgressDialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryIdArrayList, categoryTitleArrayList;

    private static final String TAG = "Book_Edit_Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookid = getIntent().getStringExtra("bookId"); // 받은 아이디 init
        booktitle = getIntent().getStringExtra("bookTitle"); // title값 받아오기

        //setup progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // 1 카테고리 불러오기
        loadCategories();
        // 3. 책정보 불러오기
        loadBookInfo();

        // 2 - 카테고리 선택
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 4 - submit
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
        // 4-1 검증
    private String title = "", description = "";
    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "텍스트가 비었습니다.", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "설명이 비었습니다.", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "카테고리를 선택하세요.", Toast.LENGTH_SHORT).show();
        }
        else{
            updatePdf();
        }
    }
    // 4-2 pdf 정보 변경
    private void updatePdf() {
        Log.d(TAG, "updatePdf: 시작");
        progressDialog.setMessage("책 정보 업데이트 중");
        progressDialog.show();

        // setup data to update db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryTitle",""+selectedCategoryTitle);
        hashMap.put("categoryId",""+selectedCategoryId);

        // start update
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(booktitle)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: 업데이트 성공");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: e"+e.getMessage());
                        progressDialog.dismiss();
                    }
                });

    }

    // 책 정보 불러오기
    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: 책 정보 불러오기");
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(booktitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectedCategoryTitle = ""+snapshot.child("categoryTitle").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        // set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onDataChange: Loading Book Category info");
//                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
//                        refBookCategory.child(selectedCategoryTitle)
//                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                        // get Category
//                                        String category = ""+snapshot.child("category").getValue();
//                                        binding.categoryTv.setText(category);
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryTitle, selectedCategoryId ;

    // 2 = 카테고리 어레이 [] 에 어레이리스트를 하나씩 넣기
    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i =0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        // 3 - alert Dialog에 list 값 설정하기
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카테고리 선택")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 클릭시 카테고리 id, title값 얻기
                        selectedCategoryId = categoryIdArrayList.get(i);
                        selectedCategoryTitle = categoryTitleArrayList.get(i);

                        // set to Text view
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    // 1- category 불러와서 categoryIdarraylist에 넣기
    private void loadCategories() {
        Log.d(TAG, "loadCategories: loding category");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: Id"+id);
                    Log.d(TAG, "onDataChange: Category"+category);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}