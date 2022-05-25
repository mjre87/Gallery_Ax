package com.gallery_ax.fragments;

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
import com.gallery_ax.adpaters.PicturesListRecyclerViewAdapter;
import com.gallery_ax.databinding.FragmentLatestPicturesBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class LatestPicturesFragment extends Fragment {

    private FragmentLatestPicturesBinding binding;

    // FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLatestPicturesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // با کشیدن صفحه به پایین صفحه تازه سازی میشود
        binding.latestPicturesSrl.setOnRefreshListener(this::getPicturesDocuments);
        // تعیین نوع چینش RecyclerView
        binding.latestPicturesRv.setLayoutManager(new GridLayoutManager(
                requireContext(), 3, LinearLayoutManager.VERTICAL, false));

        // لیست تصاویر را دریافت میکنیم
        getPicturesDocuments();
    }

    // لیست تصاویر را دریافت میکند
    private void getPicturesDocuments() {
        // مخفی کردن RecyclerView و نمایش لودینگ
        binding.latestPicturesRv.setVisibility(View.GONE);
        binding.latestPicturesSrl.setRefreshing(true);
        db.collection("pictures")
                .orderBy("id", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // نمایش RecyclerView و مخفی کردن لودینگ
                        binding.latestPicturesRv.setVisibility(View.VISIBLE);
                        binding.latestPicturesSrl.setRefreshing(false);

                        List<DocumentSnapshot> result = task.getResult().getDocuments();
                        // تعیین آداپتور RecyclerView
                        binding.latestPicturesRv.setAdapter(new PicturesListRecyclerViewAdapter(requireContext(), result, getParentFragmentManager(), this::onImageInfoChanged));
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
        getPicturesDocuments();
        return null;
    }
}