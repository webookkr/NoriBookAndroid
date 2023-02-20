package com.bookapp.noribook.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Model.ModelComment;
import com.bookapp.noribook.databinding.RowCommentBinding;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment>{

    Context context;
    private ArrayList<ModelComment> commentArrayList;

    private RowCommentBinding binding;

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {

        // get data
        ModelComment modelComment = commentArrayList.get(position);
        String comment = modelComment.getComment();
        String date = modelComment.getDate();
        String subNumber = modelComment.getSubNumber();
        String subTitle = modelComment.getSubTitle();
        String uid = modelComment.getUid();


    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    class HolderComment extends RecyclerView.ViewHolder{

        TextView nameTv, dateTv, commentTv;
        ImageView profileIv;

        public HolderComment(View itemView){
            super(itemView);

            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;
            profileIv = binding.profileIv;

        }
    }
}
