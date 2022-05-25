package com.gallery_ax;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.gallery_ax.adpaters.PicturesListRecyclerViewAdapter;
import com.gallery_ax.databinding.ActivityCategoryPicturesListBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class CategoryPicturesListActivity extends AppCompatActivity {

    private ActivityCategoryPicturesListBinding binding;

    // FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryPicturesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // اطلاعات مربوط به دسته بندی را ذخیره میکنیم
        Bundle bundle = getIntent().getExtras();
        categoryId = bundle.getString("categoryId");
        // عنوان صفحه را به نام دسته بندی تغییر میدهیم
        binding.categoryTitle.setText( "تصاویر " + bundle.getString("categoryTitle"));
        binding.picturesListSrl.setOnRefreshListener(this::getCategoryPicturesDocuments);
        binding.backButtonIb.setOnClickListener(v -> finish());
        binding.picturesListRv.setLayoutManager(new GridLayoutManager(
                this, 3, LinearLayoutManager.VERTICAL, false));

        // لیست تصاویر دسته بندی را دریافت میکنیم
        getCategoryPicturesDocuments();
    }

    private void getCategoryPicturesDocuments() {
        // مخفی کردن RecyclerView و نمایش لودینگ
        binding.picturesListRv.setVisibility(View.GONE);
        binding.picturesListSrl.setRefreshing(true);
        db.collection("pictures")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // نمایش RecyclerView و مخفی کردن لودینگ
                        binding.picturesListRv.setVisibility(View.VISIBLE);
                        binding.picturesListSrl.setRefreshing(false);

                        List<DocumentSnapshot> result = task.getResult().getDocuments();
                        // تعیین آداپتور RecyclerView
                        binding.picturesListRv.setAdapter(new PicturesListRecyclerViewAdapter(this, result, getSupportFragmentManager(), this::onImageInfoChanged));
                    } else {
                        Log.e("GetPicturesDocs", task.getException().getMessage());
                        Toast.makeText(this, "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Void onImageInfoChanged(Void unused) {
        getCategoryPicturesDocuments();
        return null;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}