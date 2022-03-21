package com.unity3d.services.core.request.metrics;

public interface IMetricSenderWithBatch extends ISDKMetrics {
    void updateOriginal(ISDKMetrics metrics);
    void sendQueueIfNeeded();
}
