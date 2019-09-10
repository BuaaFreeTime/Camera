package comp5216.sydney.edu.au.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public final String APP_TAG = "Camera";
    public String photoFileName = "photo.jpg";
    public String videoFileName = "video.mp4";
    public String audioFileName = "audio.3gp";
    //request codes
    public final int MY_PERMISSIONS_REQUEST_OPEN_CAMERA = 101;
    public final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
    public final int EDIT_ITEM_REQUEST_CODE = 647;
    public final int TAKE_PHOTO_CODE = 648;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    private File file;
    private ArrayList<ImageInfo> imageList;
    private GridViewAdapter gridViewAdapter;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = (GridView)findViewById(R.id.gridview);
        readImage();
        sortByDate();
        for (ImageInfo i : imageList) {
            System.out.println(i.getDate());
        }
        gridViewAdapter = new GridViewAdapter(this, imageList);
        gridView.setAdapter(gridViewAdapter);

        setupGridViewListener();
    }




    public void readImage() {
        if (!marshmallowPermission.checkPermissionForReadfiles()) {
            marshmallowPermission.requestPermissionForReadfiles();
        } else {
            imageList = new ArrayList<ImageInfo>();
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            try {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    ImageInfo imageInfo = new ImageInfo(path);
                    imageList.add(imageInfo);
                }
            } catch (Exception ex) {
                Log.e("read photo error", ex.getStackTrace().toString());
            }
        }

    }


    private void setupGridViewListener() {

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String photoPath = gridViewAdapter.getItemPath(position);
                Intent intent = new Intent(MainActivity.this, EditImageActivity.class);

                if (intent != null) {
                    intent.putExtra("path", photoPath);

                    startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE);
                }
            }
        });
    }

    // Returns the Uri for a photo/media stored on disk given the fileName and type
    public Uri getFileUri(String fileName, int type) {
        Uri fileUri = null;
        try {
            String typestr = "/images/";

            // Get safe storage directory depending on type
            File mediaStorageDir = new
                    File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    typestr + fileName);
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.getParentFile().exists() &&
                    !mediaStorageDir.getParentFile().mkdirs()) {
                Log.d(APP_TAG, "failed to create directory");
            }
            // Create the file target for the media based on filename
            file = new File(mediaStorageDir.getParentFile().getPath() + File.separator +
                    fileName);
            // Wrap File object into a content provider, required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-withIntents#sharing-files-with-api-24-or-higher
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(
                        this.getApplicationContext(),
                        "comp5216.sydney.edu.au.camera.fileProvider", file);
            } else {
                fileUri = Uri.fromFile(mediaStorageDir);
            }
        } catch (Exception ex) {
            Log.d("getFileUri", ex.getStackTrace().toString());
        }
        return fileUri;
    }


    public void onTakePhotoClick(View v) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()
                || !marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to take a picture and return control to the calling application

            Intent intent = new Intent(MainActivity.this, CameraActivity.class);

            startActivityForResult(intent,TAKE_PHOTO_CODE);

            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            /*
            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            photoFileName = "IMG_" + timeStamp + ".jpg";
            // Create a photo file reference
            Uri file_uri = getFileUri(photoFileName, 0);
            // Add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OPEN_CAMERA);
            }*/
        }
    }

    public void onLoadPhotoClick(View view) {
        if (!marshmallowPermission.checkPermissionForReadfiles()) {
            marshmallowPermission.requestPermissionForReadfiles();
        } else {
            // Create intent for picking a photo from the gallery
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Bring up gallery to select a photo
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_PHOTOS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EDIT_ITEM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ImageInfo imageInfo = new ImageInfo(data.getStringExtra("path"));
                imageList.add(imageInfo);
                sortByDate();
                gridViewAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == TAKE_PHOTO_CODE) {
            ImageInfo imageInfo = new ImageInfo(data.getStringExtra("path"));
            imageList.add(imageInfo);
            sortByDate();
            gridViewAdapter.notifyDataSetChanged();
        }
        /*
        ImageView ivPreview = (ImageView) findViewById(R.id.photopreview);
       // mVideoView.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        if (requestCode == MY_PERMISSIONS_REQUEST_OPEN_CAMERA) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(file.getAbsolutePath());
                // Load the taken image into a preview
                ivPreview.setImageBitmap(takenImage);
                ivPreview.setVisibility(View.VISIBLE);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken AAA!",
                        Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHOTOS) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                Bitmap selectedImage;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(), photoUri);
                    ivPreview.setImageBitmap(selectedImage);
                    ivPreview.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_VIDEOS) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(videoUri);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri takenVideoUri = getFileUri(videoFileName, 1);
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(takenVideoUri);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            }
        }*/
    }

    public void sortByDate() {
        // A method to sort items by date
        Collections.sort(imageList, new Comparator<ImageInfo>() {

            @Override
            public int compare(ImageInfo o1, ImageInfo o2) {
                boolean flag = o1.getDate().before(o2.getDate());
                if (flag) return 1;
                else return -1;
            }
        });
    }

}
