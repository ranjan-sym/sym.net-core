package net.symplifier.core.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ranjan on 6/11/15.
 */
public class HexDump {

  public static void dump(byte[] data) {
    try {
      dump(data, 0, data.length, System.out);
    } catch(IOException e) {
      // failed to write to console. Can't help it
    }
  }
  public static void dump(byte[] data, OutputStream out) throws IOException {
    dump(data, 0, data.length, out);
  }

  public static void dump(byte[] data, int offset, int length) {
    try {
      dump(data, offset, length, System.out);
    } catch(IOException e) {
      // failed to write to console. Can't help it
    }
  }

  public static void dump(byte[] data, int offset, int length, OutputStream out) throws IOException {

    for(int i=0; i<length; i+=16) {
      String address = String.format("%08d", i);
      print(out, "    ");
      print(out, address);

      print(out, "   ");

      // Write the bytes in hex string first
      for(int count=0; count<16; ++count) {
        if (count == 8) {
          print(out, " ");
        }

        if (count + i < length) {
          print(out, String.format("%02x ", data[i + count + offset]));
        } else {
          print(out, "   ");
        }
      }

      print(out, "    ");

      // Write the byte value if possible
      for(int count=0; count<16; ++count) {
        if (count == 8) {
          print(out, " ");
        }

        if (count+i<length) {
          byte b = data[i+count+offset];
          if (b > 32 && b <128) {
            out.write(b);
          } else {
            out.write('.');
          }
        } else {
          out.write(' ');
        }
      }

      out.write('\r');
      out.write('\n');


    }
  }

  private static void print(OutputStream out, String str) throws IOException {
    for(int i=0; i<str.length(); ++i) {
      out.write((byte)str.charAt(i));
    }
  }

}
