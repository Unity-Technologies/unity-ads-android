package com.unity3d.services.core.request.metrics;

import com.unity3d.services.core.properties.InitializationStatusReader;

public abstract class IMetricSenderWithBatch extends MetricSenderBase {

	public IMetricSenderWithBatch(InitializationStatusReader initializationStatusReader) {
		super(initializationStatusReader);
	}

    abstract void updateOriginal(ISDKMetrics metrics);
	abstract void sendQueueIfNeeded();
}
