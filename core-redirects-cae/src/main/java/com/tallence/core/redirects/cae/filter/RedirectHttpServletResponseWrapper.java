/*
 * Copyright 2019 Tallence AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tallence.core.redirects.cae.filter;


import com.tallence.core.redirects.cae.model.Redirect;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Internal wrapper class to prevent to response from being committed too early.
 *
 * This wrapper stores all written data in internal variables and prevents them
 * from being written on the wire, so that the final response code can be checked
 * (in this case for a 404) and rewritten.
 * Otherwise, changing the response code would not be possible anymore, once
 * control is returned to the redirect filter.
 */
class RedirectHttpServletResponseWrapper extends HttpServletResponseWrapper {
  private int tempStatus = 200;
  private String tempMsg;
  private String tempLocation;
  private boolean isError;
  private boolean isRedirect;

  // Reference to the redirect which this wrapper is used for
  private final Redirect redirect;

  private CharArrayWriter writer = new CharArrayWriter();

  public RedirectHttpServletResponseWrapper(HttpServletResponse response, Redirect redirect) {
    super(response);
    this.redirect = redirect;
  }

  public Redirect getRedirect() {
    return redirect;
  }

  @Override
  public void setStatus(int sc) {
    tempStatus = sc;
  }

  @Override
  public int getStatus() {
    return tempStatus;
  }

  @Override
  public void sendError(int sc, String msg) {
    tempStatus = sc;
    tempMsg = msg;
    isError = true;
  }

  @Override
  public void sendError(int sc) {
    tempStatus = sc;
    isError = true;
  }

  @Override
  public void sendRedirect(String location) {
    tempLocation = location;
    isRedirect = true;
  }

  @Override
  public PrintWriter getWriter() {
    return new PrintWriter(writer);
  }

  public String toString() {
    return writer.toString();
  }


  @Override
  public void flushBuffer() {
    //Do nothing. This will be called for HEAD requests from the
    //javax.servlet.http.HttpServlet.NoBodyResponse
    //But it commits the response, which we do not want in the wrapper
  }

  /**
   * Writes the stored request on the wire (by writing the data on the super instance,
   * which writes it to the original request to be sent).
   */
  public void writeOnSuper() throws IOException {
    if (isError) {
      if (tempMsg != null) {
        super.sendError(tempStatus, tempMsg);
      } else {
        super.sendError(tempStatus);
      }
    } else if (isRedirect) {
      super.sendRedirect(tempLocation);
    } else {
      super.setStatus(tempStatus);
      String result = writer.toString();
      super.getWriter().write(result);
    }
  }


}
