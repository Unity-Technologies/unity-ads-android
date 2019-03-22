package com.unity3d.services.store;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.store.core.StoreException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class StoreBilling {
	public static Object asInterface(Context context, IBinder service) {
		Object[] args = new Object[] { service };

		Class billingServiceStub;

		try {
			billingServiceStub = Class.forName("com.android.vending.billing.IInAppBillingService$Stub");
		} catch (ClassNotFoundException e) {
			DeviceLog.exception("Billing service stub not found", e);
			return null;
		}

		Method asInterface;

		try {
			asInterface = billingServiceStub.getMethod("asInterface", IBinder.class);
		} catch (NoSuchMethodException e) {
			DeviceLog.exception("asInterface method not found", e);
			return null;
		}

		try {
			return asInterface.invoke(null, service);
		} catch (IllegalAccessException e) {
			DeviceLog.exception("Illegal access exception while invoking asInterface", e);
		} catch (InvocationTargetException e) {
			DeviceLog.exception("Invocation target exception while invoking asInterface", e);
		}

		return null;
	}

	public static int isBillingSupported(Context context, Object billingServiceObject, String purchaseType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, StoreException {
		Class billingService = Class.forName("com.android.vending.billing.IInAppBillingService");
		Method isBillingSupported = billingService.getMethod("isBillingSupported", Integer.TYPE, String.class, String.class);

		Object result = isBillingSupported.invoke(billingServiceObject, 3, ClientProperties.getAppName(), purchaseType);

		if(result != null) {
			return (int) result;
		}

		throw new StoreException();
	}

	public static JSONObject getPurchases(Context context, Object billingServiceObject, String purchaseType) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, JSONException, StoreException {
		Class billingService = Class.forName("com.android.vending.billing.IInAppBillingService");
		Method getPurchases = billingService.getMethod("getPurchases", Integer.TYPE, String.class, String.class, String.class);

		JSONObject resultObject = new JSONObject();
		JSONArray purchaseDataArray = new JSONArray();
		JSONArray signatureArray = new JSONArray();
		JSONArray productArray = new JSONArray();
		String continuationToken = null;

		do {
			Object result = getPurchases.invoke(billingServiceObject, 3, ClientProperties.getAppName(), purchaseType, continuationToken);
			continuationToken = null;

			if(result instanceof Bundle) {
				Bundle resultBundle = (Bundle) result;

				int responseCode = resultBundle.getInt("RESPONSE_CODE");
				DeviceLog.debug("getPurchases responds with code " + responseCode);

				if(responseCode == 0) {
					ArrayList<String> purchaseDataList = resultBundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					for(String purchase : purchaseDataList) {
						purchaseDataArray.put(new JSONObject(purchase));
					}

					ArrayList<String> signatureList = resultBundle.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
					for(String signature : signatureList) {
						signatureArray.put(signature);
					}

					ArrayList<String> productList = resultBundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					for(String product : productList) {
						productArray.put(product);
					}

					continuationToken = resultBundle.getString("INAPP_CONTINUATION_TOKEN");
				} else {
					throw new StoreException(responseCode);
				}
			} else {
				throw new StoreException();
			}
		} while(continuationToken != null);

		resultObject.put("purchaseDataList", purchaseDataArray);
		resultObject.put("signatureList", signatureArray);
		resultObject.put("purchaseItemList", productArray);

		return resultObject;
	}

	public static JSONObject getPurchaseHistory(Context context, Object billingServiceObject, String purchaseType, int maxPurchases) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException, StoreException {
		Class billingService = Class.forName("com.android.vending.billing.IInAppBillingService");
		Method getPurchaseHistory = billingService.getMethod("getPurchaseHistory", Integer.TYPE, String.class, String.class, String.class, Bundle.class);

		JSONObject resultObject = new JSONObject();
		JSONArray purchaseDataArray = new JSONArray();
		JSONArray signatureArray = new JSONArray();
		JSONArray productArray = new JSONArray();

		String continuationToken = null;
		int purchaseCount = 0;

		do {
			Object result = getPurchaseHistory.invoke(billingServiceObject, 6, ClientProperties.getAppName(), purchaseType, continuationToken, new Bundle());
			continuationToken = null;

			if(result instanceof Bundle) {
				Bundle resultBundle = (Bundle) result;

				int responseCode = resultBundle.getInt("RESPONSE_CODE");

				if(responseCode == 0) {
					ArrayList<String> purchaseDataList = resultBundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					for(String purchase : purchaseDataList) {
						purchaseDataArray.put(new JSONObject(purchase));
						purchaseCount++;
					}

					ArrayList<String> signatureList = resultBundle.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
					for(String signature : signatureList) {
						signatureArray.put(signature);
					}

					ArrayList<String> productList = resultBundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					for(String product : productList) {
						productArray.put(product);
					}

					continuationToken = resultBundle.getString("INAPP_CONTINUATION_TOKEN");
				} else {
					throw new StoreException(responseCode);
				}
			} else {
				throw new StoreException();
			}
		} while(continuationToken != null && (maxPurchases == 0 || purchaseCount < maxPurchases));

		resultObject.put("purchaseDataList", purchaseDataArray);
		resultObject.put("signatureList", signatureArray);
		resultObject.put("purchaseItemList", productArray);

		return resultObject;
	}

	public static JSONArray getSkuDetails(Context context, Object billingServiceObject, String purchaseType, ArrayList<String> skuList) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, StoreException, JSONException {
		Class billingService = Class.forName("com.android.vending.billing.IInAppBillingService");
		Method getSkuDetails = billingService.getMethod("getSkuDetails", Integer.TYPE, String.class, String.class, Bundle.class);

		Bundle args = new Bundle();
		args.putStringArrayList("ITEM_ID_LIST", skuList);

		Object result = getSkuDetails.invoke(billingServiceObject, 3, ClientProperties.getAppName(), purchaseType, args);

		JSONArray resultArray = new JSONArray();

		if(result instanceof Bundle) {
			Bundle resultBundle = (Bundle) result;

			int responseCode = resultBundle.getInt("RESPONSE_CODE");

			if(responseCode == 0) {
				ArrayList<String> detailsList = resultBundle.getStringArrayList("DETAILS_LIST");

				for(String detail : detailsList) {
					resultArray.put(new JSONObject(detail));
				}
			} else {
				throw new StoreException(responseCode);
			}
		} else {
			throw new StoreException();
		}

		return resultArray;
	}
}
