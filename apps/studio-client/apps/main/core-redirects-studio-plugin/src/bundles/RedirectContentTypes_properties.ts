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
 * Interface values for ResourceBundle "RedirectContentTypes".
 * @see RedirectContentTypes_properties#INSTANCE
 */
interface RedirectContentTypes_properties {

  Redirect_icon: string;
  Redirect_edit_hint_text: string;
  Redirect_redirectType_text: string;
  Redirect_sourceUrlType_text: string;
  Redirect_source_text: string;
  Redirect_targetLink_text: string;
  Redirect_description_text: string;
}

/**
 * Singleton for the current user Locale's instance of ResourceBundle "RedirectContentTypes".
 * @see RedirectContentTypes_properties
 */
const RedirectContentTypes_properties: RedirectContentTypes_properties = {
  Redirect_icon: "tallence-icons tallence-icons--redirects",
  Redirect_edit_hint_text: "Redirects cannot be edited in this form. Please use the redirect manager:",
  Redirect_redirectType_text: "Type",
  Redirect_sourceUrlType_text: "Source Url Type",
  Redirect_source_text: "Source",
  Redirect_targetLink_text: "Target Link",
  Redirect_description_text: "Description",
};

export default RedirectContentTypes_properties;
