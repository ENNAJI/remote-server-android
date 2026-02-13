#!/bin/bash

echo "========================================="
echo "Installation de Gradle Wrapper"
echo "========================================="
echo ""

# Télécharger Gradle Wrapper JAR
echo "[1/3] Téléchargement de Gradle Wrapper..."
mkdir -p gradle/wrapper
curl -L -o gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar

# Télécharger gradlew
echo "[2/3] Téléchargement de gradlew..."
curl -L -o gradlew https://raw.githubusercontent.com/gradle/gradle/master/gradlew
chmod +x gradlew

# Télécharger gradlew.bat
echo "[3/3] Téléchargement de gradlew.bat..."
curl -L -o gradlew.bat https://raw.githubusercontent.com/gradle/gradle/master/gradlew.bat

echo ""
echo "✅ Gradle Wrapper installé!"
echo ""
echo "Vous pouvez maintenant compiler avec:"
echo "  ./gradlew assembleDebug"
echo ""
