
package com.faceplusplus.apitest;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.faceplusplus.api.FaceDetecter;
import com.faceplusplus.api.FaceDetecter.Face;
import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.megvii.apitest.R;

import org.json.JSONObject;

public class MainActivity extends Activity {

    private Bitmap curBitmap;
    private final static int REQUEST_GET_PHOTO = 1;
    ImageView imageView = null;
    Bitmap bitmap=null;

    HandlerThread detectThread = null;
    Handler detectHandler = null;
    Button button = null;
    FaceDetecter detecter = null;
    HttpRequests request = null;// 在线api

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detectThread = new HandlerThread("detect");
        detectThread.start();
        detectHandler = new Handler(detectThread.getLooper());
        Log.d("activity", "onCreate");
        imageView = (ImageView) findViewById(R.id.imageview);
        curBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo);
        imageView.setImageBitmap(curBitmap);
        detecter = new FaceDetecter();
        detecter.init(this, "35a467be6126eda75a31818ddd9e483e");

        
        //FIXME 替换成申请的key
        request = new HttpRequests("35a467be6126eda75a31818ddd9e483e",
                "BRaGt8folSXNK6htil3410nMyejYkRyM");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detecter.release(this);// 释放引擎
    }

    public static Bitmap getFaceInfoBitmap(Face[] faceinfos,
            Bitmap oribitmap) {
        Bitmap tmp;
        tmp = oribitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas localCanvas = new Canvas(tmp);
        Paint localPaint = new Paint();
        localPaint.setColor(0xffff0000);
        localPaint.setStyle(Paint.Style.STROKE);
        for (Face localFaceInfo : faceinfos) {
            RectF rect = new RectF(oribitmap.getWidth() * localFaceInfo.left, oribitmap.getHeight()
                    * localFaceInfo.top, oribitmap.getWidth() * localFaceInfo.right,
                    oribitmap.getHeight()
                            * localFaceInfo.bottom);
            localCanvas.drawRect(rect, localPaint);
        }
        return tmp;
    }

//    public static Bitmap getScaledBitmap(String fileName, int dstWidth)
//    {
//        BitmapFactory.Options localOptions = new BitmapFactory.Options();
//        localOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(fileName, localOptions);
//        int originWidth = localOptions.outWidth;
//        int originHeight = localOptions.outHeight;
//
//        localOptions.inSampleSize = originWidth > originHeight ? originWidth / dstWidth
//                : originHeight / dstWidth;
//        localOptions.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(fileName, localOptions);
//    }

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.pick:
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_GET_PHOTO);

                break;
            case R.id.detect:
                detectHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        Log.d("检测开始","1");
                        Face[] faceinfo = detecter.findFaces(bitmap);// 进行人脸检测

                        if (faceinfo == null)
                        {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "未发现人脸信息", Toast.LENGTH_LONG)
                                            .show();
                                    Log.d("检测情况","未发现人脸");
                                }
                            });
                            return;
                        }

                        final Bitmap bit = getFaceInfoBitmap(faceinfo, bitmap);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                imageView.setImageBitmap(bit);
                                System.gc();
                            }
                        });
                        //在线api交互
                        try {

                           JSONObject x=request.offlineDetect(detecter.getImageByteArray(),detecter.getResultJsonString(), new PostParameters());
                            Log.d("在线获取到数据", x.toString());

                        } catch (FaceppParseException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }

                    }
                });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_PHOTO: {
                    if (data != null) {
                        Uri localUri = data.getData();
                        String imagePath=null;
                        if (DocumentsContract.isDocumentUri(this, localUri)) {
                            Log.d("activity", "1");
                            String docId = DocumentsContract.getDocumentId(localUri);
                            if ("com.android.providers.media.documents".equals(localUri.getAuthority())) {
                                Log.d("activity", "2");
                                String id = docId.split(":")[1];
                                String selection = MediaStore.Images.Media._ID + "=" + id;
                                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);

                            } else if ("com.android.providers.downloads.documents".equals(localUri.getAuthority())) {
                                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://" +
                                        "downloads/public_downloads"), Long.valueOf(docId));
                                imagePath = getImagePath(contentUri, null);
                                Log.d("activity", "3");
                            }
                        }else if ("content".equalsIgnoreCase(localUri.getScheme())) {
                            Log.d("activity", localUri+"");
                            imagePath = getImagePath(localUri, null);


                        }
                        displayImage(imagePath);
                        Log.d("activity", "5");
                    }
                    break;
                }
            }

        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path=null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        Log.d("activity", "6");
        if (cursor != null) {
            Log.d("activity", "7");
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            Log.d("activity", "8");
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
              bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        }
        else {
            Toast.makeText(this, "加载失败了",Toast.LENGTH_SHORT).show();
        }
    }
}
