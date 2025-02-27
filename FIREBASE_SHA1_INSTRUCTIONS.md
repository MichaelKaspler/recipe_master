# Firebase SHA-1 Fingerprint Update Instructions

## Your SHA-1 Fingerprint
```
D0:C6:EF:86:2C:7E:39:01:C8:3B:BD:C8:3D:BD:95:3B:F2:B7:29:86
```

## Steps to Update Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Select your project "recipe-misha"
3. Click on the gear icon (⚙️) next to "Project Overview" to open Project settings
4. Select the "General" tab
5. Scroll down to the "Your apps" section
6. Find your Android app with package name "com.example.recipe_misha"
7. Click "Add fingerprint" under the SHA certificate fingerprints section
8. Enter the SHA-1 fingerprint exactly as shown above
9. Click "Save"
10. Scroll to the bottom and click "Download google-services.json"
11. Replace the existing google-services.json file in your app directory with this new file

## Verify the Updated google-services.json

After downloading the new google-services.json file, check that it contains:
1. Your package name: "com.example.recipe_misha"
2. Your SHA-1 fingerprint in the "certificate_hash" field
3. The correct Firebase project ID: "recipe-misha"

## Additional Troubleshooting

If you still encounter the "Unknown calling package name 'com.google.android.gms'" error after updating:

1. **Clean and Rebuild Your Project**
   ```
   .\gradlew clean build
   ```

2. **Check Google Play Services**
   - Make sure Google Play Services is up to date on your device/emulator
   - Use the "Check Google Play Services" button in the Firebase Test Activity

3. **Verify Firebase Initialization**
   - Make sure Firebase is initialized before any Firebase operations
   - Check that FirebaseApp.initializeApp() is called in your Application class

4. **Check Internet Connection**
   - Ensure your device/emulator has a stable internet connection

5. **Check Firebase Rules**
   - Verify your Firestore security rules allow read/write operations 