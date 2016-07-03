package net.symplifier.core.application;

/**
 * Created by ranjan on 9/24/15.
 */
public interface Resource {

  String getName();

  Long getUserRoles(User user);


  default boolean hasRole(User user, User.Role role) {
    Long roleFlag = getUserRoles(user);
    return roleFlag != null && ((roleFlag & role.getFlag()) == role.getFlag());
  }
}
