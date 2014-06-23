/*
 * Copyright (C) 2014 Andreas Willich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.the_engineers.reading_lens;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnLayoutChangeListener {

    public static final int ZOOM_STEPS = 10;
    SurfaceView _image;

    private Camera _camera;
    private SurfaceHolder _surfaceHolder;
    private int _height;
    private int _width;
    private boolean _cameraRunning;
    private Button _toogle_negative;

    private Button _zoom_plus_button;
    private Button _zoom_minus_button;
    private int _actual_zoom;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        _actual_zoom = 0;

        getActionBar().hide();

        _zoom_minus_button = (Button)findViewById(R.id.zoom_minus);
        _zoom_plus_button = (Button)findViewById(R.id.zoom_plus);

        _zoom_plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setZoom(_actual_zoom + ZOOM_STEPS);
            }
        });

        _zoom_minus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setZoom(_actual_zoom - ZOOM_STEPS);
            }
        });

        _image = (SurfaceView)findViewById(R.id.image);
        _image.addOnLayoutChangeListener(this);
        _image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                    }
                });
            }
        });
        _surfaceHolder =  _image.getHolder();
        _surfaceHolder.addCallback(this);

        _toogle_negative =(Button)findViewById(R.id.toogle_negative);
        _toogle_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters parameters = _camera.getParameters();

                if (parameters.getColorEffect().contains(Camera.Parameters.EFFECT_NEGATIVE))
                    parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
                else
                    parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);

                _camera.setParameters(parameters);
            }
        });
    }

    private void setZoom(int nextZoom) {
        if (_camera == null)
            return;

        Camera.Parameters parameters = _camera.getParameters();
        if (nextZoom <= 0)
            return;

        if (nextZoom >= parameters.getMaxZoom())
            return;

        parameters.setZoom(nextZoom);
        _camera.setParameters(parameters);

        _actual_zoom = nextZoom;
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

        if (!parameters.isZoomSupported())
        {
            _zoom_plus_button.setVisibility(View.GONE);
            _zoom_minus_button.setVisibility(View.GONE);
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraIndex, info);

        parameters.setRotation(info.orientation + 90);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

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
