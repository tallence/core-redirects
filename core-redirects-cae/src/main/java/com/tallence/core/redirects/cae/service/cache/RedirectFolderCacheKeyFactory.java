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
package com.tallence.core.redirects.cae.service.cache;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * This factory is used to create configured instances of the {@link RedirectFolderCacheKey}.
 */
@Service
public class RedirectFolderCacheKeyFactory {

  private final ContentRepository contentRepository;
  private final ExecutorService redirectCacheKeyRecomputeThreadPool;
  private final String redirectsPath;
  private boolean testmode = false;
  private boolean developerMode;

  @Autowired
  public RedirectFolderCacheKeyFactory(ContentRepository contentRepository,
                                       @Qualifier("getRedirectCacheKeyRecomputeThreadPool")
                                       ExecutorService redirectCacheKeyRecomputeThreadPool,
                                       @Value("${core.redirects.path}") String redirectsPath,
                                       @Value("${cae.developer.mode:false}") Boolean developerMode) {
    this.contentRepository = contentRepository;
    this.redirectCacheKeyRecomputeThreadPool = redirectCacheKeyRecomputeThreadPool;
    this.redirectsPath = redirectsPath;
    this.developerMode = developerMode;
  }

  public RedirectFolderCacheKey getCacheKeyFor(@NonNull Site site) {
    return new RedirectFolderCacheKey(contentRepository, redirectCacheKeyRecomputeThreadPool, redirectsPath, site, testmode, developerMode);
  }

  // Enable testMode on cache keys
  public void setTestmode(boolean testmode) {
    this.testmode = testmode;
  }
}
