package com.bookapp.noribook;

import static com.bookapp.noribook.Constants.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bookapp.noribook.Activities.TextViewActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //calender 함수 이용해서 timestamp를 날짜 형태로 변환
    public static final String formatTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("yyyy/MM/dd", calendar).toString();

        return date;
    }

    // url 정보로 storage에서 북id 삭제
    public static void deleteBook(Context context, String bookId, String bookTitle, String bookUrl) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("wait");
        progressDialog.setMessage(bookTitle + "삭제 중");
        progressDialog.show();

        // storage에서 삭제 및 데이터 베이스 삭제
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookTitle)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    // url 정보로 storage에서 meta데이터 가져오기
    public static void loadPdfSize(String pdfUrl, TextView sizeTv) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();

                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb>1){
                            sizeTv.setText(String.format("%.2f", mb)+"mb");
                        }
                        else if (kb>1){
                            sizeTv.setText(String.format("%.2f", kb)+"kb");
                        }
                        else {
                            sizeTv.setText(String.format("%.2f", bytes)+"bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });



    }

    // pdfView 띄우기 위해 url 정보를 이용해서 storeage에서 가져와서 띄우기
    public static void loadPdfFromUrl(String pdfUrl, PDFView pdfView, ProgressBar progressBar) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        pdfView.fromBytes(bytes)
                                .pages(0)//show 첫페이지
                                .spacing(0)
                                .swipeHorizontal(true)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    // 조회수 늘리기 : 클릭시 viewCount + 1해서 업데이트  : Books -> bookTitle -> viewCount
    public static void incrementBookViewCount(String bookTitle){
        // 1) get view book count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        if (viewCount.equals("")||viewCount.equals("null")){
                            viewCount = "0";
                        }
                        // 2) increment views count
                        long newViewCount = Long.parseLong(viewCount) + 1 ;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewCount", newViewCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookTitle)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void decrementBookViewCount(String bookTitle){
        // 1) get view book count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = ""+snapshot.child("viewCountMinus").getValue();
                        if (viewCount.equals("")||viewCount.equals("null")){
                            viewCount = "0";
                        }
                        // 2) increment views count
                        long newViewCount = Long.parseLong(viewCount) - 1 ;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewCountMinus", newViewCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookTitle)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    // 서브북 조회수 늘리기 : 클릭시 viewCount +1 해서 subBook -> subNumber -> viewCount 늘리기
    public static void incrementSubBookViewCount(String bookTitle,String subNumber){
        // 1) get view book count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
        ref.child(subNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        if (viewCount.equals("")||viewCount.equals("null")){
                            viewCount = "0";
                        }
                        // 2) increment views count
                        long newViewCount = Long.parseLong(viewCount) + 1 ;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewCount", newViewCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
                        reference.child(subNumber)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void descrementSubBookViewCount(String bookTitle,String subNumber){
        // 1) get view book count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
        ref.child(subNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = ""+snapshot.child("viewCountMinus").getValue();
                        if (viewCount.equals("")||viewCount.equals("null")){
                            viewCount = "0";
                        }
                        // 2) increment views count
                        long newViewCount = Long.parseLong(viewCount) - 1 ;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewCountMinus", newViewCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SubBooks/"+bookTitle+"/");
                        reference.child(subNumber)
                                .updateChildren(hashMap);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // 선호작 추가 : 선호작 클릭시 Toast 띄우고, Users -> Uid -> Favorite 추가
    public static void addFavorite(Context context, String bookId, String bookTitle){
        // 선호작 추가는 유저가 로그인 되어 있어야 함
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "로그인 하세요.", Toast.LENGTH_SHORT).show();
        }
        else {
            long timeStamp = System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", bookId);
            hashMap.put("bookTitle", bookTitle);
            hashMap.put("timestamp", ""+timeStamp);

            // save to Db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "선호작 리스트에 추가하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "선호작 리스트 추가에 실패하였습니다."+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String recommendCount= ""+snapshot.child("recommendCount").getValue();
                        if(recommendCount.equals("")||recommendCount.equals("null")){
                            recommendCount = "0" ;
                        }else{
                            long newRecommendCount = Long.parseLong(recommendCount) + 1;
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("recommendCount", newRecommendCount);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                                    reference1.child(bookTitle)
                                    .updateChildren(hashMap);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Books");
        reference2.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String recommendCountMinus = ""+snapshot.child("recommendCountMinus").getValue();
                        if(recommendCountMinus.equals("")||recommendCountMinus.equals("null")){
                            recommendCountMinus = "0" ;
                        }else{
                            long newRecommendCount = Long.parseLong(recommendCountMinus) - 1;
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("recommendCountMinus", newRecommendCount);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                            reference1.child(bookTitle)
                                    .updateChildren(hashMap);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // 선호작 제거 : 제거 클릭시 Users -> Uid -> Favorite 삭제
    public static void removeFromFavorite(Context context,String bookId, String bookTitle){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() ==null){
            Toast.makeText(context, "로그인 하세요.", Toast.LENGTH_SHORT).show();
        }else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "선호작 리스트에서 제거하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "선호작 리스트 제거에 실패하였습니다."+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String recommendCount = ""+snapshot.child("recommendCount").getValue();
                        if(recommendCount.equals("")||recommendCount.equals("null")){
                            recommendCount = "0" ;
                        }else{
                            long newRecommendCount = Long.parseLong(recommendCount) -1;
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("recommendCount", newRecommendCount);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                            reference1.child(bookTitle)
                                    .updateChildren(hashMap);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Books");
        reference2.child(bookTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String recommendCountMinus = ""+snapshot.child("recommendCountMinus").getValue();
                        if(recommendCountMinus.equals("")||recommendCountMinus.equals("null")){
                            recommendCountMinus = "0" ;
                        }else{
                            long newRecommendCount = Long.parseLong(recommendCountMinus) + 1;
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("recommendCountMinus", newRecommendCount);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                            reference1.child(bookTitle)
                                    .updateChildren(hashMap);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

//    noriCoin 표시
    public static void noriCoinCheck(TextView noriGoldTv){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){

        }else
        {
            String uid = firebaseAuth.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String noriCoin =""+snapshot.child("noriCoin").getValue();
                    noriGoldTv.setText(noriCoin+"원");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //subBook 구매 확인
    public static void subBuyCheck(Context context, String bookTitle, String subNumber) {
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("buyBooks").child(bookTitle).child(subNumber)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isBuySub = snapshot.exists();
                        if (isBuySub){ //구매 내역 존재
                            Intent intent = new Intent(context, TextViewActivity.class);
                            intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                            intent.putExtra("subNumber",subNumber);
                            context.startActivity(intent);
                        }else{ //구매내역 없음
                            askBuySub(context, bookTitle, subNumber);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static void askBuySub(Context context, String bookTitle, String subNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("구매 확인")
               .setMessage("100코인이 소모됩니다. 소설을 구매하시겠습니까?")
               .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       Toast.makeText(context, "구매중입니다.", Toast.LENGTH_SHORT).show();
                       confirmBuy(context, bookTitle, subNumber);
                   }
               }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private static void confirmBuy(Context context, String bookTitle, String subNumber) {
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String noriCoin = ""+snapshot.child("noriCoin").getValue();
                        long newNoriCoin = Long.parseLong(noriCoin) - 100;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("noriCoin", ""+newNoriCoin);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid())
                                .updateChildren(hashMap);

                        HashMap<String, Object> hashMap2 = new HashMap<>();
                        hashMap2.put("subNumber",subNumber);

                        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users");
                        ref2.child(firebaseAuth.getUid()).child("buyBooks").child(bookTitle).child(subNumber)
                                .setValue(hashMap2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "구매하였습니다.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(context, TextViewActivity.class);
                                        intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                                        intent.putExtra("subNumber",subNumber);
                                        context.startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

//
////    선호작이면 추가 아니면 제거
//    private void checkIsFavorite(String bookId){
//        boolean isInMyFavorite = false ;
//
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        isInMyFavorite = snapshot.exists(); //존재하면 true, 존재하지 않으면 false
//                        if (isInMyFavorite){
//                            // 존재
//                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
//                            binding.favoriteBtn.setText("선호작 제거");
//                        }else{
//                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_border_white, 0, 0, 0);
//                            binding.favoriteBtn.setText("선호작 추가");
//
//                        }
//
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }



}
