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
import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import CustomLabels_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/CustomLabels_properties";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import ButtonSkin from "@coremedia/studio-client.ext.ui-components/skins/ButtonSkin";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentInfo from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentInfo";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import ReferrerListPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/ReferrerListPanel";
import VersionHistory from "@coremedia/studio-client.main.editor-components/sdk/premular/VersionHistory";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import TextAreaStringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/TextAreaStringPropertyField";
import Button from "@jangaroo/ext-ts/button/Button";
import Container from "@jangaroo/ext-ts/container/Container";
import DisplayField from "@jangaroo/ext-ts/form/field/Display";
import HBoxLayout from "@jangaroo/ext-ts/layout/container/HBox";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import OpenRedirectManagerEditorAction from "../action/OpenRedirectManagerEditorAction";
import RedirectContentTypes_properties from "../bundles/RedirectContentTypes_properties";
import RedirectManagerStudioPlugin_properties from "../bundles/RedirectManagerStudioPlugin_properties";

interface RedirectFormConfig extends Config<DocumentTabPanel> {
}

class RedirectForm extends DocumentTabPanel {
  declare Config: RedirectFormConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.studioform.redirectForm";

  constructor(config: Config<RedirectForm> = null) {
    super(ConfigUtils.apply(Config(RedirectForm, {

      items: [

        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          items: [
            Config(PropertyFieldGroup, {
              title: CustomLabels_properties.PropertyGroup_Details_label,
              itemId: "detailsDocumentForm",
              propertyNames: ["redirectType", "sourceUrlType", "source", "targetLink", "imported", "description"],
              expandOnValues: "redirectType,sourceUrlType,source,targetLink,imported,description",
              manageHeight: false,
              forceReadOnlyValueExpression: ValueExpressionFactory.createFromValue(true),
              items: [
                Config(Container, {
                  items: [
                    Config(DisplayField, { value: RedirectContentTypes_properties.Redirect_edit_hint_text }),
                    Config(Button, {
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_button_text,
                      tooltip: RedirectManagerStudioPlugin_properties.redirectmanager_button_tooltip,
                      ui: ButtonSkin.LINK.getSkin(),
                      baseAction: new OpenRedirectManagerEditorAction({}),
                    }),
                  ],
                  layout: Config(HBoxLayout),
                }),
                Config(StringPropertyField, { propertyName: "redirectType" }),
                Config(StringPropertyField, { propertyName: "sourceUrlType" }),
                Config(StringPropertyField, { propertyName: "source" }),
                Config(LinkListPropertyField, { propertyName: "targetLink" }),
                Config(TextAreaStringPropertyField, { propertyName: "description" }),
              ],
            }),
          ],
        }),

        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_system_title,
          itemId: "system",
          autoHide: true,
          items: [
            Config(DocumentInfo),
            Config(VersionHistory),
            Config(ReferrerListPanel),
          ],
        }),

      ],

    }), config));
  }
}

export default RedirectForm;
