/*
 * Copyright (c) 2014 Diona Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Diona
 * ("Confidential Information"). You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement you
 * entered into with Diona.
 */

package com.diona.videoplugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.R;

/**
 * 
 */
public class CameraActivity extends Activity implements OnErrorListener, OnInfoListener {
  private Camera mCamera;
  private CameraPreview mSurfaceView;
  private ImageView recordBtn;
  private FrameLayout mCameraPreview;

  private ImageView recordBlinkImage;
  private Chronometer mChronometer;
  private TextView recordVideoText;
  private TextView videoSize;

  private MediaRecorder mMediaRecorder;
  private Camera.Parameters cameraParameters;
  private File videoOutputFile;
  private long maxVideoSize;

  private Handler mHandler;

  private int mCameraId;
  private boolean isSmoothZoomEnabled;
  private boolean recording;
  private int result;
  private float mDist;

  private static final int ANGLE_360 = 360;
  private static final int ANGLE_90 = 90;
  private static final int ANGLE_180 = 180;
  private static final int ANGLE_270 = 270;

  private static final long UPDATE_INTERVAL = 2000;
  private static final int BINARY_UNIT = 1024;
  private static final int DECIMAL_UNIT = 1000;
  private static final int MILLISECONDS_IN_SECOND = 1000;

