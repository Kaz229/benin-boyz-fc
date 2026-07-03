#!/usr/bin/env node
/**
 * Récupère les stats Pro Clubs du club depuis l'API (non officielle) d'EA
 * et écrit un data.json à la racine, consommé par le dashboard HTML.
 *
 * L'API EA n'est pas documentée ni supportée : elle peut changer/disparaître.
 * Elle bloque souvent les IP de datacenter -> si un runner CI échoue,
 * lance ce script en local (IP résidentielle) : `node scripts/fetch-stats.mjs`
 *
 * Multi-clubs : la liste des clubs à récupérer est dans CLUBS ci-dessous.
 * PLATFORM (défaut common-gen5 = PS5/Xbox Series) est surchargeable par env.
 */

import { writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const PLATFORM = process.env.PLATFORM || "common-gen5";

// Clubs à agréger (ordre = ordre d'affichage dans le sélecteur du dashboard).
const CLUBS = [
  { clubId: "276969", name: "Bénin Boyz FC" },
  { clubId: "82414",  name: "Aupiais FC" },
];

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

async function fetchClub({ clubId, name }) {
  console.log(`Récupération ${name} (clubId=${clubId})…`);
  const stats = await eaFetch(`members/stats?platform=${PLATFORM}&clubId=${clubId}`);
  const members = stats.members || stats.Members || [];
  if (!members.length) throw new Error(`Réponse EA sans membres pour ${name}.`);
  const players = members
    .map(mapMember)
    .filter((p) => p.name && p.gp > 0) // écarte les membres qui n'ont jamais joué
    .sort((a, b) => a.name.localeCompare(b.name, "fr"));
  console.log(`  ${name} : ${players.length} joueurs`);
  return { name, clubId, players };
}

async function main() {
  const clubs = [];
  for (const c of CLUBS) clubs.push(await fetchClub(c)); // en série pour rester poli avec EA

  const out = {
    updated: new Date().toISOString(),
    platform: PLATFORM,
    clubs,
  };

  const root = join(dirname(fileURLToPath(import.meta.url)), "..");
  await writeFile(join(root, "data.json"), JSON.stringify(out, null, 2) + "\n");
  console.log(`✓ data.json écrit — ${clubs.length} clubs.`);
}

// N'exécute main() que lancé directement (pas à l'import pour les tests)
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main().catch((err) => {
    console.error("✗ Échec :", err.message);
    process.exit(1);
  });
}
