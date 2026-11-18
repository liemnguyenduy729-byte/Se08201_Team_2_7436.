# AI Study Mentor

Android Kotlin application connected directly to ViLao.

Run the app, open AI Mentor, enter your personal API key in the masked field, and submit an academic question. Uses occ/claude-opus-4-8 at https://api.vilao.ai/v1/chat/completions. The key stays in activity memory only and is never saved to disk or embedded in source.

Includes loading, input validation, network timeout and provider error messages. No account or cloud setup is required. Build with gradlew.bat :app:assembleDebug.
