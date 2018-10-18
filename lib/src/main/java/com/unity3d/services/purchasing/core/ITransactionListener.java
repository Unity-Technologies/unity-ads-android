package com.unity3d.services.purchasing.core;

public interface ITransactionListener {
    void onTransactionComplete(TransactionDetails details);
    void onTransactionError(TransactionErrorDetails details);
}
