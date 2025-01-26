package com.example.lioneats.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lioneats.R;

import androidx.annotation.NonNull;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.lioneats.activities.ImageResultActivity;
import com.example.lioneats.activities.LoginActivity;
import com.example.lioneats.activities.MainActivity;
import com.example.lioneats.activities.RegisterAccountActivity;
import com.example.lioneats.activities.UpdateUserActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HeaderFragment extends Fragment {

	private static final String TAG = "HeaderFragment";
	private static final int REQUEST_CAMERA_PERMISSION = 100;
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_PICK = 2;

	private Uri photoURI;
	private TextView usernameText;
	private TextView actionBtn;
	private ImageButton cameraBtn;
	private ImageView logoBtn;
	private SharedPreferences userSessionPreferences, userPreferences;
	private AlertDialog loginDialog;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_header_row, container, false);

		usernameText = view.findViewById(R.id.usernameText);
		actionBtn = view.findViewById(R.id.actionBtn);
		cameraBtn = view.findViewById(R.id.cameraBtn);
		logoBtn = view.findViewById(R.id.logoBtn);

		userPreferences = getActivity().getSharedPreferences("user", getActivity().MODE_PRIVATE);
		userSessionPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
		String username = userSessionPreferences.getString("username", null);

		setupUserSpecificUI(username);

		logoBtn.setClickable(true);
		logoBtn.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
			getActivity().finish();
		});

		return view;
	}

	private void setupUserSpecificUI(String username) {
		if (username != null) {
			configureLoggedInUser(username);
		} else {
			configureGuestUser();
		}
	}

	private void configureLoggedInUser(String username) {
		usernameText.setText(username);
		usernameText.setClickable(true);
		usernameText.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
			startActivity(intent);
			getActivity().finish();
		});
		actionBtn.setText("Logout");
		actionBtn.setVisibility(View.VISIBLE);
		actionBtn.setOnClickListener(v -> {
			logout();
		});
		cameraBtn.setOnClickListener(v -> showImageSourceDialog());
	}

	private void configureGuestUser() {
		usernameText.setText("Guest");
		actionBtn.setText("Login");
		actionBtn.setVisibility(View.VISIBLE);
		actionBtn.setOnClickListener(v -> {
			startActivity(new Intent(getActivity(), LoginActivity.class));
			getActivity().finish();
		});
		cameraBtn.setOnClickListener(v -> {
			showLoginDialog();
		});
	}

	private void logout() {
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.clear();
		sessionEditor.apply();

		SharedPreferences.Editor userEditor = userPreferences.edit();
		userEditor.clear();
		userEditor.apply();

		Toast.makeText(getActivity(), "Successfully logged out", Toast.LENGTH_SHORT).show();

		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		getActivity().finish();
	}

	private void showLoginDialog() {
		if (loginDialog == null) {
			loginDialog = createLoginDialog();
		}
		loginDialog.show();
	}

	private AlertDialog createLoginDialog() {
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_login, null);
		Button positiveButton = dialogView.findViewById(R.id.positiveButton);
		Button negativeButton = dialogView.findViewById(R.id.negativeButton);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(dialogView).setCancelable(true);
		AlertDialog dialog = builder.create();

		positiveButton.setOnClickListener(v -> {
			startActivity(new Intent(getActivity(), LoginActivity.class));
			dialog.dismiss();
			getActivity().finish();
		});

		negativeButton.setOnClickListener(v -> {
			startActivity(new Intent(getActivity(), RegisterAccountActivity.class));
			dialog.dismiss();
			getActivity().finish();
		});

		return dialog;
	}

	private void showImageSourceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Choose Image Source")
				.setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
					if (which == 0) {
						if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
							requestCameraPermission();
						} else {
							dispatchTakePictureIntent();
						}
					} else {
						dispatchPickPictureIntent();
					}
				});
		builder.create().show();
	}

	private void requestCameraPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
			new AlertDialog.Builder(getActivity())
					.setTitle("Camera Permission Needed")
					.setMessage("This app requires camera access to take pictures.")
					.setPositiveButton("OK", (dialog, which) ->
							ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION))
					.create()
					.show();
		} else {
			ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
		}
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				Log.e(TAG, "IOException occurred while creating the file", ex);
			} catch (Exception ex) {
				Log.e(TAG, "Unexpected error occurred while creating the file", ex);
			}
			if (photoFile != null) {
				photoURI = FileProvider.getUriForFile(getActivity(), "com.example.lioneats.provider", photoFile);
				Log.d(TAG, "Photo URI: " + photoURI.toString());
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	private void dispatchPickPictureIntent() {
		Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		if (pickPictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK);
		}
	}

	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return File.createTempFile(imageFileName, ".jpg", storageDir);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CAMERA_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				showImageSourceDialog();
			} else {
				Toast.makeText(getActivity(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
			Log.d(TAG, "Image capture successful. URI: " + photoURI.toString());
			navigateToImageResultActivity(photoURI);
		} else if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK) {
			if (data != null) {
				photoURI = data.getData();
				Log.d(TAG, "Image selected from gallery. URI: " + photoURI.toString());
				navigateToImageResultActivity(photoURI);
			}
		}
	}

	private void navigateToImageResultActivity(Uri imageUri) {
		Intent intent = new Intent(getActivity(), ImageResultActivity.class);
		intent.putExtra("imageUri", imageUri.toString());
		startActivity(intent);
		getActivity().finish();
	}
}