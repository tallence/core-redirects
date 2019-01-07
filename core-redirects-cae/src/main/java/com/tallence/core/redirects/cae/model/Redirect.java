package com.tallence.core.redirects.cae.model;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;

import java.util.Objects;

/**
 * Model for a Redirect (used instead of a ContentBean in order to keep the overhead low).
 * Keeps only the properties required by the CAE.
 */
public class Redirect {

  public static final String NAME = "Redirect";

  private static final String SOURCE_URL_TYPE = "sourceUrlType";
  private static final String SOURCE_URL = "source";
  private static final String TARGET_LINK = "targetLink";
  private static final String REDIRECT_TYPE = "redirectType";

  private final SourceUrlType sourceUrlType;
  private final String source;
  private final RedirectType redirectType;
  private final Content target;

  public Redirect(Content redirect) {
    sourceUrlType = SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE));
    source = redirect.getString(SOURCE_URL);
    redirectType = RedirectType.asRedirectType(redirect.getString(REDIRECT_TYPE));
    target = redirect.getLink(TARGET_LINK);
  }

  /**
   * Returns the {@link SourceUrlType} of the redirect.
   */
  public SourceUrlType getSourceUrlType() {
    return sourceUrlType;
  }

  /**
   * Returns the source url of the redirect.
   */
  public String getSource() {
    return source;
  }

  /**
   * Returns a {@link Content} to which the redirect links.
   */
  public Content getTarget() {
    return target;
  }

  /**
   * Returns the {@link RedirectType} of the redirect.
   */
  public RedirectType getRedirectType() {
    return redirectType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Redirect redirect = (Redirect) o;
    return sourceUrlType == redirect.sourceUrlType &&
            Objects.equals(source, redirect.source) &&
            redirectType == redirect.redirectType &&
            Objects.equals(target, redirect.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceUrlType, source, redirectType, target);
  }
}