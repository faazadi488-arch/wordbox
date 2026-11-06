WORDBOX - real Android app (uses the phone's own voice, file picker and backup sharing)

Option A - Android Studio (free, on a computer)
  1. File > Open > choose this folder, wait for Gradle sync.
  2. Build > Build APK(s). Click "locate" to find app-debug.apk. Send it to your phone.

Option B - GitHub (no Android Studio)
  1. Make a free GitHub account and a new repository.
  2. On a computer, upload everything in this folder (including the hidden .github folder).
  3. Open the Actions tab > Build APK > Run workflow. When done, download the Wordbox-apk artifact.

Your words: to bundle them, replace app/src/main/assets/wordbox-data.js with the file from the app's "Make app data file" button, then rebuild.
The app is always signed with the same key, so new versions install over old ones and keep the words.
