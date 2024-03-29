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

/**
 * Redirects import response containing the imported redirects and the error messages of the import.
 */
class RedirectImportResponse {

  #redirects: Array<any> = null;

  #errors: Array<any> = null;

  constructor(redirects: Array<any>, errors: Array<any>) {
    this.#redirects = redirects;
    this.#errors = errors;
  }

  getRedirects(): Array<any> {
    return this.#redirects;
  }

  getErrors(): Array<any> {
    return this.#errors;
  }
}

export default RedirectImportResponse;
