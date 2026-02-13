# Remote Access Server - Android APK

Application Android native pour accès à distance complet à votre téléphone.

## 🚀 Fonctionnalités

- ✅ Capture caméra (avant/arrière)
- ✅ Enregistrement audio
- ✅ Géolocalisation GPS
- ✅ Informations système
- ✅ Exécution commandes shell
- ✅ Service arrière-plan stable
- ✅ Démarrage automatique au boot
- ✅ Wake lock (anti-veille)
- ✅ Interface graphique simple

## 📦 Compilation

### Méthode Recommandée: GitHub Codespaces

1. Forkez ce repo
2. Ouvrez dans Codespaces
3. Exécutez:
```bash
chmod +x gradlew
./gradlew assembleDebug
```
4. Téléchargez `app/build/outputs/apk/debug/app-debug.apk`

### Méthode Locale: Android Studio

1. Installez Android Studio
2. Ouvrez le projet
3. Build → Build APK
4. Récupérez l'APK dans `app/build/outputs/apk/debug/`

## 📱 Installation

```bash
adb install app-debug.apk
```

Ou transférez l'APK sur Android et installez manuellement.

## 🔧 Utilisation

1. Ouvrez l'app
2. Accordez toutes les permissions
3. Appuyez sur "DÉMARRER LE SERVEUR"
4. Notez l'IP affichée
5. Connectez-vous depuis le client Windows avec cette IP:4444

## ⚠️ Avertissement

**Usage éducatif uniquement.** Utilisez uniquement sur vos propres appareils.
Toute utilisation non autorisée est illégale.

## 📄 License

Usage éducatif et recherche en cybersécurité uniquement.
