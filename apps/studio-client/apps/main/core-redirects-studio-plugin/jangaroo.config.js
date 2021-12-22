const { jangarooConfig } = require("@jangaroo/core");

module.exports = jangarooConfig({
  type: "code",
  sencha: {
    name: "com.coremedia.blueprint__core-redirects-studio-plugin",
    namespace: "com.tallence.core.redirects.studio",
    studioPlugins: [
      {
        mainClass: "com.tallence.core.redirects.studio.RedirectManagerStudioPlugin",
        name: "Redirect Manager Extension",
      },
    ],
  },
  appManifests: {
    en: {
      categories: [
        "Content",
      ],
      cmServiceShortcuts: [
        {
          cmKey: "redirect-manager",
          cmCategory: "Content",
          name: "Redirect manager",
          url: "",
          cmAdministrative: true,
          cmService: {
            name: "launchSubAppService",
            method: "launchSubApp",
          },
        },
      ],
    },
  },
  command: {
    build: {
      ignoreTypeErrors: false
    },
  },
});
