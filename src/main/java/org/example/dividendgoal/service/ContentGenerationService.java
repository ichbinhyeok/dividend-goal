package org.example.dividendgoal.service;

import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ContentGenerationService {

    public GeneratedContent buildContent(Stock stock, double monthlyAmount, double requiredInvestment) {
        // [SEO] 티커별 고유 시드 사용 (페이지 새로고침 시 내용은 유지되나, 다른 티커와는 문장이 다름)
        long seed = stock.getTicker().hashCode();
        Random r = new Random(seed);

        String intro = generateIntro(stock, monthlyAmount, requiredInvestment, r);
        String whatIs = generateWhatIs(stock, r);
        String meaning = generateMeaning(monthlyAmount, r);
        String angle = generateAngle(stock, requiredInvestment, r);
        String disclaimer = getDisclaimer();

        return new GeneratedContent(intro, whatIs, meaning, angle, disclaimer);
    }

    // 기존 메서드 호환성 유지 (필요 시 동일하게 스피닝 적용 가능)
    public GeneratedContent buildIncomeContent(Stock stock, double capital, double monthlyIncome) {
        // 간단히 기존 로직을 재사용하거나 별도 스피닝 로직 구현
        return buildContent(stock, monthlyIncome, capital);
    }

    // --- [SEO] 단어 단위 스피닝 로직 (Spinning Logic) ---

    private String spin(Random r, String... options) {
        return options[r.nextInt(options.length)];
    }

    private String generateIntro(Stock stock, double monthlyAmount, double requiredInvestment, Random r) {
        String intent = spin(r,
                String.format("Planning for a reliable $%.2f monthly income stream?", monthlyAmount),
                String.format("Is your goal to generate $%.2f every month passively?", monthlyAmount),
                String.format("Dreaming of an extra $%.2f in monthly cash flow?", monthlyAmount),
                String.format("Looking to cover $%.2f in monthly bills with dividends?", monthlyAmount),
                String.format("Wondering how much capital you need for $%.2f/month?", monthlyAmount)
        );

        String solution = spin(r,
                String.format(" Using %s (%s), you'd need approximately $%,.2f invested today.", stock.getName(), stock.getTicker(), requiredInvestment),
                String.format(" With %s (%s), a capital allocation of roughly $%,.2f could get you there.", stock.getName(), stock.getTicker(), requiredInvestment),
                String.format(" By investing about $%,.2f in %s (%s), you could theoretically hit that mark.", requiredInvestment, stock.getName(), stock.getTicker()),
                String.format(" Based on the yield of %s (%s), your target portfolio size is around $%,.2f.", stock.getName(), stock.getTicker(), requiredInvestment)
        );

        String basis = spin(r,
                String.format(" This calculation utilizes the current trailing yield of %.2f%%.", stock.getYield()),
                String.format(" We base this on its recent trailing yield of %.2f%%.", stock.getYield()),
                String.format(" This figure assumes the yield holds steady at %.2f%%.", stock.getYield()),
                String.format(" Note that this math relies on a %.2f%% yield.", stock.getYield())
        );

        return intent + solution + basis;
    }

    private String generateWhatIs(Stock stock, Random r) {
        String opener = spin(r,
                String.format("%s (%s) is a key player in the %s sector.", stock.getName(), stock.getTicker(), stock.getSector()),
                String.format("Traded as %s, %s operates within the %s space.", stock.getTicker(), stock.getName(), stock.getSector()),
                String.format("Investors in the %s sector often watch %s (%s).", stock.getSector(), stock.getName(), stock.getTicker())
        );

        String detail;
        if ("Technology".equalsIgnoreCase(stock.getSector())) {
            detail = spin(r,
                    " It is often looked at for a blend of growth and income.",
                    " Tech dividends like this are prized for potential appreciation.",
                    " It combines modern tech growth with shareholder payouts."
            );
        } else if (stock.getRisk().equals("HIGH")) {
            detail = spin(r,
                    " However, its high yield suggests higher volatility.",
                    " Note that the market prices this with a risk premium.",
                    " Investors should be wary of its higher-than-average risk profile."
            );
        } else {
            detail = spin(r,
                    " It is generally viewed as a stable income generator.",
                    " Many portfolios hold it for consistency.",
                    " Stability is the key trait associated with this ticker."
            );
        }

        return opener + detail;
    }

    private String generateMeaning(double amount, Random r) {
        return spin(r,
                String.format("What does $%.2f really mean? It could cover a significant portion of your bills.", amount),
                String.format("Think about $%.2f in real terms: groceries, utilities, or subscriptions.", amount),
                String.format("Real-world impact: $%.2f/month is enough to offset major recurring expenses.", amount)
        );
    }

    private String generateAngle(Stock s, double inv, Random r) {
        return spin(r,
                String.format("From an investment angle, committing $%,.2f requires conviction in %s's future.", inv, s.getTicker()),
                String.format("Deploying $%,.2f is a serious step. Ensure %s aligns with your risk tolerance.", inv, s.getTicker()),
                String.format("Why %s? Because at $%,.2f capital, yield stability becomes your best friend.", s.getTicker(), inv)
        );
    }

    private String getDisclaimer() {
        return "Disclaimer: Informational only. Not financial advice. Yields vary. Past performance does not guarantee future results.";
    }
}