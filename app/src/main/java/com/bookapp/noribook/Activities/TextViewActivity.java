package com.bookapp.noribook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.databinding.ActivityTextViewBinding;
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
import java.util.List;

public class TextViewActivity extends AppCompatActivity {


    String bookId;
    String bookTitle;
    String subTitle, subNumber;
    private ActivityTextViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookTitle = intent.getStringExtra("bookTitle");
        subTitle = intent.getStringExtra("subTitle");
        subNumber = intent.getStringExtra("subNumber");

        loadBookDetails();
        MyApplication.incrementSubBookViewCount(bookTitle, subNumber);


    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
        ref.child(subNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pdfUrl = ""+snapshot.child("url").getValue();

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