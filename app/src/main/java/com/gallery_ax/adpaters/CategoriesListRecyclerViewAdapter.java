package com.gallery_ax.adpaters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gallery_ax.CategoryPicturesListActivity;
import com.gallery_ax.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CategoriesListRecyclerViewAdapter extends RecyclerView.Adapter<CategoryItemViewHolder> {

    private final Context context;
    private final List<DocumentSnapshot> categoriesList;

    // Storage
    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://gallery-ax.appspot.com");
    private final StorageReference storageRef = storage.getReference();

    public CategoriesListRecyclerViewAdapter(Context context, List<DocumentSnapshot> categoriesList) {
        this.context = context;
        this.categoriesList = categoriesList;
    }

    @NonNull
    @Override
    public CategoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ساخت لایه مربوط به هر آیتم
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.category_item_layout, parent, false);
        return new  CategoryItemViewHolder(itemView, storageRef);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemViewHolder holder, int position) {
        // ارسال اطلاعات به کلاس ViewHolder برای جایگذاری در هر آیتم
        holder.bind(categoriesList.get(position), position == 0, position == (getItemCount() - 1));
    }

    @Override
    public int getItemCount() {
        return categoriesList.size();
    }
}

class CategoryItemViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final ConstraintLayout mainFrame;
    private final MaterialCardView mainMcv;
    private final TextView titleTv;
    private final ImageView imageViewIv;
    private final StorageReference storageRef;

    public CategoryItemViewHolder(@NonNull View itemView, StorageReference storageRef) {
        super(itemView);

        this.context = itemView.getContext();
        this.storageRef = storageRef;
        // اتصال المان های لایه آیتم به کد جاوا
        this.mainFrame = itemView.findViewById(R.id.category_item_main_layout_cl);
        this.mainMcv = itemView.findViewById(R.id.categories_item_mcv);
        this.titleTv = itemView.findViewById(R.id.category_item_title_tv);
        this.imageViewIv = itemView.findViewById(R.id.category_item_image_iv);
    }

    public void bind(DocumentSnapshot categoryDocument, boolean isFirstItem, boolean isLastItem) {
        // تعیین فاصله هر آیتم از اطراف بر اساس جایگاه آن در لیست
        if (isFirstItem) {
            mainFrame.setPadding(0, 58, 0, 0);
        } else if (isLastItem) {
            mainFrame.setPadding(0, 0, 0, 58);
        } else {
            mainFrame.setPadding(0, 0, 0, 0);
        }

        // قرار دادن اطلاعات مربوط به آیتم در لایه آن
        titleTv.setText(categoryDocument.getString("title"));
        StorageReference imageRef = storageRef.child("categories_image/" + categoryDocument.getString("imageName"));
        Glide.with(context).load(imageRef).into(imageViewIv);

        mainMcv.setOnClickListener(v -> {
            // هدایت کاربر به صفحه لیست تصاویر با کلیک روی هر آیتم
            Intent intent = new Intent(context, CategoryPicturesListActivity.class);
            intent.putExtra("categoryId", categoryDocument.getId());
            intent.putExtra("categoryTitle", categoryDocument.getString("title"));
            context.startActivity(intent);
        });
    }
}
