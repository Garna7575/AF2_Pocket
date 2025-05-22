package com.example.apptfc.Activities;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BillingManager {
    private final BillingClient billingClient;
    private final PurchasesUpdatedListener purchasesUpdatedListener;

    public BillingManager(Context context, PurchasesUpdatedListener listener) {
        this.purchasesUpdatedListener = listener;

        this.billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();
    }

    public void startConnection(BillingClientStateListener listener) {
        billingClient.startConnection(listener);
    }

    public void queryProducts(List<String> productIds, ProductDetailsResponseListener listener) {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        for (String productId : productIds) {
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, listener);
    }

    public void launchBillingFlow(Activity activity, ProductDetails productDetails,
                                  BillingFlowParams.SubscriptionUpdateParams subscriptionUpdateParams) {
        BillingFlowParams.Builder builder = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                        Collections.singletonList(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                        )
                );

        if (subscriptionUpdateParams != null) {
            builder.setSubscriptionUpdateParams(subscriptionUpdateParams);
        }

        billingClient.launchBillingFlow(activity, builder.build());
    }

    public void acknowledgePurchase(Purchase purchase, AcknowledgePurchaseResponseListener listener) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            billingClient.acknowledgePurchase(params, listener);
        } else {
            listener.onAcknowledgePurchaseResponse(
                    BillingResult.newBuilder()
                            .setResponseCode(BillingClient.BillingResponseCode.OK)
                            .build());
        }
    }

    public void queryPurchases(@BillingClient.ProductType String productType,
                               PurchasesResponseListener listener) {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build();

        billingClient.queryPurchasesAsync(params, listener);
    }

    public void queryAllPurchases(PurchasesResponseListener listener) {
        queryPurchases(BillingClient.ProductType.INAPP, (billingResult, inAppPurchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                queryPurchases(BillingClient.ProductType.SUBS, (subsResult, subsPurchases) -> {
                    List<Purchase> allPurchases = new ArrayList<>();
                    allPurchases.addAll(inAppPurchases);
                    allPurchases.addAll(subsPurchases);
                    listener.onQueryPurchasesResponse(
                            BillingResult.newBuilder()
                                    .setResponseCode(BillingClient.BillingResponseCode.OK)
                                    .build(),
                            allPurchases);
                });
            } else {
                listener.onQueryPurchasesResponse(billingResult, inAppPurchases);
            }
        });
    }

    public void endConnection() {
        billingClient.endConnection();
    }
}