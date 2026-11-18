# AI Study Mentor
Android Kotlin MVP following ASM1: account registration/login, profile, logout, AI academic questions, private learning history and retry/error states.

## Setup
1. Register the Android application in Firebase using package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258. Put the downloaded configuration in app/google-services.json (ignored by Git).
2. Enable Authentication > Email/Password and create Cloud Firestore. Select your Firebase project with Firebase CLI: firebase use --add.
3. Install backend dependencies: cd functions, npm install. Copy .env.example to .env and configure an HTTPS OpenAI-compatible chat completions endpoint and model. The provider named “vilao” has not been identified; do not assume compatibility until its API documentation is confirmed.
4. Run firebase functions:secrets:set AI_API_KEY. Never place the AI key in Android source or Git.
5. Deploy: firebase deploy --only firestore:rules,functions. Cloud Functions deployment may require a billing-enabled Firebase project. Android uses the default us-central1 function region.
6. Sync Android Studio and run the app. Without the Firebase configuration the app displays setup instructions rather than pretending to authenticate.

## Verification checklist — not executed yet
- Register a valid account; reject invalid email, empty name and short password.
- Sign in, edit and save profile, sign out, restart and check session routing.
- Ask an academic question, inspect the answer and reopen history.
- Empty/oversized question is rejected; unavailable network/provider permits retry.
- User B cannot read user A history; unauthenticated reads and direct history writes fail.
- Check device rotation, loading, screen sizes and Android back navigation.

## Scope and limitations
The repository started with the default Android screen. This implementation supplies the MVP source; Firebase provisioning, AI credentials, device testing and peer review require real configuration and evidence. No test or survey results are fabricated. The current UI is a simple programmatic prototype; history displays at most 100 records and has no pagination. Add lifecycle-aware ViewModels and navigation state restoration before a production release.

## Verification performed
Backend JavaScript syntax parsed successfully; git diff --check passed. Android assembleDebug was attempted but did not finish within the 120-second execution limit during Gradle setup; APK and runtime behavior are not verified.
