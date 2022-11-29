package com.tallence.core.redirects.cae.service;

import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.model.SourceUrlType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for the {@link SiteRedirects}.
 */
public class SteRedirectsTest {

  private SiteRedirects siteRedirects;

  @Before
  public void setUp() {
    siteRedirects = new SiteRedirects();

    var patternRedirect = createRedirect(SourceUrlType.REGEX, "/my-page.*", "coremedia://cap/content/1222");
    siteRedirects.addRedirect(patternRedirect);
    var plainRedirect = createRedirect(SourceUrlType.PLAIN, "/other-page", "coremedia://cap/content/1244");
    siteRedirects.addRedirect(plainRedirect);
  }

  @Test
  public void testSwitchToPlain() {

    var oldRedirect = createRedirect(SourceUrlType.REGEX, "/abc.*", "coremedia://cap/content/123");
    var updatedRedirect = createRedirect(SourceUrlType.PLAIN, "/abc", "coremedia://cap/content/123");

    siteRedirects.addRedirect(oldRedirect);

    //Make sure, the new plainRedirect exists alongside with the default redirect from the setUp method
    assertTrue(siteRedirects.getPatternRedirects().keySet().stream().anyMatch(p -> p.pattern().equals(oldRedirect.getSource())));
    assertFalse(siteRedirects.getPlainRedirects().containsKey(updatedRedirect.getSource()));
    assertEquals(2, countRedirects(siteRedirects.getPatternRedirects()));
    assertEquals(1, countRedirects(siteRedirects.getPlainRedirects()));

    siteRedirects.addRedirect(updatedRedirect);
    //Make sure, the redirect has been updated to a plainRedirect and it exists alongside with the default redirects from the setUp method
    assertTrue(siteRedirects.getPatternRedirects().keySet().stream().noneMatch(p -> p.pattern().equals(oldRedirect.getSource())));
    assertTrue(siteRedirects.getPlainRedirects().get(updatedRedirect.getSource()).stream().findFirst().isPresent());
    assertEquals(2, countRedirects(siteRedirects.getPlainRedirects()));
    assertEquals(1, countRedirects(siteRedirects.getPatternRedirects()));
  }

  private long countRedirects(Map<?, List<Redirect>> siteRedirects) {
    return siteRedirects.values().stream().mapToLong(Collection::size).sum();
  }

  @Test
  public void testSwitchToRegex() {

    var oldRedirect = createRedirect(SourceUrlType.PLAIN, "/abc", "coremedia://cap/content/123");
    var updatedRedirect = createRedirect(SourceUrlType.REGEX, "/abc.*", "coremedia://cap/content/123");

    siteRedirects.addRedirect(oldRedirect);

    //Make sure, the new patternRedirect exists alongside with the default redirect from the setUp method
    assertTrue(siteRedirects.getPatternRedirects().keySet().stream().noneMatch(p -> p.pattern().equals(oldRedirect.getSource())));
    assertTrue(siteRedirects.getPlainRedirects().get(oldRedirect.getSource()).stream().findFirst().isPresent());
    assertEquals(2, countRedirects(siteRedirects.getPlainRedirects()));
    assertEquals(1, countRedirects(siteRedirects.getPatternRedirects()));

    siteRedirects.addRedirect(updatedRedirect);
    //Make sure, the redirect has been updated to a patternRedirect and it exists alongside with the default redirects from the setUp method
    assertTrue(siteRedirects.getPatternRedirects().keySet().stream().anyMatch(p -> p.pattern().equals(updatedRedirect.getSource())));
    assertFalse(siteRedirects.getPlainRedirects().containsKey(updatedRedirect.getSource()));
    assertEquals(2, countRedirects(siteRedirects.getPatternRedirects()));
    assertEquals(1, countRedirects(siteRedirects.getPlainRedirects()));
  }

  private Redirect createRedirect(SourceUrlType sourceUrlType, String source, String contentId) {
    var redirect = mock(Redirect.class);
    when(redirect.getSourceUrlType()).thenReturn(sourceUrlType);
    when(redirect.getSource()).thenReturn(source);
    when(redirect.getContentId()).thenReturn(contentId);
    return redirect;
  }
}
