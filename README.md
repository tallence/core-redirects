Core Redirects
==============

Introduction
------------

Core-Redirects is a [CoreMedia](http://www.coremedia.com) extension for managing redirects from within CoreMedia Studio.
The project is [Apache-2.0](./LICENSE) licensed, so it can be easily used and modified.

**Features:**

- Static redirects from an absolute url or a regular expression to a content
- Conditional redirects (redirect only, if the original page returns a 404)
- Import of redirects from a CSV file
- Optimized caching in the CAE to minimize response times
- Configuration of redirects per site

Requests can only be redirected to a CoreMedia content, but the regular `LinkFormatter` is used to create the target
link, so for example `CMExternalLink`s can be configured to redirect to their target url.


Getting started
===============

The extension is always kept compatible with the newest CoreMedia version (currently cmcc-10-2104.3). The redirect 
manager is also known to work with cms-9-1904, cms-9-1901 and cms-9-1810 (https://github.com/tallence/core-redirects/tree/cm9-1904).

There is a [simple user guide](docs/userguide.md) available.

Integrate the Code in your CoreMedia Blueprint Workspace
--------------------------------------------------------

You can integrate the extension in two ways:

**1. Git SubModule**

Add this repo or your fork (recommended) as a Git Submodule to your existing CoreMedia Blueprint-Workspace in the
extensions-folder. This way, you will be able to merge new commits made in this repo back to your fork.

This is the recommended approach because you will also be able to develop quickly, performing a make on the sources with
a running studio- or cae-webapp.
 
**2. Copy files**

Download the repo and copy the files into your Blueprint-Workspace Extension-Folder.
This way you won't be able to merge new commits made in this repo back to yours. But if you do not like Git Submodules,
you don't have to deal with them. 


Setup
-----
1. Change the groupId and versionID of all pom.xml to your project values.

2. The [schema.xml](../../modules/search/solr-config/src/main/app/configsets/content/conf/schema.xml) (this link only
works, if this code is within a blueprint workspace) of the content config-set must contain these two fields: 
     `<field name="source" type="string" indexed="true" stored="true"/>`
     `<field name="sourceUrlType" type="string" indexed="true" stored="true"/>`

3. Redirects are stored in/read from a site-specific folder (`Options/Settings/Redirects`), to which users need to have
permissions to edit and publish Redirect documents.

Configuration
-------
These options can be configured:
1. `core.redirects.filter.keepParams` if enabled, the query params of the source url will be appended to the redirect target url.
2. `core.redirects.path` the site-relative path where the redirect-documents are stored
3. `core.redirects.cache.parallel.site.recompute.threads` Maximum number of threads for complete site index updates. Will be used at CAE startUp.
4. `core.redirects.cache.parallel.item.recompute.threads` Maximum number of threads for item (single redirect) updates. Will be used for changes in a running CAE.  

That's it. Have fun ;) If you have any problems, questions, ideas, or feedback please contact us or
[create an issue](https://github.com/tallence/core-redirects/issues). 
