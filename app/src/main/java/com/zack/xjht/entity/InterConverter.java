package com.zack.xjht.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InterConverter implements PropertyConverter<List<Integer>, String> {

    private final Gson mGson;

    public InterConverter() {
        mGson = new Gson();
    }

    @Override
    public List<Integer> convertToEntityProperty(String databaseValue) {
        Type type = new TypeToken<ArrayList<Integer>>() {
        }.getType();
        ArrayList<Integer> list = mGson.fromJson(databaseValue , type);
        return list;
    }

    @Override
    public String convertToDatabaseValue(List<Integer> entityProperty) {
        String dbString = mGson.toJson(entityProperty);
        return dbString;
    }


}

