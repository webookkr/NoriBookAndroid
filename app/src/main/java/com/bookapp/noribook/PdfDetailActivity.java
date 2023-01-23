package com.bookapp.noribook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bookapp.noribook.databinding.ActivityPdfDetailBinding;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}