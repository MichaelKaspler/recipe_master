# Firebase Troubleshooting Guide

## Current Issue
You're encountering a `SecurityException` with the message "Unknown calling package name 'com.google.android.gms'." This is a common issue with Firebase authentication in Android apps.

## Root Causes and Solutions

### 1. Missing SHA-1 Fingerprint in Firebase Console

**Symptoms:**
- SecurityException with "Unknown calling package name 'com.google.android.gms'"
- Authentication failures with Firebase
- Google Sign-In failures

**Solution:**
1. Get your app's SHA-1 fingerprint:
   ```
   .\gradlew signingReport
   ```
   Your debug SHA-1 fingerprint is: `D0:C6:EF:86:2C:7E:39:01:C8:3B:BD:C8:3D:BD:95:3B:F2:B7:29:86`

2. Add this fingerprint to your Firebase project:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project
   - Go to Project Settings (gear icon)
   - Under "Your apps", find your Android app
   - Add the SHA-1 fingerprint
   - Download the updated google-services.json file
   - Replace the existing file in your app directory

3. Clean and rebuild your project:
   ```
   .\gradlew clean build
   ```

### 2. Outdated Google Play Services

**Symptoms:**
- Authentication errors
- API availability issues

**Solution:**
1. Check Google Play Services version on your device/emulator
2. Update Google Play Services if needed
3. Use the "Check Google Play Services" button in FirebaseTestActivity

### 3. Improper Firebase Initialization

**Symptoms:**
- Firebase operations fail
- NullPointerExceptions when using Firebase

**Solution:**
1. Ensure Firebase is initialized in your Application class
2. Make sure initialization happens before any Firebase operations
3. Use try-catch blocks to handle initialization errors

### 4. Network Issues

**Symptoms:**
- Timeouts
- Connection failures

**Solution:**
1. Check internet connection on your device/emulator
2. Verify firewall settings
3. Try using a different network

### 5. Firebase Rules

**Symptoms:**
- Permission denied errors
- Read/write operations fail

**Solution:**
1. Check your Firestore security rules
2. Ensure rules allow the operations you're trying to perform

## Debugging Steps

1. **Enable Verbose Logging**
   Add this to your Application class:
   ```java
   FirebaseFirestore.setLoggingEnabled(true);
   ```

2. **Check Initialization Order**
   Make sure Firebase is initialized before any Firebase operations.

3. **Verify google-services.json**
   Ensure it contains your package name and SHA-1 fingerprint.

4. **Test with a New Project**
   Create a simple test project to isolate the issue.

## Common Error Messages and Solutions

### "Unknown calling package name 'com.google.android.gms'"
- Add SHA-1 fingerprint to Firebase Console
- Ensure google-services.json is up to date
- Check that the package name in your app matches the one in Firebase Console

### "PERMISSION_DENIED"
- Check Firestore security rules
- Verify authentication state

### "UNAVAILABLE"
- Check internet connection
- Verify Firebase service status

### "UNAUTHENTICATED"
- Check authentication setup
- Verify SHA-1 fingerprint in Firebase Console

## Next Steps

If you continue to experience issues after following these steps:

1. Check the Firebase status page for service disruptions
2. Review the Firebase documentation for any recent changes
3. Search for similar issues on Stack Overflow
4. Contact Firebase support with detailed logs 