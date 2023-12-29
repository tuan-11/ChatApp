package com.zileanstdio.chatapp.Ui.main.connections.profile;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.auth.AuthActivity;
import com.zileanstdio.chatapp.Ui.change.ChangePasswordActivity;
import com.zileanstdio.chatapp.Ui.main.MainActivity;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;

@SuppressLint("SetTextI18n")
public class ProfileView extends BaseFragment {

    private String userName, numberPhone;

    private ShapeableImageView imvAvatar;
    private MaterialButton btnUsername, btnPassword, btnLogout;
    private MaterialTextView txvPhone, txvBirthdate, txvGender;

    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;


    public ProfileView() {
        // Required empty public constructor
    }

    @Override
    public ViewModel getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(ProfileViewModel.class);
        }
        return viewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initAppBar() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.layout_profile_view, container, false);
        return super.onCreateView(inflater, viewGroup, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        subscribeObserversUpdateUserName();
        subscribeObserversLogout();

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        ((MainActivity) baseActivity).getViewModel().getUserInfo().observe(getViewLifecycleOwner(), user -> {
            if (user.getAvatarImageUrl() != null && !user.getAvatarImageUrl().isEmpty()) {
                Glide.with(baseActivity)
                    .load(user.getAvatarImageUrl())
                    .error(R.drawable.ic_default_user)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imvAvatar);
            } else {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            }
            btnUsername.setText("Người dùng: " + user.getUserName());
            txvPhone.setText("Số điện thoại: " + user.getPhoneNumber());
            txvBirthdate.setText("Ngày sinh: " + user.getBirthDate());
            if (user.getGender().equals("Male")) {
                txvGender.setText("Giới tính: Nam");
            } else {
                txvGender.setText("Giới tính: Nữ");
            }

            userName = user.getUserName();
            numberPhone = user.getPhoneNumber();
        });

        btnUsername.setOnClickListener(v -> {
            View viewDialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_text_username, null, false);
            TextInputLayout userNameInputLayout = viewDialog.findViewById(R.id.text_input_user_name);
            EditText userNameEditText = userNameInputLayout.getEditText();

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(baseActivity);
            builder.setTitle("Đổi tên");
            builder.setIcon(R.drawable.icon_start);
            builder.setView(viewDialog);
            builder.setPositiveButton("Lưu", null);
            builder.setNegativeButton("Hủy", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(position -> {
                if ((userNameEditText != null) && !userNameEditText.getText().toString().isEmpty()) {
                    if (!userNameEditText.getText().toString().equals(userName)) {
                        ((ProfileViewModel) viewModel).updateUserName(userNameEditText.getText().toString(), numberPhone);
                        dialog.cancel();
                    } else {
                        userNameInputLayout.setError("Tên mới phải khác tên hiện tại '" + userName + "'");
                        userNameInputLayout.setErrorIconDrawable(null);
                    }
                } else {
                    userNameInputLayout.setError("Tên người dùng không thể trống");
                    userNameInputLayout.setErrorIconDrawable(null);
                }
            });
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(negative -> dialog.cancel());
        });

        btnPassword.setOnClickListener(v -> {
            Intent startChangePasswordActivity = new Intent(getContext(), ChangePasswordActivity.class);
            startChangePasswordActivity.putExtra("email", String.format("%s@gmail.com", numberPhone));
            startActivity(startChangePasswordActivity);
        });

        btnLogout.setOnClickListener(v -> ((ProfileViewModel) viewModel).logout());

        imvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
    }

    private void openImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if(imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        ((MainActivity) baseActivity).getViewModel().getUserInfo().observe(getViewLifecycleOwner(), user -> {
                            firebaseFirestore = FirebaseFirestore.getInstance();
                            DocumentReference userRef = firebaseFirestore.collection("User").document(CipherUtils.Hash.sha256(user.getPhoneNumber()));

                            // Lưu imageUrl vào thông tin user trong Firestore
                            userRef.update("avatarImageUrl", mUri)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("UploadImage", "Image URL updated successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UploadImage", "Failed to update image URL: " + e.getMessage());
                                    });
                        });
                        pd.dismiss();
                    }else {
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Uploading in preogress", Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }
    }

    private void initView(View view) {
        imvAvatar = view.findViewById(R.id.imv_avatar);
        btnUsername = view.findViewById(R.id.btn_user_name);
        btnPassword = view.findViewById(R.id.btn_password);
        txvPhone = view.findViewById(R.id.txv_phone);
        txvBirthdate = view.findViewById(R.id.txv_birthdate);
        txvGender = view.findViewById(R.id.txv_gender);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    public void subscribeObserversUpdateUserName() {
        ((ProfileViewModel) viewModel).observeUpdateUserName().observe(getViewLifecycleOwner(), stateResource -> {
            if (stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        baseActivity.showLoadingDialog();
                        break;
                    case SUCCESS:
                        baseActivity.closeLoadingDialog();
                        Toast.makeText(getContext(), "Đổi tên thành công", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        baseActivity.closeLoadingDialog();
                        Toast.makeText(getContext(), "Đã có lỗi xảy ra!\nVui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    public void subscribeObserversLogout() {
        ((ProfileViewModel) viewModel).observeLogout().observe(getViewLifecycleOwner(), stateResource -> {
            if (stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        baseActivity.showLoadingDialog();
                        break;
                    case SUCCESS:
                        Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        Intent startAuthActivity = new Intent(getContext(), AuthActivity.class);
                        startAuthActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(startAuthActivity);
                        break;
                    case ERROR:
                        baseActivity.closeLoadingDialog();
                        Toast.makeText(getContext(), stateResource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }
}