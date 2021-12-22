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

import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";
import IconColumn from "@coremedia/studio-client.ext.ui-components/grid/column/IconColumn";
import BeanRecord from "@coremedia/studio-client.ext.ui-components/store/BeanRecord";
import Model from "@jangaroo/ext-ts/data/Model";
import Store from "@jangaroo/ext-ts/data/Store";
import { as } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import Redirect from "../../../data/Redirect";
import RedirectStatusColumn from "./RedirectStatusColumn";

interface RedirectStatusColumnBaseConfig extends Config<IconColumn> {
}

class RedirectStatusColumnBase extends IconColumn {
  declare Config: RedirectStatusColumnBaseConfig;

  constructor(config: Config<RedirectStatusColumn> = null) {
    super(config);
  }

  protected override calculateIconCls(value: any, metadata: any, record: Model, rowIndex: number, colIndex: number, store: Store): string {
    const redirect = as(as(record, BeanRecord).getBean(), Redirect);
    if (redirect.isActive()) {
      return CoreIcons_properties.checkbox_checked;
    }
    return CoreIcons_properties.checkbox_unchecked;
  }

}

export default RedirectStatusColumnBase;
