package com.gallery_ax;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.gallery_ax.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean isFromSplash = false;

    // Auth
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 1001;

    // Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ذخیره اینکه آیا کاربر از صفحه Splash هدایت شده یا صفحه مشخصات تصویر
        isFromSplash = getIntent().getExtras().getBoolean("isFromSplash");

        // مقداردهی اولیه FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // GoogleSignIn کانفیگ
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // شروع انیمیشن ها
        binding.backgroundImageIv.animate().alpha(1f).setDuration(700).setStartDelay(300).start();
        binding.splashScreenBackgroundFrame.animate().translationYBy(-1 * getResources().getDimension(R.dimen.app_icon_y_transition)).setDuration(500).setStartDelay(300).start();
        binding.appDescriptionTv.animate().alpha(1f).setDuration(300).setStartDelay(600).start();

        binding.loginWithGoogleButton.setOnClickListener(v -> signIn());
        binding.loginAsGuestLl.setOnClickListener(v -> {
            // هدایت کاریر به صفحه اصلی
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }

    private void signIn() {
        // باز کردن صفحه انتخاب حساب گوگل
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        binding.loginLoadingPb.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // اگر عملیات GoogleSignIn موفق بود با Firebase هم تصدیق میکنیم
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                if (e.getMessage().contains("12500:")) {
                    Toast.makeText(this, "هیچ اکانت گوگلی روی گوشیت ثبت نشده!", Toast.LENGTH_SHORT).show();
                    binding.loginLoadingPb.setVisibility(View.GONE);
                } else {
                    showErrorMessage();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        // سند اعتبار سنجی را ذخیره میکنیم
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    goToMainPage();
                } else {
                    showErrorMessage();
                }
            });
    }

    private void goToMainPage() {
        if (isFromSplash) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            finish();
        }
    }

    private void showErrorMessage() {
        binding.loginLoadingPb.setVisibility(View.GONE);
        Toast.makeText(this, "مشکلی پیش آمده است :(", Toast.LENGTH_SHORT).show();
    }
}