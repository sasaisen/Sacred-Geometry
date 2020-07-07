package sg.console;

import sg.common.SacredGeometry;

import com.google.common.collect.ImmutableList;

public class SacredGeometryConsole {
  public static void main(String[] args) {
    try {
      SacredGeometry.sacredGeometry(ImmutableList.copyOf(args), System.out::print);
    } catch (IllegalArgumentException e) {
      System.out.print(e.getMessage());
    }
  }
}
