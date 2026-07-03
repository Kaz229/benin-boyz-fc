# Bénin Boyz FC — Stats

Dashboard des stats Pro Clubs du club (EA Sports FC 26), hébergé en page statique.

## Comment ça marche

- `benin-boyz-fc-dashboard-1.html` — le dashboard. Au chargement il lit `data.json`.
  S'il est absent (ex. ouverture en `file://`), il retombe sur des données intégrées.
- `data.json` — les stats des joueurs. **Généré**, ne pas éditer à la main.
- `scripts/fetch-stats.mjs` — récupère les stats depuis l'API (non officielle) d'EA
  et (ré)écrit `data.json`.
- `.github/workflows/update-stats.yml` — lance le script tous les jours (06:00 UTC)
  et commit `data.json` s'il a changé. Déclenchable aussi à la main (onglet **Actions**).

> L'API EA n'est ni officielle ni documentée : elle peut changer sans préavis, et
> bloque souvent les IP de datacenter (403). Si le workflow échoue, lance le script
> en local (IP résidentielle) — voir plus bas.

Le club est **Bénin Boyz FC**, `clubId=276969`, plateforme `common-gen5` (PS5/Xbox
Series). Ces valeurs sont figées dans le workflow et le script — rien à configurer.

Seule chose éventuelle : dans l'onglet **Actions** du repo, autoriser les workflows
si GitHub le demande.

> Pour retrouver un clubId : `…/api/fc/allTimeLeaderboard/search?platform=common-gen5&clubName=NOM`
> (l'ancien `clubs/search` renvoie 404 sur FC 26).

## Mettre à jour manuellement (filet de secours)

```bash
node scripts/fetch-stats.mjs                    # écrit data.json (clubId figé)
git add data.json && git commit -m "maj stats" && git push
```

Variables (facultatives, valeurs par défaut déjà bonnes) :
`PLATFORM` (`common-gen5`), `CLUB_ID` (`276969`), `CLUB_NAME`.
