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

import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy to resolve a redirect for the given request.
 */
public interface RedirectMatchingService {

  Result getMatchingRedirect(HttpServletRequest request);

  /**
   * Simple wrapper class for the pre-handle redirect result.
   */
  class Result {
    enum Action {
      NONE, SEND, WRAP
    }

    private final Redirect redirect;
    private final Action action;

    private Result(Redirect redirect, Action action) {
      this.redirect = redirect;
      this.action = action;
    }

    static Result send(Redirect redirect) {
      return new Result(redirect, Action.SEND);
    }

    static Result wrap(Redirect redirect) {
      return new Result(redirect, Action.WRAP);
    }

    static Result none() {
      return new Result(null, Action.NONE);
    }

    public Redirect getRedirect() {
      return redirect;
    }

    public Action getAction() {
      return action;
    }
  }
}
