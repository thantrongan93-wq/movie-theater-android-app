package com.example.lab10.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lab10.R;
import com.example.lab10.activities.LoginActivity;
import com.example.lab10.api.ApiClient;
import com.example.lab10.api.MovieApiService;
import com.example.lab10.models.ApiResponse;
import com.example.lab10.models.ChangePasswordRequest;
import com.example.lab10.models.User;
import com.example.lab10.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername, tvEmail, tvPhone, tvAddress;
    private View btnEditProfile, btnChangePassword, btnLogout;

    private MovieApiService apiService;
    private SessionManager sessionManager;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        initData();
        loadUserProfile();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void initData() {
        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(requireContext());
        currentUser = sessionManager.getUser();
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            displayUserInfo(currentUser);
        }

        apiService.getMyInfo().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getResult();
                    if (user != null) {
                        sessionManager.saveUser(user);
                        currentUser = user;
                        displayUserInfo(user);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Ignore failure, use local data
            }
        });
    }

    private void displayUserInfo(User user) {
        tvUsername.setText(user.getUsername() != null ? user.getUsername() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvPhone.setText(user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() 
                ? user.getPhoneNumber() : "Chưa cập nhật SĐT");
        
        if (tvAddress != null) {
            tvAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() 
                    ? user.getAddress() : "Chưa cập nhật địa chỉ");
        }
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        
        TextInputLayout tilFullName = view.findViewById(R.id.tilFullName);
        TextInputLayout tilPhone = view.findViewById(R.id.tilPhone);
        
        TextInputEditText etFullName = view.findViewById(R.id.etFullName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etAddress = view.findViewById(R.id.etAddress);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        // Pre-fill data
        etFullName.setText(currentUser.getFullName());
        etPhone.setText(currentUser.getPhoneNumber());
        etAddress.setText(currentUser.getAddress());

        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            boolean isValid = true;

            // Validate Name
            if (TextUtils.isEmpty(name)) {
                tilFullName.setError("Họ tên không được để trống");
                isValid = false;
            } else {
                tilFullName.setError(null);
            }

            // Validate Phone (Vietnam format 10 digits)
            if (!phone.isEmpty() && !phone.matches("(0[3|5|7|8|9])([0-9]{8})")) {
                tilPhone.setError("Số điện thoại không hợp lệ (ví dụ: 0912345678)");
                isValid = false;
            } else {
                tilPhone.setError(null);
            }

            if (isValid) {
                updateProfile(name, phone, address, dialog);
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void updateProfile(String name, String phone, String address, BottomSheetDialog dialog) {
        currentUser.setFullName(name);
        currentUser.setPhoneNumber(phone);
        currentUser.setAddress(address);

        apiService.updateProfile(currentUser).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    sessionManager.saveUser(currentUser);
                    displayUserInfo(currentUser);
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        TextInputLayout tilOldPassword = view.findViewById(R.id.tilOldPassword);
        TextInputLayout tilNewPassword = view.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);

        TextInputEditText etOldPassword = view.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        MaterialButton btnSavePassword = view.findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String oldPass = etOldPassword.getText().toString();
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            boolean isValid = true;

            if (TextUtils.isEmpty(oldPass)) {
                tilOldPassword.setError("Vui lòng nhập mật khẩu cũ");
                isValid = false;
            } else {
                tilOldPassword.setError(null);
            }

            if (newPass.length() < 6) {
                tilNewPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
                isValid = false;
            } else {
                tilNewPassword.setError(null);
            }

            if (!newPass.equals(confirmPass)) {
                tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                isValid = false;
            } else {
                tilConfirmPassword.setError(null);
            }

            if (isValid) {
                ChangePasswordRequest request = new ChangePasswordRequest(oldPass, newPass, confirmPass);
                apiService.changePassword(request).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void logout() {
        sessionManager.logout();
        ApiClient.resetClient();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}