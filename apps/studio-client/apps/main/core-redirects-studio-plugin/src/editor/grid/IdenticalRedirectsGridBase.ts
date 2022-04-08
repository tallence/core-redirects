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

import Config from "@jangaroo/runtime/Config";
import IdenticalRedirectsGrid from "./IdenticalRedirectsGrid";
import RedirectsGrid from "./RedirectsGrid";

interface IdenticalRedirectsGridBaseConfig extends Config<RedirectsGrid> {
}

/**
 * Pageable grid component for redirects. Redirects can be filtered by source.
 * Redirects are edited or deleted via the context menu or can be edited with a double-click.
 */
class IdenticalRedirectsGridBase extends RedirectsGrid {
  declare Config: IdenticalRedirectsGridBaseConfig;

  constructor(config: Config<IdenticalRedirectsGrid> = null) {
    super(config);
  }

}

export default IdenticalRedirectsGridBase;
