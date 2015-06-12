package net.symplifier.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ranjan on 6/12/15.
 */
public class CSV {
  public static List<String> split(String line) {
    return split(line, ',');
  }
  public static List<String> split(String line, char separator) {
    ArrayList<String> res = new ArrayList<>();

    String item = "";
    boolean inQuote = false;
    String whiteSpace = "";
    for(int i=0; i<line.length(); ++i) {
      char ch = line.charAt(i);
      if(inQuote) {
        if (ch == '"') {
          inQuote = false;
        } else {
          item += ch;
        }
      } else {
        if (ch == separator) {
          res.add(item);
          item = "";
          whiteSpace = "";
        } else if (ch == '"' && item.isEmpty()) {
          inQuote = true;
        } else if (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
          if (!item.isEmpty()) {
            whiteSpace += ch;
          }
        } else {
          if (whiteSpace.length() > 0) {
            item += whiteSpace;
            whiteSpace = "";
          }
          item += ch;
        }
      }
    }
    res.add(item);
    return res;
  }
}
