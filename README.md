# Bénin Boyz FC — Stats

Dashboard des stats Pro Clubs (EA Sports FC 26), hébergé en page statique.
Multi-clubs : vue par club + vue **Overall** (cumul, joueurs fusionnés par nom).

## Comment ça marche

- `index.html` — le dashboard. Au chargement il lit `data.json`.
  S'il est absent (ex. ouverture en `file://`), il retombe sur des données intégrées.
- `data.json` — les stats, structure multi-clubs `{updated, platform, clubs:[{name,
  clubId, players:[…]}]}`. **Généré**, ne pas éditer à la main.
- `scripts/fetch-stats.mjs` — récupère les stats depuis l'API (non officielle) d'EA
  et (ré)écrit `data.json`.
- `.github/workflows/update-stats.yml` — lance le script tous les jours (06:00 UTC)
  et commit `data.json` s'il a changé. Déclenchable aussi à la main (onglet **Actions**).

> L'API EA n'est ni officielle ni documentée : elle peut changer sans préavis, et
> bloque souvent les IP de datacenter (403). Si le workflow échoue, lance le script
> en local (IP résidentielle) — voir plus bas.

Les clubs suivis sont figés dans `scripts/fetch-stats.mjs` (constante `CLUBS`),
plateforme `common-gen5` (PS5/Xbox Series) :

| Club | clubId |
|---|---|
| Bénin Boyz FC | 276969 |
| Aupiais FC | 82414 |

Pour retrouver un clubId : `…/api/fc/allTimeLeaderboard/search?platform=common-gen5&clubName=NOM`
(l'ancien `clubs/search` renvoie 404 sur FC 26).

## Mettre à jour manuellement (filet de secours)

Voir [MISE-A-JOUR.md](MISE-A-JOUR.md) pour le détail (et le plan B via navigateur
si EA bloque aussi ton IP en local).

```bash
node scripts/fetch-stats.mjs                    # écrit data.json (les 2 clubs)
git add data.json && git commit -m "maj stats" && git push
```
