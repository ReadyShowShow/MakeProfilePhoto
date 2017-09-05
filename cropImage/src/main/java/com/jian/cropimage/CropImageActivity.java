/*
 * Copyright (c) 2017. The ReadyShowShow@gmail Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jian.cropimage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import java.io.File;

/**
 * 图片剪裁界面
 * 会运行在独立的进程中，以保证主进程不会OOM
 * 通过Intent进行数据交互，剪裁结束后返回图片的uri
 */
@SuppressWarnings("WrongActivitySuperClass")
public class CropImageActivity extends Activity {
    private static final String ORIGINAL_FILE_PATH = "path";
    public static final String RESULT_PATH = "crop_image_path";
    public CropZoomImageView mClipImageLayout;

    public static void start(Activity activity, String path, int requestCode) {
        Intent intent = new Intent(activity, CropImageActivity.class);
        intent.putExtra(ORIGINAL_FILE_PATH, path);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_activity_crop_image);
    }

//    private void findViews() {
//        mClipImageLayout = (CropZoomImageView) findViewById(R.id.clipImageLayout);
//
//        View ok = findViewById(R.id.okBtn);
//        ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onOKClick();
//            }
//        });
//
//        View close = findViewById(R.id.close_icon);
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finishWithAnimation();
//            }
//
//        });
//    }

//    private void initView() {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Window window = getWindow();
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                window.setStatusBarColor(0xff000000);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String path = getIntent().getStringExtra(ORIGINAL_FILE_PATH);
//        Glide.with(this).load(path).dontAnimate().into(mClipImageLayout);
//    }


    public void onOKClick() {
        File file = FileUtils.getOutputMediaFileUri();
        boolean success = mClipImageLayout.clipToFile(file);
        if (success) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_PATH, file.getAbsolutePath());
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finishWithAnimation();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(0, R.anim.crop_image_push_out_down);
    }
//    private void initView() {
//        // 有的系统返回的图片是旋转了，有的没有旋转，所以处理
//        int degreee = readBitmapDegree(path);
//        Bitmap bitmap = createBitmap(path);
//        if (bitmap != null) {
//            if (degreee == 0) {
//                mClipImageLayout.setImageBitmap(bitmap);
//            } else {
//                mClipImageLayout.setImageBitmap(rotateBitmap(degreee, bitmap));
//            }
//        } else {
//            finish();
//        }
//    }
//
//    private void saveBitmap(Bitmap bitmap, String path) {
//        File f = new File(path);
//        if (f.exists()) {
//            f.delete();
//        }
//
//        FileOutputStream fOut = null;
//        try {
//            f.createNewFile();
//            fOut = new FileOutputStream(f);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
//            fOut.flush();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } finally {
//            try {
//                if (fOut != null)
//                    fOut.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private Bitmap createBitmap(String path) {
//        if (path == null) {
//            return null;
//        }
//
//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inSampleSize = 2;
//        opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
//        opts.inPurgeable = true;
//        opts.inInputShareable = true;
//        opts.inDither = false;
//        opts.inPurgeable = true;
//        FileInputStream is = null;
//        Bitmap bitmap = null;
//        try {
//            is = new FileInputStream(path);
//            bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return bitmap;
//    }
//
//    private int readBitmapDegree(String path) {
//        int degree = 0;
//        try {
//            ExifInterface exifInterface = new ExifInterface(path);
//            int orientation = exifInterface.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return degree;
//    }
//
//    // 旋转图片
//    private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
//        // 旋转图片 动作
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        // 创建新的图片
//        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//        return resizedBitmap;
//    }
}
