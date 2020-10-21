package sg.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class FileUtil {
  static final String RESOURCE_PATH = "src/main/resources/";

  static final String SG_0 = "sg0";
  static final String SG_1 = "sg1";
  static final String SG_2 = "sg2";
  static final String SG_3 = "sg3";
  static final String SG_4 = "sg4";
  static final String SG_5 = "sg5";
  static final String SG_6 = "sg6";
  static final String SG_7 = "sg7";
  static final String SG_8 = "sg8";
  static final String SG_9 = "sg9";

  static final String FAILED = "f";

  static byte[] rollSetToBytes(Multiset<Integer> rollSet) {
    for (Entry<Integer> roll : rollSet.entrySet()) {
      if (roll.getCount() > 15) {
        throw new IllegalArgumentException(
            "Roll sets with more than 15 of a single roll are not supported for this process.");
      }
    }

    byte[] bytes = new byte[4];
    for (int i = 0; i < 8; i++) {
      byte b = (byte) rollSet.count(i+1);

      if (i % 2 == 0) {
        bytes[i/2] = (byte) (b << 4);
      } else {
        bytes[i/2] = (byte) (bytes[i/2] | b);
      }
    }
    return bytes;
  }

  static Multiset<Integer> bytesToRollSet(byte[] bytes) {
    ImmutableMultiset.Builder<Integer> builder = ImmutableMultiset.builder();

    for (int i = 0; i < 4; i++) {
      byte b = bytes[i];

      builder.addCopies(i * 2 + 1, (b & 0xf0) >> 4);
      builder.addCopies(i * 2 + 2, b & 0xf);
    }

    return builder.build();
  }

  static byte[] expressionToBytes(PostfixExpression expression) {
    String expressionString = expression.toString();
    byte[] bytes = new byte[(expressionString.length() + 1) / 2];

    for (int i = 0; i < expressionString.length(); i++) {
      char c = expressionString.charAt(i);
      byte b = switch (c) {
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (byte) (c - '0');
        case '+' -> (byte) 10;
        case '-' -> (byte) 11;
        case '*' -> (byte) 12;
        case '/' -> (byte) 13;
        default ->    // Should never occur, expression should catch this at creation
            throw new IllegalArgumentException(
                "Expression " + expressionString + " contains an invalid character (" + c + ").");
      };

      if (i % 2 == 0) {
        bytes[i/2] = (byte) (b << 4);
      } else {
        bytes[i/2] = (byte) (bytes[i/2] | b);
      }
    }

    if (expressionString.length() % 2 == 1) {
      bytes[expressionString.length()/2] = (byte) (bytes[expressionString.length()/2] | 0xf);
    }

    return bytes;
  }

  static PostfixExpression bytesToExpression(byte[] bytes) {
    StringBuilder builder = new StringBuilder();
    for (byte b : bytes) {
      int high = (b & 0xf0) >> 4;
      int low = b & 0xf;
      builder.append(
          switch (high) {
            case 0,1,2,3,4,5,6,7,8,9 -> high;
            case 10 -> '+';
            case 11 -> '-';
            case 12 -> '*';
            case 13 -> '/';
            default -> "";
          });
      builder.append(
          switch (low) {
            case 0,1,2,3,4,5,6,7,8,9 -> low;
            case 10 -> '+';
            case 11 -> '-';
            case 12 -> '*';
            case 13 -> '/';
            default -> "";
          });
    }
    return PostfixExpression.create(builder.toString());
  }

  static void writeSolutionFile(String filename, Map<Multiset<Integer>, PostfixExpression> map) throws IOException {
    try (OutputStream out = new FileOutputStream(RESOURCE_PATH + filename)) {
      for (Map.Entry<Multiset<Integer>, PostfixExpression> entry : map.entrySet()) {
        byte[] rollSetBytes = rollSetToBytes(entry.getKey());
        out.write(rollSetBytes);

        byte[] expressionBytes = expressionToBytes(entry.getValue());
        out.write(expressionBytes);
      }
    }
  }

  static Map<Multiset<Integer>, PostfixExpression> readSolutionFile(String filename) throws IOException {
    try (InputStream in = new FileInputStream(RESOURCE_PATH + filename)) {
      ImmutableMap.Builder<Multiset<Integer>, PostfixExpression> map = ImmutableMap.builder();

      while (in.available() > 0) {
        byte[] rollSetBytes = new byte[4];
        if (in.read(rollSetBytes) != 4) {
          throw new IllegalStateException();
        }
        Multiset<Integer> rollSet = bytesToRollSet(rollSetBytes);

        byte[] expressionBytes = new byte[rollSet.size()];
        if (in.read(expressionBytes) != rollSet.size()) {
          throw new IllegalStateException();
        }
        PostfixExpression expression = bytesToExpression(expressionBytes);

        map.put(rollSet, expression);
      }
      return map.build();
    }
  }

  static void writeFailureFile(String filename, Set<Multiset<Integer>> set) throws IOException {
    try (OutputStream out = new FileOutputStream(RESOURCE_PATH + filename + FAILED)) {
      for (Multiset<Integer> entry : set) {
        byte[] rollSetBytes = rollSetToBytes(entry);
        out.write(rollSetBytes);
      }
    }
  }

  static Set<Multiset<Integer>> readFailureFile(String filename) throws IOException {
    try (InputStream in = new FileInputStream(RESOURCE_PATH + filename + FAILED)) {
      ImmutableSet.Builder<Multiset<Integer>> set = ImmutableSet.builder();

      while (in.available() > 0) {
        byte[] rollSetBytes = new byte[4];
        if (in.read(rollSetBytes) != 4) {
          throw new IllegalStateException();
        }
        Multiset<Integer> rollSet = bytesToRollSet(rollSetBytes);

        set.add(rollSet);
      }
      return set.build();
    }
  }

}
