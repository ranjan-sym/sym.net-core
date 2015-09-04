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

    /**
     * An event callback when a session is trying to commit
     *
     * @param session Instance to the session object
     */
    void onSessionCommit(Session session);

    /**
     * An event callback when a session is trying to rollback to its
     * previous commit or the start of the session
     *
     * @param session Instance to the session object
     */
    void onSessionRollback(Session session);


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
  private final Delegation delegation;

  /* The user for the session */
  private final User user;

  private HashMap<Object, Object> attachments = new HashMap<>();

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

  public void attach(Object key, Object value) {
    attachments.put(key, value);
  }

  public <T> T getAttachment(Object key, Class<T> type) {
    return (T) attachments.get(key);
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

  @SuppressWarnings("unchecked")
  public static <T> T get(Object obj, Class<T> type) {
    return (T) get().attachments.get(obj);
  }


  private Session(User user, Delegation delegation) {
    this.user = user;
    this.delegation = delegation;
  }

  public static Session start(User user) {
    return start(user, new DefaultDelegation());
  }

  public static Session start(User user, Delegation delegation) {
    Session s = new Session(user, delegation);
    session.set(s);

    for(Listener listener: listeners) {
      listener.onSessionBegin(s);
    }

    return s;
  }

  public static Session get() {
    return session.get();
  }

  public void commit() {
    for(Listener listener: listeners) {
      listener.onSessionCommit(this);
    }
  }

  public void rollback() {
    for(Listener listener: listeners) {
      listener.onSessionRollback(this);
    }
  }

  public void end() {
    //Session s = session.get();
    for(Listener listener: listeners) {
      listener.onSessionEnd(this);
    }
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
