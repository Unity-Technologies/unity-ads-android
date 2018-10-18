package com.unity3d.services.purchasing.core;

public class Product {
    private String productId;
    private String localizedPriceString;
    private String localizedTitle;
    private String isoCurrencyCode;
    private double localizedPrice;
    private String localizedDescription;
    private String productType;

    private Product(Builder builder) {
        productId = builder.productId;
        localizedPriceString = builder.localizedPriceString;
        localizedTitle = builder.localizedTitle;
        isoCurrencyCode = builder.isoCurrencyCode;
        localizedPrice = builder.localizedPrice;
        localizedDescription = builder.localizedDescription;
        productType = builder.productType;
    }

    public String getProductId() {
        return productId;
    }

    public String getLocalizedPriceString() {
        return localizedPriceString;
    }

    public String getLocalizedTitle() {
        return localizedTitle;
    }

    public String getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public double getLocalizedPrice() {
        return localizedPrice;
    }

    public String getLocalizedDescription() {
        return localizedDescription;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getProductType() {
        return productType;
    }

    public static final class Builder {
        private String productId;
        private String localizedPriceString;
        private String localizedTitle;
        private String isoCurrencyCode;
        private double localizedPrice;
        private String localizedDescription;
        private String productType;

        private Builder() {
        }

        public Builder withProductId(String val) {
            productId = val;
            return this;
        }

        public Builder withLocalizedPriceString(String val) {
            localizedPriceString = val;
            return this;
        }

        public Builder withLocalizedTitle(String val) {
            localizedTitle = val;
            return this;
        }

        public Builder withIsoCurrencyCode(String val) {
            isoCurrencyCode = val;
            return this;
        }

        public Builder withLocalizedPrice(double val) {
            localizedPrice = val;
            return this;
        }

        public Builder withLocalizedDescription(String val) {
            localizedDescription = val;
            return this;
        }

        public Builder withProductType(String val) {
            productType = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
