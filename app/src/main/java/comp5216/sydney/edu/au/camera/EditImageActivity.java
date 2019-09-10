package comp5216.sydney.edu.au.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EditImageActivity extends Activity {

    ImageView editImageView;
    int bitmapNumber = -1;
    ArrayList<Bitmap> bitmaps;
    Uri deleteUri;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_view);

        editImageView = (ImageView) findViewById(R.id.editimagevierw);

        String editImagePath = getIntent().getStringExtra("path");

        Bitmap editImage = BitmapFactory.decodeFile(editImagePath);

        bitmaps = new ArrayList<Bitmap>();
        renewImageView(editImage);
    }

    public void onSubmit(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(EditImageActivity.this);
        builder.setTitle(R.string.dialog_save_title)
                .setMessage(R.string.dialog_save_msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel what have done
                        // set result code and bundle data for response
                        String path = saveFile(bitmaps.get(bitmapNumber));
                        Intent intent = new Intent();
                        intent.putExtra("path", path);
                        setResult(RESULT_OK, intent);
                        finish(); // closes the activity, pass data to parent
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User cancelled the dialog
                        setResult(RESULT_CANCELED, getIntent());
                        finish(); // closes the activity, pass data to parent
                    }
                });
        builder.create().show();

    }

    private void renewImageView(Bitmap bitmap) {
        bitmapNumber++;
        bitmaps.add(bitmap);
        editImageView.setImageBitmap(bitmap);
        editImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap cropBitmap = data.getExtras().getParcelable("data");
        renewImageView(cropBitmap);
        getContentResolver().delete(deleteUri, null, null);
    }

    public void onClick(View view) {

        // rotate function
        if (view.getId() == R.id.rotate) {
            Bitmap rotateBitmap;
            Bitmap originalBitmap = bitmaps.get(bitmapNumber);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotateBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(),
                    matrix, true);

            renewImageView(rotateBitmap);
        }

        if (view.getId() == R.id.crop) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            deleteUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmaps.get(bitmapNumber), null,null));
            intent.setDataAndType(deleteUri, "image/*");
            intent.putExtra("return-data", true);
            startActivityForResult(intent, 1);
        }

        // undo function
        if ((view.getId() == R.id.undo) && (bitmapNumber > 0)) {
            Bitmap undoBitmap;
            undoBitmap = bitmaps.get(bitmapNumber-1);
            editImageView.setImageBitmap(undoBitmap);
            editImageView.setVisibility(View.VISIBLE);
            bitmaps.remove(bitmapNumber);
            bitmapNumber--;
        }
    }

    private String saveFile(Bitmap saveBitmap) {
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
                Log.e("write edit photo", "failed to create directory");
            }

            // save data locally
            try {
                FileOutputStream fos = new FileOutputStream(file);
                saveBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                //fos.write(photo);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("write edit photo", "Write Photo error");
            }
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("write edit photo", "File not found");
            }
            return file.getAbsolutePath();
        }
    }

}
