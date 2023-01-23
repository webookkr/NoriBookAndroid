package com.bookapp.noribook.Adapter;

import static com.bookapp.noribook.Constants.MAX_BYTES_PDF;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookapp.noribook.Filter.FilterPdfAdmin;
import com.bookapp.noribook.Model.ModelPdf;
import com.bookapp.noribook.MyApplication;
import com.bookapp.noribook.PdfDetailActivity;
import com.bookapp.noribook.PdfEditActivity;
import com.bookapp.noribook.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

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
        String pdfUrl = model.getUrl();


        // 기능선언
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);
        holder.categoryTv.setText(category);
        // pdf 북에는 정보에는 없는 카테고리, pdf(url로 받아오기) , size 찾기
//        loadCategory(model, holder); 그냥 카테고리에 타이틀 넣음
//        loadPdfFromUrl(model, holder);
        MyApplication.loadPdfFromUrl(context,
                                    ""+pdfUrl,
                                     holder.pdfView,
                                     holder.progressBar);
//        loadPdfSize(model, holder);  MyApplication으로
        MyApplication.loadPdfSize(""+pdfUrl,
                                  holder.sizeTv);

        // 7. more btn 1)edit 2)delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(model, holder);
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
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv,dateTv ;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
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
