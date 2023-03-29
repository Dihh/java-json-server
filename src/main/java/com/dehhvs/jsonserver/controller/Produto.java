package com.dehhvs.jsonserver.controller;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dehhvs.jsonserver.models.Database;

@RestController
public class Produto {

    Database database = new Database();
    final HttpHeaders httpHeaders = new HttpHeaders();

    Produto() {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @GetMapping(value = "/")
    public ResponseEntity<String> init() {
        JSONObject data = database.getAll();
        return new ResponseEntity<String>(data.toString(2), httpHeaders, HttpStatus.OK);
    }

    @GetMapping(value = "/{collection}")
    public ResponseEntity<String> getCollection(@PathVariable String collection,
            @RequestParam Map<String, Object> reqParam) {
        JSONArray data = database.getCollection(collection);
        HttpStatus status = HttpStatus.OK;
        if (data.length() == 0) {
            status = HttpStatus.NOT_FOUND;
        }
        data = database.filter(data, reqParam);
        return new ResponseEntity<String>(data.toString(2), httpHeaders, status);
    }

    @GetMapping(value = "/{collection}/{id}")
    public ResponseEntity<String> getCollection(@PathVariable String collection, @PathVariable String id) {
        JSONObject data = database.get(collection, id);
        HttpStatus status = HttpStatus.OK;
        if (data.length() == 0) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<String>(data.toString(2), httpHeaders, status);

    }

    @PostMapping(value = "/{collections}")
    public ResponseEntity<String> create(@PathVariable String collections, @RequestParam Map<String, Object> reqParam) {
        JSONObject data = database.post(reqParam, collections);
        HttpStatus status = HttpStatus.OK;
        if (data.length() == 0) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<String>(data.toString(2), httpHeaders, status);
    }

    @PutMapping(value = "/{collections}/{id}")
    public ResponseEntity<String> update(@PathVariable String collections, @RequestParam Map<String, Object> reqParam,
            @PathVariable String id) {
        JSONObject data = database.put(reqParam, collections, id);
        HttpStatus status = HttpStatus.OK;
        if (data.length() == 0) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<String>(data.toString(2), httpHeaders, status);
    }

    @PatchMapping(value = "/{collections}/{id}")
    public ResponseEntity<String> softUpdate(@PathVariable String collections,
            @RequestParam Map<String, Object> reqParam,
            @PathVariable String id) {
        JSONObject data = database.patch(reqParam, collections, id);
        HttpStatus status = HttpStatus.OK;
        if (data.length() == 0) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<String>(data.toString(2), httpHeaders, status);
    }

    @DeleteMapping(value = "/{collections}/{id}")
    public ResponseEntity<String> delete(@PathVariable String collections, @PathVariable String id) {
        JSONObject data = database.delete(collections, id);
        HttpStatus status = HttpStatus.OK;
        if (data.length() == 0) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<String>(data.toString(2), httpHeaders, status);
    }
}
