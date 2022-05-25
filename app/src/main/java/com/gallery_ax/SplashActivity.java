package com.gallery_ax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // چک کردن اینکه آیا کاربر با حساب گوگلش وارد شده است یا نه
        if (currentUser == null) {
            // اگر نکرده بود کاربر را به صفحه ورود هدایت میکنیم
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("isFromSplash", true);
            startActivity(intent);
        } else {
            // وگرنه به صفحه اصلی
            startActivity(new Intent(this, MainActivity.class));
        }
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        finish();
    }
}