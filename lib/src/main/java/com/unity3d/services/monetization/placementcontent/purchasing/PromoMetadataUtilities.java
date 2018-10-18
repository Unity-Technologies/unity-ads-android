package com.unity3d.services.monetization.placementcontent.purchasing;

import com.unity3d.services.purchasing.core.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PromoMetadataUtilities {
    public static PromoMetadata createPromoMetadataFromParamsMap(Map<String, Object> params) {
        PromoMetadata.Builder builder = PromoMetadata.newBuilder();
        if (params.containsKey("impressionDate")) {
            long impression = ((long) params.get("impressionDate"));
            builder.withImpressionDate(new Date(impression));
        }
        if (params.containsKey("offerDuration")) {
            builder.withOfferDuration((long) params.get("offerDuration"));
        }
        if (params.containsKey("costs")) {
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) params.get("costs");
            List<Item> costs = getItemListFromList(itemsList);
            builder.withCosts(costs);
        }
        if (params.containsKey("payouts")) {
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) params.get("payouts");
            List<Item> payouts = getItemListFromList(itemsList);
            builder.withPayouts(payouts);
        }
        if (params.containsKey("product")) {
            Map<String, Object> productParams = (Map<String, Object>) params.get("product");
            builder.withPremiumProduct(createProductFromMap(productParams));
        }
        if (params.containsKey("userInfo")) {
            builder.withCustomInfo((Map<String, Object>) params.get("userInfo"));
        }
        return builder.build();
    }

    private static List<Item> getItemListFromList(List<Map<String, Object>> itemsList) {
        List<Item> items = new ArrayList<>(itemsList.size());
        for (Map<String, Object> itemMap : itemsList) {
            Item item = createItemFromMap(itemMap);
            items.add(item);
        }
        return items;
    }

    private static Item createItemFromMap(Map<String, Object> itemMap) {
        Item.Builder itemBuilder = Item.newBuilder();
        if (itemMap.containsKey("itemId")) {
            itemBuilder.withItemId((String) itemMap.get("itemId"));
        }
        if (itemMap.containsKey("quantity")) {
            itemBuilder.withQuantity(((Number) itemMap.get("quantity")).longValue());
        }
        if (itemMap.containsKey("type")) {
            itemBuilder.withType((String) itemMap.get("type"));
        }
        return itemBuilder.build();
    }

    private static Product createProductFromMap(Map<String, Object> productParams) {
        Product.Builder productBuilder = Product.newBuilder();
        if (productParams.containsKey("productId")) {
            productBuilder.withProductId((String) productParams.get("productId"));
        }
        if (productParams.containsKey("isoCurrencyCode")) {
            productBuilder.withIsoCurrencyCode((String) productParams.get("isoCurrencyCode"));
        }
        if (productParams.containsKey("localizedPriceString")) {
            productBuilder.withLocalizedPriceString((String) productParams.get("localizedPriceString"));
        }
        if (productParams.containsKey("localizedDescription")) {
            productBuilder.withLocalizedDescription((String) productParams.get("localizedDescription"));
        }
        if (productParams.containsKey("localizedTitle")) {
            productBuilder.withLocalizedTitle((String) productParams.get("localizedTitle"));
        }
        if (productParams.containsKey("localizedPrice")) {
            productBuilder.withLocalizedPrice((double) productParams.get("localizedPrice"));
        }
        if (productParams.containsKey("productType")) {
            productBuilder.withProductType((String) productParams.get("productType"));
        }

        return productBuilder.build();
    }
}
