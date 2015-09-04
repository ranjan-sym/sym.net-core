package net.symplifier.core.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A Plugin class to implement Plugin pattern in the application.
 *
 * <p>
 *   A plugin could be loaded in two ways
 *   <ol>
 *     <li><b>Through jar files.</b> The plugin should be bundled in an independent
 *     jar file with a manifest file <b>META-INF/plugin.config</b> and put inside
 *     <b>plugins</b> folder in the application directory. The manifest file
 *     should define {@code plugin.class}, {@code plugin.name},
 *     {@code plugin.version} and {@code plugin.description}. These plugins are
 *     loaded after the {@link Application#onInit(Loader)} and before
 *     {@link Application#onStart()}</li>
 *
 *     <li><b>During application init.</b> The plugin included within the project
 *     could be loaded during the {@link Application#onInit(Loader)} using the
 *     {@link net.symplifier.core.application.Plugin.Loader} instance provided
 *     as the method parameter.</li>
 *   </ol>
 *   One type of plugin should only be loaded once, specially while doing
 *   it through {@link Application#onInit(Loader)}. If the plugin is tried to be
 *   loaded more, it is silently ignored.
 * </p>
 * <p>
 *   Use {@link Module} to make the plugin useful to the {@link Application}
 * </p>
 *
 *<p>
 * Created by ranjan on 6/17/15.
 * </p>
 */
public abstract class Plugin {
  private static final String MANIFEST_FILE = "META-INF/plugin.config";
  private static final String PLUGIN_FOLDER = "plugins";

  private String name;
  private String description;
  private Version version;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Version getVersion() {
    return version;
  }

  public abstract void onInit(Application app);

  public abstract void onStart(Application app);

  public abstract void onStop(Application app);

  public static class Loader {
    private final Map<Class<? extends Plugin>, Plugin> PLUGINS = new LinkedHashMap<>();
    private final Application app;
    private String pluginFolder = PLUGIN_FOLDER;

    Loader(Application app) {
      this.app = app;
    }

    public void setPluginFolder(String folder) {
      pluginFolder = folder;
    }

    public void load(Class<? extends Plugin> pluginClass) {
      load(pluginClass, null);
    }

    private void load(Class<? extends Plugin> pluginClass, Properties properties) {
      // A plugin can be instantiated only once
      if (PLUGINS.containsKey(pluginClass)) {
        Application.LOGGER.error("Plugin for " + pluginClass + " is already loaded");
        return;
      }

      Plugin plugin;
      try {
        plugin = pluginClass.newInstance();
      } catch (InstantiationException e) {
        Application.LOGGER.error("Error while instantiating plugin from " + pluginClass, e);
        return;
      } catch(IllegalAccessException e) {
        Application.LOGGER.error("Plugin class default constructor not accessible for " + pluginClass, e);
        return;
      }

      // register the plugin
      PLUGINS.put(pluginClass, plugin);

      // Load the properties if available
      if (properties != null) {
        plugin.name = properties.getProperty("plugin.name");
        plugin.version = Version.parse(properties.getProperty("plugin.version"));
        plugin.description = properties.getProperty("plugin.description");
      } else {
        // in case the application is loading the plugin directly
        plugin.name = pluginClass.getSimpleName();
        plugin.version = null;
        plugin.description = null;
      }

      // Let the plugin initialize
      Application.LOGGER.trace("Initializing Plugin - " + plugin.name + " - " + pluginClass.getCanonicalName());
      plugin.onInit(app);
    }

    private void load(File jarFile) {
      ClassLoader loader;
      try {
        loader = URLClassLoader.newInstance(new URL[] { jarFile.toURI().toURL()}, getClass().getClassLoader());
      } catch(MalformedURLException e) {
        Application.LOGGER.error("Error while loading plugin file - " + jarFile.getAbsolutePath(), e);
        return;
      }

      InputStream stream = loader.getResourceAsStream(MANIFEST_FILE);
      Properties properties = new Properties();
      try {
        properties.load(stream);
      } catch(IOException e) {
        Application.LOGGER.error("Manifest file - " + MANIFEST_FILE + " not found in the plugin jar - " + jarFile.getAbsolutePath(), e);
        return;
      }

      String pluginClassName = properties.getProperty("plugin.class");
      if (!properties.containsKey("plugin.class")) {
        Application.LOGGER.error("plugin.class not defined in the manifest file in the plugin jar - " + jarFile.getAbsolutePath());
        return;
      }

      Class<?> clazz;
      try {
        clazz = Class.forName(pluginClassName, true, loader);
      } catch (ClassNotFoundException e) {
        Application.LOGGER.error("Plugin main class - " + pluginClassName + " defined in manifest not found in the plugin jar - " + jarFile.getAbsolutePath(), e);
        return;
      }

      if (!Plugin.class.isAssignableFrom(clazz)) {
        Application.LOGGER.error("Plugin class - " + clazz + " is not a Plugin, in the plugin jar - " + jarFile.getAbsolutePath());
      }

      load((Class<? extends Plugin>) clazz, properties);
    }

    void loadPlugins() {
      // go through all the jar files within the plugin folder
      File folder = new File(pluginFolder);
      if (folder.isDirectory()) {
        for(File jarFile:folder.listFiles()) {
          if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
            load(jarFile);
          }
        }
      } else {
        Application.LOGGER.error("Error loading plugins - " + pluginFolder + " is not a directory");
      }
    }

    void startPlugins() {
      for(Plugin plugin:PLUGINS.values()) {
        plugin.onStart(app);
      }
    }

    void stopPlugins() {
      for(Plugin plugin:PLUGINS.values()) {
        plugin.onStop(app);
      }
    }
  }

}
