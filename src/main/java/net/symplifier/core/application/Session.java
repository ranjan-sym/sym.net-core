package net.symplifier.core.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An application session on a per thread basis
 *
 * Created by ranjan on 8/12/15.
 */
public class Session {


  public interface Listener {

    /**
     * An event callback when a session begins
     *
     * @param session Instance to the session object
     */
    void onSessionBegin(Session session);

    /**
     * An event callback when a session ends
     *
     * @param session Instance to the session object
     */
    void onSessionEnd(Session session);

  }

  public interface Delegation {

    Object getAttribute(String name);

    void setAttribute(String name, Object value);

  }

  private static class DefaultDelegation implements Delegation {
    private final Map<String, Object> objects = new HashMap<>();

    @Override
    public Object getAttribute(String name) {
      return objects.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
      objects.put(name, value);
    }
  }

  private static ThreadLocal<Session> session = new ThreadLocal<>();

  private static Set<Listener> listeners = new HashSet<>();

  public static void addListener(Listener listener) {
    listeners.add(listener);
  }

  public static void removeListener(Listener listener) {
    listeners.remove(listener);
  }


  /* A delegation used to retrieve object if not found in the session */
  private Delegation delegation;

  /* The user for the session */
  private User user;

  private HashMap<Class, Object> attachments = new HashMap<>();

  /**
   * Attaches an object of the specific class type. It might seem redundant to
   * supply the object class as the first parameter but it is necessary since
   * {@link Object#getClass()} will return the child class which may not actually
   * be the class we are expecting
   *
   * @param type The type of object
   * @param object The object to be attached
   * @param <T> The type of object
   */
  public <T> void attach(Class<T> type, T object) {
    attachments.put(type, object);
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttachment(Class<T> type) {
    return (T)attachments.get(type);
  }

  /**
   * Retrieve the attached object based on its type
   *
   * @param type The type of object
   * @param <T> The type of object
   * @return The attached object
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> type) {
    return (T) get().attachments.get(type);
  }




  public static Session start(User user) {
    return start(user, new DefaultDelegation());
  }

  public static Session start(User user, Delegation delegation) {
    Session s = session.get();

    s.user = user;
    s.delegation = delegation;

    for(Listener listener: listeners) {
      listener.onSessionBegin(s);
    }

    return s;
  }

  public static Session get() {
    return session.get();
  }

  public static void end() {
    Session s = session.get();

    for(Listener listener: listeners) {
      listener.onSessionEnd(s);
    }

    s.attachments.clear();
    s.user = null;
    s.delegation = null;
  }



  public User getUser() throws SessionException {
    if (user == null) {
      throw new SessionException("User not available on the session");
    }
    return user;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String name, Class<T> clazz) {
    if (delegation != null) {
      return (T) delegation.getAttribute(name);
    }

    return null;
  }

  public void set(String name, Object object) {
    if (delegation != null) {
      delegation.setAttribute(name, object);
    }
  }



}
