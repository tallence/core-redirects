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

import DateUtil from "@jangaroo/ext-ts/Date";
import Column from "@jangaroo/ext-ts/grid/column/Column";
import Config from "@jangaroo/runtime/Config";
import RedirectCreationDateColumn from "./RedirectCreationDateColumn";

interface RedirectCreationDateColumnBaseConfig extends Config<Column> {
}

class RedirectCreationDateColumnBase extends Column {
  declare Config: RedirectCreationDateColumnBaseConfig;

  constructor(config: Config<RedirectCreationDateColumn> = null) {
    super(config);
  }

  protected static creationDateColRenderer(value: Date): string {
    let date = "";
    if (value) {
      date = DateUtil.format(value, "d.m.Y");
    }
    return date;
  }

}

export default RedirectCreationDateColumnBase;
