package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bookapp.noribook.Adapter.AdapterComment;
import com.bookapp.noribook.Model.ModelComment;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.R;
import com.bookapp.noribook.databinding.ActivityTextViewBinding;
import com.bookapp.noribook.databinding.DialogCommentAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextViewActivity extends AppCompatActivity {


    String bookId;
    String bookTitle;
    String subTitle, subNumber, subCount, maxSubCount;
    private ActivityTextViewBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    boolean isInMyFavorite = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.etcRl.setVisibility(View.GONE);

        // loadBookDetail 호출 후 뷰를 GONE으로 설정
//        binding.getRoot().setVisibility(View.GONE);

        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("bookTitle");
        subNumber = intent.getStringExtra("subNumber");

        // init progress
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadBookDetails();

        loadComments();

        MyApplication.incrementSubBookViewCount(bookTitle, subNumber);

//      다음화 , 이전화
        binding.beforeTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subNumber = "" + (Long.parseLong(subNumber) - 1);
                if (subNumber.equals("0")) {
                    Toast.makeText(TextViewActivity.this, "첫페이지 입니다.", Toast.LENGTH_SHORT).show();
                    subNumber = "" + (Long.parseLong(subNumber) + 1);
                }
                else if (Long.parseLong(subNumber) > 40 ) // 볼 subNum이 5보다 크다면
                {
                  MyApplication.subBuyCheck(TextViewActivity.this, bookTitle, subNumber);
                    subNumber = "" + (Long.parseLong(subNumber) + 1);
//                    if (FirebaseAuth.getInstance().getCurrentUser() != null){
//                        MyApplication.askBuySub(TextViewActivity.this, bookTitle, subNumber);
//                    }

//
//                    finish();
//                    Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
//                    intent.putExtra("bookTitle", bookTitle);
//                    intent.putExtra("subNumber", subNumber);
//                    startActivity(intent);
                }else // 1 < subnum < 5
                {
                    finish();
                    Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
                    intent.putExtra("bookTitle", bookTitle);
                    intent.putExtra("subNumber", subNumber);
                    startActivity(intent);
                }
            }
        });
        // subBooks>bookTitle 의 childrenCount로 subNumber 개수 구해서 최대 개수 +1 이상 접근 못하도록 막음
        afterBtn();

//      선호작 추가 제거
//        checkIsMyFavorite();


