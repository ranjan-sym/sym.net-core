package net.symplifier.core.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ranjan on 8/12/15.
 */
public interface User {
  Logger LOGGER = LogManager.getLogger(User.class);

  // All registered roles on the system
  List<Role> registeredRoles = new ArrayList<Role>();

  /**
   * A role represents the type of the access the user can have on the different
   * modules of the system. The modules can use the role to check for the
   * access on the specific resource or the actions.
   *
   * The Role actually uses a 64 bit integer to check for access. Each bit could
   * mean a different type of access level and a combination of bits could be
   * used for more complex access types
   *
   */
  class Role {
    private final long roleFlag;
    private final String name;

    private Role(long roleFlag, String name) {
      this.roleFlag = roleFlag;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public long getFlag() {
      return roleFlag;
    }
  }

  /**
   * Register a role with the given flag and the name. If a role with the given
   * name already exists, the existing role is returned. Note that the role
   * flags must be same in that case otherwise the method doesn't register a role
   * and returns null
   *
   * @param role The role flag
   * @param name The name of the role, for user identification
   * @return The Role object with the given name and the flag
   */
  static Role registerRole(long role, String name) {
    // First check if the role is already there, the roles are considered to be unique by name
    for(Role r: registeredRoles) {
      if(r.name.equals(name)) {
        // ok, we found a role, let's make sure they are the same role
        if (role != r.roleFlag) {
          LOGGER.error("Registering role " + name + " whose role flags do not match with an already registered role");
          return null;
        }
        return r;
      }
    }

    // role not found, create a new one
    Role r = new Role(role, name);
    registeredRoles.add(r);
    return r;
  }

  // Predefined roles
  Role ADMIN = registerRole(-1, "Admin");     // Full access role
  Role GENERAL = registerRole(0, "General");  // No access role

  /**
   * Get the unique system id of the user. Generally database id
   * @return unique id
   */
  Long getId();

  /**
   * Get the username of the user
   * @return
   */
  String getUsername();

  String getName();



  /**
   * Check if the user has the specific type of role access to the given resource
   *
   * @param resource The resource for which the access has to be checked
   * @param role The role to be checked
   * @return {@code true} if the user has the given role
   */
  default boolean checkResourceRole(Resource resource, Role role) {
    return resource.hasRole(this, role);
  }

}
