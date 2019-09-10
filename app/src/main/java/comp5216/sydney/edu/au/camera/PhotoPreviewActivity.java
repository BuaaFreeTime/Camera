package comp5216.sydney.edu.au.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class PhotoPreviewActivity extends Activity {

    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_preview);

        ImageView imageView = (ImageView) findViewById(R.id.photopreview);
        path = getIntent().getStringExtra("path");
        imageView.setImageURI(Uri.fromFile(new File(path)));
    }

    public void onSubmit(View view) {
        Intent intent = new Intent();

        intent.putExtra("path", path);

        setResult(RESULT_OK, intent);

        finish();
    }
}
