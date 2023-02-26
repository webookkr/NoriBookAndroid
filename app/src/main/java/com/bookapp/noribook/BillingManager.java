package com.bookapp.noribook;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BillingManager<mConsumeResponseListener> implements PurchasesUpdatedListener{

    String TAG = "BillingManager";
    BillingClient mBillingClient ;
    Activity mActivity;
    public List<SkuDetails> mSkuDetails;



    public enum connectStatusTypes { waiting, connected, fail, disconnected }
    public connectStatusTypes connectStatus = connectStatusTypes.waiting ;
    private ConsumeResponseListener mConsumeResponseListener;
    String billCode100 = "nori_coin_100";
    String billCode1000 = "nori_coin_1000";

    public BillingManager (Activity _activity) {
        mActivity = _activity;

        mBillingClient = BillingClient.newBuilder(mActivity)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connectStatus = connectStatusTypes.disconnected;
                Log.i(TAG, "onBillingServiceDisconnected ");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.i(TAG, "onBillingSetupFinished: "+billingResult.getResponseCode());
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    connectStatus = connectStatusTypes.connected;
                    Log.i(TAG, "onBillingSetupFinished: connected");
                    Log.i(TAG, "onBillingSetupFinished: "+mBillingClient.queryPurchases(billCode100).getBillingResult());
                    Log.i(TAG, "onBillingSetupFinished: "+mBillingClient.queryPurchases(billCode1000).getBillingResult());

                    getSkuDetailList();
                }else {
                    connectStatus = connectStatusTypes.fail;
                    Log.i(TAG, "onBillingSetupFinished: fail");
                }
            }
        });
        
        mConsumeResponseListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Log.i(TAG, "onConsumeResponse: 전환 완료"+s);
                }else {
                    Log.i(TAG, "onConsumeResponse: 전환 실패"+billingResult.getResponseCode()+"대상 상품 :"+s);
                }
            }
        };
    }
    
    public int purchase(SkuDetails skuDetails){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        return mBillingClient.launchBillingFlow(mActivity,flowParams).getResponseCode();
    }

    private void getSkuDetailList() {
        List<String> skuIdList = new ArrayList<>();
        skuIdList.add(billCode100);
        skuIdList.add(billCode1000);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        
        params.setSkusList(skuIdList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
                if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK){
                    Log.i(TAG, "onSkuDetailsResponse: detail resCode : "+billingResult.getResponseCode());
                }else if(skuDetailsList == null){
                    Log.i(TAG, "onSkuDetailsResponse: not info");
                }
                Log.i(TAG, "onSkuDetailsResponse: listCount:"+skuDetailsList.size());
                for(SkuDetails skuDetails : skuDetailsList){
                    Log.i(TAG, "\n" + skuDetails.getSku()
                            + "\n" + skuDetails.getTitle()
                            + "\n" + skuDetails.getPrice()
                            + "\n" + skuDetails.getDescription()
                            + "\n" + skuDetails.getFreeTrialPeriod()
                            + "\n" + skuDetails.getIconUrl()
                            + "\n" + skuDetails.getIntroductoryPrice()
                            + "\n" + skuDetails.getIntroductoryPriceAmountMicros()
                            + "\n" + skuDetails.getOriginalPrice()
                            + "\n" + skuDetails.getPriceCurrencyCode()) ;
                }
                mSkuDetails = skuDetailsList;
            }
        });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.i(TAG, "구매 성공>>>" + billingResult.getDebugMessage());
            JSONObject object = null ;
            String pID = "" ;
            String pDate = "" ;
            for (Purchase purchase : purchases){
                handlePurchase(purchase);
            }
            for(Purchase purchase : purchases) {
                ConsumeParams params = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build() ;
                mBillingClient.consumeAsync(params, mConsumeResponseListener);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "결제 취소");
        } else {
            Log.i(TAG, "오류 코드=" + billingResult.getResponseCode()) ;
        }
    }

    private void handlePurchase(Purchase purchase) {
        String purchaseToken, payLoad;
        purchaseToken = purchase.getPurchaseToken();
        payLoad = purchase.getDeveloperPayload();

        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
        {
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchaseToken)
                            .build();

            mBillingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s)
                {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                    {
                        Log.d(TAG, "google purchase success");
                    }else{
                        Log.d(TAG, "google purchase consume error");
                    }
                }
            });
        }
    }
}
