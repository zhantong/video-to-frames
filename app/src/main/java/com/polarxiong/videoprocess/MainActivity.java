package com.polarxiong.videoprocess;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.polarxiong.videotoframes.VideoToFrames;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_GET_FILE_PATH = 1;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonFilePathInput = findViewById(R.id.button_file_path_input);
        buttonFilePathInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
                } else {
                    getFilePath(REQUEST_CODE_GET_FILE_PATH);
                }
            }
        });

        final Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextInputFilePath = findViewById(R.id.file_path_input);
                String inputFilePath = editTextInputFilePath.getText().toString();
                VideoToFrames videoToFrames = new VideoToFrames(inputFilePath);
                VideoToFrames.Callback callback = new VideoToFrames.Callback() {
                    @Override
                    public void onDecodeFrame(byte[] frame) {
                        System.out.println("test");
                    }

                    @Override
                    public void onFinishDecode() {
                        System.out.println("finish");
                    }
                };
                videoToFrames.setCallback(callback);
                try {
                    videoToFrames.start();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFilePath(REQUEST_CODE_GET_FILE_PATH);
                } else {
                    Toast.makeText(this, "需要开启文件读写权限", Toast.LENGTH_SHORT).show();
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int id = 0;
        switch (requestCode) {
            case REQUEST_CODE_GET_FILE_PATH:
                id = R.id.file_path_input;
                break;
        }
        if (resultCode == Activity.RESULT_OK) {
            EditText editText = findViewById(id);
            String curFileName = getRealPathFromURI(data.getData());
            editText.setText(curFileName);
        }
    }

    private void getFilePath(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "选择视频文件"), requestCode);
        } else {
            new AlertDialog.Builder(this).setTitle("未找到文件管理器")
                    .setMessage("请安装文件管理器以选择文件")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentURI, proj, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
