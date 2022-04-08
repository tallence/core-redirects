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
import Bean from "@coremedia/studio-client.client-core/data/Bean";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import StatefulDateField from "@coremedia/studio-client.ext.ui-components/components/StatefulDateField";
import CollapsiblePanel from "@coremedia/studio-client.ext.ui-components/components/panel/CollapsiblePanel";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import DisplayFieldSkin from "@coremedia/studio-client.ext.ui-components/skins/DisplayFieldSkin";
import FormSpacerElement from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/FormSpacerElement";
import SingleLinkEditor from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/SingleLinkEditor";
import FieldContainer from "@jangaroo/ext-ts/form/FieldContainer";
import Checkbox from "@jangaroo/ext-ts/form/field/Checkbox";
import ComboBox from "@jangaroo/ext-ts/form/field/ComboBox";
import DisplayField from "@jangaroo/ext-ts/form/field/Display";
import TextArea from "@jangaroo/ext-ts/form/field/TextArea";
import FitLayout from "@jangaroo/ext-ts/layout/container/Fit";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import { AnyFunction } from "@jangaroo/runtime/types";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import Redirect from "../../data/Redirect";
import RedirectImpl from "../../data/RedirectImpl";
import BindValidationStatePlugin from "../../plugins/BindValidationStatePlugin";
import IdenticalRedirectsGrid from "../grid/IdenticalRedirectsGrid";
import RedirectParametersGrid from "../parameters/RedirectParametersGrid";
import ErrorFieldContainer from "./ErrorFieldContainer";
import RedirectEditPanelBase from "./RedirectEditPanelBase";
import RedirectSourceFieldContainer from "./RedirectSourceFieldContainer";
import RedirectTargetUrlFieldContainer from "./RedirectTargetUrlFieldContainer";

interface RedirectEditPanelConfig extends Config<RedirectEditPanelBase>, Partial<Pick<RedirectEditPanel,
  "localModel" |
  "errorMessagesVE" |
  "mayNotPublishVE" |
  "mayNotUseRegexVE" |
  "mayNotUseTargetUrlsVE" |
  "selectedSiteIdVE" |
  "validateRedirectHandler" |
  "redirect"
>> {
}

class RedirectEditPanel extends RedirectEditPanelBase {
  declare Config: RedirectEditPanelConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.form.redirectEditPanel";

