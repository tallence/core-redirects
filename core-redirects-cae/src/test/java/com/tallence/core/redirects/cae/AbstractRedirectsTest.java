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
package com.tallence.core.redirects.cae;

import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cms.delivery.configuration.DeliveryConfigurationProperties;
import com.coremedia.objectserver.configuration.CaeConfigurationProperties;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.*;
import static com.tallence.core.redirects.cae.AbstractRedirectsTest.LocalConfig.PROFILE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * Abstract setup class to config test infrastructure.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = AbstractRedirectsTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public abstract class AbstractRedirectsTest {


  @Configuration(proxyBeanMethods = false)
  @PropertySource("classpath:/test.properties")
  @ImportResource(
          value = {
                  CACHE,
                  CONTENT_BEAN_FACTORY,
                  DATA_VIEW_FACTORY,
                  ID_PROVIDER,
                  LINK_FORMATTER,
                  "classpath*:/META-INF/coremedia/component-core-redirects-cae.xml",
                  "classpath:/framework/spring/blueprint-handlers.xml",
                  "classpath*:/com/coremedia/blueprint/base/multisite/bpbase-multisite-cae-services.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @EnableConfigurationProperties({
          DeliveryConfigurationProperties.class,
          CaeConfigurationProperties.class
  })
  @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
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

  protected <T> T getContentBean(int id) {
    return contentTestHelper.getContentBean(id);
  }

}
