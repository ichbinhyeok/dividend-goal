# SEO Recovery Tracker

Last updated: 2026-04-07

## Goal

Track whether the SEO cleanup improves indexing quality and search traffic enough to justify keeping `dividend-goal.com` alive.

## Current Decision Window

- Observation start date: 2026-03-09
- First review: 2026-03-23
- Main review: 2026-04-06
- Final keep/kill review: 2026-05-04

## Baseline Before Deploy

Source of truth: `sc-domain:dividend-goal.com` Search Console audit performed on `2026-03-24`.

- Live canonical host: `https://dividend-goal.com`
- Old `https://www.dividend-goal.com/` prefix-property baseline should not be compared directly to current domain-property data
- Last 28 days (`2026-02-09` to `2026-03-08`): `6 clicks / 402 impressions`
- Previous 28 days (`2026-01-12` to `2026-02-08`): `9 clicks / 651 impressions`
- Last non-zero click date inside the current baseline window: `2026-03-08`
- Last 180 days: legacy clicks came mostly from wide comparison-page surface, not the current curated core

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
- Current hardening commit pushed to `origin/main`: `60ff65f` (`Harden canonical SEO surface`)

## Deployment Checklist

- [x] Deploy commit `60ff65f` (`Harden canonical SEO surface`)
- [ ] Deploy tracker document commit
- [x] Run [SEO_POST_DEPLOY_CHECKLIST.md](./SEO_POST_DEPLOY_CHECKLIST.md) after deploy
- [x] Confirm `https://dividend-goal.com/sitemap.xml` returns reduced sitemap sets
- [x] Confirm `https://dividend-goal.com/how-much-income/100000/LEG` returns `<meta name="robots" content="noindex, follow">`
- [x] Confirm non-canonical comparison URL redirects to canonical order
- [x] Confirm `https://dividend-goal.com/compare/JEPI-vs-SCHD` includes canonical tag and the wrong-order URL returns a single permanent redirect

## Search Console Actions

- [x] Verify domain property: `sc-domain:dividend-goal.com`
- [x] Submit sitemap: `https://dividend-goal.com/sitemap.xml`
- [ ] Request reindex for homepage
- [ ] Request reindex for these core URLs:
- [ ] `https://dividend-goal.com/`
- [ ] `https://dividend-goal.com/articles/what-is-dividend-yield`
- [ ] `https://dividend-goal.com/articles/dividend-income-vs-interest`
- [ ] `https://dividend-goal.com/how-much-dividend/1000-per-month/SCHD`
- [ ] `https://dividend-goal.com/how-much-dividend/1000-per-month/VTI`
- [ ] `https://dividend-goal.com/compare/JEPI-vs-SCHD`

## Weekly Tracking

Fill this once per week after deploy.

| Check Date | Last 7d Clicks | Last 7d Impressions | Last 28d Clicks | Last 28d Impressions | Indexed Core URLs | Notes |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| 2026-03-09 | - | - | 6 | 402 | TBD | Domain-property comparable baseline |
| 2026-03-16 |  |  |  |  |  |  |
| 2026-03-23 | 0 | 7 | 0 | 123 | 6/6 | Final data available through 2026-03-22. Canonical compare URL is `/compare/JEPI-vs-SCHD`. `LEG` noindex page has not been recrawled yet. |
| 2026-03-30 | 0 | 52 | 0 | 75 | 6/6 | Final data currently available through 2026-03-29. Live redirect is now a single 308 and core-link concentration is deployed, but most impressions still come from legacy compare URLs. Core pages only showed clear impressions on `/` and `/how-much-dividend/1000-per-month/SCHD`. |
| 2026-04-06 | 0 | 47 | 0 | 106 | 6/6 | Final data currently available through 2026-04-04. Core URLs are still indexed and recrawled, but recent impressions remain concentrated in legacy compare URLs and not the intended core cluster. Treat the current recovery hypothesis as failed. |
| 2026-04-13 |  |  |  |  |  |  |
| 2026-04-20 |  |  |  |  |  |  |
| 2026-04-27 |  |  |  |  |  |  |
| 2026-05-04 |  |  |  |  |  | Final keep/kill review |

## URL Inspection Tracker

Use this table for the same small set of URLs every time.

| URL | Status Before | Status After Deploy | Last Crawl | Notes |
| --- | --- | --- | --- | --- |
| `/` | Page with redirect on `www` property | Submitted and indexed | 2026-04-06 | Apex property is now the source of truth and was recrawled during the main review window |
| `/articles/what-is-dividend-yield` | Page with redirect on `www` property | Submitted and indexed | 2026-03-17 | Core article is still being crawled |
| `/how-much-dividend/1000-per-month/VTI` | Submitted and indexed | Submitted and indexed | 2026-04-06 | Core ETF calculator page; recrawled again during the main review window |
| `/compare/JEPI-vs-SCHD` | Not tracked correctly in old prefix-property workflow | Submitted and indexed | 2026-02-02 | FAQ rich result detected |
| `/compare/SCHD-vs-JEPI` | URL unknown to Google | 308 permanent redirect to canonical | n/a | Live single-hop redirect verified on 2026-04-01 |
| `/how-much-income/100000/LEG` | Page with redirect on `www` property | Crawled - currently not indexed | 2026-02-27 | Live page still returns HTML robots meta and `X-Robots-Tag: noindex, follow`, but Google has not recrawled it yet |

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
- Use [SEO_POST_DEPLOY_CHECKLIST.md](./SEO_POST_DEPLOY_CHECKLIST.md) as the deploy-day validation runbook.
- If any change is made to SEO logic, record the date and exact change below.

## Change Log

| Date | Change | Reason |
| --- | --- | --- |
| 2026-03-09 | Reduced indexable surface and aligned sitemap with robots policy | Remove low-value crawl/index noise |
| 2026-03-24 | Corrected tracker to domain-property baseline, fixed canonical compare target to `/compare/JEPI-vs-SCHD`, and queued redirect/canonical hardening | Align monitoring with the actual canonical SEO surface |
| 2026-03-24 | Updated deploy target to commit `60ff65f` and linked the post-deploy verification runbook | Keep the tracker aligned with the current rollout plan |
| 2026-04-01 | Completed the post-deploy live checks and filled the 2026-03-30 weekly review row | Reflect the first post-deploy monitoring checkpoint |
| 2026-04-07 | Filled the 2026-04-06 main review row using final data through 2026-04-04 and confirmed core URLs are still indexed | Record that the technical cleanup held, but the current SEO recovery thesis did not |
