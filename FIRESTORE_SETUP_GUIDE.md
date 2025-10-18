# 🔥 Complete Firebase Firestore Setup Guide

This guide will help you set up Firebase Firestore to store user data for your Agro Krishi app.

## 📋 Prerequisites

✅ Firebase project already created (`agro-krishi-seva`)  
✅ Firebase Authentication already integrated  
✅ `google-services.json` file already added  

## 🚀 Step-by-Step Setup

### **Step 1: Enable Firestore Database**

1. **Open Firebase Console**
   - Go to https://console.firebase.google.com/
   - Select your project: `agro-krishi-seva`

2. **Create Firestore Database**
   - Click "Firestore Database" in the left sidebar
   - Click "Create database"
   - **Choose "Start in test mode"** (for development)
   - Select your preferred location (closest to your users)
   - Click "Done"

### **Step 2: Configure Security Rules**

1. **Go to Firestore Database → Rules tab**
2. **Replace the default rules with:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read and write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read products and tips
    match /products/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null; // For admin operations
    }
    
    match /tips/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null; // For admin operations
    }
    
    // Allow users to read and write their bookmarks
    match /users/{userId}/bookmarks/{bookmarkId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

3. **Click "Publish"**

### **Step 3: Verify Authentication Setup**

1. **Go to Authentication → Sign-in method**
2. **Ensure Email/Password is enabled:**
   - Click on "Email/Password"
   - Toggle "Enable" if not already enabled
   - Click "Save"

### **Step 4: Test the Integration**

1. **Build and run your app**
2. **Register a new user**
3. **Check Firestore Console:**
   - Go to Firestore Database → Data tab
   - You should see a `users` collection
   - With a document containing user data

## 📊 Data Structure

### **User Document Structure**
```
users/{userId}
├── uid: string
├── name: string
├── email: string
├── phoneNumber: string (optional)
├── farmLocation: string (optional)
├── farmSize: string (optional)
├── cropTypes: array (optional)
├── joinDate: timestamp
├── totalOrders: number
├── tipsSaved: number
└── profileImageUrl: string (optional)
```

### **Future Collections (Optional)**
```
products/{productId}
├── name: string
├── category: string
├── price: number
├── description: string
└── imageUrl: string

tips/{tipId}
├── title: string
├── content: string
├── category: string
├── author: string
└── createdAt: timestamp

users/{userId}/bookmarks/{bookmarkId}
├── type: string ("tip" | "product")
├── itemId: string
└── createdAt: timestamp
```

## 🔧 Code Features Implemented

### **✅ User Registration**
- Saves user data to Firestore during registration
- Creates user document with basic profile information

### **✅ User Profile**
- Loads user data from Firestore
- Displays user information in profile section

### **✅ Analytics Tracking**
- `incrementOrderCount()` - Track user orders
- `incrementTipsSaved()` - Track saved tips

### **✅ Error Handling**
- Proper error handling for network issues
- Fallback to local data when Firestore is unavailable

## 🧪 Testing Your Setup

### **Test 1: User Registration**
1. Register a new user in your app
2. Check Firebase Console → Firestore Database
3. Verify user document is created in `users` collection

### **Test 2: User Profile Loading**
1. Login with registered user
2. Go to Profile section
3. Verify user data is displayed correctly

### **Test 3: Data Updates**
1. Use bookmark feature (when implemented)
2. Check if counters update in Firestore

## 🔒 Security Best Practices

### **Current Security Rules:**
- ✅ Users can only access their own data
- ✅ Authentication required for all operations
- ✅ Read access to shared content (products, tips)

### **Production Recommendations:**
1. **Enable App Check** for additional security
2. **Set up proper indexes** for query performance
3. **Monitor usage** in Firebase Console
4. **Set up billing alerts** to avoid unexpected charges

## 📱 Usage in Your App

### **Save User Data:**
```kotlin
val userData = UserData(
    uid = user.uid,
    name = "John Farmer",
    email = "john@example.com"
)
firestoreManager.saveUserData(userData)
```

### **Load User Data:**
```kotlin
lifecycleScope.launch {
    val result = firestoreManager.getUserData()
    if (result.isSuccess) {
        val userData = result.getOrNull()
        // Use userData
    }
}
```

### **Update Analytics:**
```kotlin
// When user saves a tip
firestoreManager.incrementTipsSaved()

// When user places an order
firestoreManager.incrementOrderCount()
```

## 🚨 Troubleshooting

### **Common Issues:**

1. **"Permission denied" errors**
   - Check Firestore security rules
   - Ensure user is authenticated

2. **Data not saving**
   - Check internet connection
   - Verify Firestore is enabled
   - Check app logs for errors

3. **Build errors**
   - Ensure all Firebase dependencies are added
   - Sync project with Gradle files

### **Debug Steps:**
1. Check Firebase Console for error logs
2. Enable Firestore debug logging in app
3. Test with Firebase Emulator for local development

## ✅ Verification Checklist

- [ ] Firestore Database created and enabled
- [ ] Security rules configured and published
- [ ] Email/Password authentication enabled
- [ ] App builds successfully
- [ ] User registration creates Firestore document
- [ ] User profile loads data from Firestore
- [ ] Error handling works properly

---

**🎉 Congratulations!** Your Agro Krishi app now has complete Firebase Firestore integration for user data storage!