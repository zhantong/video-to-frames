package com.polarxiong.videoprocess;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.polarxiong.videotoframes.VideoToFrames;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends Activity {
    private static final int REQUEST_CODE_GET_FILE_PATH = 1;

    private static final int WHAT_DEBUG_OUTPUT = 1;

    private TextView mTextViewDebugOutput;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DEBUG_OUTPUT:
                    mTextViewDebugOutput.setText((String) msg.obj);
                    break;
            }
        }
    };
    private VideoToFrames.Callback mCallbackSaveFrames = new VideoToFrames.Callback() {
        @Override
        public void onDecodeFrame(int index, Image image) {
            mHandler.sendMessage(mHandler.obtainMessage(WHAT_DEBUG_OUTPUT, String.format("保存第%d帧...", index)));
        }

        @Override
        public void onFinishDecode() {
            mHandler.sendMessage(mHandler.obtainMessage(WHAT_DEBUG_OUTPUT, "完成！"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewDebugOutput = findViewById(R.id.debug_output);

        final Button buttonFilePathInput = findViewById(R.id.button_file_path_input);
        buttonFilePathInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getFilePath(REQUEST_CODE_GET_FILE_PATH);
                MainActivityPermissionsDispatcher.getFilePathWithPermissionCheck(MainActivity.this, REQUEST_CODE_GET_FILE_PATH);
            }
        });
        Button buttonSaveNv21 = findViewById(R.id.button_save_nv21);
        buttonSaveNv21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNv21();
            }
        });
        Button buttonSaveI420 = findViewById(R.id.button_save_i420);
        buttonSaveI420.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveI420();
            }
        });
        Button buttonSaveJpeg = findViewById(R.id.button_save_jpeg);
        buttonSaveJpeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveJpeg();
            }
        });
    }

    void saveNv21() {
        String inputFilePath = getInputFilePath();
        String outputDirectory = getOutputDirectory();
        VideoToFrames videoToFrames = new VideoToFrames(inputFilePath);
        videoToFrames.saveNv21Frames(outputDirectory);
        videoToFrames.addCallback(mCallbackSaveFrames);
        try {
            videoToFrames.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    void saveI420() {
        String inputFilePath = getInputFilePath();
        String outputDirectory = getOutputDirectory();
        VideoToFrames videoToFrames = new VideoToFrames(inputFilePath);
        videoToFrames.saveI420Frames(outputDirectory);
        videoToFrames.addCallback(mCallbackSaveFrames);
        try {
            videoToFrames.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    void saveJpeg() {
        String inputFilePath = getInputFilePath();
        String outputDirectory = getOutputDirectory();
        VideoToFrames videoToFrames = new VideoToFrames(inputFilePath);
        videoToFrames.saveJpegFrames(outputDirectory);
        videoToFrames.addCallback(mCallbackSaveFrames);
        try {
            videoToFrames.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
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

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void getFilePath(int requestCode) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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

    private String getInputFilePath() {
        EditText editTextInputFilePath = findViewById(R.id.file_path_input);
        return editTextInputFilePath.getText().toString();
    }

    private String getOutputDirectory() {
        EditText editTextOutputDirectory = findViewById(R.id.output_directory);
        return new File(Environment.getExternalStorageDirectory(), editTextOutputDirectory.getText().toString()).getAbsolutePath();
    }
}
