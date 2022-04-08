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
import RemoteBean from "@coremedia/studio-client.client-core/data/RemoteBean";
import { AnyFunction } from "@jangaroo/runtime/types";

/**
 * A model representing a redirect.
 */
abstract class Redirect extends RemoteBean {

  abstract deleteMe(callback?: AnyFunction): void;

  abstract getId(): string;

  abstract isActive(): boolean;

  abstract setActive(active: boolean): void;

  abstract getTargetLink(): UndocContent;

  abstract getTargetUrl(): string;

  abstract setTargetLink(content: UndocContent): void;

  abstract setTargetUrl(targetUrl: string): void;

  abstract getTargetLinkName(): string;

  abstract setTargetLinkName(name: string): void;

  abstract getCreationDate(): Date;

  abstract setCreationDate(creationDate: Date): void;

  abstract getRedirectType(): number;

  abstract setRedirectType(redirectType: number): void;

  abstract getDescription(): string;

  abstract setDescription(description: string): void;

  abstract isImported(): boolean;

  abstract getSourceType(): string;

  abstract setSourceType(sourceType: string): void;

  abstract getSource(): string;

  abstract setSource(source: string): void;

  abstract getSiteId(): string;

  abstract setSourceParameters(parameters: Array<any>): void;

  abstract getSourceParameters(): Array<any>;

  abstract setTargetParameters(parameters: Array<any>): void;

  abstract getTargetParameters(): Array<any>;

}

export default Redirect;
