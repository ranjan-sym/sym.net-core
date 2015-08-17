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

    void onSessionBegin(Session session);

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
