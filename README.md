# Core Redirects

Core-Redirects is a [CoreMedia](http://www.coremedia.com) extension for managing redirects from within CoreMedia Studio.
The project is [Apache-2.0](./LICENSE) licensed, so it can be easily used and modified.


## Features

- Static redirects from an absolute URL or a regular expression
- Conditional redirects: redirect only, if:
  - the original page returns a 404
  - the source contains certain URL parameters
- Features can be activated for members of certain CoreMedia groups.
- Import of redirects from a CSV file
- Optimized caching in the CAE to minimize response times
- Configuration of redirects per site

Per default, requests can only be redirected to a CoreMedia content. Redirects to external URLs can be done in two ways:
* linking a document of the type `CMExternalLink`
* using targetUrls for certain redirects, instead of linking contents. This should be used with care, because it might redirect to a non-existing page. The nice thing about linking contents is that the redirect target will exist as long as the redirect exists.


## Feedback

If you have any problems, questions, ideas, or feedback, please contact us or
[create an issue](https://github.com/tallence/core-redirects/issues). 


## Compatibility

See the [releases](https://github.com/tallence/core-redirects/releases) for tested compatibilities.

For earlier versions, take a look into these branches:
- CoreMedia CMS-9 (v18.10). See the branch: `1904.2-compatible`.


## Usage

There is a [simple user guide](docs/userguide.md) available.


## Integration

Integrate the Code in your CoreMedia Blueprint Workspace:

### GIT submodules

```
git submodule add https://github.com/tallence/core-redirects.git modules/extensions/core-redirects
```

Activate the extension using the respective CoreMedia Content Cloud activation
scheme for the version in use, like e.g. the management tool for the latest
releases:

```
mvn extensions:sync -Denable=core-redirects -f workspace-configuration/extensions/pom.xml
```
 

### Alternative way

Of course, it would also be possible to download the repo and copy the files
into your Blueprint-Workspace global Extensions-Folder `modules/extensions`.
While you do not have to bother with submodules, you also lose the direct
connection to the original repository and the option to easily contribute or
merge new things.


## Setup

1. Change the `groupId` and `versionId` of all pom.xml to your project values.

2. The [schema.xml](../../modules/search/solr-config/src/main/app/configsets/content/conf/schema.xml) (this link only
works, if this code is within a blueprint workspace) of the content config-set must contain these two fields: 
     `<field name="source" type="string" indexed="true" stored="true"/>`
     `<field name="sourceUrlType" type="string" indexed="true" stored="true"/>`

3. Redirects are stored in/read from a site-specific folder (`Options/Settings/Redirects`), to which users need to have
permissions to edit and publish Redirect documents.


## Configuration

These options can be configured:
1. `core.redirects.filter.keepParams` if enabled, the query parameters of the source URL will be appended to the redirect target URL.
2. `core.redirects.path` the site-relative path where the redirect-documents are stored
3. `core.redirects.cache.parallel.site.recompute.threads` Maximum number of threads for complete site index updates. Will be used at CAE start up.
4. `core.redirects.cache.parallel.item.recompute.threads` Maximum number of threads for item (single redirect) updates. Will be used for changes in a running CAE.
5. `core.redirects.permissions.targetUrlGroup` The group, which allows members to describe a redirect target with a URL instead of a document. Should be used with care. Use "*" to allow this for editor.
6. `core.redirects.permissions.regexGroup` The group, which allows members to use the sourceType "regexp". Should be used with care.
