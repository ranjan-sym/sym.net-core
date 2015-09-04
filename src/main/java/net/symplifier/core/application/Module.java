package net.symplifier.core.application;

/**
 * A marker interface for defining Modules for {@link Plugin} to synchronize
 * with the {@link Application}.
 *
 * <p>
 * Create a interface in a common project extending this Module. Define
 * all the funtionalities available through the module. The application
 * should implement this Module and during the {@link Application#onInit(Plugin.Loader)}
 * register this module using {@link Application#registerModule(Class, Module)}.
 * </p>
 * <p>
 * The plugin should use {@link Application#getModule(Class)} to get
 * instance of the module to use the Module.
 * </p>
 * <p>
 * Created by ranjan on 6/17/15.
 * </p>
 */
public interface Module {
}
