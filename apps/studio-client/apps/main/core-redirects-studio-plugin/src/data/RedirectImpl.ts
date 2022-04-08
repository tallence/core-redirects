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

import UndocContent from "@coremedia/studio-client.cap-rest-client/content/UndocContent";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import RemoteBeanImpl from "@coremedia/studio-client.client-core-impl/data/impl/RemoteBeanImpl";
import RemoteServiceMethod from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethod";
import RemoteServiceMethodResponse from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethodResponse";
import { mixin } from "@jangaroo/runtime";
import { AnyFunction } from "@jangaroo/runtime/types";
import RedirectManagerStudioPlugin_properties from "../bundles/RedirectManagerStudioPlugin_properties";
import NotificationUtil from "../util/NotificationUtil";
import Redirect from "./Redirect";
import RedirectSourceParameter from "./RedirectSourceParameter";
import RedirectTargetParameter from "./RedirectTargetParameter";

class RedirectImpl extends RemoteBeanImpl implements Redirect {
  static readonly REST_RESOURCE_URI_TEMPLATE: string = "redirect/{siteId:[^/]+}/{id:[^/]+}";

  static readonly ACTIVE: string = "active";

  static readonly CREATION_DATE: string = "creationDate";

  static readonly REDIRECT_TYPE: string = "redirectType";

  static readonly REDIRECT_TYPE_ALWAYS: string = "ALWAYS";

  static readonly REDIRECT_TYPE_404: string = "AFTER_NOT_FOUND";

  static readonly SOURCE: string = "source";

  static readonly SOURCE_TYPE: string = "sourceUrlType";

  static readonly SOURCE_TYPE_PLAIN: string = "PLAIN";

  static readonly SOURCE_TYPE_REGEX: string = "REGEX";

  static readonly TARGET_LINK: string = "targetLink";

  static readonly TARGET_LINK_NAME: string = "targetLinkName";

  static readonly TARGET_URL: string = "targetUrl";

  static readonly DESCRIPTION: string = "description";

  static readonly IMPORTED: string = "imported";

  static readonly SITE_ID: string = "siteId";

  static readonly SOURCE_PARAMETERS: string = "sourceParameters";

  static readonly TARGET_PARAMETERS: string = "targetParameters";

  /**
   * List of all redirect properties, used by the grid.
   */
  static readonly REDIRECT_PROPERTIES: Array<any> = [
    RedirectImpl.ACTIVE,
    RedirectImpl.CREATION_DATE,
    RedirectImpl.REDIRECT_TYPE,
    RedirectImpl.SOURCE,
    RedirectImpl.SOURCE_TYPE,
    RedirectImpl.SOURCE_TYPE_PLAIN,
    RedirectImpl.SOURCE_TYPE_REGEX,
    RedirectImpl.TARGET_LINK,
    RedirectImpl.TARGET_LINK_NAME,
    RedirectImpl.DESCRIPTION,
    RedirectImpl.IMPORTED,
    RedirectImpl.SITE_ID,
    RedirectImpl.SOURCE_PARAMETERS,
    RedirectImpl.TARGET_PARAMETERS,
  ];

  constructor(path: string) {
    super(path);
  }

  deleteMe(callback: AnyFunction = null): void {
    const rsm = new RemoteServiceMethod(this.getUriPath(), "DELETE");
    rsm.request(
      null,
      function success(rsmr: RemoteServiceMethodResponse): void {
        NotificationUtil.showInfo(RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_delete_result_text_success);
        callback.call(this);
      },
      (rsmr: RemoteServiceMethodResponse): void =>
        NotificationUtil.showError(RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_delete_result_text_error_w_msg + rsmr.getError()),
    );
  }

  isActive(): boolean {
    return this.get(RedirectImpl.ACTIVE);
  }

  setActive(active: boolean): void {
    this.set(RedirectImpl.ACTIVE, active);
  }

  getTargetLink(): UndocContent {
    return this.get(RedirectImpl.TARGET_LINK);
  }

  setTargetLink(content: Content): void {
    this.set(RedirectImpl.TARGET_LINK, content);
  }

  getTargetUrl(): string {
    return this.get(RedirectImpl.TARGET_URL);
  }

  setTargetUrl(targetUrl: string): void {
    this.set(RedirectImpl.TARGET_URL, targetUrl);
  }

  getTargetLinkName(): string {
    return this.get(RedirectImpl.TARGET_LINK_NAME);
  }

  setTargetLinkName(name: string): void {
    this.set(RedirectImpl.TARGET_LINK_NAME, name);
  }

  getCreationDate(): Date {
    return this.get(RedirectImpl.CREATION_DATE);
  }

  setCreationDate(creationDate: Date): void {
    this.set(RedirectImpl.CREATION_DATE, creationDate);
  }

  getRedirectType(): number {
    return this.get(RedirectImpl.REDIRECT_TYPE);
  }

  setRedirectType(redirectType: number): void {
    this.set(RedirectImpl.REDIRECT_TYPE, redirectType);
  }

  getDescription(): string {
    return this.get(RedirectImpl.DESCRIPTION);
  }

  setDescription(description: string): void {
    this.set(RedirectImpl.DESCRIPTION, description);
  }

  isImported(): boolean {
    return this.get(RedirectImpl.IMPORTED);
  }

  getSourceType(): string {
    return this.get(RedirectImpl.SOURCE_TYPE);
  }

  setSourceType(sourceType: string): void {
    this.set(RedirectImpl.SOURCE_TYPE, sourceType);
  }

  getSource(): string {
    return this.get(RedirectImpl.SOURCE);
  }

  setSource(source: string): void {
    this.set(RedirectImpl.SOURCE, source);
  }

  getSiteId(): string {
    return this.get(RedirectImpl.SITE_ID);
  }

  setSourceParameters(parameters: Array<any>): void {
    this.set(RedirectImpl.SOURCE_PARAMETERS, parameters);
  }

  getSourceParameters(): Array<RedirectSourceParameter> {
    return this.get(RedirectImpl.SOURCE_PARAMETERS);
  }

  setTargetParameters(parameters: Array<any>): void {
    this.set(RedirectImpl.TARGET_PARAMETERS, parameters);
  }

  getTargetParameters(): Array<RedirectTargetParameter> {
    return this.get(RedirectImpl.TARGET_PARAMETERS);
  }

  protected override propertiesUpdated(overwrittenValues: any, newValues: any): void {
    NotificationUtil.showInfo(RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_save_result_text_success);
    super.propertiesUpdated(overwrittenValues, newValues);
  }

}
mixin(RedirectImpl, Redirect);

export default RedirectImpl;
