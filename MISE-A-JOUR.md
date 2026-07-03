# Mémo — mettre à jour les stats

Tout ce qu'il faut savoir pour rafraîchir les stats du dashboard, et pourquoi ça
marche comme ça.

## Infos du club (à retenir)

| Champ | Valeur |
|---|---|
| Club | **Bénin Boyz FC** |
| `clubId` | **276969** |
| Plateforme | **common-gen5** (PS5 / Xbox Series) |

> Retrouver un clubId : ouvrir dans le navigateur
> `https://proclubs.ea.com/api/fc/allTimeLeaderboard/search?platform=common-gen5&clubName=NOM`
> (l'ancien `clubs/search` renvoie 404 sur FC 26).

## Comment marche le site

- `benin-boyz-fc-dashboard-1.html` lit `data.json` au chargement. Si `data.json`
  manque, il retombe sur des données intégrées (fallback). Le rendu ne plante pas
  si un champ manque (affiche `—`).
- `data.json` = les stats des joueurs. **Fichier généré, ne pas éditer à la main.**
- `scripts/fetch-stats.mjs` = récupère les stats depuis l'API EA et réécrit `data.json`.
- `.github/workflows/update-stats.yml` = tâche planifiée (échoue, voir ci-dessous).

## ⚠️ Pourquoi l'automatisation cloud ne marche pas

L'API Pro Clubs d'EA n'est ni officielle ni documentée, et surtout elle **bloque les
IP de datacenter** (protection Akamai → `403 Access Denied`). Les runners GitHub
Actions sont des IP de datacenter → le workflow **échoue systématiquement**
(mail d'échec quotidien qu'on peut ignorer).

**Ta machine (IP résidentielle) passe**, elle. Donc la mise à jour se fait depuis
chez toi, à la main, quand tu veux.

## Mettre à jour — Méthode 1 (à tenter en premier)

Depuis ton ordi, IP résidentielle :

```bash
cd benin-boyz-fc
node scripts/fetch-stats.mjs                       # régénère data.json
git add data.json && git commit -m "maj stats" && git push
```

Puis **Cmd + Shift + R** sur le site pour vider le cache du navigateur.

## Mettre à jour — Méthode 2 (plan B, marche à coup sûr)

Si la méthode 1 renvoie quand même `403` (EA analyse aussi la signature TLS, pas
seulement l'IP), on passe par le navigateur qui, lui, est toujours accepté :

1. Ouvrir dans le navigateur :
   ```
   https://proclubs.ea.com/api/fc/members/stats?platform=common-gen5&clubId=276969
   ```
2. Enregistrer la réponse JSON dans un fichier `members.json` (à la racine du repo).
3. La convertir en `data.json` :
   ```bash
   node --input-type=module -e '
   import {mapMember} from "./scripts/fetch-stats.mjs";
   import {readFile,writeFile} from "node:fs/promises";
   const raw=JSON.parse(await readFile("members.json","utf8"));
   const players=raw.members.map(mapMember).filter(p=>p.name&&p.gp>0).sort((a,b)=>a.name.localeCompare(b.name,"fr"));
   await writeFile("data.json",JSON.stringify({club:"Bénin Boyz FC",clubId:"276969",platform:"common-gen5",updated:new Date().toISOString(),players},null,2)+"\n");
   console.log("✓ data.json à jour");'
   ```
4. Pousser :
   ```bash
   git add data.json && git commit -m "maj stats" && git push
   ```

## Endpoints EA utiles (FC 26)

Base : `https://proclubs.ea.com/api/fc`

| But | Chemin |
|---|---|
| Chercher un club | `allTimeLeaderboard/search?platform=common-gen5&clubName=NOM` |
| Infos club | `clubs/info?platform=common-gen5&clubIds=276969` |
| **Stats par joueur** | `members/stats?platform=common-gen5&clubId=276969` |
| Matchs du club | `clubs/matches?platform=common-gen5&clubIds=276969&matchType=leagueMatch` |

## Champs récupérés par joueur

`name, pos, ovr, gp, goals, assists, passes, passpct, tackles, tklpct, winpct,`
`rating` (note moy. /10), `shotpct` (% tirs), `motm` (homme du match),
`reds` (cartons rouges), `form` (buts des 10 derniers matchs).

Données bonus dispo dans la réponse EA mais pas encore affichées :
`cleanSheets`, buts encaissés, etc.
