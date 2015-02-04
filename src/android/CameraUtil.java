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
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

/**
 * @author navaneetsarma
 * @since 29/05/14
 */
public final class CameraUtil {

  private CameraUtil() {
  }

  private static final String TAG = "CameraUtil";

  /**
   * Camera Request id.
   */
  public static final int CAMERA_REQUEST = 1888;
  /**
   * Video request ID.
   */
  public static final int VIDEO_REQUEST = 1889;
  private static volatile Fragment callingActivity;
  /**
   * If set to a value > 1, requests the decoder to subsample the original image, returning a smaller image to save
   * memory. The sample size is the number of pixels in either dimension that correspond to a single pixel in the
   * decoded bitmap. For example, inSampleSize == 4 returns an image that is 1/4 the width/height of the original, and
   * 1/16 the number of pixels. Any value <= 1 is treated the same as 1. Note: the decoder uses a final value based on
   * powers of 2, any other value will be rounded down to the nearest power of 2.
   */
  public static final int SAMPLE_QUALITY = 2;

  /**
   * quality of the image between 0 to 100.
   */
  public static final int PHOTO_QUALITY = 100;

  private static final double SCALED_HEIGHT = 512.0;

  /**
   * Method to get the photo from the camera.
   * 
   * @param fragment
   *          fragment instance that holds the callback method for the camera utility
   */
  public static void getPhotoFromCamera(final Fragment fragment) {
    final File imageFile = FileCacheUtil.getOutputMediaFile();
    if (imageFile != null && imageFile.exists()) {
      FileUtils.deleteQuietly(imageFile);
    }
    callingActivity = fragment;
    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

    // start the image capture Intent
    callingActivity.startActivityForResult(intent, CAMERA_REQUEST);
  }

  /**
   * Method to get a video clip.
   * 
   * @param fragment
   *          fragment instance that holds the callback method for the camera utility.
   */
  public static void getVideoFromCamera(final Fragment fragment) {
    callingActivity = fragment;
    /*final SocialWorkerSharedPreferences preferences = SocialWorkerSharedPreferences.getInstance();
    final Intent videoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, preferences.getVideoQuality());

    final long videoSize = preferences.getVideoSize();
    if (videoSize > 0) {
      videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, videoSize);
    }

    final int videoDuration = preferences.getVideoDuration();
    if (videoDuration > 0) {
      videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, videoDuration);
    }

    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileCacheUtil.getOutputMediaFileUriForVideos());

    callingActivity.startActivityForResult(videoIntent, VIDEO_REQUEST);*/
    
    final Intent videoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

    //final long videoSize = preferences.getVideoSize();
   /* if (videoSize > 0) {
      videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 100);
    }*/

    //final int videoDuration = preferences.getVideoDuration();
    /*if (videoDuration > 0) {
      videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
    }*/

    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileCacheUtil.getOutputMediaFileUriForVideos());

    callingActivity.startActivityForResult(videoIntent, VIDEO_REQUEST);
  }

  /**
   * Get the bitmap returned via the camera utility.
   * 
   * @param activity
   *          context used to access the file system.
   * @param data
   *          Intent that contains the returned data from the camera utility
   * @return Bitmap the bitmap that is returned
   */
  public static Bitmap getBitmap(final Activity activity, final Intent data) {
    try {
      final BitmapFactory.Options options = new BitmapFactory.Options();

      options.inSampleSize = SAMPLE_QUALITY;

      final File file = FileCacheUtil.getOutputMediaFile();
      final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
      final int nh = (int) (bitmap.getHeight() * (SCALED_HEIGHT / bitmap.getWidth()));
      final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int) SCALED_HEIGHT, nh, true);

      // Delete the file once we have the bitmap
      FileUtils.deleteQuietly(file);

      return scaled;
    } catch (final NullPointerException e) {
      LogUtil.error(TAG, e);
      return null;
    }
  }

  /**
   * Gets a byte array from an Intent.
   * 
   * @param context
   *          used to access the file system.
   * @param data
   *          the Intent that holds the data.
   * @return the data in the Intent converted to a byte[].
   */
  public static byte[] getByteArray(final Context context, final Intent data) {
    byte[] videoBytes = null;
    try {
      final InputStream iStream = context.getContentResolver().openInputStream(
          FileCacheUtil.getOutputMediaFileUriForVideos());
      videoBytes = IOUtils.toByteArray(iStream);
      iStream.close();
    } catch (final IOException e) {
      LogUtil.error(TAG, e);
      return null;
    }
    return videoBytes;
  }

  /**
   * Iterate over supported camera preview sizes to see which one best fits the dimensions of the given view while
   * maintaining the aspect ratio. If none can, be lenient with the aspect ratio.
   *
   * @param sizes
   *          Supported camera preview sizes.
   * @param w
   *          The width of the view.
   * @param h
   *          The height of the view.
   * @return Best match camera preview size to fit in the view.
   */
  public static Camera.Size getOptimalPreviewSize(final List<Camera.Size> sizes, final int w, final int h) {
    // Use a very small tolerance because we want an exact match.
    final double aspectTolerance = 0.1;
    final double targetRatio = (double) w / h;
    if (sizes == null) {
      return null;
    }
    LogUtil.error(TAG, "the width height" + w + "::" + h);
    Camera.Size optimalSize = null;

    // Start with max value and refine as we iterate over available preview sizes. This is the
    // minimum difference between view and camera height.
    double minDiff = Double.MAX_VALUE;

    // Target view height
    final int targetHeight = h;

    // Try to find a preview size that matches aspect ratio and the target view size.
    // Iterate over all available sizes and pick the largest size that can fit in the view and
    // still maintain the aspect ratio.
    for (final Camera.Size size : sizes) {
      final double ratio = (double) size.width / size.height;
      if (Math.abs(ratio - targetRatio) > aspectTolerance) {
        continue;
      }
      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    // Cannot find preview size that matches the aspect ratio, ignore the requirement
    if (optimalSize == null) {
      minDiff = Double.MAX_VALUE;
      for (final Camera.Size size : sizes) {
        if (Math.abs(size.height - targetHeight) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - targetHeight);
        }
      }
    }
    return optimalSize;
  }
}