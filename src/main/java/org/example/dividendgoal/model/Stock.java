package org.example.dividendgoal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stock {
    private String ticker;
    private String name;
    private double yield;
    private String description;
    private String sector;
    private String frequency;
    private String risk;
    private int dividendYears;

    @JsonProperty("ticker")
    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("yield")
    public double getYield() {
        return yield;
    }

    public void setYield(double yield) {
        this.yield = yield;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("sector")
    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    @JsonProperty("frequency")
    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    @JsonProperty("risk")
    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    @JsonProperty("dividendYears")
    public int getDividendYears() {
        return dividendYears;
    }

    public void setDividendYears(int dividendYears) {
        this.dividendYears = dividendYears;
    }
}
