# Firebase Authentication Setup Fix

## Problem
You're getting `CONFIGURATION_NOT_FOUND` error when trying to sign up. This happens because Firebase Authentication needs proper configuration.

## Solution Steps

### Step 1: Enable Email/Password Authentication in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **engiassist-9bc6a**
3. Navigate to **Authentication** → **Sign-in method**
4. Click on **Email/Password**
5. **Enable** the "Email/Password" provider (toggle it ON)
6. Click **Save**

### Step 2: Add SHA-1 and SHA-256 Fingerprints

1. In Firebase Console, go to **Project Settings** (gear icon ⚙️)
2. Scroll down to **Your apps** section
3. Find your Android app: **com.example.auth**
4. Click **Add fingerprint** button
5. Add these two fingerprints:

**SHA-1:**
```
21:44:95:BC:54:EA:8A:E8:AE:D8:DD:9B:A0:43:EC:7C:6A:9A:EC:74
```

**SHA-256:**
```
E8:03:01:CA:2A:F8:D3:E0:38:2C:E6:25:89:05:D4:66:E2:61:32:98:4E:C6:A6:DF:C7:9F:DA:5B:2A:A9:47:A4
```

6. Click **Save** after adding each fingerprint

### Step 3: Download Updated google-services.json (Optional)

After adding fingerprints, you may need to:
1. Download the updated `google-services.json` from Firebase Console
2. Replace the existing file in `app/google-services.json`

**Note:** Usually this isn't necessary, but if issues persist, try this.

### Step 4: Clean and Rebuild Your Project

After making changes in Firebase Console:

1. In Android Studio, go to **Build** → **Clean Project**
2. Then **Build** → **Rebuild Project**
3. Run the app again

## Why This Happens

Firebase uses reCAPTCHA for email/password authentication to prevent abuse. The `CONFIGURATION_NOT_FOUND` error means:
- Email/Password auth is not enabled, OR
- SHA fingerprints are missing (Firebase needs them to verify your app)

## Verification

After completing these steps, try signing up again. The error should be resolved.

If you still get errors:
1. Wait 5-10 minutes for Firebase to propagate changes
2. Make sure you're using the correct Firebase project
3. Verify the package name matches: `com.example.auth`

