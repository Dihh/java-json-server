package com.dehhvs.jsonserver.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class Database {

    FileManagment fileManagment = new FileManagment();

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

    public JSONArray filter(JSONArray data, Map<String, Object> filters) {
        if (filters.containsKey("q")) {
            data = full_text_search(data, (String) filters.get("q"));
            filters.remove("q");
        }
        List<String> filtersLike = filters.keySet().stream().filter(key -> key.contains("_like")).toList();
        List<String> filtersOr = filters.keySet().stream().filter(key -> key.contains("_or")).toList();
        List<String> filtersNot = filters.keySet().stream().filter(key -> key.contains("_not")).toList();
        List<String> filtersGte = filters.keySet().stream().filter(key -> key.contains("_gte")).toList();
        List<String> filtersLte = filters.keySet().stream().filter(key -> key.contains("_lte")).toList();
        List<String> filtersAnd = filters.keySet().stream()
                .filter(key -> !key.contains("_or") && !key.contains("_like") && !key.contains("_not")
                        && !key.contains("_gte") && !key.contains("_lte"))
                .toList();
        JSONArray JSONcollectionObjectList = new JSONArray(
                data.toList().stream()
                        .map(ele -> new JSONObject(HashMap.class.cast(ele)))
                        .filter(element -> {
                            StringBuilder filtered = new StringBuilder("true");
                            this.filterAnd(filtersAnd, element, filters, filtered);
                            this.filterOr(filtersOr, element, filters, filtered);
                            this.filterLike(filtersLike, element, filters, filtered);
                            this.filterNot(filtersNot, element, filters, filtered);
                            this.filterGte(filtersGte, element, filters, filtered);
                            this.filterLte(filtersLte, element, filters, filtered);
                            return filtered.toString().equals("true");
                        })
                        .toList());
        return JSONcollectionObjectList;
    }

    public void filterAnd(List<String> filtersAnd, JSONObject element, Map<String, Object> filters,
            StringBuilder filtered) {
        for (String key : filtersAnd) {
            String elementString = Utils.stripAccents(element.get(key).toString()).toLowerCase();
            String searchString = Utils.stripAccents(filters.get(key).toString()).toLowerCase();
            if (!elementString.equals(searchString)) {
                filtered.append("false");
            }
        }
    }

    public void filterOr(List<String> filtersOr, JSONObject element, Map<String, Object> filters,
            StringBuilder filtered) {
        for (String key : filtersOr) {
            String elementKey = key.subSequence(0, key.length() - 3).toString();
            String elementString = Utils.stripAccents(element.get(elementKey).toString())
                    .toLowerCase();
            String searchString = Utils.stripAccents(filters.get(key).toString()).toLowerCase();
            if (elementString.equals(searchString)) {
                filtered.append("true");
            }
        }
    }

    public void filterLike(List<String> filtersLike, JSONObject element, Map<String, Object> filters,
            StringBuilder filtered) {
        for (String key : filtersLike) {
            String elementKey = key.subSequence(0, key.length() - 5).toString();
            String elementString = Utils.stripAccents(element.get(elementKey).toString())
                    .toLowerCase();
            String searchString = Utils.stripAccents(filters.get(key).toString()).toLowerCase();
            if (!elementString.contains(searchString)) {
                filtered.append("false");
            }
        }
    }

    public void filterNot(List<String> filtersNot, JSONObject element, Map<String, Object> filters,
            StringBuilder filtered) {
        for (String key : filtersNot) {
            String elementKey = key.subSequence(0, key.length() - 4).toString();
            String elementString = Utils.stripAccents(element.get(elementKey).toString())
                    .toLowerCase();
            String searchString = Utils.stripAccents(filters.get(key).toString()).toLowerCase();
            if (elementString.equals(searchString)) {
                filtered.append("false");
            }
        }
    }

    public void filterGte(List<String> filtersGte, JSONObject element, Map<String, Object> filters,
            StringBuilder filtered) {
        for (String key : filtersGte) {
            String elementKey = key.subSequence(0, key.length() - 4).toString();
            String elementString = Utils.stripAccents(element.get(elementKey).toString())
                    .toLowerCase();
            try {
                Float elementFloat = Float.parseFloat(elementString);
                Float searchFloat = Float.parseFloat(filters.get(key).toString());
                if (elementFloat < searchFloat) {
                    filtered.append("false");
                }
            } catch (Exception e) {
                filtered.append("false");
            }
        }
    }

    public void filterLte(List<String> filtersLte, JSONObject element, Map<String, Object> filters,
            StringBuilder filtered) {
        for (String key : filtersLte) {
            String elementKey = key.subSequence(0, key.length() - 4).toString();
            String elementString = Utils.stripAccents(element.get(elementKey).toString())
                    .toLowerCase();
            try {
                Float elementFloat = Float.parseFloat(elementString);
                Float searchFloat = Float.parseFloat(filters.get(key).toString());
                if (elementFloat > searchFloat) {
                    filtered.append("false");
                }
            } catch (Exception e) {
                filtered.append("false");
            }
        }
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
        JSONObject JSONreqParam = Utils.typing(reqParam);

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
        JSONObject JSONreqParam = Utils.typing(reqParam);
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
        JSONObject JSONreqParam = Utils.typing(reqParam);
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
