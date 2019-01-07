package com.tallence.core.redirects.cae.filter;

import com.coremedia.blueprint.cae.handlers.HandlerTestConfiguration;
import com.coremedia.blueprint.cae.handlers.RequestTestHelper;
import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.*;
import static com.tallence.core.redirects.cae.filter.AbstractRedirectsTest.LocalConfig.PROFILE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * Abstract setup class to config test infrastructure.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = AbstractRedirectsTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public abstract class AbstractRedirectsTest {


  @Configuration
  @PropertySource("classpath:/component-core-redirects-cae.properties")
  @ImportResource(
          value = {
                  CACHE,
                  CONTENT_BEAN_FACTORY,
                  DATA_VIEW_FACTORY,
                  ID_PROVIDER,
                  LINK_FORMATTER,
                  "classpath*:/component-core-redirects-cae.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class, HandlerTestConfiguration.class})
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "RedirectContentBeanTestBase";
    private static final String CONTENT_REPOSITORY = "classpath:/com/tallence/core/redirects/cae/testdata/contenttest.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }

  }

  @Autowired
  protected ContentTestHelper contentTestHelper;

  @Autowired
  protected RequestTestHelper requestTestHelper;

  protected <T> T getContentBean(int id) {
    return contentTestHelper.getContentBean(id);
  }

}
