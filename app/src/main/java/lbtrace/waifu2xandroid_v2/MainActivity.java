package lbtrace.waifu2xandroid_v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import lbtrace.imageutils.Image;

import static lbtrace.waifu2xandroid_v2.R.*;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE_CODE = 1;
    private ImageView mImageView;
    private Button mPickBtn;
    private Button mProcessBtn;
    private Bitmap oriBitmap;
    private ProgressBar mScaleProgressBar;

    private boolean mIsScale;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        mImageView = (ImageView) findViewById(id.imageView2);
        mPickBtn = (Button) findViewById(id.pick_btn);
        mProcessBtn = (Button)findViewById(id.process_btn);
        mScaleProgressBar = (ProgressBar) findViewById(id.scale_process_pb);
        mScaleProgressBar.setVisibility(View.GONE);

        mPickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_CODE);
            }
        });

        mProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "ori Bitmap size: (" + oriBitmap.getHeight() + "," + oriBitmap.getWidth() + ")");
                new ImageScaleTask(getAssets(), new ViewExecuteCallback()).execute(oriBitmap);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_IMAGE_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        oriBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        mImageView.setImageBitmap(oriBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    class ViewExecuteCallback implements ImageScaleTask.ExecuteCallback {
        @Override
        public void onPreExecute() {
            mPickBtn.setClickable(false);
            mImageView.setVisibility(View.GONE);
            mScaleProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
            oriBitmap = bitmap;
            mPickBtn.setClickable(true);
            mScaleProgressBar.setVisibility(View.GONE);
        }
    }
}
