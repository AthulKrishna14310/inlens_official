package com.integrals.inlens.Weather.Model;

/**
 * Created by Athul Krishna on 03/09/2017.
 */

public class Sys {
    private int type;
    private int id;
    private double messages;
    private String country;
    private double sunrise;
    private double sunset;

    public Sys(int type, int id, double messages, String country, double sunrise, double sunset) {
        this.type = type;
        this.id = id;
        this.messages = messages;
        this.country = country;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMessages() {
        return messages;
    }

    public void setMessages(double messages) {
        this.messages = messages;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getSunrise() {
        return sunrise;
    }

    public void setSunrise(double sunrise) {
        this.sunrise = sunrise;
    }

    public double getSunset() {
        return sunset;
    }

    public void setSunset(double sunset) {
        this.sunset = sunset;
    }

    public Sys() {
    }
}
