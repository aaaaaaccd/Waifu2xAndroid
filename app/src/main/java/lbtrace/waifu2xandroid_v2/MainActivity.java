package lbtrace.waifu2xandroid_v2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static lbtrace.waifu2xandroid_v2.R.*;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE_CODE = 1;
    private ImageView mImageView;
    private Button mPickBtn;
    private Button mProcessBtn;
    private Bitmap oriBitmap;
    private Button mOutputBtn;
    private ProgressBar mScaleProgressBar;

    private boolean mIsScale;
    public void saveJPG_After(Bitmap bitmap){
        mPickBtn.setClickable(true);
        FileOutputStream out = null;
        String Path = Environment.getExternalStorageDirectory().getPath()+"/pictures/Waifu2X/%s.png";
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        Path = String.format(Path,str);

        try {
            //先创建目录
            File dirr = new File(Environment.getExternalStorageDirectory().getPath()+"/pictures/Waifu2X/");
            if (!dirr.exists())
				if(!dirr.mkdirs()){
					Toast ts = Toast.makeText(getApplicationContext(),"存储目录创建失败!", Toast.LENGTH_LONG);
					ts.show();
					return;
				}
            }
            //再生成文件
            File file = new File(Path);
            if (!file.exists()) {
                if (!file.createNewFile()){
                    Toast ts = Toast.makeText(getApplicationContext(),"存储文件创建失败!", Toast.LENGTH_LONG);
                    ts.show();
					return;
                }
            }
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
                Toast ts = Toast.makeText(getApplicationContext(),"成功保存到 "+Path, Toast.LENGTH_LONG);
                ts.show();
            }else{
                Toast ts = Toast.makeText(getApplicationContext(),"保存失败,图片处理错误", Toast.LENGTH_LONG);
                ts.show();
			}
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        mImageView = (ImageView) findViewById(id.imageView2);
        mPickBtn = (Button) findViewById(id.pick_btn);
        mProcessBtn = (Button)findViewById(id.output_button);
        mOutputBtn = (Button)findViewById(id.output_btn);
        mScaleProgressBar = (ProgressBar) findViewById(id.scale_process_pb);
        mScaleProgressBar.setVisibility(View.GONE);
        mProcessBtn.setEnabled(false);
        mOutputBtn.setEnabled(false);
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
                Toast ts = Toast.makeText(getApplicationContext(),"正在处理,这可能需要一段时间......", Toast.LENGTH_LONG);
                ts.show();
                new ImageScaleTask(getAssets(), new ViewExecuteCallback()).execute(oriBitmap);
            }
        });
        mOutputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveJPG_After(oriBitmap);
            }
        });

        //请求存储权限 (Android 6.0后需要动态申请)
        isGrantExternalRW(this,1);
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean isGrantExternalRW(Activity activity, int requestCode) {

        int storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //检测是否有权限，如果没有权限，就需要申请
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            activity.requestPermissions(PERMISSIONS_STORAGE, requestCode);
            //返回false。说明没有授权
            return false;
        }
        //说明已经授权
        return true;
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
                        mProcessBtn.setEnabled(true);
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
            mOutputBtn.setEnabled(true);
        }
        }
    }

