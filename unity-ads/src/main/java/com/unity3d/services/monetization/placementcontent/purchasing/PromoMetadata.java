package com.unity3d.services.monetization.placementcontent.purchasing;

import com.unity3d.services.purchasing.core.Product;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Metadata about a promotion.
 */
public class PromoMetadata {
    private Date impressionDate;
    private long offerDuration;
    private Product premiumProduct;
    private List<Item> costs;
    private List<Item> payouts;
    private Map<String, Object> customInfo;

    private PromoMetadata(Builder builder) {
        impressionDate = builder.impressionDate;
        offerDuration = builder.offerDuration;
        premiumProduct = builder.premiumProduct;
        costs = builder.costs;
        payouts = builder.payouts;
        customInfo = builder.customInfo;
    }

    public Date getImpressionDate() {
        return impressionDate;
    }

    public long getOfferDuration() {
        return offerDuration;
    }

    public List<Item> getCosts() {
        return costs;
    }

    public List<Item> getPayouts() {
        return payouts;
    }

    public Product getPremiumProduct() {
        return premiumProduct;
    }

    public Map<String, Object> getCustomInfo() {
        return customInfo;
    }

    public Item getCost() {
        if (costs != null && costs.size() > 0) {
            return costs.get(0);
        }
        return null;
    }

    public Item getPayout() {
        if (payouts != null && payouts.size() > 0) {
            return payouts.get(0);
        }
        return null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Date impressionDate;
        private long offerDuration;
        private Product premiumProduct;
        private List<Item> costs;
        private List<Item> payouts;
        private Map<String, Object> customInfo;

        private Builder() {
        }

        public PromoMetadata build() {
            return new PromoMetadata(this);
        }

        public Builder withImpressionDate(Date impressionDate) {
            this.impressionDate = impressionDate;
            return this;
        }

        public Builder withOfferDuration(long val) {
            this.offerDuration = val;
            return this;
        }

        public Builder withPremiumProduct(Product product) {
            this.premiumProduct = product;
            return this;
        }

        public Builder withCosts(List<Item> costs) {
            this.costs = costs;
            return this;
        }

        public Builder withPayouts(List<Item> payouts) {
            this.payouts = payouts;
            return this;
        }

        public Builder withCustomInfo(Map<String, Object> info) {
            this.customInfo = info;
            return this;
        }
    }
}
