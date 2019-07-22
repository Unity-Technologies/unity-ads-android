package com.unity3d.services.monetization.placementcontent.purchasing;

public class Item {
    private String itemId;
    private long quantity;
    private String type;

    private Item(Builder builder) {
        itemId = builder.itemId;
        quantity = builder.quantity;
        type = builder.type;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getItemId() {
        return itemId;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getType() {
        return type;
    }

    public static final class Builder {
        private String itemId;
        private long quantity;
        private String type;

        private Builder() {
        }

        public Builder withItemId(String val) {
            itemId = val;
            return this;
        }

        public Builder withQuantity(long val) {
            quantity = val;
            return this;
        }

        public Builder withType(String val) {
            type = val;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}
