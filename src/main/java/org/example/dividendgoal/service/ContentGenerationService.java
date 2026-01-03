package org.example.dividendgoal.service;



import org.example.dividendgoal.model.GeneratedContent;
import org.example.dividendgoal.model.Stock;
import org.springframework.stereotype.Service;

@Service
public class ContentGenerationService {

    public GeneratedContent buildContent(Stock stock, double monthlyAmount, double requiredInvestment) {
        String introduction = String.format("""
                Planning for reliable monthly income starts with a clear target. To illustrate approximately $%.2f per month from %s (%s), you would need to earmark about $%,.2f based on its current trailing yield. This walkthrough keeps everything straightforward so dividend beginners and seasoned savers can understand the math without jargon or logins. The goal is to outline how an income-first approach can translate a dollar goal into a realistic capital requirement while staying informational.
                """, monthlyAmount, stock.getName(), stock.getTicker(), requiredInvestment);

        String whatIsTicker = String.format("""
                What is %s? %s is traded under the ticker %s within the %s space and is known for its consistent distributions. Readers often watch its payout cadence and yield stability because that helps plan predictable cash flow. Knowing the story behind the ticker provides context to the numbers: why the company exists, where it earns money, and how that supports a dividend that observers hope will remain competitive over time.
                """, stock.getTicker(), stock.getName(), stock.getTicker(), stock.getSector());

        String realLifeMeaning = String.format("""
                What does $%.2f per month really cover? That cash flow could offset a utility bill, a mobile family plan, or a week of groceries in many U.S. cities. Framing dividends as everyday line items keeps the discussion grounded in practical outcomes instead of abstract percentages. If you are aiming for a bigger lifestyle change, you can scale the target up or down using the same calculation to see how more or less saving would affect the path.
                """, monthlyAmount);

        String investingAngle = String.format("""
                Why does the illustrated $%,.2f matter? Because dividend math is most useful when expectations match the numbers. A %.2f%% yield means the market is pricing the company so that each dollar set aside is expected to produce roughly that annual return before taxes. Comparing %s to other dividend ETFs or blue-chip payers in the same sector provides context to decide if diversification or concentration aligns with your comfort level. Reinvested dividends, disciplined contributions, and periodic reviews of payout health can help understand how resilient a plan might feel.
                """, requiredInvestment, stock.getYield(), stock.getTicker());

        String disclaimer = """
                Disclaimer: This page is informational only and does not provide financial, tax, or legal advice. Dividend yields change, share prices move daily, and your personal situation may require a different approach. Consider speaking with a fiduciary advisor before acting, and remember that all investing involves risk, including possible loss of principal. Always validate numbers against current data and your own comfort with volatility.
                """;

        String additionalContext = """
                Additional context: The calculator uses static data packaged with the application and does not store your inputs. Because it is server-rendered and stateless, every page request starts fresh, and no personal information is kept. Use the numbers here as a starting point for conversations with professionals who know your circumstances, and revisit the math periodically if yields or goals change.
                """;

        return new GeneratedContent(introduction.trim(), whatIsTicker.trim(), realLifeMeaning.trim(), investingAngle.trim(), (disclaimer + " " + additionalContext).trim());
    }

    public GeneratedContent buildIncomeContent(Stock stock, double capital, double monthlyIncome) {
        String introduction = String.format("""
                Curious what $%,.2f in %s (%s) could illustrate in monthly dividends? Using the trailing yield only, that capital level could map to roughly $%.2f per month before taxes and fees. This page keeps the framing educational, leaning on static numbers to help readers see how yield percentages translate into cash flow without introducing accounts or external data calls.
                """, capital, stock.getName(), stock.getTicker(), monthlyIncome);

        String whatIsTicker = String.format("""
                Understanding %s matters when pairing capital with expectations. %s is traded under %s in the %s space and is widely followed for its payout cadence and stability history. Knowing the sector, reputation, and distribution style provides context for why the yield looks the way it does today.
                """, stock.getTicker(), stock.getName(), stock.getTicker(), stock.getSector());

        String realLifeMeaning = String.format("""
                Translating the illustration into daily life: about $%.2f per month could offset recurring expenses such as subscriptions, utilities, or transportation. Thinking in terms of budgets—rather than abstract percentages—keeps the discussion grounded and practical for goal-setters.
                """, monthlyIncome);

        String investingAngle = String.format("""
                A yield near %.2f%% shapes how long capital may need to stay invested to sustain this illustration. Because yields move and dividends can be adjusted, revisiting the math periodically is prudent. Comparing %s to other dividend-focused ETFs or long-tenured payers can help readers frame diversification ideas without prescribing action.
                """, stock.getYield(), stock.getTicker());

        String disclaimer = """
                Disclaimer: This illustration is informational and not investment, tax, or legal advice. Yields change, share prices fluctuate, and distributions can be revised. Always validate the numbers with current data and consider talking to a fiduciary who understands your situation. Nothing here recommends buying or selling any security.
                """;

        String additionalContext = """
                Additional context: The calculator remains stateless and server-rendered. No personal inputs are saved, and the data lives only in static JSON bundled with the application. If you adjust the capital or ticker, the page refreshes with new illustrative math to support learning, not recommendations.
                """;

        return new GeneratedContent(introduction.trim(), whatIsTicker.trim(), realLifeMeaning.trim(), investingAngle.trim(), (disclaimer + " " + additionalContext).trim());
    }
}
