# FitTrack

Application Android pour **suivre ses séances de sport** et **tracker ses calories**.
100 % locale : aucune donnée ne quitte le téléphone, pas de compte, pas de réseau.

## Fonctionnalités

- **Accueil** : bilan du jour (calories ingérées, brûlées, solde net), progression vers l'objectif, historique des 7 derniers jours.
- **Séances** : ajout d'une séance (11 sports au choix, durée, note). Les calories brûlées sont estimées automatiquement.
- **Repas** : ajout d'un aliment ou d'un plat avec ses calories, classé par moment de la journée.
- **Profil** : poids et objectif calorique journalier, utilisés pour les calculs.

### Estimation des calories brûlées

Formule MET (Compendium of Physical Activities), la même que celle des trackers grand public :

```
kcal = MET × 3,5 × poids(kg) ÷ 200 × durée(min)
```

Chaque sport a son MET (course 9,8 · football 7,0 · natation 8,3 · yoga 2,5…).
C'est une estimation, pas une mesure médicale.

## Stack technique

| | |
|---|---|
| Langage | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 (thème dynamique Android 12+) |
| Navigation | Navigation Compose (4 onglets) |
| Base de données | Room (SQLite) |
| Préférences | DataStore |
| Build | Gradle 8.11 + AGP 8.7, `minSdk 26`, `targetSdk 35` |

Architecture : `data` (entités Room, DAO, repository, préférences) → `ui` (ViewModel exposant des `StateFlow`) → écrans Compose sans état.

## Démarrer

Prérequis : [Android Studio](https://developer.android.com/studio) (version récente) et un JDK 17.

```bash
git clone <url-du-repo>
cd fittrack-android
./gradlew assembleDebug          # génère app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # lance les tests unitaires
```

Dans Android Studio : `File > Open`, sélectionner le dossier, puis **Run** sur un émulateur ou un téléphone en mode développeur.

L'APK de debug est aussi produit par la CI GitHub Actions à chaque push (onglet *Actions* → artefact `fittrack-debug-apk`).

## Structure

```
app/src/main/java/com/kaz229/fittrack/
├── MainActivity.kt
├── data/
│   ├── Model.kt              # entités Workout / Meal + bilan journalier
│   ├── Activities.kt         # catalogue des sports (MET) et calcul des calories
│   ├── Daos.kt               # requêtes Room
│   ├── FitTrackDatabase.kt
│   ├── UserPreferences.kt    # poids + objectif (DataStore)
│   └── FitTrackRepository.kt
└── ui/
    ├── FitTrackApp.kt        # navigation, barre d'onglets, bouton +
    ├── FitTrackViewModel.kt
    ├── theme/Theme.kt
    └── screens/              # Home, Workouts, Meals, Profile, dialogues d'ajout
```

## Idées pour la suite

- Base d'aliments avec recherche et calories pré-remplies (Open Food Facts).
- Choix de la date lors de l'ajout (aujourd'hui uniquement pour l'instant).
- Graphique de tendance du poids et export CSV.
- Intégration Health Connect pour récupérer les pas et les séances d'autres apps.
