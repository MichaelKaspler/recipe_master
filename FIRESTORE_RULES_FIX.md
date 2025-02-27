# Firestore Security Rules Fix

## Current Issue
Your app is encountering a permission error when trying to write to Firestore:
```
Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
```

This happens because your Firestore security rules are too restrictive. The user authentication is working correctly (users are created in Firebase Auth), but the app doesn't have permission to write to the Firestore database.

## How to Fix

1. **Go to the Firebase Console**:
   - Visit [Firebase Console](https://console.firebase.google.com/)
   - Select your project "recipe-misha"

2. **Navigate to Firestore Database**:
   - In the left sidebar, click on "Firestore Database"

3. **Go to Rules Tab**:
   - At the top of the Firestore Database page, click on the "Rules" tab

4. **Update the Security Rules**:
   - Replace the current rules with the following:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read and write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read and write recipes
    match /recipes/{recipeId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Default deny
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

5. **Publish the Rules**:
   - Click the "Publish" button to apply the new rules

## Explanation of the Rules

These rules do the following:

1. **User Documents**:
   - Allow users to read and write only their own user document
   - The document ID must match the user's UID

2. **Recipe Documents**:
   - Allow any authenticated user to read and write recipes
   - This enables sharing recipes between users

3. **Default Deny**:
   - Deny access to all other documents by default
   - This is a security best practice

## Testing the Fix

After updating the rules:

1. **Clear App Data**:
   - Go to your device settings
   - Find your app in the Apps list
   - Clear app data/cache

2. **Try Registering Again**:
   - Open your app
   - Register a new user
   - The registration should complete without errors

## Additional Security Considerations

For a production app, you might want to add more granular rules:

- Add validation for recipe fields
- Limit the number of recipes a user can create
- Add rate limiting to prevent abuse

Example of more advanced rules:
```
match /recipes/{recipeId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null 
                && request.resource.data.title is string
                && request.resource.data.title.size() <= 100;
  allow update: if request.auth != null 
                && request.resource.data.title is string
                && request.resource.data.title.size() <= 100;
  allow delete: if request.auth != null;
}
``` 