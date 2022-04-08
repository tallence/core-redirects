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

/**
 *
 * We extend the javascript object and store the data directly in the object as properties. We can't use private
 * variables because they are automatically changed by the compiler (for example, name to name$123). The changed names
 * are then automatically used for serializing, so parsing in the BE would not be possible.
 */
class RedirectTargetParameter extends Object {

  static readonly NAME: string = "name";

  static readonly VALUE: string = "value";

  constructor(json: any) {
    super();
    this[RedirectTargetParameter.NAME] = json[RedirectTargetParameter.NAME];
    this[RedirectTargetParameter.VALUE] = json[RedirectTargetParameter.VALUE];
  }

  getName(): string {
    return this[RedirectTargetParameter.NAME];
  }

  getValue(): string {
    return this[RedirectTargetParameter.VALUE];
  }

  getParametersAsMap(): any {
    const map: Record<string, any> = {};
    map[RedirectTargetParameter.NAME] = this.getName();
    map[RedirectTargetParameter.VALUE] = this.getValue();
    return map;
  }

}

export default RedirectTargetParameter;
