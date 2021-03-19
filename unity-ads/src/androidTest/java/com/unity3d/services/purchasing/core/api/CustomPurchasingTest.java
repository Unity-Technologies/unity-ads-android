package com.unity3d.services.purchasing.core.api;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.purchasing.core.IPurchasingAdapter;
import com.unity3d.services.purchasing.core.IRetrieveProductsListener;
import com.unity3d.services.purchasing.core.ITransactionListener;
import com.unity3d.services.purchasing.core.Product;
import com.unity3d.services.purchasing.core.Store;
import com.unity3d.services.purchasing.core.TransactionDetails;
import com.unity3d.services.purchasing.core.TransactionDetailsUtilities;
import com.unity3d.services.purchasing.core.TransactionError;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.purchasing.core.TransactionErrorDetails;
import com.unity3d.services.purchasing.core.TransactionErrorDetailsUtilities;
import com.unity3d.services.purchasing.core.properties.ClientProperties;
import com.unity3d.services.purchasing.core.PurchasingEvent;
import com.unity3d.services.monetization.core.utilities.JSONUtilities;
import com.unity3d.services.monetization.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class CustomPurchasingTest {

    private IPurchasingAdapter purchasingAdapter;
    private WebViewApp webViewApp;

    @Before
    public void setUp() {
        purchasingAdapter = mock(IPurchasingAdapter.class);
        webViewApp = mock(WebViewApp.class);
        ClientProperties.setAdapter(purchasingAdapter);
        WebViewApp.setCurrentApp(webViewApp);
    }

    @After
    public void tearDown() {
        ClientProperties.setAdapter(null);
        WebViewApp.setCurrentApp(null);
    }

    @Test
    public void availableShouldReturnTrueIfPurchasingAdapterIsSet() {
        WebViewCallback callback = mock(WebViewCallback.class);
        CustomPurchasing.available(callback);
        verify(callback).invoke(true);
    }

    @Test
    public void availableShouldReturnFalseIfPurchasingAdapterIsNotSet() {
        WebViewCallback callback = mock(WebViewCallback.class);
        ClientProperties.setAdapter(null);
        CustomPurchasing.available(callback);
        verify(callback).invoke(false);
    }

    @Test
    public void retrieveCatalogsShouldCallThePurchasingAdapter() {
        WebViewCallback callback = mock(WebViewCallback.class);

        CustomPurchasing.refreshCatalog(callback);
        verify(callback).invoke();
        verify(purchasingAdapter).retrieveProducts(any(IRetrieveProductsListener.class));
    }

    @Test
    public void retrieveCatalogShouldSendCatalogEventToWebView() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                IRetrieveProductsListener listener = invocation.getArgument(0);
                listener.onProductsRetrieved(Fixture.products);
                return null;
            }
        }).when(purchasingAdapter).retrieveProducts(any(IRetrieveProductsListener.class));

        WebViewCallback callback = mock(WebViewCallback.class);
        CustomPurchasing.refreshCatalog(callback);

        verify(webViewApp).sendEvent(eq(WebViewEventCategory.CUSTOM_PURCHASING),
                eq(PurchasingEvent.PRODUCTS_RETRIEVED),
                argThat(new JsonArrayMatcher(CustomPurchasing.getJSONArrayFromProductList(Fixture.products))));
    }

    @Test
    public void purchaseItemShouldCallThePurchasingAdapter() {
        WebViewCallback callback = mock(WebViewCallback.class);
        CustomPurchasing.purchaseItem(Fixture.productID, Fixture.purchaseExtras, callback);

        verify(purchasingAdapter).onPurchase(eq(Fixture.productID), any(ITransactionListener.class), eq(JSONUtilities.jsonObjectToMap(Fixture.purchaseExtras)));
    }

    @Test
    public void onTransactionCompleteShouldSendTransactionCompleteEventToWebView() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ITransactionListener listener = invocation.getArgument(1);
                listener.onTransactionComplete(Fixture.transactionDetails);
                return null;
            }
        }).when(purchasingAdapter).onPurchase(eq(Fixture.productID), any(ITransactionListener.class), eq(JSONUtilities.jsonObjectToMap(Fixture.purchaseExtras)));

        WebViewCallback callback = mock(WebViewCallback.class);
        CustomPurchasing.purchaseItem(Fixture.productID, Fixture.purchaseExtras, callback);

        verify(webViewApp).sendEvent(eq(WebViewEventCategory.CUSTOM_PURCHASING),
                eq(PurchasingEvent.TRANSACTION_COMPLETE),
                argThat(new JsonObjectMatcher(TransactionDetailsUtilities.getJSONObjectForTransactionDetails(Fixture.transactionDetails))));
    }

    @Test
    public void onTransactionErrorShouldSendTransactionErrorEventToWebView() {
        final TransactionErrorDetails detail = TransactionErrorDetails.newBuilder()
                .withStore(Store.GOOGLE_PLAY)
                .withTransactionError(TransactionError.ITEM_UNAVAILABLE)
                .withExceptionMessage("test exception")
                .withStoreSpecificErrorCode("google failed")
                .build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ITransactionListener listener = invocation.getArgument(1);
                listener.onTransactionError(detail);
                return null;
            }
        }).when(purchasingAdapter).onPurchase(eq(Fixture.productID), any(ITransactionListener.class), eq(JSONUtilities.jsonObjectToMap(Fixture.purchaseExtras)));

        WebViewCallback callback = mock(WebViewCallback.class);
        CustomPurchasing.purchaseItem(Fixture.productID, Fixture.purchaseExtras, callback);

        ;
        verify(webViewApp).sendEvent(eq(WebViewEventCategory.CUSTOM_PURCHASING),
                eq(PurchasingEvent.TRANSACTION_ERROR),
                argThat(new JsonObjectMatcher(TransactionErrorDetailsUtilities.getJSONObjectForTransactionErrorDetails(detail))));
    }

    private static class Fixture {
        public static String productID = "DEADBEEF-BEEFCACE";
        public static List<Product> products;
        public static JSONObject purchaseExtras;
        public static TransactionDetails transactionDetails;
        public static TransactionError transactionError = TransactionError.UNKNOWN_ERROR;
        public static String transactionErrorMessage = "Someone set us up the bomb.";

        static {
            products = new ArrayList<>();
            products.add(Product.newBuilder()
                .withProductId("DEADBEEF-BEEFCACE")
                .withLocalizedTitle("Sword of a Thousand Truths")
                .withLocalizedPriceString("$9000.00")
                .build());

            purchaseExtras = new JSONObject();
            try {
                purchaseExtras.put("foo", "Bar");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            transactionDetails = TransactionDetails.newBuilder()
                    .withProductId("foo")
                    .withTransactionId("abcd1234")
                    .withCurrency("USD")
                    .withPrice(new BigDecimal(23))
                    .withReceipt("Receipt=True")
                    .putExtra("foo", "bar")
                    .build();
        }
    }

    private static class JsonArrayMatcher implements ArgumentMatcher<JSONArray> {
        private final JSONArray src;

        private JsonArrayMatcher(JSONArray src) {
            this.src = src;
        }

        @Override
        public boolean matches(JSONArray o) {
            return src.toString().equals(o.toString());
        }
    }

    private static class JsonObjectMatcher implements ArgumentMatcher<JSONObject> {
        private final JSONObject src;

        private JsonObjectMatcher(JSONObject src) {
            this.src = src;
        }

        @Override
        public boolean matches(JSONObject o) {
            return src.toString().equals(o.toString());
        }
    }
}
