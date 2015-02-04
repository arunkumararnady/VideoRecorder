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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * A class for applying custom fonts on views.
 */
public final class FontUtil {
  private static FontUtil instance;
  private Typeface regularFont = null;
  private Typeface mediumFont = null;

  /**
   * Gets an instance of FontUtil.
   *
   * @param context
   *          used to create the fonts.
   * @return if an instance already exists, return that. Otherwise, instantiate a new one.
   */
  public static FontUtil getInstance(final Context context) {
    if (instance == null) {
      instance = new FontUtil(context);
    }
    return instance;
  }

  private FontUtil(final Context context) {
    final AssetManager assetManager = context.getAssets();
    final String fontPath = "fonts/";
    try {
      this.regularFont = Typeface.createFromAsset(assetManager, fontPath + "Roboto-Regular.ttf");
    } catch (final Exception e) {
      LogUtil.error(getClass().getSimpleName(), e);
    }
    try {
      this.mediumFont = Typeface.createFromAsset(assetManager, fontPath + "Roboto-Medium.ttf");
    } catch (final Exception e) {
      LogUtil.error(getClass().getSimpleName(), e);
    }
  }

  /**
   * Applies the regular font on a TextView.
   *
   * @param textView
   *          the TextView to which we wish to apply the font.
   */
  public void useNormalRegularFont(final TextView textView) {
    setTypeface(textView, this.regularFont, Typeface.NORMAL);
  }

  /**
   * Applies the medium font on a TextView.
   *
   * @param textView
   *          the TextView to which we wish to apply the font.
   */
  public void useNormalMediumFont(final TextView textView) {
    setTypeface(textView, this.mediumFont, Typeface.NORMAL);
  }

  /**
   * Applies the bold variety of the regular font on a TextView.
   *
   * @param textView
   *          the TextView to which we wish to apply the font.
   */
  public void useBoldRegularFont(final TextView textView) {
    setTypeface(textView, this.regularFont, Typeface.BOLD);
  }

  /**
   * Applies the bold variety of the medium font on a TextView.
   *
   * @param textView
   *          the TextView to which we wish to apply the font.
   */
  public void useBoldMediumFont(final TextView textView) {
    setTypeface(textView, this.mediumFont, Typeface.BOLD);
  }

  /**
   * Sets the correct font for dialog headings.
   * 
   * @param textView
   *          the TextView to which we wish to apply the font.
   */
  public void setDialogHeadingFont(final TextView textView) {
    useBoldRegularFont(textView);
  }

  private void setTypeface(final TextView textView, final Typeface typeface, final int style) {
    if (textView != null) {
      textView.setTypeface(typeface, style);
    }
  }
}
