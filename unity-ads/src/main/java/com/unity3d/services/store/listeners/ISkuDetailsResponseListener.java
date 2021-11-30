package com.unity3d.services.store.listeners;

import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsBridge;

import java.util.List;

public interface ISkuDetailsResponseListener extends IBillingDataResponseListener<SkuDetailsBridge> {
	void onBillingResponse(BillingResultBridge billingResult, List<SkuDetailsBridge> skuDetailsList);
}
