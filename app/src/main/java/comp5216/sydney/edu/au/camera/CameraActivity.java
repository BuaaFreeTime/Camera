/*
 * Copyright 2019 by BuaaFreeTime
 */

package comp5216.sydney.edu.au.camera;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class CameraActivity extends Activity {
    // camera view activity
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    private Camera camera = null;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_view);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        surfaceHolder = surfaceView.getHolder();

        // Add a callback lister to the surface holder(including open and stop camera)
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // open camera
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // stop camera
                stopCamera();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    // capture button
    public void capture(View view) {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                String path;
                path = saveFile(bytes);
                if (path != null) {
                    Intent intent = new Intent(CameraActivity.this,
                            PhotoPreviewActivity.class);
                    if (intent != null) {
                        intent.putExtra("path", path);
                        startActivityForResult(intent, 1);
                    }
                } else {
                    Toast.makeText(CameraActivity.this, "Picture wasn't taken!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // open camera function
    private void openCamera() {
        // a method of open camera
        camera = Camera.open();
        // let camera turn 90°
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(surfaceHolder);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("Camera error", "Camera set preview display error");
        }
        camera.startPreview();
    }

    // stop camera function
    private void stopCamera() {
        // a method of stop camera
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    // save file locally
    private String saveFile(byte[] bytes) {
        // a method of save photo file
        if (!marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForExternalStorage();
            return null;
        } else {
            String fileName;
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            fileName = "IMG_" + timeStamp + ".jpg";
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "/images/" + fileName);
            
            // Create the storage directory if it does not exist
            if (!file.getParentFile().exists() &&
                    !file.getParentFile().mkdirs()) {
                Log.e("write photo", "failed to create directory");
            }

            // let the photo turn back 90° because when we capture the photo already turn 90°
            Bitmap photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(),
                    matrix, true);
            // save data locally
            try {
                FileOutputStream fos = new FileOutputStream(file);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                //fos.write(photo);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("write photo", "Write Photo error");
            }
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("write photo", "File not found");
            }
            return file.getAbsolutePath();
        }
    }


}
