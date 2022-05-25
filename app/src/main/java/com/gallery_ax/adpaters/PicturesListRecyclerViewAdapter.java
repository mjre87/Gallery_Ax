package com.gallery_ax.adpaters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gallery_ax.R;
import com.gallery_ax.bottom_sheets.ImageViewerBottomSheetFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;
import java.util.function.Function;

public class PicturesListRecyclerViewAdapter extends RecyclerView.Adapter<PictureViewHolder> {

    private final Context context;
    private final List<DocumentSnapshot> picturesList;
    private final FragmentManager fragmentManager;

    // Storage
    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://gallery-ax.appspot.com");
    private final StorageReference storageRef = storage.getReference();

    private Function<Void, Void> onImageInfoChanged;

    public PicturesListRecyclerViewAdapter(
            Context context,
            List<DocumentSnapshot> picturesList,
            FragmentManager fragmentManage,
            Function<Void, Void> onImageInfoChanged) {
        this.context = context;
        this.picturesList = picturesList;
        this.fragmentManager = fragmentManage;
        this.onImageInfoChanged = onImageInfoChanged;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ساخت لایه مربوط به هر آیتم
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.picture_item_layout, parent, false);
        return new PictureViewHolder(itemView, storageRef, this, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        // ارسال اطلاعات به کلاس ViewHolder برای جایگذاری در هر آیتم
        holder.bind(picturesList.get(position), position < 3);
    }

    @Override
    public int getItemCount() {
        return picturesList.size();
    }

    public void refreshRecyclerView() {
        onImageInfoChanged.apply(null);
    }
}

class PictureViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final PicturesListRecyclerViewAdapter adapter;
    private final FragmentManager fragmentManager;
    private final StorageReference storageRef;
    private final ConstraintLayout mainFrame;
    private final MaterialCardView mainCard;
    private final ImageView pictureIv;

    public PictureViewHolder(@NonNull View itemView, StorageReference storageRef, PicturesListRecyclerViewAdapter adapter, FragmentManager fragmentManager) {
        super(itemView);

        this.context = itemView.getContext();
        this.adapter = adapter;
        this.fragmentManager = fragmentManager;
        this.storageRef = storageRef;
        // اتصال المان های لایه آیتم به کد جاوا
        this.mainFrame = itemView.findViewById(R.id.picture_item_main_layout);
        this.mainCard = itemView.findViewById(R.id.picture_item_image_card);
        this.pictureIv = itemView.findViewById(R.id.picture_item_iv);
    }

    public void bind(DocumentSnapshot pictureDocument, boolean isTopThreeItem) {
        // تعیین فاصله هر آیتم از اطراف بر اساس جایگاه آن در لیست
        if (isTopThreeItem) {
            mainFrame.setPadding(0, 58, 0, 0);
        } else {
            mainFrame.setPadding(0, 0, 0, 0);
        }

        // لود کردن تصویر و قراردادن آن در آیتم
        String pictureSrc = "pictures/" + pictureDocument.getString("categoryId") + "/" + pictureDocument.getString("imageName");
        StorageReference imageRef = storageRef.child(pictureSrc);
        Glide.with(context).load(imageRef).into(pictureIv);

        // نمایش BottomSheet مشخصات تصویر با کلیک روی هر آیتم
        mainCard.setOnClickListener(v -> new ImageViewerBottomSheetFragment(pictureDocument.getId(), adapter)
                .show(fragmentManager, "ImageViewer"));
    }
}