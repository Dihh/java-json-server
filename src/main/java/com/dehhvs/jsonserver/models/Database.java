package com.dehhvs.jsonserver.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class Database {

    FileManagment fileManagment = new FileManagment();

    public JSONObject typing(Map<String, Object> reqParam) {
        JSONObject element = new JSONObject();
        for (String key : reqParam.keySet()) {
            try {
                Integer value = Integer.parseInt((String) reqParam.get(key));
                element.put(key, value);
            } catch (Exception e) {
                try {
                    Float value = Float.parseFloat((String) reqParam.get(key));
                    element.put(key, value);
                } catch (Exception er) {
                    if (reqParam.get(key).equals("true") || reqParam.get(key).equals("false")) {
                        Boolean value = Boolean.parseBoolean((String) reqParam.get(key));
                        element.put(key, value);
                    } else {
                        element.put(key, reqParam.get(key));
                    }
                }
            }
        }
        return element;
    }

    public void write(JSONObject database) {
        try {
            fileManagment.writeFile(database.toString(2));
        } catch (Exception e) {
        }
    }

    public JSONArray findCollection(JSONObject database, String collection) {
        JSONArray JSONcollectionObject = new JSONArray("[]");
        try {
            JSONcollectionObject = database.getJSONArray(collection);
        } catch (Exception e) {
        }
        return JSONcollectionObject;
    }

    public OptionalInt findElementIndex(JSONArray JSONCollection, String id) {
        return IntStream.range(0, JSONCollection.length()).filter(index -> {
            JSONObject element = new JSONObject(JSONCollection.get(index).toString());
            return element.getString("id").equals(id);
        }).findFirst();
    }

    public JSONObject findElement(JSONArray JSONCollection, String id) {
        OptionalInt elementIndex = findElementIndex(JSONCollection, id);
        if (elementIndex.isPresent()) {
            return JSONObject.class.cast(JSONCollection.get(elementIndex.getAsInt()));
        } else {
            return new JSONObject("{}");
        }
    }

    public JSONArray getCollection(String collection) {
        JSONObject database = getAll();
        JSONArray JSONCollection = findCollection(database, collection);
        return JSONCollection;
    }

    public JSONArray filter_and(JSONArray data, Map<String, Object> filters) {
        if (filters.containsKey("q")) {
            data = full_text_search(data, (String) filters.get("q"));
        }
        JSONArray JSONcollectionObjectList = new JSONArray(
                data.toList().stream()
                        .map(ele -> new JSONObject(HashMap.class.cast(ele)))
                        .filter(ele -> {
                            Boolean filtered = true;
                            for (String key : filters.keySet()) {
                                if (key.equals("q")) {
                                    continue;
                                }
                                if (!ele.getString(key).equals(filters.get(key))) {
                                    filtered = false;
                                }
                            }
                            return filtered;
                        })
                        .toList());
        return JSONcollectionObjectList;
    }

    public JSONArray full_text_search(JSONArray data, String textSearch) {
        JSONArray JSONcollectionObjectList = new JSONArray(
                data.toList().stream()
                        .map(ele -> new JSONObject(HashMap.class.cast(ele)))
                        .filter(ele -> {
                            Boolean filtered = false;
                            for (String key : ele.keySet()) {
                                String elementString = Utils.stripAccents(ele.get(key).toString()).toLowerCase();
                                String searchString = Utils.stripAccents(textSearch).toLowerCase();
                                if (elementString.contains(searchString)) {
                                    filtered = true;
                                }
                            }
                            return filtered;
                        })
                        .toList());
        return JSONcollectionObjectList;
    }

    public JSONObject getAll() {
        JSONObject JSONdata = new JSONObject("{}");
        try {
            String fileData = fileManagment.readFile();
            JSONdata = new JSONObject(fileData);
        } catch (Exception e) {
        }
        return JSONdata;
    }

    public JSONObject get(String collection, String id) {
        JSONArray JSONCollection = getCollection(collection);
        return findElement(JSONCollection, id);
    }

    public JSONObject post(Map<String, Object> reqParam, String collection) {
        JSONObject JSONreqParam = typing(reqParam);

        JSONObject database = getAll();

        UUID uuid = UUID.randomUUID();
        JSONreqParam.put("id", uuid);
        JSONObject element = new JSONObject(JSONreqParam.toString());
        JSONArray JSONCollection = findCollection(database, collection);
        JSONCollection.put(element);
        if (JSONCollection.length() == 1) {
            database.put(collection, JSONCollection);
        }
        write(database);
        return element;
    }

    public JSONObject put(Map<String, Object> reqParam, String collection, String id) {
        JSONObject JSONreqParam = typing(reqParam);
        JSONObject database = getAll();

        JSONArray JSONCollection = findCollection(database, collection);
        JSONObject databaseElement = findElement(JSONCollection, id);

        Set<String> keys = databaseElement.keySet();
        Set<String> keysClone = new HashSet<>();
        keysClone.addAll(keys);

        for (String key : keysClone) {
            if (!key.equals("id")) {
                databaseElement.remove(key);
            }
        }
        for (String key : JSONreqParam.keySet()) {
            databaseElement.put(key, JSONreqParam.get(key));
        }
        write(database);
        return databaseElement;
    }

    public JSONObject patch(Map<String, Object> reqParam, String collection, String id) {
        JSONObject JSONreqParam = typing(reqParam);
        JSONObject database = getAll();

        JSONArray JSONCollection = findCollection(database, collection);
        JSONObject databaseElement = findElement(JSONCollection, id);

        for (String key : JSONreqParam.keySet()) {
            databaseElement.put(key, JSONreqParam.get(key));
        }

        write(database);
        return databaseElement;
    }

    public JSONObject delete(String collection, String id) {
        JSONObject database = getAll();

        JSONArray JSONCollection = findCollection(database, collection);
        OptionalInt elementIndex = findElementIndex(JSONCollection, id);
        JSONObject element = new JSONObject("{}");
        if (elementIndex.isPresent()) {
            element = JSONCollection.getJSONObject(elementIndex.getAsInt());
            JSONCollection.remove(elementIndex.getAsInt());
            if (JSONCollection.length() == 0) {
                database.remove(collection);
            }
            write(database);
        }
        return element;
    }
}
