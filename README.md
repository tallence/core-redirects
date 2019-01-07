Core-Redirects
==============

:warning: WARNING: THIS MODULE IS NOT YET PRODUCTION-READY.

Introduction
------------

Core-Redirects is a CoreMedia extension for managing redirects from within CoreMedia Studio.

Setup
-----

1. For each Site, you need to add a folder containing the redirects (should be at `Options/Settings/Redirects`,
configured in `redirectmanager.redirects.path`). TODO: why not create the folder programmatically on cae startUp?
2. The schema.xml of the content-config-set must contain these two fields: 
  `<field name="source" type="string" indexed="true" stored="true"/>`
  `<field name="sourceUrlType" type="string" indexed="true" stored="true"/>`
