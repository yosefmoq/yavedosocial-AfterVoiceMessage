package ru.ifsoft.network.constants;

public final class AppConstants {


    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    public final static String AUTHORIZATION = "clientId";
    public final static String FAIL = "fail";
    public final static String SUCCESS = "success";
    public static final String ID = "id";
    public static final String TOTAL = "total";
    public static final String NAME = "name";
    public static final String GIPHY_KEY = "EXQTUb0BZaYr47glfXUr1XDLUPcuJE12";
    public static String getGifUrl(String id) {
        return "https://i.giphy.com/media/" + id + "/giphy.gif";
    }
}
