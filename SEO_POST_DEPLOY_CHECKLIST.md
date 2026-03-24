# SEO Post-Deploy Checklist

Last updated: 2026-03-24

## Purpose

Run these checks immediately after deploying the latest SEO hardening changes.

## 1. Redirect Chain

Expected:
- `https://dividend-goal.com/compare/SCHD-vs-JEPI` should end in a single permanent redirect to `https://dividend-goal.com/compare/JEPI-vs-SCHD`
- No intermediate `http://` hop should appear

Command:

```powershell
curl.exe -I -L -s -o NUL -w "final=%{url_effective} redirects=%{num_redirects}`n" https://dividend-goal.com/compare/SCHD-vs-JEPI
curl.exe -I https://dividend-goal.com/compare/SCHD-vs-JEPI
```

What to check:
- `final=https://dividend-goal.com/compare/JEPI-vs-SCHD`
- `redirects=1`
- first response should be `308` or the intended permanent redirect status

## 2. Canonical URL

Expected:
- canonical tag on the compare page should point to `https://dividend-goal.com/compare/JEPI-vs-SCHD`

Command:

```powershell
curl.exe -s https://dividend-goal.com/compare/JEPI-vs-SCHD | Select-String 'rel="canonical"'
```

## 3. Noindex Income Page

Expected:
- `https://dividend-goal.com/how-much-income/100000/LEG` should still output `noindex, follow`

Command:

```powershell
curl.exe -s https://dividend-goal.com/how-much-income/100000/LEG | Select-String 'name="robots"'
curl.exe -I https://dividend-goal.com/how-much-income/100000/LEG
```

What to check:
- HTML contains `noindex, follow`
- optional header check: `X-Robots-Tag: noindex, follow`

## 4. Sitemap Surface

Expected:
- root sitemap loads
- reduced sitemap set is still present

Commands:

```powershell
curl.exe -s https://dividend-goal.com/sitemap.xml
curl.exe -s https://dividend-goal.com/sitemap-main.xml
curl.exe -s https://dividend-goal.com/sitemap-lifestyle.xml
curl.exe -s https://dividend-goal.com/sitemap-comparison.xml
```

Quick count check:

```powershell
(curl.exe -s https://dividend-goal.com/sitemap-main.xml | Select-String '<loc>' -AllMatches).Matches.Count
(curl.exe -s https://dividend-goal.com/sitemap-lifestyle.xml | Select-String '<loc>' -AllMatches).Matches.Count
(curl.exe -s https://dividend-goal.com/sitemap-comparison.xml | Select-String '<loc>' -AllMatches).Matches.Count
```

Expected counts:
- main: `35`
- lifestyle: `36`
- comparison: `15`

## 5. Core Links

Expected:
- result page and articles page should point to core URLs only

Commands:

```powershell
curl.exe -s https://dividend-goal.com/how-much-dividend/1000-per-month/SCHD | Select-String '/how-much-dividend/1000-per-month/SCHD|/how-much-dividend/1000-per-month/VTI|/compare/JEPI-vs-SCHD|/articles/what-is-dividend-yield'
curl.exe -s https://dividend-goal.com/articles | Select-String '/how-much-dividend/1000-per-month/SCHD|/how-much-dividend/1000-per-month/VTI|/compare/JEPI-vs-SCHD'
```

## 6. Search Console Actions

Request inspection or reindex for:
- `https://dividend-goal.com/`
- `https://dividend-goal.com/articles/what-is-dividend-yield`
- `https://dividend-goal.com/articles/dividend-income-vs-interest`
- `https://dividend-goal.com/how-much-dividend/1000-per-month/SCHD`
- `https://dividend-goal.com/how-much-dividend/1000-per-month/VTI`
- `https://dividend-goal.com/compare/JEPI-vs-SCHD`

## 7. Review Dates

Re-check GSC performance on:
- `2026-03-30`
- `2026-04-06`

Decision rule:
- if core pages still show almost no repeated impressions by `2026-04-06`, pivot harder or prepare shutdown
