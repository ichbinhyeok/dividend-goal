# SEO Recovery Tracker

Last updated: 2026-03-09

## Goal

Track whether the SEO cleanup improves indexing quality and search traffic enough to justify keeping `dividend-goal.com` alive.

## Current Decision Window

- Observation start date: 2026-03-09
- First review: 2026-03-23
- Main review: 2026-04-06
- Final keep/kill review: 2026-05-04

## Baseline Before Deploy

Source: Google Search Console checks performed on 2026-03-09.

- Property currently accessible: `https://www.dividend-goal.com/`
- Live canonical host: `https://dividend-goal.com`
- Last 28 days (`2026-02-09` to `2026-03-08`): `0 clicks / 46 impressions`
- Previous 28 days (`2026-01-12` to `2026-02-08`): `2 clicks / 304 impressions`
- Last non-zero click date: `2026-02-08`
- Last non-zero impression date: `2026-02-24`
- Last 180 days: `3 clicks / 854 impressions`

## Structural Baseline Before Deploy

- Old sitemap total: `8113 URLs`
- Old sitemap main: `898 URLs`
- Old sitemap lifestyle: `1110 URLs`
- Old sitemap comparison: `6105 URLs`

## Expected Structure After Deploy

- New sitemap total: `86 URLs`
- New sitemap main: `35 URLs`
- New sitemap lifestyle: `36 URLs`
- New sitemap comparison: `15 URLs`
- `how-much-income/*`: `noindex`
- Only curated target/lifestyle/comparison pages should remain indexable

## Deployment Checklist

- [ ] Deploy commit `3b1418d` (`Fix SEO indexing policy and trim sitemap surface`)
- [ ] Deploy tracker document commit
- [ ] Confirm `https://dividend-goal.com/sitemap.xml` returns reduced sitemap sets
- [ ] Confirm `https://dividend-goal.com/how-much-income/100000/LEG` returns `<meta name="robots" content="noindex, follow">`
- [ ] Confirm non-canonical comparison URL redirects to canonical order
- [ ] Confirm `https://dividend-goal.com/compare/SCHD-vs-JEPI` includes canonical tag

## Search Console Actions

- [ ] Add or verify apex property: `https://dividend-goal.com/`
- [ ] Submit sitemap: `https://dividend-goal.com/sitemap.xml`
- [ ] Request reindex for homepage
- [ ] Request reindex for these core URLs:
- [ ] `https://dividend-goal.com/`
- [ ] `https://dividend-goal.com/articles/what-is-dividend-yield`
- [ ] `https://dividend-goal.com/articles/dividend-income-vs-interest`
- [ ] `https://dividend-goal.com/how-much-dividend/1000-per-month/SCHD`
- [ ] `https://dividend-goal.com/how-much-dividend/1000-per-month/VTI`
- [ ] `https://dividend-goal.com/compare/SCHD-vs-JEPI`

## Weekly Tracking

Fill this once per week after deploy.

| Check Date | Last 7d Clicks | Last 7d Impressions | Last 28d Clicks | Last 28d Impressions | Indexed Core URLs | Notes |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| 2026-03-09 | - | - | 0 | 46 | TBD | Baseline before deploy |
| 2026-03-16 |  |  |  |  |  |  |
| 2026-03-23 |  |  |  |  |  |  |
| 2026-03-30 |  |  |  |  |  |  |
| 2026-04-06 |  |  |  |  |  | Main review |
| 2026-04-13 |  |  |  |  |  |  |
| 2026-04-20 |  |  |  |  |  |  |
| 2026-04-27 |  |  |  |  |  |  |
| 2026-05-04 |  |  |  |  |  | Final keep/kill review |

## URL Inspection Tracker

Use this table for the same small set of URLs every time.

| URL | Status Before | Status After Deploy | Last Crawl | Notes |
| --- | --- | --- | --- | --- |
| `/` | Page with redirect on `www` property |  |  |  |
| `/articles/what-is-dividend-yield` | Page with redirect on `www` property |  |  |  |
| `/how-much-dividend/1000-per-month/VTI` | Submitted and indexed |  |  |  |
| `/compare/SCHD-vs-JEPI` | URL unknown to Google |  |  |  |
| `/how-much-income/100000/LEG` | Page with redirect on `www` property |  |  | Should become noindex on apex |

## Success Criteria

- By 2026-04-06:
- Core pages are recrawled
- Reduced sitemap is accepted
- At least 1 to 3 core URLs show stable impressions
- Last 28 days impressions recover above `100`

- By 2026-05-04:
- Last 28 days clicks are above `3`
- Last 28 days impressions are above `250`
- At least 2 core pages show recurring impressions and ranking movement

## Kill Criteria

Kill the project if all of the following are still true by 2026-05-04:

- Last 28 days clicks remain `0`
- Last 28 days impressions remain under `100`
- Core pages are indexed but do not gain meaningful impressions
- No single content cluster shows traction

## Notes

- Do not expand sitemap size during this observation window.
- Do not add new programmatic page families during this observation window.
- If any change is made to SEO logic, record the date and exact change below.

## Change Log

| Date | Change | Reason |
| --- | --- | --- |
| 2026-03-09 | Reduced indexable surface and aligned sitemap with robots policy | Remove low-value crawl/index noise |
