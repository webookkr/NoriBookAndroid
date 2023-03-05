package com.bookapp.noribook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
//import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.android.billingclient.api.QueryProductDetailsParams;
//import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bookapp.noribook.Activities.ProfileActivity;
import com.bookapp.noribook.Activities.ProfileEditActivity;
import com.bookapp.noribook.databinding.ActivityNoriCoinAddBinding;
import com.bumptech.glide.Glide;
//import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoriCoinAddActivity extends AppCompatActivity {

    ActivityNoriCoinAddBinding binding;

    BillingClient billingClient;
    List<ProductDetails> productDetailsList;
    String TAG = "TestInApp";

    Handler handler;

    Activity activity;

    Prefs prefs;

    ArrayList<Integer> coins;
    ArrayList<String> productIds;

    private String noriCoin = "100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoriCoinAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;


        productIds = new ArrayList<>();
        productIds.add("nori_coin_100");
        productIds.add("nori_coin_1000");
        productIds.add("nori_coin_10000");



        prefs = new Prefs(this);

        productDetailsList = new ArrayList<>();

        loadUserData();

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NoriCoinAddActivity.this, ProfileEditActivity.class));
            }
        });

        binding.renewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

//        inApp

        //Initialize a BillingClient with PurchasesUpdatedListener onCreate method

//        billingClient = BillingClient.newBuilder(this)
//                .enablePendingPurchases()
//                .setListener(
//                        new PurchasesUpdatedListener() {
//                            @Override
//                            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
//                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
//                                    for (Purchase purchase : list) {
//                                        verifyPurchase(purchase);
//                                    }
//                                }
//                            }
//                        }
//                ).build();

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {
                            if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase: list){
                                    verifyPurchase(purchase);
                                }
                            }
                        }
                ).build();

//        start the connection after initializing the billing client
        connectGooglePlayBilling(); // productDetailList 받아오기

        binding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPurchaseFlow(productDetailsList.get(0));
            }
        });

        binding.confirmBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPurchaseFlow(productDetailsList.get(1));
            }
        });

        binding.confirmBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPurchaseFlow(productDetailsList.get(2));
            }
        });

    }


    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    getProductDetails();
                }
            }
        });

    }


    void getProductDetails() {

        Log.d(TAG, "showProducts ");

        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                //Product 1
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("nori_coin_100")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                //Product 2
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("nori_coin_1000")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                //Product 2
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("nori_coin_10000")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            //Clear the list
            productDetailsList.clear();

            Log.d(TAG, "Size " + list.size());


            //Adding new productList, returned from google play
            productDetailsList.addAll(list);

            //Since we have one product, we use index zero (0) from list
            ProductDetails productDetails = list.get(0);

//                //Getting product details
//                String price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
//                String productName = productDetails.getName();
//
//                //Updating the UI
//                //If the product is not showing then it means that you didn't properly setup your Testing email.
//                binding.coin100Label.setText(price +"  -  "+productName);

        });
    }

    // getProductDetail에서 가져온 productList 참조, ex) 상품 0번 : productlist.get(0) = productDetails
    void launchPurchaseFlow(ProductDetails productDetails) {

        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    void verifyPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                giveUserCoins(purchase);
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    void giveUserCoins(Purchase purchase) {

        Log.d("TestINAPP", purchase.getProducts().get(0));
        Log.d("TestINAPP", purchase.getQuantity() + " Quantity");

        coins = new ArrayList<>();
        coins.add(100);
        coins.add(1000);
        coins.add(10000);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String noriCoin = "" + snapshot.child("noriCoin").getValue();

                long newNoriCoin = 0;
                if(purchase.getProducts().get(0).equals(productIds.get(0))){
                    newNoriCoin = coins.get(0)+Long.parseLong(noriCoin);
                } else if (purchase.getProducts().get(0).equals(productIds.get(1))) {
                    newNoriCoin = coins.get(1)+Long.parseLong(noriCoin);
                } else if (purchase.getProducts().get(0).equals(productIds.get(2))) {
                    newNoriCoin = coins.get(2) + Long.parseLong(noriCoin);
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("noriCoin",""+newNoriCoin);

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseAuth.getUid())
                        .updateChildren(hashMap);

                Toast.makeText(NoriCoinAddActivity.this, "구매되었습니다. 인터넷환경에 따라 3초간 지연될 수 있습니다.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {

            }
        });


//        for(int i=0;i<productIds.size();i++){
//            if(purchase.getProducts().get(0).equals(productIds.get(i))){
//                Log.d(TAG,"Allocating "+coins.get(i) + " Coins");
//
//                //set coins
//                prefs.setInt("coins",coins.get(i) + prefs.getInt("coins",0));
//
//                Log.d(TAG,"New Balance "+prefs.getInt("coins",0)+ " Coins");
//
//                //Update UI
//                binding.coinTv.setText(prefs.getInt("coins",0)+"");
//            }
//        prefs.setInt("coins", (coins.get(0) * purchase.getQuantity()) + prefs.getInt("coins", 0));
//        //Update UI
//        binding.coinTv.setText("You have " + prefs.getInt("coins", 0) + " coins(s)");

    }

//
    protected void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                verifyPurchase(purchase);
                            }
                        }
                    }
                }
        );
    }


    private void loadUserData() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                String profileUrl = "" + snapshot.child("profileImage").getValue();
                String noriCoin = "" + snapshot.child("noriCoin").getValue();

                binding.nameTv.setText(name);
                binding.coinTv.setText(noriCoin + "원");

                Glide.with(NoriCoinAddActivity.this)
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}