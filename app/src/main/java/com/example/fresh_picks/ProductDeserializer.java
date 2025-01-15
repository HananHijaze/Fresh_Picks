package com.example.fresh_picks;

import com.example.fresh_picks.classes.Product;
import com.google.gson.*;
import java.lang.reflect.Type;

public class ProductDeserializer implements JsonDeserializer<Product> {

    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize `_id` field (with `$oid`)
        Product.IdWrapper idWrapper = new Product.IdWrapper();
        if (jsonObject.has("_id") && jsonObject.get("_id").getAsJsonObject().has("$oid")) {
            idWrapper.setOid(jsonObject.get("_id").getAsJsonObject().get("$oid").getAsString());
        }

        // Deserialize other fields
        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : null;
        double price = jsonObject.has("price") && jsonObject.get("price").getAsJsonObject().has("$numberDouble")
                ? jsonObject.get("price").getAsJsonObject().get("$numberDouble").getAsDouble()
                : 0.0;
        String category = jsonObject.has("category") ? jsonObject.get("category").getAsString() : null;
        int stockQuantity = jsonObject.has("stockQuantity") && jsonObject.get("stockQuantity").getAsJsonObject().has("$numberInt")
                ? jsonObject.get("stockQuantity").getAsJsonObject().get("$numberInt").getAsInt()
                : 0;
        String imageUrl = jsonObject.has("imageUrl") ? jsonObject.get("imageUrl").getAsString() : null;

        // Return the populated `Product` object
        return new Product(idWrapper, name, price, category, stockQuantity, imageUrl);
    }
}
