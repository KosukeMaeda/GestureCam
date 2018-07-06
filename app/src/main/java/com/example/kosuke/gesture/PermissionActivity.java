package com.example.kosuke.gesture;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PermissionActivity.class.getSimpleName();
    static final int REQUEST_CODE_PERMISSION = 100;

    ArrayList<String> permissionsForRequest = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.CAMERA);
        }
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        ActivityCompat.requestPermissions(this, permissionsForRequest.toArray(new String[0]), REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResult) {
        Intent intent = new Intent();
        if (requestCode == REQUEST_CODE_PERMISSION) {
            List<String> permissionList = Arrays.asList(permissions);
            for (String permission : permissions) {
                int index = permissionList.indexOf(permission);
                if (index < 0 || grantResult[index] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Denied permission: " + permission);
                    Toast.makeText(this, "許可されないとアプリを使用できません", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }

        intent.putExtra("resutl", true);
        setResult(RESULT_OK);
        finish();
    }
}
