package net.the_engineers.reading_lens;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnLayoutChangeListener {

    SurfaceView _image;

    Camera _camera;
    private SurfaceHolder _surfaceHolder;
    private int _height;
    private int _width;
    private boolean _cameraRunning;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getActionBar().hide();

        _image = (SurfaceView)findViewById(R.id.image);
        _image.addOnLayoutChangeListener(this);
        _surfaceHolder =  _image.getHolder();
        _surfaceHolder.addCallback(this);
    }

    private int GetCameraIndex()
    {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++)
        {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(i, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                return i;
            }
        }

        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        StartCameraPreview();
    }

    private void StartCameraPreview() {

        if (_width <= 0 && _height <= 0)
            return;

        if (_cameraRunning)
            return;

        _cameraRunning = true;
        int cameraIndex = GetCameraIndex();

        _camera = Camera.open(cameraIndex);
        Camera.Parameters parameters = _camera.getParameters();

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraIndex, info);

        parameters.setRotation(info.orientation + 90);
        //List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        //parameters.setPreviewSize(_width, _height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        _camera.setParameters(parameters);
        _camera.setDisplayOrientation(90);
    }

    @Override
    protected void onPause() {
        super.onPause();

        _camera.release();
        _cameraRunning = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (_camera == null)
            return;

        try {
            _camera.setPreviewDisplay(holder);
            _camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        _width = right - left;
        _height = bottom - top;

        StartCameraPreview();
    }
}
