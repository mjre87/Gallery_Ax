package com.gallery_ax.bottom_sheets;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gallery_ax.LoginActivity;
import com.gallery_ax.R;
import com.gallery_ax.adpaters.PicturesListRecyclerViewAdapter;
import com.gallery_ax.databinding.BottomSheetFragmentImageViewerBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerBottomSheetFragment extends BottomSheetDialogFragment {

    private final int READ_EXTERNAL_PERMISSION_REQUEST_CODE = 1001;

    private BottomSheetFragmentImageViewerBinding binding;
    private String imageId;
    private DocumentSnapshot imageDocument;
    private PicturesListRecyclerViewAdapter picturesListAdapter;

    private long likes;
    private boolean isUserLikeIt;
    private boolean isImageInfoChanged = false;

    // Dialogs
    private ProgressDialog progressDialog;
    private AlertDialog.Builder loginAlert;

    private final File picturesDirectory = new File(Environment.getExternalStorageDirectory(), "Pictures/Gallery Ax");

    // Current User
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    // Firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Storage
    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://gallery-ax.appspot.com");
    private final StorageReference storageRef = storage.getReference();

    public ImageViewerBottomSheetFragment(String imageId, PicturesListRecyclerViewAdapter picturesListAdapter) {
        this.imageId = imageId;
        this.picturesListAdapter = picturesListAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetFragmentImageViewerBinding.inflate(inflater, container, false);
        getDialog().setOnShowListener(dialogInterface -> setupFullHeight((BottomSheetDialog) dialogInterface));
        return binding.getRoot();
    }

    // برای تمام صفحه کردن BottomSheet
    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        View bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        // تمام صفحه کردن BottomSheet
        if (layoutParams != null) layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomSheet.setLayoutParams(layoutParams);
        // باز کردن آن به صورت کامل
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        // خاموش کردن قابلیت جابجایی آن
        behavior.setDraggable(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // دریافت اطلاعات تصویر
        getImageDocument();
    }

    // دریافت اطلاعات تصویر
    private void getImageDocument() {
        db.collection("pictures").document(imageId)
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       imageDocument = task.getResult();
                       // قرار دادن عنوان تصویر در لایه
                       binding.pictureTitleTv.setText(imageDocument.getString("title"));
                       // قراردادن تصویر در لایه
                       String imageSrc = "pictures/" + imageDocument.getString("categoryId") + "/" + imageDocument.getString("imageName");
                       StorageReference pictureRef = storageRef.child(imageSrc);
                       Glide.with(this).load(pictureRef).into(binding.imageIv);
                       // گرفتن اطلاعات مربوط به لایک های تصویر
                       likes = imageDocument.getLong("likes");
                       List<String> lovers = (List<String>) imageDocument.get("lovers");
                       isUserLikeIt = currentUser != null && lovers.contains(currentUser.getEmail());
                       updateLikeCounts();

                       // نمایان کردن المان های لایه
                       binding.imageViewerMainLayoutCl.animate().alpha(1f).setDuration(300).withStartAction(() -> {
                           binding.imageViewerPb.setVisibility(View.GONE);
                           binding.imageViewerMainLayoutCl.setVisibility(View.VISIBLE);
                       }).start();

                       // ساخت دیالوگ های پیشرفت و هشدار لاگین
                       createProgressDialog();
                       createLoginAlertDialog();

                       binding.imageViewerCloseMcv.setOnClickListener(v -> dismiss());
                       binding.imageViewerLikeMb.setOnClickListener(v -> performLikeImage());
                       binding.imageViewerDownloadMb.setOnClickListener(v -> downloadPicture(pictureRef));
                   } else {
                       Toast.makeText(requireContext(), "یه مشکلی پیش اومده", Toast.LENGTH_LONG).show();
                       dismiss();
                   }
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_LONG).show();
                    dismiss();
                });
    }

    // ساخت دیالوگ پیشرفت
    private void createProgressDialog() {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        progressDialog.setMessage(imageDocument.getString("title"));
        progressDialog.setCancelable(false);
    }

    // ساخت دیالوگ هشدار لاگین
    private void createLoginAlertDialog() {
        loginAlert = new AlertDialog.Builder(requireContext());
        loginAlert.setMessage("واسه لایک و دانلود تصویر حتما باید با حساب گوگلت وارد بشی.");
        loginAlert.setNegativeButton("بی خیال", (dialog, which) -> dialog.dismiss());
        loginAlert.setPositiveButton("باشه، چرا که نه!", (dialog, which) -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.putExtra("isFromSplash", false);
            startActivity(intent);
        });
    }

    // چک کردن اینکه آیا کاربر لاگین کرده یا نه
    private boolean isUserLoggedIn() {
        if (currentUser == null) {
            // اگه نکرده بود دیالوگ هشدار را به کاربر نشان میدهیم
            loginAlert.show();
            return false;
        } else {
            return true;
        }
    }

    // چک کردن اینکه ایا قبلا کاربر اجازه دسترسی به فایل ها داده یا نه
    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // برای اندروید 11 به بالا
            return Environment.isExternalStorageManager();
        } else {
            // برای اندروید 10 به پائین
            return requireActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    // گرفتن اجازه دسترسی به فایلها از کاربر
    private void takePermission(){
        // برای اندروید 11 به بالا
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse("package:" + requireActivity().getApplicationContext().getPackageName()));
                startActivityForResult(intent, READ_EXTERNAL_PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, READ_EXTERNAL_PERMISSION_REQUEST_CODE);
            }
        } else {
            // برای اندروید 10 به پائین
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_PERMISSION_REQUEST_CODE);
        }
    }

    // دانلود تصویر
    private void downloadPicture(StorageReference pictureRef) {
        // اول اجازه دسترسی به حافظه را چک میکنیم
        if (isPermissionGranted()) {
            // بعد وضعیت لاگین کاربر را چک میکنیم
            if (isUserLoggedIn()) {
                // عنوان دیالوگ پیشرفت را تغییر میدهیم و آنرا نمایان میکنیم
                progressDialog.setTitle("در حال دانلود تصویر...");
                progressDialog.show();

                // اگر پوشه دانلود تصاویر وجود نداشت آنرا میسازیم
                if (!picturesDirectory.exists()) picturesDirectory.mkdirs();
                // فایل مربوط به تصویر را میسازیم
                File pictureFile = new File(picturesDirectory.getPath(), imageDocument.getString("imageName"));
                // عملیات را آغاز میکینیم
                pictureRef.getFile(pictureFile).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // اگر موفق بود دیالوگ پیشرفت را مخفی میکنیم
                        progressDialog.dismiss();
                        // و محل ذخیره تصویر را به کاربر گزارش میدهیم
                        Toast.makeText(requireContext(), "در پوشه Pictures/Gallery Ax ذخیره شد ;)", Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(requireContext(), "یه مشکلی پیش اومده", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            takePermission();
        }
    }

    // المان های لایه را بر اساس تعداد لایک های تصویر به روز میکند
    private void updateLikeCounts() {
        // اگر تعداد لایک ها بیشتر از صفر بود
        if (likes > 0) {
            // تعداد لایک هارا به عنوان متن دکمه لایک تعیین میکنیم
            binding.imageViewerLikeMb.setText(likes + " نفر");
        } else {
            // در غیر اینصورت عبارت <<لایک تصویر>> را قرار میدهیم
            binding.imageViewerLikeMb.setText("لایک تصویر");
        }

        // اگر کاربر تصویر را لایک کرده بود
        if (isUserLikeIt) {
            // رنگ پس زمینه دکمه لایک را قرمز میکنیم
            binding.imageViewerLikeMb.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red)));
        } else {
            // در غیر اینصورت رنگ پس زمینه دکمه لایک را خاکستری میکنیم
            binding.imageViewerLikeMb.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark_grey)));
        }
    }

    // آماده شدن برای لایک تصویر
    private void performLikeImage() {
        // اول وضعیت لاگین کاربر را چک میکنیم
        if (isUserLoggedIn()) {
            // اگه لاگین کرده بود
            // عنوان دیالوگ پیشرفت را تغییر میدهیم و آنرا نمایان میکنیم
            progressDialog.setTitle("در حال ثبت لایک تصویر...");
            progressDialog.show();
            // عملیات لایک را آغاز میکنیم
            likeImage((isUserLikeIt)? -1 : 1);
        }
    }

    private void likeImage(int num) {
        // تصویر را در دیتابیس پیدا میکنیم
        DocumentReference pictureRef = db.collection("pictures").document(imageDocument.getId());
        db.runTransaction(transaction -> {
            // اطلاعات تصویر را ذخیره میکنیم
            DocumentSnapshot doc = transaction.get(pictureRef);
            // تعداد لایک ها به روز میکنیم
            transaction.update(pictureRef, "likes", doc.getLong("likes") + num);

            // لیست برگزیدگان تصویر را ذخیره میکنیم
            ArrayList<String> lovers = (ArrayList<String>) doc.get("lovers");
            // ایمیل کاربر را ذخیره میکنیم
            String userEmail = currentUser.getEmail();
            // اگه کاربر قبلا تصویر را لایک نکرده بود آنرا به لیست اضافه میکنیم
            if (num == 1) {
                lovers.add(userEmail);
            } else {
                // در غیر اینصورت حذف میکنیم
                lovers.remove(userEmail);
            }
            // تغییرات را ثبت میکنیم
            transaction.update(pictureRef, "lovers", lovers);
            return null;
        }).addOnCompleteListener(task -> {
            // اگر موفق بود
            if (task.isSuccessful()) {
                // تعداد و وضعیت لایک را تغییر میدهیم
                likes += num;
                isUserLikeIt = (num == 1);
                isImageInfoChanged = true;
                // و المان ها را به روز میکنیم
                updateLikeCounts();

                Toast.makeText(requireContext(), "لایک با موفقیت ثبت شد ;)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "مشکلی پیش اومده!", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            Toast.makeText(requireContext(), "خطایی رخ داده است.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // در هنگام بسته شدن BottomSheet لیست مورد علاقه ها را تازه سازی میکنیم
        if (isImageInfoChanged) {
            picturesListAdapter.refreshRecyclerView();
        }
    }
}