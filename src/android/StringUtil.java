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
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

/**
 * A utility for manipulating Strings.
 */
public final class StringUtil {

  private static final int BUFFER_SIZE = 1024 * 8;

  /**
   * Creates text displaying a case reference and case members formatted suitable for displaying to users with
   * appropriate colours and styling.
   *
   * @param caseReference
   *          the reference number/name for a case
   * @param caseMembers
   *          a HashMap of individuals involved in a case.
   * @param context
   *          the context of the calling activity.
   * @return a styled and coloured SpannableStringBuilder that includes the case reference and case members concatenated
   *         into a human readable form.
   */
  /*public SpannableStringBuilder getCaseInfoString(final String caseReference, final List<CaseMember> caseMembers,
      final Context context) {

    // the color value should be retrieved from colours.xml file e.g. getResources().getColor(R.color
    // .case_reference_text) - R.color.case_reference_text
    final ForegroundColorSpan caseReferenceColor = new ForegroundColorSpan(Color.parseColor("#547FBF"));
    final CharacterStyle bold = new StyleSpan(Typeface.BOLD);
    final int pxSize = 17;
    final AbsoluteSizeSpan size = new AbsoluteSizeSpan(pxSize, true);

    final SpannableStringBuilder familyInfo = new SpannableStringBuilder(caseReference);

    final int numCaseMembers = caseMembers.size();

    for (int i = 0; i < numCaseMembers; i++) {
      final CaseMember member = caseMembers.get(i);
      int start = familyInfo.length();
      familyInfo.append(member.getName()).append(" ");
      familyInfo.setSpan(new ForegroundColorSpan(Color.parseColor("#30333A")), start,
          start + member.getName().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
      start = familyInfo.length();
      final String memberAge = "(" + member.getAge() + ")";
      familyInfo.append(memberAge);
      if (i < (numCaseMembers - 1)) {
        familyInfo.append(",");
      }
      familyInfo.append(" ");
      familyInfo.setSpan(new ForegroundColorSpan(Color.parseColor("#30333A")), start, start + memberAge.length(),
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    // possible to have different styles combined here such as color AND font-weight AND font-size
    // for substring starting at index 0 up to index of 0+length(caseRef)
    if (caseReference.length() > 0) {
      familyInfo.setSpan(caseReferenceColor, 0, caseReference.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      familyInfo.setSpan(bold, 0, caseReference.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      familyInfo.setSpan(size, 0, caseReference.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    return familyInfo;
  }*/

  /**
   * Reads a String from an InputStream with progress updates.
   * 
   * @param inputStream
   *          the stream that contains the data from which we wish to read.
   * @param httpUtil
   *          the instance of HTTPUtil being used.
   * @return the String version of the InputStream.
   * @throws IOException
   *           thrown if we have a problem reading the stream.
   */
  /*public static String readInputStreamWithProgressUpdates(final InputStream inputStream, final HTTPUtil httpUtil)
      throws IOException {
    try {

      final StringBuilder sb = new StringBuilder();
      long downloadedSize = 0;
      // create a buffer...
      final byte[] buffer = new byte[BUFFER_SIZE];

      int bufferLength = 0; // used to store a temporary size of the buffer
      final HTTPAsyncTask httpAsyncTask = httpUtil.getHttpAysncTask();

      // now, read through the input buffer and write the contents to the file
      while ((bufferLength = inputStream.read(buffer)) != -1 && !httpAsyncTask.isCancelled()) {

        sb.append(new String(buffer, 0, bufferLength));

        // add up the size so we know how much is downloaded
        downloadedSize += bufferLength;
        // this is where you would do something to report the progress
        if (httpUtil != null) {
          httpUtil.invokeProgressEvent(downloadedSize);
        }
      }

      return sb.toString();
    } catch (final IOException e) {
      LogUtil.error("StringUtil", e);
      throw e;
    }

  }*/

  /**
   * If a String exceeds a certain size it is trimmed, otherwise it is left as it is.
   * 
   * @param input
   *          the String we wish to trim.
   * @param length
   *          the maximum length of the String.
   * @return the trimmed String.
   */
  public static String truncateString(final String input, final int length) {
    if (input == null || input.length() < 1 || length < 1) {
      return "SocialWorker";
    }
    final int maxLength = (input.length() < length) ? input.length() : length;
    return input.substring(0, maxLength);
  }
}