  constructor(config: Config<RedirectEditPanel> = null) {
    super(ConfigUtils.apply(Config(RedirectEditPanel, {
      items: [
        Config(CollapsiblePanel, {
          collapsible: false,
          defaultType: FieldContainer["xtype"],
          defaults: Config<FieldContainer>({
            labelAlign: "left",
            width: "100%",
            labelWidth: 130,
          }),
          items: [

            Config(Checkbox, {
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_active,
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    bidirectional: true,
                    bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.ACTIVE),
                  }),
                  Config(BindPropertyPlugin, {
                    componentProperty: "hidden",
                    bindTo: config.mayNotPublishVE,
                  }),
                ],
              }),
            }),

            Config(FormSpacerElement, { height: "10px" }),

            Config(StatefulDateField, {
              disabled: true,
              editable: false,
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_creationDate,
              startDay: 1,
              plugins: [
                Config(BindPropertyPlugin, {
                  bidirectional: true,
                  bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.CREATION_DATE),
                }),
              ],
            }),

            Config(FormSpacerElement, { height: "10px" }),

            Config(ComboBox, {
              itemId: "redirectTypeField",
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type,
              forceSelection: true,
              triggerAction: "all",
              editable: false,
              store: [
                [RedirectImpl.REDIRECT_TYPE_404, RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type_value_1],
                [RedirectImpl.REDIRECT_TYPE_ALWAYS, RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type_value_0],
              ],
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    bidirectional: true,
                    bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.REDIRECT_TYPE),
                  }),
                ],
              }),
            }),

            Config(FormSpacerElement, { height: "10px" }),

            Config(RedirectSourceFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              sourceUrlVE: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.SOURCE),
              sourceUrlParametersVE: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.SOURCE_PARAMETERS),
            }),

            Config(ErrorFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              propertyName: RedirectImpl.SOURCE,
            }),

            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_same_source_title,
              ui: DisplayFieldSkin.BOLD.getSkin(),
              margin: "10 0 0 0",
            }),

            Config(IdenticalRedirectsGrid, {
              selectedSiteIdVE: config.selectedSiteIdVE,
              height: "25px",
              selectedRedirect: config.redirect,
              exactMatch: true,
              emptyText: RedirectManagerStudioPlugin_properties.redirectmanager_editor_list_nothing_found_match,
              searchFieldVE: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.SOURCE),
            }),

            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_sourceParameters,
              ui: DisplayFieldSkin.BOLD.getSkin(),
            }),
            Config(RedirectParametersGrid, {
              bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.SOURCE_PARAMETERS),
              validateRedirectHandler: config.validateRedirectHandler,
            }),

            Config(ErrorFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              propertyName: RedirectImpl.SOURCE_PARAMETERS,
            }),

            Config(FormSpacerElement, { height: "10px" }),

            /*TODO both elements (spacer and comboBox) are bound to the mayNotUseRegexVE. There might is a more elegant way!*/
            Config(FormSpacerElement, {
              height: "10px",
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    componentProperty: "hidden",
                    bindTo: config.mayNotUseRegexVE,
                  }),
                ],
              }),
            }),
            Config(ComboBox, {
              itemId: "sourceTypeField",
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_sourceType,
              forceSelection: true,
              triggerAction: "all",
              editable: false,
              store: [
                [RedirectImpl.SOURCE_TYPE_PLAIN, RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_sourceType_plain],
                [RedirectImpl.SOURCE_TYPE_REGEX, RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_sourceType_regex],
              ],
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    bidirectional: true,
                    bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.SOURCE_TYPE),
                  }),
                  Config(BindPropertyPlugin, {
                    componentProperty: "hidden",
                    bindTo: config.mayNotUseRegexVE,
                  }),

                ],
              }),
            }),

            Config(FormSpacerElement, { height: "10px" }),

            Config(SingleLinkEditor, {
              linkContentType: "CMLinkable",
              labelAlign: "left",
              width: 640,
              bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.TARGET_LINK),
              linkListLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_targetLink,
              ...ConfigUtils.append({
                plugins: [
                  Config(BindValidationStatePlugin, {
                    bindTo: config.errorMessagesVE,
                    propertyName: RedirectImpl.TARGET_LINK,
                  }),
                ],
              }),
            }),

            Config(ErrorFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              propertyName: RedirectImpl.TARGET_LINK,
            }),

            Config(FormSpacerElement, { height: "10px" }),

            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_targetParameters,
              ui: DisplayFieldSkin.BOLD.getSkin(),
            }),
            Config(RedirectParametersGrid, { bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.TARGET_PARAMETERS) }),

            Config(ErrorFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              propertyName: RedirectImpl.TARGET_PARAMETERS,
            }),

            Config(FormSpacerElement, {
              height: "10px",
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    componentProperty: "hidden",
                    bindTo: config.mayNotUseTargetUrlsVE,
                  }),
                ],
              }),
            }),

            Config(RedirectTargetUrlFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              targetUrlVE: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.TARGET_URL),
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    componentProperty: "hidden",
                    bindTo: config.mayNotUseTargetUrlsVE,
                  }),
                ],
              }),
            }),

            Config(ErrorFieldContainer, {
              errorMessagesVE: config.errorMessagesVE,
              propertyName: RedirectImpl.TARGET_URL,
            }),

            Config(FormSpacerElement, { height: "10px" }),

            Config(TextArea, {
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_description,
              ...ConfigUtils.append({
                plugins: [
                  Config(BindPropertyPlugin, {
                    bidirectional: true,
                    bindTo: RedirectEditPanelBase.getBindTo(config.localModel, RedirectImpl.DESCRIPTION),
                  }),
                ],
              }),
            }),

          ],
        }),
      ],
      layout: Config(FitLayout),

    }), config));
  }

  #localModel: Bean = null;

  get localModel(): Bean {
    return this.#localModel;
  }

  set localModel(value: Bean) {
    this.#localModel = value;
  }

  #errorMessagesVE: ValueExpression = null;

  get errorMessagesVE(): ValueExpression {
    return this.#errorMessagesVE;
  }

  set errorMessagesVE(value: ValueExpression) {
    this.#errorMessagesVE = value;
  }

  #mayNotPublishVE: ValueExpression = null;

  get mayNotPublishVE(): ValueExpression {
    return this.#mayNotPublishVE;
  }

  set mayNotPublishVE(value: ValueExpression) {
    this.#mayNotPublishVE = value;
  }

  #mayNotUseRegexVE: ValueExpression = null;

  get mayNotUseRegexVE(): ValueExpression {
    return this.#mayNotUseRegexVE;
  }

  set mayNotUseRegexVE(value: ValueExpression) {
    this.#mayNotUseRegexVE = value;
  }

  #mayNotUseTargetUrlsVE: ValueExpression = null;

  get mayNotUseTargetUrlsVE(): ValueExpression {
    return this.#mayNotUseTargetUrlsVE;
  }

  set mayNotUseTargetUrlsVE(value: ValueExpression) {
    this.#mayNotUseTargetUrlsVE = value;
  }

  #selectedSiteIdVE: ValueExpression = null;

  get selectedSiteIdVE(): ValueExpression {
    return this.#selectedSiteIdVE;
  }

  set selectedSiteIdVE(value: ValueExpression) {
    this.#selectedSiteIdVE = value;
  }

  #validateRedirectHandler: AnyFunction = null;

  get validateRedirectHandler(): AnyFunction {
    return this.#validateRedirectHandler;
  }

  set validateRedirectHandler(value: AnyFunction) {
    this.#validateRedirectHandler = value;
  }

  #redirect: Redirect = null;

  get redirect(): Redirect {
    return this.#redirect;
  }

  set redirect(value: Redirect) {
    this.#redirect = value;
  }
}

export default RedirectEditPanel;
