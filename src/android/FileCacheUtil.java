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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;


/**
 * Utility for caching images using persistent storage.
 */
public final class FileCacheUtil {

  private File cacheDir;

  // Set the name of the cache directory to be hidden, so it doesn't appear in file explorers by default
  private static final String CACHE_DIR_NAME = ".dmc";

  // Set the name of tmp file to be hidden, so it doesn't show up by default in file explorers
  private static final String TMP_FILE_NAME = ".tmp";

  private String formsPath;
  private static final String TAG = "FileCacheUtil";

  private static volatile FileCacheUtil instance;
  private static volatile Context applicationContext;

  /**
   * Initialises the singleton instance of this class, only creates an instance if one has not already been created.
   * 
   * @param context
   *          The context required for reading shared preferences.
   */
  public static void init(final Context context) {
    // Lazy initialise the singleton instance
    if (instance == null) {
      instance = new FileCacheUtil(context);
    }
  }

  /**
   * get an instance of the file cache util class if it doesn't already exist.
   * 
   * @return an instance of utility class.
   */
  public static FileCacheUtil getInstance() {
    if (instance == null) {
      throw new RuntimeException("FileCacheUtil singleton accessed before initialisation.");
    }
    return instance;
  }

  /**
   * Constructor to set location of cached files.
   *
   * @param context
   *          used to access cache directory.
   */
  private FileCacheUtil(final Context context) {
    applicationContext = context;

    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
      cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), CACHE_DIR_NAME);
    } else {
      cacheDir = context.getCacheDir();
    }
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
  }

  /**
   * Returns the file matching the specified filename.
   * 
   * @param filename
   *          the name of the file.
   *
   * @return if the file exists, that is returned. Otherwise, a new file is created and then returned.
   */
  public synchronized File getFile(final String filename) {
    return new File(cacheDir, filename);
  }

  /**
   * Constructs the temporary file used by the application for viewing files in an external viewer.
   * 
   * @return The file
   */
  public synchronized File getTempFile() {
    return new File(cacheDir, TMP_FILE_NAME);
  }

  /**
   * Deletes the temporary fiel if it exists.
   * 
   * @return <code>true</code> if the file was deleted, otherwise <code>false</code>.
   */
  /*public synchronized boolean deleteTempFile() {
    return FileUtils.deleteQuietly(getTempFile());
  }*/

  /**
   * Adds the contents to the temporarily file location in the cache.
   * 
   * @param contents
   *          The contents to add to the cache.
   * @return The temporarily file details.
   */
  public synchronized File addTempFile(final byte[] contents) {
    return addFile(TMP_FILE_NAME, contents);
  }

  /**
   * Returns the absolute path to the directory that is used to store forms.
   * 
   * @return The path to the forms directory.
   */
  public synchronized String getFormsPath() {
    if (formsPath == null) {
      formsPath = cacheDir.getAbsolutePath() + File.separator + "forms";
    }

    // Always ensure the directory exists, it gets deleted when a refresh happens
    final File formsDir = new File(formsPath);
    if (!formsDir.exists()) {
      formsDir.mkdirs();
    }

    return formsPath;
  }

  /**
   * Adds the specified contents to the file cache, the file name is based on the current time in milliseconds.
   * 
   * @param contents
   *          The file contents as a byte array.
   * @return The newly created file details.
   * @see FileCacheUtil#addFile(String, byte[])
   */
  public File addFile(final byte[] contents) {
    return addFile(String.valueOf(System.currentTimeMillis()), contents);
  }

  /**
   * Creates a new File or replaces the existing one.
   * 
   * @param name
   *          the name of the file.
   * @param contents
   *          the contents of the File.
   *
   * @return the newly created File.
   */
  public File addFile(final String name, final byte[] contents) {
    final File file = getFile(name);
    if (file.exists()) {
      file.delete();
    }

    // Ensure the parent folder exists
    final File parentFile = file.getParentFile();
    if (!parentFile.exists()) {
      parentFile.mkdirs();
    }

    FileOutputStream writer;
    try {
      writer = new FileOutputStream(file);
      writer.write(contents);
      writer.close();
    } catch (final FileNotFoundException e) {
      LogUtil.error(TAG, e);
    } catch (final IOException e) {
      LogUtil.error(TAG, e);
    }
    return file;
  }



  /**
   * Gets the temporary file used for viewing a document with an external viewer.
   * 
   * @return the temporary file used for viewing documents with an external viewer.
   */
  public File getOutputDocumentFile() {
    return getTempFile();
  }

  /**
   * Creates a file for storing video media.
   * 
   * @return the created file.
   */
  public static File getOutputMediaFileForVideos() {
    return getInstance().getTempFile();
  }

  /**
   * Gets a URI to a media file for videos.
   * 
   * @return the URI to the media file.
   */
  public static Uri getOutputMediaFileUriForVideos() {
    return Uri.fromFile(FileCacheUtil.getOutputMediaFileForVideos());
  }

  /**
   * Creates a temporary file for storing media.
   * 
   * @return the file reference.
   */
  public static File getOutputMediaFile() {
    return getInstance().getTempFile();
  }
}
