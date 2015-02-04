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

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private final SurfaceHolder mHolder;
  private final Camera mCamera;
  private final Context mContext;
  private static final String TAG = "CameraPreview";

  /**
   * Surface view to capture video.
   * 
   * @param context
   *          context
   * @param camera
   *          camera object
   */
  public CameraPreview(final Context context, final Camera camera) {
    super(context);
    mCamera = camera;
    mContext = context;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.addCallback(this);
  }

  @Override
  public void surfaceChanged(final SurfaceHolder holder, final int format, final int weight, final int height) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.
    if (mHolder.getSurface() == null) {
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try {
      mCamera.stopPreview();
    } catch (final Exception e) {
      LogUtil.error(TAG, e.getMessage());
    }

    // start preview with new settings
    try {
      final Camera.Parameters params = mCamera.getParameters();
      if (params.isZoomSupported()) {
        ((CameraActivity) mContext).setZoomControls(params);
      }
      mCamera.setParameters(params);
      mCamera.setPreviewDisplay(mHolder);
      mCamera.startPreview();

    } catch (final Exception e) {
      LogUtil.error(TAG, e.getMessage());
    }
  }

  @Override
  public void surfaceCreated(final SurfaceHolder holder) {
    // The Surface has been created, now tell the camera where to draw the preview.
    if (mCamera != null) {
      try {
        mCamera.setPreviewDisplay(holder);
        mCamera.startPreview();
      } catch (final IOException e) {
        LogUtil.error(TAG, e.getMessage());
      }
    }
  }

  @Override
  public void surfaceDestroyed(final SurfaceHolder holder) {

  }
}