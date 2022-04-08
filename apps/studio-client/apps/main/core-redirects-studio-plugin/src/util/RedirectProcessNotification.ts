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
import AnimatedNotification from "@coremedia/studio-client.ext.ui-components/components/AnimatedNotification";
import TipSkin from "@coremedia/studio-client.ext.ui-components/skins/TipSkin";
import FitLayout from "@jangaroo/ext-ts/layout/container/Fit";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";

interface RedirectProcessNotificationConfig extends Config<AnimatedNotification> {
}

class RedirectProcessNotification extends AnimatedNotification {
  declare Config: RedirectProcessNotificationConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.form.redirectProcessNotification";

  constructor(config: Config<RedirectProcessNotification> = null) {
    super(ConfigUtils.apply(Config(RedirectProcessNotification, {
      position: "rt",
      yOffset: 50.0,
      xOffset: 37.0,
      target: "topContainer-innerCt",
      isMouseAware: true,
      hideAnchor: true,
      minHeight: 60.0,
      width: 400,
      ui: TipSkin.EMBEDDING.getSkin(),
      layout: Config(FitLayout),
      // alignToTarget: "tr-tl",
      visibilityDuration: 6000

    }), config));
  }
}

export default RedirectProcessNotification;
