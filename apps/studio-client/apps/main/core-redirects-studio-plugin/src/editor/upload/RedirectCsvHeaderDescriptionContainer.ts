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
import DisplayFieldSkin from "@coremedia/studio-client.ext.ui-components/skins/DisplayFieldSkin";
import Container from "@jangaroo/ext-ts/container/Container";
import DisplayField from "@jangaroo/ext-ts/form/field/Display";
import HBoxLayout from "@jangaroo/ext-ts/layout/container/HBox";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";

interface RedirectCsvHeaderDescriptionContainerConfig extends Config<Container>, Partial<Pick<RedirectCsvHeaderDescriptionContainer,
  "title" |
  "text"
>> {
}

class RedirectCsvHeaderDescriptionContainer extends Container {
  declare Config: RedirectCsvHeaderDescriptionContainerConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.upload.redirectCsvHeaderDescriptionContainer";

  #title: string = null;

  get title(): string {
    return this.#title;
  }

  set title(value: string) {
    this.#title = value;
  }

  #text: string = null;

  get text(): string {
    return this.#text;
  }

  set text(value: string) {
    this.#text = value;
  }

  constructor(config: Config<RedirectCsvHeaderDescriptionContainer> = null) {
    super(ConfigUtils.apply(Config(RedirectCsvHeaderDescriptionContainer, {
      items: [
        Config(DisplayField, {
          value: config.title,
          ui: DisplayFieldSkin.BOLD.getSkin(),
        }),
        Config(DisplayField, {
          value: config.text,
          margin: "0 0 0 10",
          ui: DisplayFieldSkin.EMBEDDED.getSkin(),
        }),
      ],
      layout: Config(HBoxLayout),

    }), config));
  }
}

export default RedirectCsvHeaderDescriptionContainer;
