package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.services.ads.operation.IAdOperation;

public interface ILoadOperation extends IAdOperation, IUnityAdsLoadListener {
	LoadOperationState getLoadOperationState();
}
