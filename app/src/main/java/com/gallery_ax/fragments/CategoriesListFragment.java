package com.gallery_ax.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.gallery_ax.adpaters.CategoriesListRecyclerViewAdapter;
import com.gallery_ax.databinding.FragmentCategoriesListBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CategoriesListFragment extends Fragment {

    private FragmentCategoriesListBinding binding;

    // FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoriesListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // با کشیدن صفحه به پایین صفحه تازه سازی میشود
        binding.categoriesListSrl.setOnRefreshListener(this::getCategoriesDocuments);
        // تعیین نوع چینش RecyclerView
        binding.categoriesListRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        getCategoriesDocuments();
    }

    private void getCategoriesDocuments() {
        // مخفی کردن RecyclerView و نمایش لودینگ
        binding.categoriesListRv.setVisibility(View.GONE);
        binding.categoriesListSrl.setRefreshing(true);
        db.collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // نمایش RecyclerView و مخفی کردن لودینگ
                        binding.categoriesListRv.setVisibility(View.VISIBLE);
                        binding.categoriesListSrl.setRefreshing(false);

                        List<DocumentSnapshot> result = task.getResult().getDocuments();
                        // تعیین آداپتور RecyclerView
                        binding.categoriesListRv.setAdapter(new CategoriesListRecyclerViewAdapter(requireContext(), result));
                    } else {
                        Log.e("GetCategoriesDocs", task.getException().getMessage());
                        Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
                });
    }
}