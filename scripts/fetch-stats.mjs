#!/usr/bin/env node
/**
 * Récupère les stats Pro Clubs du club depuis l'API (non officielle) d'EA
 * et écrit un data.json à la racine, consommé par le dashboard HTML.
 *
 * L'API EA n'est pas documentée ni supportée : elle peut changer/disparaître.
 * Elle bloque souvent les IP de datacenter -> si un runner CI échoue,
 * lance ce script en local (IP résidentielle) : `node scripts/fetch-stats.mjs`
 *
 * Config par variables d'environnement :
 *   PLATFORM   plateforme EA (défaut: common-gen5 = PS5/Xbox Series)
 *   CLUB_ID    id numérique du club (recommandé). Sinon recherche par nom.
 *   CLUB_NAME  nom du club pour la recherche (défaut: "Bénin Boyz FC")
 */

import { writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const PLATFORM  = process.env.PLATFORM  || "common-gen5";
const CLUB_ID   = process.env.CLUB_ID   || "276969"; // Bénin Boyz FC (common-gen5)
const CLUB_NAME = process.env.CLUB_NAME || "Bénin Boyz FC";

const BASE = "https://proclubs.ea.com/api/fc";
// En-têtes "navigateur" indispensables : sans eux EA renvoie 403 / ferme la socket.
const HEADERS = {
  "User-Agent":
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
    "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
  "Accept": "application/json",
  "Accept-Language": "fr-FR,fr;q=0.9,en;q=0.8",
  "Referer": "https://proclubs.ea.com/",
  "Origin": "https://proclubs.ea.com",
};

async function eaFetch(path) {
  const url = `${BASE}/${path}`;
  const res = await fetch(url, { headers: HEADERS });
  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`EA ${res.status} sur ${url}\n${body.slice(0, 300)}`);
  }
  return res.json();
}

async function resolveClubId() {
  if (CLUB_ID) return CLUB_ID;
  console.log(`Recherche du club "${CLUB_NAME}" (${PLATFORM})…`);
  // FC 26 : la recherche de club passe par allTimeLeaderboard/search
  // (l'ancien clubs/search renvoie désormais 404).
  const results = await eaFetch(
    `allTimeLeaderboard/search?platform=${PLATFORM}&clubName=${encodeURIComponent(CLUB_NAME)}`
  );
  const list = Array.isArray(results) ? results : Object.values(results || {});
  if (!list.length) throw new Error(`Aucun club trouvé pour "${CLUB_NAME}".`);
  const club = list[0];
  const id = club.clubId || club.clubInfo?.clubId;
  console.log(`Club trouvé : ${club.clubInfo?.name || CLUB_NAME} -> clubId=${id}`);
  return String(id);
}

const POS_FR = {
  goalkeeper: "Gardien",
  defender:   "Défenseur",
  midfielder: "Milieu",
  forward:    "Attaquant",
};

const num = (v) => {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
};
const pct = (v) => Math.round(num(v));

export function mapMember(m) {
  return {
    name:     m.name,
    pos:      POS_FR[m.favoritePosition] || "Milieu",
    ovr:      num(m.proOverall),
    gp:       num(m.gamesPlayed),
    goals:    num(m.goals),
    assists:  num(m.assists),
    passes:   num(m.passesMade),
    passpct:  pct(m.passSuccessRate),
    tackles:  num(m.tacklesMade),
    tklpct:   pct(m.tackleSuccessRate),
    winpct:   pct(m.winRate),
    rating:   Math.round(num(m.ratingAve) * 10) / 10, // note moyenne /10
    shotpct:  pct(m.shotSuccessRate),                  // % de tirs réussis
    motm:     num(m.manOfTheMatch),                    // hommes du match
    reds:     num(m.redCards),                         // cartons rouges
    form:     Array.from({ length: 10 }, (_, i) => num(m["prevGoals" + (i + 1)])), // buts, 10 derniers matchs
  };
}

async function main() {
  const clubId = await resolveClubId();
  console.log(`Récupération des stats des membres (clubId=${clubId})…`);
  const stats = await eaFetch(`members/stats?platform=${PLATFORM}&clubId=${clubId}`);
  const members = stats.members || stats.Members || [];
  if (!members.length) throw new Error("Réponse EA sans membres.");

  const players = members
    .map(mapMember)
    .filter((p) => p.name && p.gp > 0) // écarte les membres qui n'ont jamais joué
    .sort((a, b) => a.name.localeCompare(b.name, "fr"));

  const out = {
    club: CLUB_NAME,
    clubId,
    platform: PLATFORM,
    updated: new Date().toISOString(),
    players,
  };

  const root = join(dirname(fileURLToPath(import.meta.url)), "..");
  await writeFile(join(root, "data.json"), JSON.stringify(out, null, 2) + "\n");
  console.log(`✓ data.json écrit — ${players.length} joueurs.`);
}

// N'exécute main() que lancé directement (pas à l'import pour les tests)
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main().catch((err) => {
    console.error("✗ Échec :", err.message);
    process.exit(1);
  });
}
