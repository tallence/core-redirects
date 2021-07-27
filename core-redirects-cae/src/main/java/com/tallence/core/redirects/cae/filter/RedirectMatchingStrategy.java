package com.tallence.core.redirects.cae.filter;

import com.tallence.core.redirects.cae.model.Redirect;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface RedirectMatchingStrategy {

  Result getMatchingRedirect(HttpServletRequest request);

  /**
   * Simple wrapper class for the pre-handle redirect result.
   */
  class Result {
    enum Action {
      NONE, SEND, WRAP
    }

    private Redirect redirect;
    private Action action;

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
