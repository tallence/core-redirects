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
import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import RedirectContentTypes_properties from "./RedirectContentTypes_properties";

/**
 * Overrides of ResourceBundle "RedirectContentTypes" for Locale "de".
 * @see RedirectContentTypes_properties#INSTANCE
 */
ResourceBundleUtil.override(RedirectContentTypes_properties, {
  Redirect_redirectType_text: "Typ",
  Redirect_edit_hint_text: "Die Umleitung kann in diesem Formular nicht editiert werden. Bitte nutzen Sie dafür den Redirect-Manager:",
  Redirect_sourceUrlType_text: "Url Typ",
  Redirect_source_text: "Quelle",
  Redirect_targetLink_text: "Ziel",
  Redirect_description_text: "Beschreibung",
});
