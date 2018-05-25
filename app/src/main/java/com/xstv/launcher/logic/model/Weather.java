package com.xstv.launcher.logic.model;

public class Weather {
    private String city; // 城市
    private String temperature; // 温度值
    private String weatherDes; // 天气描述
    private int unit; // 单位
    private int image_icon; // 图片对应的资源id

    public Weather() {
        super();
    }

    public Weather(String city, String temperature, String weatherDes, int unit, int image_icon) {
        super();
        this.city = city;
        this.temperature = temperature;
        this.weatherDes = weatherDes;
        this.unit = unit;
        this.image_icon = image_icon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeatherDes() {
        return weatherDes;
    }

    public void setWeatherDes(String weatherDes) {
        this.weatherDes = weatherDes;
    }

    public int getUnit() {
        return unit;
    }

    public void setImageTitle(int unit) {
        this.unit = unit;
    }

    public int getImage_icon() {
        return image_icon;
    }

    public void setImage_icon(int image_icon) {
        this.image_icon = image_icon;
    }

    @Override
    public String toString() {
        return "Weather [city=" + city + ", temperature=" + temperature + ", weatherDes=" + weatherDes + ", unit=" + unit + ", image_icon=" + image_icon + "]";
    }
}