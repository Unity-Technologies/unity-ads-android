package com.unity3d.services.monetization.placementcontent.core;

import java.util.HashMap;
import java.util.Map;

public class CustomEvent {
    private String category;
    private String type;
    private Map<String, Object> data;

    public CustomEvent() {
    }

    public CustomEvent(String type) {
        this.type = type;
    }

    public CustomEvent(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public CustomEvent(String category, String type, Map<String, Object> data) {
        this.category = category;
        this.type = type;
        this.data = data;
    }

    private CustomEvent(Builder builder) {
        setCategory(builder.category);
        setType(builder.type);
        setData(builder.data);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }


    public static final class Builder {
        private String category;
        private String type;
        private Map<String, Object> data;

        private Builder() {
        }

        public Builder withCategory(String val) {
            category = val;
            return this;
        }

        public Builder withType(String val) {
            type = val;
            return this;
        }

        public Builder withData(Map<String, Object> val) {
            data = val;
            return this;
        }

        public Builder putAllData(Map<String, Object> vals) {
            if (data == null) {
                data = new HashMap<>(vals);
            } else {
                data.putAll(vals);
            }
            return this;
        }

        public Builder putData(String key, Object value) {
            if (data == null) {
                data = new HashMap<>();
            }
            data.put(key, value);
            return this;
        }

        public CustomEvent build() {
            return new CustomEvent(this);
        }
    }
}