//       댓글 추가 버튼
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                firebase user가 로그인 되어 있어야함
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(TextViewActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    addCommentDialog();

                }
            }
        });

        DatabaseReference subBookRef = FirebaseDatabase.getInstance().getReference()
                .child("SubBooks")
                .child(bookTitle)
                .child(subNumber);

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // 좋아요를 누른 적이 있는지 검증합니다.
                subBookRef.child("Likes").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // 좋아요를 이미 누른 경우, 좋아요를 취소합니다.
                            subBookRef.child("Likes").child(uid).setValue(null);
                            binding.favoriteBtn.setImageResource(R.drawable.ic_favorite_border_black);

                            subBookRef.child("recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        long recommend = (long) snapshot.getValue();
                                        subBookRef.child("recommend").setValue(recommend - 1);
                                        Toast.makeText(TextViewActivity.this, "추천이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        } else {
                            // 좋아요를 누르지 않은 경우, 좋아요를 추가합니다.
                            subBookRef.child("Likes").child(uid).setValue(true);
                            binding.favoriteBtn.setImageResource(R.drawable.ic_favorite_black);

                            // 추천 수를 1 증가합니다.
                            subBookRef.child("recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        long recommend = (long) snapshot.getValue();
                                        subBookRef.child("recommend").setValue(recommend + 1);
                                        Toast.makeText(TextViewActivity.this, "추천 되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

    }

//    @Override
//    public void onBackPressed()
//    {
//        Intent intent = new Intent(this,TextDetailActivity.class);
//        startActivity(intent);
//    }

    //    선호작 추가 제거 (아이콘 변경)
//    private void checkIsMyFavorite() {
//        firebaseAuth = FirebaseAuth.getInstance();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getCurrentUser().getUid()).child("Favorite").child(bookId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        isInMyFavorite = snapshot.exists(); //존재하면 true, 존재하지 않으면 false
//                        if (isInMyFavorite) {
//                            // 존재
//                            binding.favoriteBtn.setBackgroundResource(R.drawable.ic_favorite_white);
//                            Toast.makeText(TextViewActivity.this, "선호작 제거", Toast.LENGTH_SHORT).show();
//                        } else {
//                            binding.favoriteBtn.setBackgroundResource(R.drawable.ic_favorite_border_white);
//                            Toast.makeText(TextViewActivity.this, "선호작 추가", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }




    //    댓글 추가 어뎁터 연결
    private void loadComments() {
//        init
        commentArrayList = new ArrayList<>();

        // dp path
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks");
        ref.child(bookTitle).child(subNumber).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelComment model = ds.getValue(ModelComment.class);
                    commentArrayList.add(model);

                }
                adapterComment = new AdapterComment(TextViewActivity.this, commentArrayList);
                binding.commentsRv.setAdapter(adapterComment);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //    다음 페이지 (전체 페이지 수 구해서 제한)
    private void afterBtn() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks");
        ref.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String subCount = ""+snapshot.getChildrenCount();
                        binding.afterTitleTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                long subCheckNumber= Long.parseLong(subNumber);
                                subNumber = ""+(Long.parseLong(subNumber)+1);
                                maxSubCount = ""+(Long.parseLong(subCount)+1);
                                if(subNumber.equals(maxSubCount)){
                                    Toast.makeText(TextViewActivity.this, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show();
                                }
                                else if( subCheckNumber > 39) {
                                    if (firebaseAuth.getCurrentUser() == null) {
                                        Toast.makeText(TextViewActivity.this, "로그인 하시면 40편까지 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    // 40편 초과, 로그인 된 경우 -> 구매했는지 체크하여 구매하지 않았으면 구매 권유(Alert), 구매했으면 볼 수 있도록
                                    else {
                                        MyApplication.subBuyCheck(TextViewActivity.this, bookTitle, subNumber);

                                    }
                                }
                                else if( subCheckNumber >19) {
                                    if (firebaseAuth.getCurrentUser() == null) {
                                        Toast.makeText(TextViewActivity.this, "로그인 하시면 40편까지 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        finish();
                                        Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
                                        intent.putExtra("bookTitle", bookTitle);
                                        intent.putExtra("subNumber",subNumber);
                                        startActivity(intent);
                                        }
                                    }
                                else{
                                    finish();
                                    Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
                                    intent.putExtra("bookTitle", bookTitle);
                                    intent.putExtra("subNumber",subNumber);
                                    startActivity(intent);
                                }
                                subNumber = ""+(Long.parseLong(subNumber)-1);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    // 커멘트 달기 다이어로그
    private String comment ="";
    private void addCommentDialog() {
        // inflate bind view for dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        // setup alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);

        builder.setTitle("댓글 입력창");
        builder.setView(commentAddBinding.getRoot());

        //create and show alert
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setDimAmount(0);
        alertDialog.show();

        // handle click : back btn
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // handle click : submit btn
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                comment 가져오기
                comment = commentAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)){
                    Toast.makeText(TextViewActivity.this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

//    댓글 입력 : add data to fb
    private void addComment() {
        progressDialog.setMessage("댓글 추가 중...");
        progressDialog.show();

        String uid = ""+firebaseAuth.getUid();
        Long timeStamp = System.currentTimeMillis();
        String date = ""+MyApplication.formatTimestamp(timeStamp);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timeStamp);
        hashMap.put("subNumber", ""+subNumber);
        hashMap.put("comment", ""+comment);
        hashMap.put("date",""+date);
        hashMap.put("uid",""+uid);
        hashMap.put("bookTitle",""+bookTitle);
//        uid를 폴더의 기준으로 잡으면 앞으로 uid당 하나밖에 못잡으므로 겹치지 않게 timestamp로...

        //Db path to add data into it
        // SubBooks > bookTitle > subNumber > Comments > uid > commentData
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks");
        reference.child(bookTitle).child(subNumber).child("Comments").child(""+timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(TextViewActivity.this, "댓글 작성 중..", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TextViewActivity.this, "댓글 작성 실패 : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

    }

    // 책정보 가져오기,
    private void loadBookDetails() {
//        binding.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
        ref.child(subNumber)

                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pdfUrl = ""+snapshot.child("url").getValue();
                        String subTitle = ""+snapshot.child("subTitle").getValue();



                        new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final List<String> addressList = getTextFromWeb(pdfUrl);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                StringBuilder sb = new StringBuilder();
                                                for(String s : addressList){
                                                    sb.append(s);
                                                    sb.append("\n");
                                                }
                                                binding.textTv.setText(sb.toString());
                                                binding.textTv.setMovementMethod(new ScrollingMovementMethod());
                                                binding.progressBar.setVisibility(View.GONE);
                                                binding.titleTv.setText(subNumber+". "+subTitle);
                                                // 데이터가 로드된 이후에 뷰를 VISIBLE로 설정
//                                                binding.getRoot().setVisibility(View.VISIBLE);
                                                binding.etcRl.setVisibility(View.VISIBLE);
                                            }
                                        });

                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TextViewActivity.this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
                        subNumber = ""+(Long.parseLong(subNumber)-1);
                        recreate();
                    }
                });
    }

    private List<String> getTextFromWeb(String urlString) {
        URLConnection feedUrl;
        List<String> placeAddress = new ArrayList<>();

        try {
            feedUrl = new URL(urlString).openConnection();
            InputStream is = feedUrl.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;

            while ((line = reader.readLine()) != null)
            {
                placeAddress.add(line);
            }
            is.close();
            return  placeAddress;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }
}