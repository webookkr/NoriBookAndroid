package com.bookapp.noribook;

import static com.bookapp.noribook.Constants.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bookapp.noribook.Activities.TextViewActivity;
import com.bookapp.noribook.databinding.ActivityNoriCoinAddBinding;
import com.bookapp.noribook.databinding.DialogSubBuyBinding;
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
        checkForForceClose();
    }

    private void checkForForceClose() {
        SharedPreferences sharedPreferences = getSharedPreferences("NoriBookAndroid", MODE_PRIVATE);
        boolean isForceClosed = sharedPreferences.getBoolean("is_force_closed", false);
        if (isForceClosed) {
            // ????????? ?????? ?????????????????? sharedpreference ?????? ?????????????????????.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_force_closed", false);
            editor.apply();
        }
    }

    //calender ?????? ???????????? timestamp??? ?????? ????????? ??????
    public static final String formatTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("yyyy/MM/dd", calendar).toString();

        return date;
    }

    // url ????????? storage?????? ???id ??????
    public static void deleteBook(Context context, String bookId, String bookTitle, String bookUrl) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("wait");
        progressDialog.setMessage(bookTitle + "?????? ???");
        progressDialog.show();

        // storage?????? ?????? ??? ????????? ????????? ??????
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
                                        Toast.makeText(context, "?????? ??????", Toast.LENGTH_SHORT).show();
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

    // url ????????? storage?????? meta????????? ????????????
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

    // pdfView ????????? ?????? url ????????? ???????????? storeage?????? ???????????? ?????????
    public static void loadPdfFromUrl(String pdfUrl, PDFView pdfView, ProgressBar progressBar) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        pdfView.fromBytes(bytes)
                                .pages(0)//show ????????????
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

    // ????????? ????????? : ????????? viewCount + 1?????? ????????????  : Books -> bookTitle -> viewCount
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


    // ????????? ????????? ????????? : ????????? viewCount +1 ?????? subBook -> subNumber -> viewCount ?????????
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

    // ????????? ?????? : ????????? ????????? Toast ?????????, Users -> Uid -> Favorite ??????
    public static void addFavorite(Context context, String bookId, String bookTitle){
        // ????????? ????????? ????????? ????????? ?????? ????????? ???
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "????????? ?????????.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, "????????? ???????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "????????? ????????? ????????? ?????????????????????."+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // ????????? ?????? : ?????? ????????? Users -> Uid -> Favorite ??????
    public static void removeFromFavorite(Context context,String bookId, String bookTitle){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() ==null){
            Toast.makeText(context, "????????? ?????????.", Toast.LENGTH_SHORT).show();
        }else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "????????? ??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "????????? ????????? ????????? ?????????????????????."+ e.getMessage(), Toast.LENGTH_SHORT).show();
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

//    noriCoin ??????
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
                    noriGoldTv.setText(noriCoin+"???");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //subBook ?????? ??????
    public static void subBuyCheck(Context context, String bookTitle, String subNumber) {
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("buyBooks").child(bookTitle).child(subNumber)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isBuySub = snapshot.exists();
                        if (isBuySub){ //?????? ?????? ??????
                            Intent intent = new Intent(context, TextViewActivity.class);
                            intent.putExtra("bookTitle", bookTitle);
//                intent.putExtra("subTitle", subTitle);
                            intent.putExtra("subNumber",subNumber);
                            context.startActivity(intent);
                        }else{ //???????????? ??????
                            askBuySub(context, bookTitle, subNumber);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static void askBuySub(Context context, String bookTitle, String subNumber) {

        DialogSubBuyBinding askBinding = DialogSubBuyBinding.inflate(LayoutInflater.from(context));


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog);
        builder.setView(askBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setDimAmount(0);
        alertDialog.getWindow().setBackgroundDrawable(null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();

        askBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        askBinding.confirmFl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(FirebaseAuth.getInstance().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String noriCoin = ""+snapshot.child("noriCoin").getValue();
                                if (Long.parseLong(noriCoin) >= 100){
                                    Toast.makeText(context, "??????????????????.", Toast.LENGTH_SHORT).show();
                                    confirmBuy(context, bookTitle, subNumber);
                                }else {
                                    Toast.makeText(context, "????????? ???????????????. ????????? ???????????????", Toast.LENGTH_SHORT).show();
                                    alertDialog.cancel();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });

        askBinding.cancleFl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(FirebaseAuth.getInstance().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String noriCoin = ""+snapshot.child("noriCoin").getValue();
                                askBinding.noriGoldTv.setText(noriCoin);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SubBooks");
        ref.child(bookTitle).child(subNumber)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String subTitle = ""+snapshot.child("subTitle").getValue();
                                askBinding.subTitle.setText(subNumber+"."+subTitle);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

//
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("?????? ??????")
//               .setMessage("100????????? ???????????????. ????????? ?????????????????????????")
//               .setPositiveButton("??????", new DialogInterface.OnClickListener() {
//                   @Override
//                   public void onClick(DialogInterface dialog, int which) {
//                       Toast.makeText(context, "??????????????????.", Toast.LENGTH_SHORT).show();
//                       confirmBuy(context, bookTitle, subNumber);
//                   }
//               }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                }).show();
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
                                        Toast.makeText(context, "?????????????????????.", Toast.LENGTH_SHORT).show();
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

//    ?????? ?????? ??????
    public static void askGoldBuy(Context context){
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(context, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(context, NoriCoinAddActivity.class);
            context.startActivity(intent);
        }

    }


//
////    ??????????????? ?????? ????????? ??????
//    private void checkIsFavorite(String bookId){
//        boolean isInMyFavorite = false ;
//
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        isInMyFavorite = snapshot.exists(); //???????????? true, ???????????? ????????? false
//                        if (isInMyFavorite){
//                            // ??????
//                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_white, 0, 0, 0);
//                            binding.favoriteBtn.setText("????????? ??????");
//                        }else{
//                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_border_white, 0, 0, 0);
//                            binding.favoriteBtn.setText("????????? ??????");
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
