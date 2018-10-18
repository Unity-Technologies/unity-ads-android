package com.unity3d.services.purchasing.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TransactionDetails {

    private String productId;
    private String transactionId;
    private BigDecimal price;
    private String currency;
    private String receipt;
    private Map<String, Object> extras;

    private TransactionDetails(Builder builder) {
        productId = builder.productId;
        transactionId = builder.transactionId;
        receipt = builder.receipt;
        extras = builder.extras;
        price = builder.price;
        currency = builder.currency;
    }

    public String getProductId() {
        return productId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getReceipt() {
        return receipt;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String productId;
        private String transactionId;
        private BigDecimal price;
        private String currency;
        private String receipt;
        private Map<String, Object> extras = new HashMap<>();

        private Builder() {
        }

        public Builder withProductId(String val) {
            productId = val;
            return this;
        }

        public Builder withTransactionId(String val) {
            transactionId = val;
            return this;
        }

        public Builder withReceipt(String val) {
            receipt = val;
            return this;
        }

        public Builder withPrice(BigDecimal val) {
            price = val;
            return this;
        }

        public Builder withPrice(double val) {
            price = new BigDecimal(val);
            return this;
        }

        public Builder withCurrency(String val) {
            currency = val;
            return this;
        }

        public Builder putExtra(String key, Object value) {
            extras.put(key, value);
            return this;
        }

        public TransactionDetails build() {
            return new TransactionDetails(this);
        }
    }
}
