package org.example.dividendgoal.model;

import java.util.List;

public class GeneratedContent {
    private String introduction;
    private String whatIsTicker;
    private String realLifeMeaning;
    private String investingAngle;
    private String disclaimer;

    public GeneratedContent(String introduction, String whatIsTicker, String realLifeMeaning, String investingAngle, String disclaimer) {
        this.introduction = introduction;
        this.whatIsTicker = whatIsTicker;
        this.realLifeMeaning = realLifeMeaning;
        this.investingAngle = investingAngle;
        this.disclaimer = disclaimer;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getWhatIsTicker() {
        return whatIsTicker;
    }

    public String getRealLifeMeaning() {
        return realLifeMeaning;
    }

    public String getInvestingAngle() {
        return investingAngle;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public List<String> asParagraphs() {
        return List.of(introduction, whatIsTicker, realLifeMeaning, investingAngle, disclaimer);
    }
}