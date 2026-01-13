package org.example.dividendgoal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Stock {
    private String ticker;
    private String name;
    private double yield;
    private String description;
    private String sector;
    private String frequency;
    private String risk;
    private int dividendYears;
    private double dividendGrowth; // [추가됨]

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

    // ▼ [새로 추가된 Getter/Setter]
    @JsonProperty("dividendGrowth")
    public double getDividendGrowth() {
        return dividendGrowth;
    }

    public void setDividendGrowth(double dividendGrowth) {
        this.dividendGrowth = dividendGrowth;
    }

    // --- [SEO] New Data Fields ---

    private double sectorMedianYield;
    private int consecutiveGrowthYears;
    private boolean dividendCutHistory;
    private double payoutRatio;
    private List<DividendRecord> dividendHistory;

    @JsonProperty("sectorMedianYield")
    public double getSectorMedianYield() {
        return sectorMedianYield;
    }

    public void setSectorMedianYield(double sectorMedianYield) {
        this.sectorMedianYield = sectorMedianYield;
    }

    @JsonProperty("consecutiveGrowthYears")
    public int getConsecutiveGrowthYears() {
        return consecutiveGrowthYears;
    }

    public void setConsecutiveGrowthYears(int consecutiveGrowthYears) {
        this.consecutiveGrowthYears = consecutiveGrowthYears;
    }

    @JsonProperty("dividendCutHistory")
    public boolean isDividendCutHistory() {
        return dividendCutHistory;
    }

    public void setDividendCutHistory(boolean dividendCutHistory) {
        this.dividendCutHistory = dividendCutHistory;
    }

    @JsonProperty("payoutRatio")
    public double getPayoutRatio() {
        return payoutRatio;
    }

    public void setPayoutRatio(double payoutRatio) {
        this.payoutRatio = payoutRatio;
    }

    @JsonProperty("dividendHistory")
    public List<DividendRecord> getDividendHistory() {
        return dividendHistory;
    }

    public void setDividendHistory(List<DividendRecord> dividendHistory) {
        this.dividendHistory = dividendHistory;
    }

    public static class DividendRecord {
        private int year;
        private double dividendPerShare;

        public DividendRecord() {
        }

        public DividendRecord(int year, double dividendPerShare) {
            this.year = year;
            this.dividendPerShare = dividendPerShare;
        }

        @JsonProperty("year")
        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        @JsonProperty("dividendPerShare")
        public double getDividendPerShare() {
            return dividendPerShare;
        }

        public void setDividendPerShare(double dividendPerShare) {
            this.dividendPerShare = dividendPerShare;
        }
    }
}