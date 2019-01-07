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

package com.tallence.core.redirects.studio.rest;

import com.tallence.core.redirects.studio.model.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * A redirect import response used by the studio to display the import result.
 */
public class RedirectImportResponse {

  private List<RedirectReference> created = new ArrayList<>();
  private List<ErrorMessages> errorMessages = new ArrayList<>();

  public List<RedirectReference> getCreated() {
    return created;
  }

  public void addCreated(Redirect redirect) {
    created.add(new RedirectReference(redirect));
  }

  public List<ErrorMessages> getErrorMessages() {
    return errorMessages;
  }

  public void addErrorMessage(String csvEntry, String errorCode) {
    errorMessages.add(new ErrorMessages(csvEntry, errorCode));
  }

  private class ErrorMessages {
    private String csvEntry;
    private String errorCode;

    private ErrorMessages(String csvEntry, String errorCode) {
      this.csvEntry = csvEntry;
      this.errorCode = errorCode;
    }

    public String getErrorCode() {
      return errorCode;
    }

    public String getCsvEntry() {
      return csvEntry;
    }
  }

}