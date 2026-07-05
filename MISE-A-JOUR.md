# Mémo — mettre à jour les stats

Tout ce qu'il faut savoir pour rafraîchir les stats du dashboard, et pourquoi ça
marche comme ça.

## Infos des clubs (à retenir)

| Club | `clubId` | Plateforme |
|---|---|---|
| **Bénin Boyz FC** | **276969** | common-gen5 (PS5 / Xbox Series) |
| **Aupiais FC** | **82414** | common-gen5 |

Le dashboard propose une vue **par club** + une vue **Overall** (cumul des deux ;
un joueur présent dans les 2 clubs est fusionné : totaux additionnés, taux au prorata
des matchs). La liste des clubs est dans `scripts/fetch-stats.mjs` (constante `CLUBS`)
— pour en ajouter/retirer un, éditer ce tableau.

**Alias de pseudos** : si un même joueur a deux pseudos EA (ex. `Kaz229` = `Kaz_229_`),
c'est géré dans `index.html` par la constante `ALIASES` (`{"Kaz229":"Kaz_229_"}`).
Les stats sont alors fusionnées partout (dans un club et en Overall). Ajouter une
entrée `"autrePseudo":"pseudoCanonique"` pour un nouveau cas.

> Retrouver un clubId : ouvrir dans le navigateur
> `https://proclubs.ea.com/api/fc/allTimeLeaderboard/search?platform=common-gen5&clubName=NOM`
> (l'ancien `clubs/search` renvoie 404 sur FC 26).

## Comment marche le site

- `index.html` lit `data.json` au chargement. Si `data.json`
  manque, il retombe sur des données intégrées (fallback). Le rendu ne plante pas
  si un champ manque (affiche `—`).
- `data.json` = les stats, structure multi-clubs `{ updated, platform, clubs:[{name,
  clubId, players:[…]}] }`. **Fichier généré, ne pas éditer à la main.**
- `scripts/fetch-stats.mjs` = boucle sur les clubs de `CLUBS`, récupère les stats
  depuis l'API EA et réécrit `data.json`.
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

1. Pour **chaque club**, ouvrir dans le navigateur et enregistrer la réponse :
   ```
   https://proclubs.ea.com/api/fc/members/stats?platform=common-gen5&clubId=276969   -> benin.json
   https://proclubs.ea.com/api/fc/members/stats?platform=common-gen5&clubId=82414    -> aupiais.json
   ```
2. Convertir les deux en un `data.json` multi-clubs :
   ```bash
   node --input-type=module -e '
   import {mapMember} from "./scripts/fetch-stats.mjs";
   import {readFile,writeFile} from "node:fs/promises";
   const CFG=[["Bénin Boyz FC","276969","benin.json"],["Aupiais FC","82414","aupiais.json"]];
   const clubs=[];
   for(const [name,clubId,file] of CFG){
     const raw=JSON.parse(await readFile(file,"utf8"));
     const players=raw.members.map(mapMember).filter(p=>p.name&&p.gp>0).sort((a,b)=>a.name.localeCompare(b.name,"fr"));
     clubs.push({name,clubId,players});
   }
   await writeFile("data.json",JSON.stringify({updated:new Date().toISOString(),platform:"common-gen5",clubs},null,2)+"\n");
   console.log("✓ data.json à jour");'
   ```
3. Pousser :
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
