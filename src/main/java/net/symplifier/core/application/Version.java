package net.symplifier.core.application;

public class Version implements Comparable<Version> {
	public String major;
	public String minor;
	public String revision;
	
	public Version(int major, int minor, int revision) {
		this.major = Integer.toString(major);
		this.minor = Integer.toString(minor);
		this.revision = Integer.toString(revision);
	}

  public Version(String major, String minor, String revision) {
    this.major = major;
    this.minor = minor;
    this.revision = revision;
  }

  public static Version parse(String versionText) {
    String parts[] = versionText.split("\\.", 3);

    String major = parts[0];
    String minor = parts.length > 1 ? parts[1]:"0";
    String revision = parts.length > 2 ? parts[2]:"0";

    return new Version(major, minor, revision);
  }

	/**
	 * Checks if the version is greater than, less than or equals to the other version.
	 *  
	 * @param other The other version to compare to
	 * @return 1 if this version is greater, -1 if this version is less otherwise if 0 if both are same
	 * 			returns 1 if the other version is null
	 */
	public int compareTo(Version other) {
		if (other == null) {
			return 1;
		}

    int res = major.compareTo(other.major);
    if (res == 0) {
      res = minor.compareTo(other.minor);
      if (res == 0) {
        res = revision.compareTo(other.revision);
      }
    }

		return res;
	}
	
	/**
	 * Checks if the versions are same
	 * @param other Version to check
	 * @return true if the versions are same otherwise false
	 */
	public boolean equals(Version other) {
		if (other == null) {
			return false;
		}

		
		return major.equals(other.major) && minor.equals(other.minor) && revision.equals(other.revision);
	}
	
	@Override
	public String toString() {
		return major + "." + minor + "." + revision;
	}
}