  /**
   * Called when the activity is first created.
   * 
   * @param savedInstanceState
   *          saved instance of the activity
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    recording = false;

    setContentView(R.layout.camera_layout);
    
 // Remove all files stored on the device
    FileCacheUtil.init(this);

    // Get Camera for preview
    mCamera = getCameraInstance();
    if (mCamera == null) {
      Toast.makeText(CameraActivity.this, "Fail to open Camera", Toast.LENGTH_LONG).show();
      finish();
    }

    mSurfaceView = new CameraPreview(this, mCamera);
    mCameraPreview = (FrameLayout) findViewById(R.id.videoview);
    mCameraPreview.addView(mSurfaceView);

    recordBlinkImage = (ImageView) findViewById(R.id.video_rec_blink_img);
    recordVideoText = (TextView) findViewById(R.id.recording_video_text);
    videoSize = (TextView) findViewById(R.id.video_size);
    mChronometer = (Chronometer) findViewById(R.id.video_time);

    recordBtn = (ImageView) findViewById(R.id.record_video);
    recordBtn.setOnClickListener(recordClickListener);

    videoOutputFile = FileCacheUtil.getOutputMediaFileForVideos();

    FontUtil.getInstance(this).useBoldRegularFont(videoSize);
    FontUtil.getInstance(this).useBoldRegularFont(recordVideoText);
    FontUtil.getInstance(this).useBoldRegularFont(mChronometer);

    setCameraDisplayOrientation();
  }

  /**
   * 
   */
  private void setCameraDisplayOrientation() {
    final Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
    if (mCamera != null) {
      final CameraInfo info = new CameraInfo();
      Camera.getCameraInfo(mCameraId, info);
      int degrees = 0;
      switch (display.getRotation()) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = ANGLE_90;
        break;
      case Surface.ROTATION_180:
        degrees = ANGLE_180;
        break;
      case Surface.ROTATION_270:
        degrees = ANGLE_270;
        break;
      default:
        break;
      }
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        result = (info.orientation + degrees) % ANGLE_360;
        result = (ANGLE_360 - result) % ANGLE_360; // compensate the mirror
      } else { // back-facing
        result = (info.orientation - degrees + ANGLE_360) % ANGLE_360;
      }
      mCamera.setDisplayOrientation(result);
    }

  }

  /**
   * Gets the camera instance.
   * 
   * @return the first back facing camera if present, if not opens the front facing camera. Returns null if camera not
   *         present.
   */
  private Camera getCameraInstance() {
    Camera camera = null;
    try {
      camera = Camera.open();
      if (camera == null) {
        // Find the total number of cameras available
        final int mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
          Camera.getCameraInfo(i, cameraInfo);
          if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = i;
            return Camera.open(i);
          } else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = i;
            return Camera.open(i);
          }
        }
      }
    } catch (final Exception e) {
      // Camera is not available (in use or does not exist)
      Toast.makeText(CameraActivity.this, "Fail to open Camera", Toast.LENGTH_LONG).show();
      finish();
    }
    return camera; // returns null if camera is unavailable
  }

  /**
   * Prepares the media recorder for video recording.
   * 
   * @return true if media recorder is prepared for recording, false otherwise
   */
  private boolean prepareMediaRecorder() {
    releaseCamera();
    mCamera = getCameraInstance();
    setCameraDisplayOrientation();
    mMediaRecorder = new MediaRecorder();

    // We need to make sure that our preview and recording video size are supported by the
    // camera. Query camera to find all the sizes and choose the optimal size given the
    // dimensions of our preview surface.
    final Camera.Parameters parameters = mCamera.getParameters();
    final List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
    final Camera.Size optimalSize = CameraUtil.getOptimalPreviewSize(mSupportedPreviewSizes, mSurfaceView.getWidth(),
        mSurfaceView.getHeight());

    //final SocialWorkerSharedPreferences preferences = SocialWorkerSharedPreferences.getInstance();
    maxVideoSize = 100;
    // Use the same size for recording profile.
    final CamcorderProfile profile = CamcorderProfile.get(mCameraId, 1);

    parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
    mCamera.setParameters(parameters);
    mCamera.unlock();
    mMediaRecorder.setOrientationHint(result);
    mMediaRecorder.setCamera(mCamera);

    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

    mMediaRecorder.setProfile(profile);

    // TODO set the values from bootstrap properties/values fetched from server
    mMediaRecorder.setOutputFile(videoOutputFile.getAbsolutePath());
    System.out.println("videoOutputFile.getAbsolutePath");
    System.out.println(videoOutputFile.getAbsolutePath());
    /*mMediaRecorder.setVideoFrameRate(preferences.getVideoFrameRate());
    mMediaRecorder.setVideoEncodingBitRate(preferences.getVideoBitrate());
    mMediaRecorder.setAudioEncodingBitRate(preferences.getAudioBitrate());
    mMediaRecorder.setMaxFileSize(maxVideoSize);
    if (preferences.getVideoDuration() > 0) {
      mMediaRecorder.setMaxDuration(preferences.getVideoDuration() * MILLISECONDS_IN_SECOND);
    }*/

    mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
    mMediaRecorder.setOnErrorListener(this);
    mMediaRecorder.setOnInfoListener(this);

    try {
      mMediaRecorder.prepare();
    } catch (final IllegalStateException e) {
      releaseMediaRecorder();
      return false;
    } catch (final IOException e) {
      releaseMediaRecorder();
      return false;
    }
    return true;

  }

  // from MediaRecorder.OnErrorListener
  @Override
  public void onError(final MediaRecorder mr, final int what, final int extra) {
    if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
      // We may have run out of space on the sdcard.
      if (recording) {
        stopMediaRecorder();
      }
      Toast.makeText(this, "Some error occured during recording", Toast.LENGTH_SHORT).show();
    }
  }

  // from MediaRecorder.OnInfoListener
  @Override
  public void onInfo(final MediaRecorder mr, final int what, final int extra) {
    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
      if (recording) {
        Toast.makeText(this, "Maximum duration reached", Toast.LENGTH_SHORT).show();
        stopMediaRecorder();
      }
    } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
      if (recording) {
        if (mHandler != null) {
          mHandler.removeCallbacksAndMessages(null);
        }
        videoSize.setText(humanReadableByteCount(maxVideoSize) + " / " + humanReadableByteCount(maxVideoSize));
        Toast.makeText(this, "Maximum file size reached", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {

          @Override
          public void run() {
            stopMediaRecorder();
          }
        }, MILLISECONDS_IN_SECOND);
      }
    }
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    // Get the pointer ID
    if (mCamera != null) {
      final Camera.Parameters params = mCamera.getParameters();
      final int action = event.getAction();

      if (event.getPointerCount() > 1) {
        // handle multi-touch events
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
          mDist = getFingerSpacing(event);
        } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
          handleZoom(event, params);
        }
      } else {
        // handle single touch events
        if (action == MotionEvent.ACTION_UP) {
          handleFocus(event, params);
        }
      }
      return true;
    }
    return false;
  }

  private void handleZoom(final MotionEvent event, final Camera.Parameters params) {
    final int maxZoom = params.getMaxZoom();
    int zoom = params.getZoom();
    final float newDist = getFingerSpacing(event);
    if (newDist > mDist) {
      // zoom in
      if (zoom < maxZoom) {
        zoom++;
      }
    } else if (newDist < mDist) {
      // zoom out
      if (zoom > 0) {
        zoom--;
      }
    }
    mDist = newDist;
    if (isSmoothZoomEnabled) {
      mCamera.startSmoothZoom(zoom);
    } else {
      if (cameraParameters != null) {
        cameraParameters.setZoom(zoom);
        mCamera.setParameters(cameraParameters);
      }
    }
  }

  /**
   * Handle focus of camera on touch events.
   * 
   * @param event
   *          motion event.
   * @param params
   *          camera parameters.
   */
  public void handleFocus(final MotionEvent event, final Camera.Parameters params) {
    final List<String> supportedFocusModes = params.getSupportedFocusModes();
    if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
      mCamera.autoFocus(new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(final boolean b, final Camera camera) {
          // currently set to auto-focus on single touch
        }
      });
    }
  }

  /** Determine the space between the first two fingers */
  private float getFingerSpacing(final MotionEvent event) {
    final float x = event.getX(0) - event.getX(1);
    final float y = event.getY(0) - event.getY(1);
    return (float) Math.sqrt(x * x + y * y);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (mCamera == null) {
      mCamera = getCameraInstance();
      if (mCamera == null) {
        Toast.makeText(CameraActivity.this, "Fail to open Camera", Toast.LENGTH_LONG).show();
        finish();
      }

      mSurfaceView = new CameraPreview(this, mCamera);
      mCameraPreview.addView(mSurfaceView);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseMediaRecorder(); // if you are using MediaRecorder, release it first
    releaseCamera(); // release the camera immediately on pause event
    if (mCameraPreview != null) {
      mCameraPreview.removeView(mSurfaceView);
      mSurfaceView = null;
    }
    recording = false;
    recordBtn.setImageResource(R.drawable.record);

    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
    }
    hideRecordingControls();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    releaseMediaRecorder();
    releaseCamera();
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
    }
  }

  /**
   * Releases media recorder object.
   */
  private void releaseMediaRecorder() {
    if (mMediaRecorder != null) {
      mMediaRecorder.reset(); // clear recorder configuration
      mMediaRecorder.release(); // release the recorder object
      mMediaRecorder.setOnErrorListener(null);
      mMediaRecorder.setOnInfoListener(null);
      mMediaRecorder = null;
      mCamera.lock(); // lock camera for later use
    }
  }

  /**
   * Release the camera for other applications.
   */
  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
  }

  /**
   * Sets the zoom controls to the camera if available.
   * 
   * @param params
   *          camera parameters
   */
  public void setZoomControls(final Camera.Parameters params) {
    if (mCamera != null) {
      cameraParameters = params;
      isSmoothZoomEnabled = params.isSmoothZoomSupported();
    }
  }

  /**
   * Stops the media recorder and finishes the activity.
   */
  public void stopMediaRecorder() {
    // stop recording and release camera
    if (mMediaRecorder != null) {
      try {
        videoSize.setText(humanReadableByteCount(Long.valueOf(maxVideoSize)) + " / " + maxVideoSize);
        mMediaRecorder.stop(); // stop the recording
      } catch (final RuntimeException e) {
        LogUtil.error("CameraActivity", e.getMessage());
      }
    }
    releaseMediaRecorder(); // release the MediaRecorder object
    System.out.println("videoOutputFile stopMediaRecorder");
    final Intent resultIntent = new Intent();
    // TODO Add extras or a data URI to this intent as appropriate.
    resultIntent.setData(FileCacheUtil.getOutputMediaFileUriForVideos());
    setResult(Activity.RESULT_OK, resultIntent);
    System.out.println("videoOutputFile stopMediaRecorder result set");
    // Exit after saved
    finish();
  }

  /**
   * Click listener for video recording.
   */
  private final ImageView.OnClickListener recordClickListener = new ImageView.OnClickListener() {

    @Override
    public void onClick(final View v) {
      if (recording) {
        v.setEnabled(false);
        hideRecordingControls();
        stopMediaRecorder();
      } else {
        if (!prepareMediaRecorder()) {
          Toast.makeText(CameraActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
          finish();
        }
        mMediaRecorder.start();
        showRecordingControls();
        recording = true;
        recordBtn.setImageResource(R.drawable.stop);
      }
    }
  };

  /**
   * Sets the rotation for the view depending on orientation.
   * 
   * @param value
   *          the orientation value.
   */
  protected void setRotationForLandscapeOrientedDevices(final int value) {
    // switch (value) {
    // case 0:
    // zoomControls.setRotation(0);
    // break;
    // case angle90:
    // zoomControls.setRotation(angle270);
    // break;
    // case angle180:
    // zoomControls.setRotation(angle180);
    // break;
    // case angle270:
    // zoomControls.setRotation(angle90);
    // break;
    // default:
    // zoomControls.setRotation(0);
    // }
  }

  /**
   * 
   */
  protected void hideRecordingControls() {
    recordVideoText.setVisibility(View.INVISIBLE);
    if (mChronometer.getVisibility() == View.VISIBLE) {
      mChronometer.stop();
      mChronometer.setVisibility(View.INVISIBLE);
    }
    recordBlinkImage.setAnimation(null);
    recordBlinkImage.setVisibility(View.INVISIBLE);
    videoSize.setVisibility(View.INVISIBLE);
  }

  /**
   * 
   */
  protected void showRecordingControls() {
    videoSize.setVisibility(View.VISIBLE);
    recordVideoText.setVisibility(View.VISIBLE);
    mChronometer.setVisibility(View.VISIBLE);
    mChronometer.setBase(SystemClock.elapsedRealtime());
    mChronometer.start();
    recordBlinkImage.setVisibility(View.VISIBLE);
    final Animation animation = AnimationUtils.loadAnimation(this, R.anim.blink);
    recordBlinkImage.setAnimation(animation);

    updateVideoSize();
  }

  /**
   * 
   */
  private void updateVideoSize() {
    if (mHandler == null) {
      mHandler = new Handler();
    }

    final Runnable updateVideoSizeRunnable = new Runnable() {

      @Override
      public void run() {
        final long size = videoOutputFile.length();
        videoSize.setText(humanReadableByteCount(size) + " / " + humanReadableByteCount(maxVideoSize));
        mHandler.postDelayed(this, UPDATE_INTERVAL);
      }
    };
    mHandler.postDelayed(updateVideoSizeRunnable, UPDATE_INTERVAL);
  }

  @SuppressLint("DefaultLocale")
  private String humanReadableByteCount(final long bytes) {
    final boolean decimalUnits = true;
    final int unit = decimalUnits ? DECIMAL_UNIT : BINARY_UNIT;
    if (bytes < unit) {
      return bytes + " B";
    }
    final int exp = (int) (Math.log(bytes) / Math.log(unit));
    final String pre = (decimalUnits ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (decimalUnits ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  /**
   * Sets the rotation for the view depending on orientation.
   * 
   * @param value
   *          the orientation value.
   */
  protected void setRotationForPortraitOrientedDevices(final int value) {
    // switch (value) {
    // case 0:
    // zoomControls.setRotation(angle270);
    // break;
    // case angle90:
    // zoomControls.setRotation(angle180);
    // break;
    // case angle180:
    // zoomControls.setRotation(angle90);
    // break;
    // case angle270:
    // zoomControls.setRotation(0);
    // break;
    // default:
    // zoomControls.setRotation(0);
    // }

  }

}
