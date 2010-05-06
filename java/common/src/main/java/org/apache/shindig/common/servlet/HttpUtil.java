/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.common.servlet;

import org.apache.shindig.common.util.TimeSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * Collection of HTTP utilities
 */
public class HttpUtil {
  // 1 year.
  private static int defaultTtl = 60 * 60 * 24 * 365;

  private static TimeSource timeSource;

  static {
    setTimeSource(new TimeSource());
  }

  public static void setTimeSource(TimeSource timeSource) {
    HttpUtil.timeSource = timeSource;
  }

  /**
   * Sets HTTP headers that instruct the browser to cache content. Implementations should take care
   * to use cache-busting techniques on the url if caching for a long period of time.
   *
   * @param response The HTTP response
   */
  public static void setCachingHeaders(HttpServletResponse response) {
    setCachingHeaders(response, defaultTtl, false);
  }

  /**
   * Sets HTTP headers that instruct the browser to cache content. Implementations should take care
   * to use cache-busting techniques on the url if caching for a long period of time.
   *
   * @param response The HTTP response
   * @param noProxy True if you don't want the response to be cacheable by proxies.
   */
  public static void setCachingHeaders(HttpServletResponse response, boolean noProxy) {
    setCachingHeaders(response, defaultTtl, noProxy);
  }

  /**
   * Sets HTTP headers that instruct the browser to cache content. Implementations should take care
   * to use cache-busting techniques on the url if caching for a long period of time.
   *
   * @param response The HTTP response
   * @param ttl The time to cache for, in seconds. If 0, then insure that
   *            this object is not cached.
   */
  public static void setCachingHeaders(HttpServletResponse response, int ttl) {
    setCachingHeaders(response, ttl, false);
  }
  
  public static void setNoCache(HttpServletResponse response) {
    setCachingHeaders(response, 0, false);
  }

  /**
   * Sets HTTP headers that instruct the browser to cache content. Implementations should take care
   * to use cache-busting techniques on the url if caching for a long period of time.
   *
   * @param response The HTTP response
   * @param ttl The time to cache for, in seconds. If 0, then insure that
   *            this object is not cached.
   * @param noProxy True if you don't want the response to be cacheable by proxies.
   */
  public static void setCachingHeaders(HttpServletResponse response, int ttl, boolean noProxy) {
    response.setDateHeader("Expires", timeSource.currentTimeMillis() + (1000L * ttl));

    if (ttl == 0) {
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Cache-Control", "no-cache");
    } else {
      if (noProxy) {
        response.setHeader("Cache-Control", "private,max-age=" + Integer.toString(ttl));
      } else {
        response.setHeader("Cache-Control", "public,max-age=" + Integer.toString(ttl));
      }
    }
  }

  public static int getDefaultTtl() {
    return defaultTtl;
  }

  public static void setDefaultTtl(int defaultTtl) {
    HttpUtil.defaultTtl = defaultTtl;
  }


  static final Pattern GET_REQUEST_CALLBACK_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\.]+");

  public static boolean isJSONP(HttpServletRequest request) throws IllegalArgumentException {
    String callback = request.getParameter("callback");

    // Must be a GET
    if (!"GET".equals(request.getMethod()))
      return false;
    
    // No callback specified
    if (callback == null) return false;

    if (!GET_REQUEST_CALLBACK_PATTERN.matcher(callback).matches()) {
       throw new IllegalArgumentException("Wrong format for parameter 'callback' specified. Must match: " +
            GET_REQUEST_CALLBACK_PATTERN.toString());
    }
    return true;
  }
}