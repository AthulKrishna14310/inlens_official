package com.integrals.inlens.Helper;

public class CountryItem {

    /*
    Phone Auth
     */

    private String CountryName;
    private String CountryCode;
    private String FullCountryName;


    public CountryItem(String countryName, String countryCode, String fullCountryName) {
        CountryName = countryName;
        CountryCode = countryCode;
        FullCountryName = fullCountryName;
    }

    public String getCountryName() {
        return CountryName;
    }

    public String getCountryCode() {
        return CountryCode;
    }

    public String getFullCountryName() {
        return FullCountryName;
    }
}
