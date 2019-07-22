package com.unity3d.services.purchasing.core;

import java.util.HashMap;
import java.util.Map;

public class TransactionErrorDetails {

    private TransactionError transactionError;
    private String exceptionMessage;
    private Store store;
    private String storeSpecificErrorCode;
    private Map<String, Object> extras;

    private TransactionErrorDetails(Builder builder) {
        transactionError = builder.transactionError;
        exceptionMessage = builder.exceptionMessage;
        store = builder.store;
        storeSpecificErrorCode = builder.storeSpecificErrorCode;
        extras = builder.extras;
    }

    public TransactionError getTransactionError() {
        return transactionError;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public Store getStore() {
        return store;
    }

    public String getStoreSpecificErrorCode() {
        return storeSpecificErrorCode;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private TransactionError transactionError;
        private String exceptionMessage;
        private Store store;
        private String storeSpecificErrorCode;
        private Map<String, Object> extras = new HashMap<>();

        private Builder() {}

        public Builder withTransactionError(TransactionError val) {
            transactionError = val;
            return this;
        }

        public Builder withExceptionMessage(String val) {
            exceptionMessage = val;
            return this;
        }

        public Builder withStore(Store val) {
            store = val;
            return this;
        }

        public Builder withStoreSpecificErrorCode(String val) {
            storeSpecificErrorCode = val;
            return this;
        }

        public Builder putExtras(Map<String, Object> val) {
            for (Map.Entry<String, Object> entry: val.entrySet()) {
                extras.put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder putExtra(String key, Object val) {
            extras.put(key, val);
            return this;
        }

        public TransactionErrorDetails build() {
            return new TransactionErrorDetails(this);
        }
    }
}
