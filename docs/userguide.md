Core Redirects User Guide
=========================

The administration of redirects is handled in CoreMedia Studio. The interface can be found by clicking the Redirects
button in the apps menu:

![Picture of menu](core-redirects_app-menu.png)

Which leads to this view of all redirects:

![All redirects](core-redirects_overview.png)

Here you can select a site (1), use a full text search filter (2), add new redirects (3) or upload CSV files with
multiple redirects for import (4). Double-clicking an entry will open an edit dialog:

![Edit dialog](core-redirects_edit_dialog.png)

Redirects contain a number of fields, some of which are only informational (marked :information_source:):

- Published: Setting or removing this checkmark will automatically publish or withdraw this redirect.
- Creation date :information_source: : Automatically filled on creation with the current date.
- Type: There are two different kinds of redirects
  - Always: This redirect will always be executed, regardless of any page that would normally be shown for this url.
  - Only after 404: These redirects will only execute, if the source url results in a 404 Not found error. This can for
    example be used to redirect from missing job descriptions to a jobs overview page.
- Source: The source path for a redirect (which url to redirect)
- Source Url Type:
  - Plain: Simple redirects, very fast, but only for a single source url
  - Regular Expression: Defines a rule for severals source urls to match. Slower, but more flexible.
- Target Link: The target of the redirect (where to redirect to). Can also be a CMExternalLink for external urls.
- Description :information_source: : Can be used to keep notes for this redirect. Also searchable.

Considerations on Redirects
---------------------------
There are a couple of considerations to take into account when creating redirects. For one, all redirects for a site are
evaluated on every request (in order to check if any match), so having a lot of rules can slow down the site. This is
especially true for regular expressions. Also: The tool tries to
[check for rules, that shadow each other][edit-validation] (have the same source url), but not every condition can be
checked, so this is a possible cause for errors. It is also possible to create redirect loops (where A -> B -> C -> A) 
which will end with a browser error (Too many redirects).

So please think before you redirect. 


[edit-validation]: core-redirects_validation.png  "Edit validation"
