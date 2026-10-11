package com.gdg.connect
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class ConnectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATORS) {
            if (FirebaseApp.getApps(this).isEmpty()) FirebaseApp.initializeApp(this, FirebaseOptions.Builder().setProjectId("demo-gdg-connect").setApplicationId("1:123456789:android:localtest").setApiKey("demo-api-key").build())
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
        }
    }
}
