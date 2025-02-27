# Firebase Setup Guide

## Fixing the "SecurityException: Unknown calling package name 'com.google.android.gms'" Error

This error typically occurs when your app's SHA-1 certificate fingerprint is not registered in your Firebase project. Here's how to fix it:

## 1. Get Your App's SHA-1 Fingerprint

### For Debug Build:

#### Using Android Studio:
1. Open your project in Android Studio
2. Click on "Gradle" in the right sidebar
3. Navigate to Tasks > android > signingReport
4. Double-click on signingReport
5. Look for the SHA-1 value in the output

#### Using Command Line (Windows):
```
cd %USERPROFILE%\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Using Command Line (Mac/Linux):
```
cd ~/.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### For Release Build:
If you're using a release keystore, use this command:
```
keytool -list -v -keystore YOUR_RELEASE_KEYSTORE_PATH -alias YOUR_ALIAS
```

## 2. Add the SHA-1 Fingerprint to Firebase

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click on the gear icon (⚙️) next to "Project Overview" to open Project settings
4. Go to the "General" tab
5. Scroll down to "Your apps" section
6. Click on your Android app
7. Click "Add fingerprint"
8. Paste your SHA-1 fingerprint and click "Save"

## 3. Download Updated google-services.json

1. After adding the fingerprint, click on "Download google-services.json"
2. Replace the existing file in your app directory with the new one

## 4. Clean and Rebuild Your Project

1. In Android Studio, go to Build > Clean Project
2. Then Build > Rebuild Project

## 5. Check Firestore Security Rules

Make sure your Firestore security rules allow read/write access. For testing, you can use these rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**Note:** These rules allow anyone to read and write to your database. For production, use more restrictive rules.

## 6. Check Java Version

Make sure you're using Java 11 or newer for building the app. In your `build.gradle.kts` file, you should have:

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```

## 7. Test with the Firebase Test Activity

Use the Firebase Test Activity in the app to diagnose issues:
1. Open the app
2. Click on "Test Firebase" in the menu
3. Click "Check Google Play Services" to verify Google Play Services is working
4. Try the "Test Write" and "Test Read" buttons
5. Check the logs for detailed error messages

## Common Issues and Solutions

### 1. Missing SHA-1 Fingerprint
- **Symptom:** SecurityException with "Unknown calling package name 'com.google.android.gms'"
- **Solution:** Add your app's SHA-1 fingerprint to Firebase as described above

### 2. Outdated Google Play Services
- **Symptom:** ConnectionResult error codes when checking Google Play Services
- **Solution:** Update Google Play Services on your device

### 3. Firestore Security Rules
- **Symptom:** PERMISSION_DENIED errors
- **Solution:** Update your Firestore security rules to allow access

### 4. Network Issues
- **Symptom:** UNAVAILABLE errors
- **Solution:** Check your internet connection and firewall settings

### 5. Java Version Issues
- **Symptom:** Build errors mentioning Java version
- **Solution:** Update your Java version to 11 or newer

## Need More Help?

If you're still experiencing issues, check the [Firebase documentation](https://firebase.google.com/docs) or post a question on [Stack Overflow](https://stackoverflow.com/questions/tagged/firebase) with the `firebase` and `android` tags. 