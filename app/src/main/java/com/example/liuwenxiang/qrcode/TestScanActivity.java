package com.example.liuwenxiang.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.paic.hyperion.core.hfqrcode.HFQRCode;
import com.paic.hyperion.core.hfqrcode.HFQRCodeConfig;
import com.paic.hyperion.core.hfqrcode.HFQRCodeDelegate;
import com.paic.hyperion.core.hfqrcode.HFQRCodeView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class TestScanActivity extends Activity implements HFQRCodeDelegate {

    private static final String TAG = TestScanActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 100;

    private String photoPath;

    private HFQRCodeView mQRCodeView;

    //调用系统相册-选择图片
    private static final int IMAGE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_test_scan);

        mQRCodeView = (HFQRCodeView) findViewById(R.id.zxingview);

        HFQRCodeConfig config = new HFQRCodeConfig(this);
        // 除去扫描框，其余部分阴影颜色
        config.mMaskColor = Color.parseColor("#BCFFFFFF");
        // 扫描框边角线的颜色
        config.mCornerColor = getResources().getColor(R.color.colorPrimary);
        // 扫描边框的颜色
        config.mBorderColor = Color.TRANSPARENT;

//        config.mCustomGridScanLineDrawable = getResources().getDrawable(R.mipmap.custom_grid_scan_line);
//        config.mCustomScanLineDrawable = getResources().getDrawable(R.mipmap.custom_scan_line);
        config.mScanLineColor = getResources().getColor(R.color.colorPrimary);

        // 扫描框距离 toolbar 底部的距离，没有 toolbar 的话就是顶部的间距
        config.mTopOffset = dp2px(this, 100);

        // Toolbar的高度，当有设置 qrcv_isCenterVertical 属性时
        // 通过该属性来修正有 Toolbar 时导致扫描框垂直居中的偏差，默认值为0dp
//        config.mToolbarHeight = dp2px(this, 50);
        // 扫描框是否垂直居中，该属性为 true 时会忽略 mTopOffset 属性，默认值为 false
//        config.mIsCenterVertical = true;
        config.mTipTextMargin =dp2px(this, 30);

        config.mQRCodeTipText = "放入框内，自动扫描";
        config.mTipTextColor = getResources().getColor(R.color.colorPrimaryDark);
        mQRCodeView.setCustomConfig(config);
        mQRCodeView.setDelegate(this);
        mQRCodeView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mQRCodeView.setVisibility(View.VISIBLE);
            }
        }, 800);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mQRCodeView.startCamera();
        mQRCodeView.startSpot();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        mQRCodeView.stopSpot();
        mQRCodeView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    public void onClick(View v) {

        if (v.getId() == R.id.my_qrcode) {
//            startActivity(new Intent(this, TestGeneratectivity.class));
        } else if (v.getId() == R.id.choose_qrcode_pic) {
            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
            innerIntent.setType("image/*");
            Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
            startActivityForResult(wrapperIntent, IMAGE);
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }

        return result;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE:
                    // 获取选中图片的路径
                    Uri uri = data.getData();
                    photoPath = getRealPathFromURI(uri);
                    Log.d(TAG, "photoPath----------:" + photoPath);
                    // 同步解析本地图片二维码，耗时操作，请在子线程中调用。
                    String result = HFQRCode.syncDecodeQRCode(getSmallBitmap(photoPath));
                    Toast.makeText(TestScanActivity.this, result, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

}