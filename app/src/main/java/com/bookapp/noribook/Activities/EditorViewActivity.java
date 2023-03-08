package com.bookapp.noribook.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bookapp.noribook.Constants;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.ActivityEditorViewBinding;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditorViewActivity extends AppCompatActivity {

    String bookId;
    String bookTitle;
    String subTitle, subNumber;

    ActivityEditorViewBinding binding;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditorViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("bookTitle");
        subNumber = intent.getStringExtra("subNumber");
        subTitle = intent.getStringExtra("subTitle");
        loadBookDetails();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.subNumberEt.setText(subNumber);
        binding.subTitleEt.setText(subTitle);

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    private void updateData() {
        String editTextContent = binding.textTv.getText().toString();
        byte[] data = editTextContent.getBytes();

        progressDialog.setMessage("subBook 업데이트 중...");
        progressDialog.show();


        String filePathAndName = "SubBooks/"+bookTitle+"/"+subNumber+".txt";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadUrl = ""+uriTask.getResult();
                        String newSubNumber = ""+binding.subNumberEt.getText().toString().trim();
                        String newSubTitle = ""+binding.subTitleEt.getText().toString().trim();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("subNumber", newSubNumber);
                        hashMap.put("subTitle", newSubTitle);
                        hashMap.put("url", uploadUrl);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks");
                        reference.child(bookTitle).child(subNumber).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditorViewActivity.this, "실패"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditorViewActivity.this, "업로드 실패"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

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
                                    }
                                });

                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
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