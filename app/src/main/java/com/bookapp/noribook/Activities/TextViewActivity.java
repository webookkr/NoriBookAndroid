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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    private boolean isSpinnerInitialized = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.etcRl.setVisibility(View.GONE);

        // loadBookDetail ?????? ??? ?????? GONE?????? ??????
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




        initPageSpinner(bookTitle, subNumber);


//         relative ???????????? ????????? ??????????????? ?????? ??????
        binding.spinnerRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pageSpinner.performClick();
            }
        });

//      ????????? , ?????????
        binding.beforeTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subNumber = "" + (Long.parseLong(subNumber) - 1);
                if (subNumber.equals("0")) {
                    Toast.makeText(TextViewActivity.this, "???????????? ?????????.", Toast.LENGTH_SHORT).show();
                    subNumber = "" + (Long.parseLong(subNumber) + 1);
                }
                else if (Long.parseLong(subNumber) > 40 ) // ??? subNum??? 5?????? ?????????
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
        // subBooks>bookTitle ??? childrenCount??? subNumber ?????? ????????? ?????? ?????? +1 ?????? ?????? ???????????? ??????
        afterBtn();

//      ????????? ?????? ??????
//        checkIsMyFavorite();


//       ?????? ?????? ??????
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                firebase user??? ????????? ?????? ????????????
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(TextViewActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
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
                // ???????????? ?????? ?????? ????????? ???????????????.
                subBookRef.child("Likes").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // ???????????? ?????? ?????? ??????, ???????????? ???????????????.
                            subBookRef.child("Likes").child(uid).setValue(null);
                            binding.favoriteBtn.setImageResource(R.drawable.ic_favorite_border_black);

                            subBookRef.child("recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        long recommend = (long) snapshot.getValue();
                                        subBookRef.child("recommend").setValue(recommend - 1);
                                        Toast.makeText(TextViewActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        } else {
                            // ???????????? ????????? ?????? ??????, ???????????? ???????????????.
                            subBookRef.child("Likes").child(uid).setValue(true);
                            binding.favoriteBtn.setImageResource(R.drawable.ic_favorite_black);

                            // ?????? ?????? 1 ???????????????.
                            subBookRef.child("recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        long recommend = (long) snapshot.getValue();
                                        subBookRef.child("recommend").setValue(recommend + 1);
                                        Toast.makeText(TextViewActivity.this, "?????? ???????????????.", Toast.LENGTH_SHORT).show();
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

    private void initPageSpinner(String bookTitle, String subNumber) {


        spinerPage(bookTitle, subNumber); // ????????? ??????


        //        ????????? ??? ????????? ?????? ??????
        binding.pageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitialized) {
                    // Spinner??? ???????????? ?????? ????????? ????????? ?????? ????????? ??????
                    isSpinnerInitialized = true;
                    return;
                }

                String subNumberPage = parent.getItemAtPosition(position).toString(); // ????????? ??????????????? ????????????
                long subCheckNumber = Long.parseLong(subNumberPage.replaceAll("[^0-9]", ""));
                String subNumber = "" + subCheckNumber;


                if(firebaseAuth.getCurrentUser() == null){
                    if(subCheckNumber>20){
                        Toast.makeText(TextViewActivity.this, "????????? ????????? 40????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
                        intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                        intent.putExtra("subNumber", subNumber);
                        startActivity(intent);
                    }
                }else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.child(firebaseAuth.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String userType = ""+snapshot.child("userType").getValue();
                                    if (userType.equals("editor")){
                                        Intent intent = new Intent(TextViewActivity.this, EditorViewActivity.class);
                                        intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                        intent.putExtra("subNumber",subNumber);
                                        startActivity(intent);
                                    }else {
                                        if(subCheckNumber <= 20){
                                            Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
                                            intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                            intent.putExtra("subNumber",subNumber);
                                            startActivity(intent);
                                        }
                                        else if( subCheckNumber > 40){
                                            if(firebaseAuth.getCurrentUser()==null){
                                                Toast.makeText(TextViewActivity.this, "????????? ????????? 40????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                            // 40??? ??????, ????????? ??? ?????? -> ??????????????? ???????????? ???????????? ???????????? ?????? ??????(Alert), ??????????????? ??? ??? ?????????
                                            else {
                                                MyApplication.subBuyCheck(TextViewActivity.this, bookTitle, subNumber);

                                            }

                                        }
                                        else if( subCheckNumber >20) {

                                            Intent intent = new Intent(TextViewActivity.this, TextViewActivity.class);
                                            intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                            intent.putExtra("subNumber", subNumber);
                                            startActivity(intent);

                                        }
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void spinerPage(String bookTitle, String subNumber) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("SubBooks").child(bookTitle);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();

                List<String> subNumList = new ArrayList<>();

                for(int i= 1; i <count ; i++) {
                    subNumList.add(i+" ?????????");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(TextViewActivity.this, android.R.layout.simple_spinner_dropdown_item, subNumList);

                binding.pageSpinner.setAdapter(adapter);
                binding.pageTv.setText("?????????");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//    @Override
//    public void onBackPressed()
//    {
//        Intent intent = new Intent(this,TextDetailActivity.class);
//        startActivity(intent);
//    }

    //    ????????? ?????? ?????? (????????? ??????)
//    private void checkIsMyFavorite() {
//        firebaseAuth = FirebaseAuth.getInstance();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getCurrentUser().getUid()).child("Favorite").child(bookId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        isInMyFavorite = snapshot.exists(); //???????????? true, ???????????? ????????? false
//                        if (isInMyFavorite) {
//                            // ??????
//                            binding.favoriteBtn.setBackgroundResource(R.drawable.ic_favorite_white);
//                            Toast.makeText(TextViewActivity.this, "????????? ??????", Toast.LENGTH_SHORT).show();
//                        } else {
//                            binding.favoriteBtn.setBackgroundResource(R.drawable.ic_favorite_border_white);
//                            Toast.makeText(TextViewActivity.this, "????????? ??????", Toast.LENGTH_SHORT).show();
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




    //    ?????? ?????? ????????? ??????
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

    //    ?????? ????????? (?????? ????????? ??? ????????? ??????)
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
                                    Toast.makeText(TextViewActivity.this, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                                }
                                else if( subCheckNumber > 39) {
                                    if (firebaseAuth.getCurrentUser() == null) {
                                        Toast.makeText(TextViewActivity.this, "????????? ????????? 40????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
                                    }
                                    // 40??? ??????, ????????? ??? ?????? -> ??????????????? ???????????? ???????????? ???????????? ?????? ??????(Alert), ??????????????? ??? ??? ?????????
                                    else {
                                        MyApplication.subBuyCheck(TextViewActivity.this, bookTitle, subNumber);

                                    }
                                }
                                else if( subCheckNumber >19) {
                                    if (firebaseAuth.getCurrentUser() == null) {
                                        Toast.makeText(TextViewActivity.this, "????????? ????????? 40????????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
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

    // ????????? ?????? ???????????????
    private String comment ="";
    private void addCommentDialog() {
        // inflate bind view for dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        // setup alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);

        builder.setTitle("?????? ?????????");
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
//                comment ????????????
                comment = commentAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)){
                    Toast.makeText(TextViewActivity.this, "????????? ???????????????", Toast.LENGTH_SHORT).show();
                }
                else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

//    ?????? ?????? : add data to fb
    private void addComment() {
        progressDialog.setMessage("?????? ?????? ???...");
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
//        uid??? ????????? ???????????? ????????? ????????? uid??? ???????????? ??????????????? ????????? ?????? timestamp???...

        //Db path to add data into it
        // SubBooks > bookTitle > subNumber > Comments > uid > commentData
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks");
        reference.child(bookTitle).child(subNumber).child("Comments").child(""+timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(TextViewActivity.this, "?????? ?????? ???..", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TextViewActivity.this, "?????? ?????? ?????? : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

    }

    // ????????? ????????????,
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
                                                // ???????????? ????????? ????????? ?????? VISIBLE??? ??????
//                                                binding.getRoot().setVisibility(View.VISIBLE);
                                                binding.etcRl.setVisibility(View.VISIBLE);
                                            }
                                        });

                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TextViewActivity.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
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