package com.gallery_ax.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gallery_ax.LoginActivity;
import com.gallery_ax.R;
import com.gallery_ax.adpaters.PicturesListRecyclerViewAdapter;
import com.gallery_ax.databinding.FragmentFavoritePicturesListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class FavoritePicturesListFragment extends Fragment {

    private FragmentFavoritePicturesListBinding binding;
    private FirebaseUser currentUser;
    private AlertDialog.Builder loginAlert;

    // FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritePicturesListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // با کشیدن صفحه به پایین صفحه تازه سازی میشود
        binding.favoriteImagesSrl.setOnRefreshListener(() -> {
            if (currentUser != null) {
                getFavoriteImageDocuments();
            } else {
                loginAlert.show();
            }
        });
        // تعیین نوع چینش RecyclerView
        binding.favoriteImagesRv.setLayoutManager(new GridLayoutManager(
                requireContext(), 3, LinearLayoutManager.VERTICAL, false));

        createLoginAlertDialog();
    }

    @Override
    public void onResume() {
        super.onResume();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // اگه کاربر لاگین نکرده بود
        if (currentUser == null) {
            // دیالوگ هشدار را به کاربر نمایش میدهیم
            loginAlert.show();
            // پیغام خالی بودن لیست را تغییر داده
            binding.emptyFavoritesImageListTv.setText("باید با اکانت گوگلت وارد بشی!");
            // و برای کاربر نمایان میکنیم
            binding.emptyFavoritesImageListLl.setVisibility(View.VISIBLE);
        } else {
            // اگه کاربر لاگین کرده بود لیست تصاویر برگزیده را دریافت میکنیم
            getFavoriteImageDocuments();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // در حالتی که کاربر از لیست برگزیده ها خارج می شود RecyclerView را مخفی میکنیم
        binding.favoriteImagesRv.setVisibility(View.GONE);
    }

    // لیست تصاویر برگزیده را دریافت میکند
    private void getFavoriteImageDocuments() {
        // مخفی کردن RecyclerView و نمایش لودینگ
        binding.favoriteImagesRv.setVisibility(View.GONE);
        binding.favoriteImagesSrl.setRefreshing(true);
        db.collection("pictures")
                .whereArrayContains("lovers", currentUser.getEmail())
                .orderBy("id", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // نمایش RecyclerView و مخفی کردن لودینگ
                        binding.favoriteImagesRv.setVisibility(View.VISIBLE);
                        binding.favoriteImagesSrl.setRefreshing(false);

                        List<DocumentSnapshot> result = task.getResult().getDocuments();
                        if (result.size() > 0) {
                            // نمایش پیغام خالی بودن لیست
                            binding.emptyFavoritesImageListLl.setVisibility(View.GONE);
                            // تعیین آداپتور RecyclerView
                            binding.favoriteImagesRv.setAdapter(new PicturesListRecyclerViewAdapter(requireContext(), result, getParentFragmentManager(), this::onImageInfoChanged));
                        } else {
                            // مخفی کردن پیغام خالی بودن لیست
                            binding.favoriteImagesRv.setVisibility(View.GONE);
                            binding.emptyFavoritesImageListTv.setText("هیچ تصویری رو لایک نکردی!");
                            // نمایش RecyclerView
                            binding.emptyFavoritesImageListLl.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("GetPicturesDocs", task.getException().getMessage());
                        Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
                });
    }

    private Void onImageInfoChanged(Void unused) {
        getFavoriteImageDocuments();
        return null;
    }

    // ساخت دیالوگ پیغام عدم لاگین کردن
    private void createLoginAlertDialog() {
        loginAlert = new AlertDialog.Builder(requireContext());
        loginAlert.setMessage("واسه ایجاد لیست تصاویر برگزیده خودت، باید با حساب گوگلت وارد بشی.");
        loginAlert.setNegativeButton("بی خیال", (dialog, which) -> dialog.dismiss());
        loginAlert.setPositiveButton("باشه، چرا که نه!", (dialog, which) -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.putExtra("isFromSplash", false);
            startActivity(intent);
        });
    }
}