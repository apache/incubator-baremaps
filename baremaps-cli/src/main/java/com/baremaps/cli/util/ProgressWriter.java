/*
 * Copyright (C) 2011 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.cli.util;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ProgressWriter {

  public static void main(String[] args) {
    long total = 235;
    long startTime = System.currentTimeMillis();

    for (int i = 1; i <= total; i = i + 3) {
      try {
        Thread.sleep(50);
        printProgress(startTime, total, i);
      } catch (InterruptedException e) {
      }
    }
  }

  public static void printProgress(long startTime, long total, long current) {
    long eta =
        current == 0 ? 0 : (total - current) * (System.currentTimeMillis() - startTime) / current;

    String etaHms =
        current == 0
            ? "N/A"
            : String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(eta),
                TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

    StringBuilder string = new StringBuilder(140);
    int percent = (int) (current * 100 / total);
    string
        .append('\r')
        .append(
            String.join(
                "", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
        .append(String.format(" %d%% [", percent))
        .append(String.join("", Collections.nCopies(percent, "=")))
        .append('>')
        .append(String.join("", Collections.nCopies(100 - percent, " ")))
        .append(']')
        .append(
            String.join(
                "",
                Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
        .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

    System.out.print(string);
  }
}
