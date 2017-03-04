package gonggongjohn.tocker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

public class CountActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private boolean gotFaceCount = true;
    private boolean lastGotFace = true;
    private boolean isStop = false;

    private TextView countView;
    private int countsec = 0;
    private int countsectemp = 1500;
    private int countmin = 0;
    private int pause = 0;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    countsectemp--;
                    countmin = countsectemp / 60;
                    countsec = countsectemp % 60;
                    countView.setText(countmin + ":" + countsec);
                    break;
                }
                case 2:{
                    new AlertDialog.Builder(CountActivity.this)
                            .setTitle("您似乎不在学习状态")
                            .setMessage("点击继续番茄倒计时")
                            .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog,int which){
                                    isStop = false;
                                }
                            })
                            .show();
                    break;
                }
                default:{
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);
        countView = (TextView) findViewById(R.id.countView);
        initVIew();
        new Thread(new CountDownThread()).start();
    }

    public class CountDownThread implements Runnable {
        @Override
        public void run() {
            while (countsectemp >= 0) {
                try {
                    Thread.sleep(1000);
                    if (countsectemp % 10 == 0) {
                        // 获取手机方向
                        int rotation = getWindowManager().getDefaultDisplay().getRotation();
                        CameraProcess tp = new CameraProcess();
                        tp.takePicture(mCameraDevice, mImageReader, mCameraCaptureSession, childHandler, rotation, ORIENTATIONS);
                    }
                    if (countsectemp % 13 == 0) {
                        if ((!lastGotFace) && (!gotFaceCount)) isStop = true;
                        lastGotFace = gotFaceCount;
                    }
                    if (!isStop) {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                    //if ((pause != 0) && (pause % 2 == 0)){
                    //    Message message = new Message();
                    //    message.what = 2;
                    //    handler.sendMessage(message);
                    //}
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 初始化
     */
    private void initVIew() {
        //mSurfaceView
        mSurfaceView = (SurfaceView) findViewById(R.id.ca_surface_view);
        //mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView创建
                // 初始化Camera
                initCamera2();
            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView销毁
                // 释放Camera资源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    CountActivity.this.mCameraDevice = null;
                }
            }
        });

    }

    /**
     * 初始化Camera2
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        mCameraID = "" + CameraCharacteristics.LENS_FACING_BACK;//后摄像头
        mImageReader = ImageReader.newInstance(720, 1280, ImageFormat.JPEG,60);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    ImageFactory imf = new ImageFactory();
                    Bitmap tempbitmap = imf.compressBitmap(bitmap);
                    byte[] tempbytes = imf.BitmapToByteArray(tempbitmap);
                    FaceDetector d = new FaceDetector(CountActivity.this);
                    d.detectFace(tempbytes);
                }
            }
        }, mainHandler);
        //获取摄像头管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开摄像头
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            mCameraDevice = camera;
            //开启预览
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            if (null != mCameraDevice) {
                mCameraDevice.close();
                CountActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            Toast.makeText(CountActivity.this, "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CountActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

/*
    private void startTimer(){
        Runnable ru = new Runnable() {
            @Override
            public void run() {
                while(countsectemp >= 0){
                    try {
                        // 获取手机方向
                        int rotation = getWindowManager().getDefaultDisplay().getRotation();
                        CameraProcess tp = new CameraProcess();
                        tp.takePicture(mCameraDevice, mImageReader, mCameraCaptureSession, childHandler, rotation, ORIENTATIONS);
                        Thread.sleep(8000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(ru).start();
    }
*/

    public void gotFaceFail(){
        this.gotFaceCount = false;
    }

    public void gotFaceSuccess(String faceinfo){
        String filePath = "" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Tocktemp/";
        String fileName = "data.txt";
        ImageFactory printstr = new ImageFactory();
        printstr.saveString(faceinfo, filePath, fileName);
        this.gotFaceCount = true;
    }
}