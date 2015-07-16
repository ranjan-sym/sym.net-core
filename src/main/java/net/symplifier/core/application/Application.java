package net.symplifier.core.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * The Application class provided by the core Library. Create a class in your
 * project to extend from {@link Application} class and override its
 * {@link #onInit(Plugin.Loader)} and {@link #onStart()} methods. The
 * {@link #onInit(Plugin.Loader)} provides an opportunity to initialize the
 * application before the {@link Plugin}s are initialized. The {@link #onStart()}
 * provides an opportunity to start the application after the {@link Plugin}s
 * have been initialized.
 *
 * <p>
 *   Include {@code application.config} file inside <b>META-INF</b> folder in
 *   the resources and define:
 *   <ul>
 *     <li><b>app.class</b> - The fully qualified name of the application class</li>
 *     <li><b>app.name</b> - The name of the application. You could use maven artifact id here</li>
 *     <li><b>app.version</b> - The application version. Could use maven project version</li>
 *     <li><b>app.description</b> - A description for the application. Could use maven project version</li>
 *   </ul>
 *   The properties other than <b>app.class</b> are not being used at the moment.
 *   and are here for future.
 * </p>
 * <p>
 *   The application could also define properties in the manifest file (
 *   {@ocde application.config} which are available through the getters
 *   {@link #get(String, boolean)}, {@link #get(String, double)}, {@link #get(String, int)},
 *   and {@link #get(String, String)}. These properties could be overridden
 *   by defining a local properties file <b>settings.ini</b> in the application
 *   working directory.
 * </p>
 * <p>
 * Created by ranjan on 6/10/15.
 * </p>
 */
public abstract class Application {
  public final static Logger LOGGER = LogManager.getRootLogger();

  public final static Charset DEFAULT_CHARSET = Charset.forName("utf-8");
  private final static String RES_APPLICATION_CONFIG = "META-INF/application.config";
  private final static String RES_APPLICATION_CONFIG_LOCAL = "settings.ini";

  /* The application entry point */
  public static void main(String args[]) {
    String appClassStr = null;
    String appVersion = null;
    String appName = null;
    String appDescription = null;

    Properties manifestProperties = null;
    try {
      InputStream stream = Application.class.getClassLoader().getResourceAsStream(RES_APPLICATION_CONFIG);

      assert(stream != null):RES_APPLICATION_CONFIG + " file not found in the class path";


      manifestProperties = new Properties();
      manifestProperties.load(stream);

      // Retrieve the application information from the application config file
      appClassStr = manifestProperties.getProperty("app.class");
      appVersion = manifestProperties.getProperty("app.version");
      appName = manifestProperties.getProperty("app.name");
      appDescription = manifestProperties.getProperty("app.description");

    } catch (IOException e) {
      assert(true):RES_APPLICATION_CONFIG + " file not found in the class path.";
    }

    // Gather properties from commandline
    for(String arg:args) {
      if (arg.startsWith("app.")) {
        String[] parts = arg.split("=", 2);

        if(parts.length == 2) {
          manifestProperties.setProperty(parts[0], parts[1]);
        }
      }
    }

    Application app;
    try {
      Class<?> appClass = Class.forName(appClassStr);
      assert(Application.class.isAssignableFrom(appClass)):appClassStr + " is not an Application.";
      app = (Application)appClass.newInstance();

      app.manifestProperties = manifestProperties;
      app.name = appName;
      app.version = Version.parse(appVersion);
      app.description = appDescription;

      app.start();

    } catch(ClassNotFoundException e) {
      assert(true):appClassStr + " is not available.";
    } catch(InstantiationException e) {
      assert(true):appClassStr + " instance cannot be created." + e.getMessage();
    } catch(IllegalAccessException e) {
      assert(true):appClassStr + " constructor is not accessible." + e.getMessage();
    }


  }

  /* The reference to the one and only application instance */
  private static Application APP;

  private String name;        /* Name of the application */
  private Version version;
  private String description;
  private Properties manifestProperties;

  private final Plugin.Loader pluginLoader = new Plugin.Loader(this);
  private final Map<Class<? extends Module>, Module> MODULES = new HashMap<>();

  /* Application specific objects to share with other libraries, like Database Connections */
  private final HashMap<String, Object> appObjects = new HashMap<>();

  protected Application() {
    assert(APP == null): "There can only be one Application instance. " + APP.name + " is already instantiated.";
    APP = this;
  }

  public static Application app() {
    return APP;
  }

  /**
   * Get the name of the application
   *
   * @return The name of the application
   */
  public String getName() {
    return name;
  }

  /**
   * Get the version of the application
   *
   * @return The version of the application
   */
  public Version getVersion() {
    return version;
  }

  /**
   * Get a description of the application
   *
   * @return The description of the application
   */
  public String getDescription() {
    return description;
  }

  /* Loads all the plugins and start the application */
  private void start() {
    // Let's see if a folder path has been specified then we set that to current directory
    String appFolder = manifestProperties.getProperty("app.folder");
    if (appFolder != null && !appFolder.isEmpty()) {
      // Let's check if its a valid folder
      File file = new File(appFolder);
      if (file.isDirectory()) {
        System.setProperty("user.dir", file.getAbsolutePath());
        System.out.println("Setting working directory to " + file.getAbsolutePath());
      } else {
        System.out.println("Invalid application folder - " + appFolder);
      }
    }

    // Load properties from settings.ini file if available overriding the
    // manifest properties
    File file = new File(RES_APPLICATION_CONFIG_LOCAL);
    if (file.isFile()) {
      try {
        FileInputStream stream = new FileInputStream(file);
        manifestProperties.load(stream);
      } catch(IOException ex) {
        LOGGER.error("Could not read local configuration file - " + file.getAbsolutePath(), ex);
      }
    }

    // Let the application do the initialization
    onInit(pluginLoader);

    // Load all the plugins from the folder
    pluginLoader.loadPlugins();

    // Let the application start
    onStart();

    pluginLoader.startPlugins();
  }

  /**
   * Retrieve a String property defined in the manifest file ({@code application.config}) and
   * overridden in the local configuration file ({@code settings.ini}). If the properties are not
   * defined, the defaultValue supplied is returned.
   * @param name The name of the property to be retrieved
   * @param defaultValue The default value to be returned if the property is not defined
   * @return The property value as String
   */
  public String get(String name, String defaultValue) {
    if (manifestProperties.containsKey(name)) {
      return manifestProperties.getProperty(name);
    } else {
      return defaultValue;
    }
  }

  /**
   * Retrieve a integer property defined in the manifest file ({@code application.config}) and overridden
   * in the local configuration file ({@code settings.ini}). If the property is not defined or the
   * defined value could not be interpreted as integer, the default value
   * supplied is returned.
   * @param name The name of the property to be retrieved
   * @param defaultValue The default value to be returned if the property is
   *                     not defined or the value is not an integer
   * @return The property value as integer
   */
  public int get(String name, int defaultValue) {
    try {
      if (manifestProperties.containsKey(name)) {
        return Integer.parseInt(manifestProperties.getProperty(name));
      } else {
        return defaultValue;
      }
    } catch(NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Retrieve a double property defined in the manifest file ({@code application.config} and overridden in
   * the local configuration file ({@code settings.ini}). If the property is not defined or the
   * defined value could not be interpreted as double, the default value
   * supplied is returned.
   * @param name The name of the property to be retrieved
   * @param defaultValue The default value to be returned if the property is
   *                     not defined or the value is not a double
   * @return The property value as double
   */
  public double get(String name, double defaultValue) {
    try {
      if (manifestProperties.containsKey(name)) {
        return Double.parseDouble(manifestProperties.getProperty(name));
      } else {
        return defaultValue;
      }
    } catch(NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Retrieve a boolean property defined in the manifest file ({@code application.config} and overridden in
   * the local configuration file ({@code settings.ini}). If the property is not defined, the default value
   * supplied is returned.
   * <p>
   *   Only the text <b>{@code true}</b> (case insensitive) is considered
   *   {@code true} and all other values including {@code null} are considered
   *   {@code false}
   * </p>
   * @param name The name of the property to be retrieved
   * @param defaultValue The default value to be returned if the property is
   *                     not defined.
   * @return The property value as boolean
   */
  public boolean get(String name, boolean defaultValue) {
    if (manifestProperties.containsKey(name)) {
      return Boolean.parseBoolean(manifestProperties.getProperty(name));
    } else {
      return defaultValue;
    }
  }


  /**
   * Retrieve an application wise shared object
   * @param clazz The expected class of an object
   * @param name The name of the object
   * @param <T> Parameterization for better access
   * @return Returns the stored object within application instance
   */
  public <T> T getObject(Class<T> clazz, String name) {
    Object o = appObjects.get(name);
    if (o.getClass().isAssignableFrom(clazz)) {
      return (T)o;
    }
    return null;
  }

  /**
   * Set an application wise shared object
   * @param name The name of the object
   * @param object The object to be stored in the application instance
   */
  public void setObject(String name, Object object) {
    appObjects.put(name, object);
  }

  /**
   * Register a {@link Module} with the application. The {@link Module} provides
   * a mechanism for {@link Plugin} to work in collaboration with the other
   * {@link Plugin} or the {@link Application}.
   *
   * <p>A module could be registered only once.</p>
   *
   * @param moduleClass The module class to be registered
   * @param module The module object of the moduleClass to be registered
   */
  public void registerModule(Class<? extends Module> moduleClass, Module module) {
    assert(!MODULES.containsKey(moduleClass)):"A module of type " + moduleClass + " is already registered";
    MODULES.put(moduleClass, module);
  }

  /**
   * Retrieve a registered {@link Module} object
   *
   * @param moduleClass The class of the module to be retrieved
   * @param <T> Safe typecasting to the required module class
   * @return The {@link Module} object
   */
  @SuppressWarnings("unchecked")
  public <T extends Module> T getModule(Class<T> moduleClass) {
    return (T) MODULES.get(moduleClass);
  }

  /**
   * End the application life.
   */
  public void stop() {
    pluginLoader.stopPlugins();

    // Also trigger the exit event
    for(ExitHandler exitHandler:exitHandlers) {
      exitHandler.onExit(this);
    }

    onStop();
  }

  /**
   * Let the application initialize before any {@link Plugin}
   *
   * @param pluginLoader A helper object to load any {@link Plugin} included
   *                     within the project and need to be loaded by the
   *                     application instead of a jar file in the plugins
   *                     folder
   */
  public abstract void onInit(Plugin.Loader pluginLoader);

  /**
   * Let the application start after all the {@link Plugin}s have initialized.
   *
   * The plugins are started after the application starts
   */
  public abstract void onStart();

  /**
   * Do the clean up before the application ends.
   */
  public abstract void onStop();


  private final Set<ExitHandler> exitHandlers = new HashSet<>();

  public void addExitHandler(ExitHandler exitHandler) {
    exitHandlers.add(exitHandler);
  }

}
