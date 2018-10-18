package com.unity3d.services.monetization.core.utilities;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONUtilities {
    public static Map<String, Object> jsonObjectToMap(JSONObject object) {
        Map<String, Object> map = new HashMap<>(object.length());
        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
            String key = it.next();
            try {
                Object value = mapTypeFromJSON(object.get(key));
                map.put(key, value);
            } catch (JSONException e) {
                DeviceLog.error("Could not put value in map: %s, %s", key, e.getMessage());
            }
        }
        return map;
    }

    public static List<Object> jsonArrayToList(JSONArray array) {
        List<Object> values = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            try {
                Object value = mapTypeFromJSON(array.get(i));
                values.add(value);
            } catch (JSONException e) {
                DeviceLog.error("Could not put value into list: %s", e.getMessage());
            }
        }
        return values;
    }

    private static Object mapTypeFromJSON(Object value) {
        if (value instanceof JSONObject) {
            value = jsonObjectToMap((JSONObject) value);
        } else if (value instanceof JSONArray) {
            value = jsonArrayToList((JSONArray) value);
        }
        return value;
    }

    public static JSONObject mapToJsonObject(Map<String, Object> extras) {
        JSONObject object = new JSONObject();
        for (Map.Entry<String, Object> entry : extras.entrySet()) {
            try {
                object.put(entry.getKey(), wrap(entry.getValue()));
            } catch (JSONException e) {
                DeviceLog.error("Could not map entry to object: %s, %s", entry.getKey(), entry.getValue());
            }
        }

        return object;
    }

    // Taken from the JSON API library. Placed here for backwards compatibility
    // so we don't up our minimum SDK.
    public static Object wrap(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(JSONObject.NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                return new JSONArray(Arrays.asList(o));
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o instanceof Enum) {
                return o.toString();
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
