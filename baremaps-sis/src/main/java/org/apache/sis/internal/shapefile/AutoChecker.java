/*
 * Copyright (C) 2020 The Baremaps Authors
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

package org.apache.sis.internal.shapefile;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.internal.system.Modules;
import org.apache.sis.util.logging.Logging;

/**
 * Base class for objets having auto-checking abilities and easy access to Bundle and logging
 * function.
 *
 * @author Marc LE BIHAN
 */
public abstract class AutoChecker {
  /** Logger. */
  static Logger LOGGER = Logging.getLogger(Modules.SHAPEFILE);

  /**
   * Format a resource bundle message.
   *
   * @param classForResourceBundleName class from which ResourceBundle name will be extracted.
   * @param key Message key.
   * @param args Message arguments.
   * @return Message.
   */
  protected final String format(Class<?> classForResourceBundleName, String key, Object... args) {
    Objects.requireNonNull(
        classForResourceBundleName,
        "Class from with the ResourceBundle name is extracted cannot be null.");
    Objects.requireNonNull(key, "Message key cannot be bull.");

    Class<?> candidateClass = classForResourceBundleName;
    MessageFormat format = null;

    // Find the key in the bundle having for name this class, or in one of its superclasses.
    do {
      try {
        ResourceBundle rsc = ResourceBundle.getBundle(candidateClass.getName());
        format = new MessageFormat(rsc.getString(key));
      } catch (MissingResourceException e) {
        candidateClass = candidateClass.getSuperclass();
      }
    } while (candidateClass != null && format == null);

    if (format == null) {
      String fmt =
          "Cannot find property key {0} in {1} properties file or any of its superclasses.";
      String message = MessageFormat.format(fmt, key, classForResourceBundleName.getName());
      throw new MissingResourceException(message, classForResourceBundleName.getName(), key);
    } else return format.format(args);
  }

  /**
   * Format a resource bundle message.
   *
   * @param key Message key.
   * @param args Message arguments.
   * @return Message.
   */
  protected final String format(String key, Object... args) {
    return format(getClass(), key, args);
  }

  /**
   * Format a resource bundle message and before returning it, log it.
   *
   * @param logLevel Log Level.
   * @param key Message key.
   * @param args Message arguments.
   * @return Message.
   */
  protected final String format(Level logLevel, String key, Object... args) {
    Objects.requireNonNull(logLevel, "The log level cannot be null.");

    String message = format(key, args);
    LOGGER.log(logLevel, message);
    return (message);
  }

  /**
   * Format a resource bundle message and before returning it, log it.
   *
   * @param classForResourceBundleName class from which ResourceBundle name will be extracted.
   * @param logLevel Log Level.
   * @param key Message key.
   * @param args Message arguments.
   * @return Message.
   */
  protected final String format(
      Level logLevel, Class<?> classForResourceBundleName, String key, Object... args) {
    Objects.requireNonNull(logLevel, "The log level cannot be null.");

    String message = format(classForResourceBundleName, key, args);
    LOGGER.log(logLevel, message);
    return (message);
  }

  /**
   * Tells if the logger of the base class will log this level of log.
   *
   * @param level Wished level of logging.
   * @return true if it will log it.
   */
  protected boolean isLoggable(Level level) {
    return LOGGER.isLoggable(level);
  }

  /**
   * Logs (and take the time to format an entry log) only if the logger accepts the message.
   *
   * @param logLevel Log level.
   * @param key Message key.
   * @param args Message arguments.
   */
  protected final void log(Level logLevel, String key, Object... args) {
    Objects.requireNonNull(logLevel, "The log level cannot be null.");

    if (LOGGER.isLoggable(logLevel)) format(logLevel, key, args);
  }

  /**
   * Throw an exception by reflection.
   *
   * @param <E> Class of the exception to build.
   * @param classException Class of the exception to build.
   * @param message Exception message.
   * @param cause Exception root cause.
   * @throws E wished exception.
   */
  public static <E extends Throwable> void throwException(
      Class<E> classException, String message, Throwable cause) throws E {
    throw (exception(classException, message, cause));
  }

  /**
   * Build an exception by reflection.
   *
   * @param <E> Class of the exception to build.
   * @param classException Class of the exception to build.
   * @param message Exception message.
   * @param cause Exception root cause.
   * @return E wished exception.
   */
  private static <E extends Throwable> E exception(
      Class<E> classException, String message, Throwable cause) {
    Objects.requireNonNull(
        classException, "The class of the exception to throw cannot be null."); // $NON-NLS-1$

    try {
      Constructor<E> cstr = classException.getConstructor(String.class, Throwable.class);
      E exception = cstr.newInstance(message, cause);
      return (exception);
    } catch (Exception e) {
      // Create the error message manually to avoid re-entrance in function of this class, that if
      // it has a trouble here could have also a problem everywhere.
      String format =
          "The exception of class {0} (message ''{1}'') can''t be created by reflection. An exception of class {2} happened with the message {3}.";
      String msg =
          MessageFormat.format(
              format, classException.getName(), message, e.getClass().getName(), e.getMessage());
      throw new RuntimeException(msg, e);
    }
  }

  /**
   * Return the class logger.
   *
   * @return logger.
   */
  public Logger getLogger() {
    return LOGGER;
  }
}
