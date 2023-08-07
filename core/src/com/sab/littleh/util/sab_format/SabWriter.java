package com.sab.littleh.util.sab_format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * For all your SAB file writing needs.
 */
public class SabWriter {

    /**
     * Writes a com.sab_format.SabData object to a file containing all the properties in the specified com.sab_format.SabData object.
     * @param file
     * The file or file path to be written to
     * @param data
     * The data to be written
     */
    public static void write(File file, SabData data) throws IOException {
        new PrintWriter(file).close(); // Clears the file or creates a new one if it doesn't exist
        FileWriter writer = new FileWriter(file);
        StringBuilder buffer = new StringBuilder();
        for (String ident : data.getValues().keySet()) {
            buffer.append(String.format("@%s %s\n", ident, data.getValue(ident)));
        }
        String rawData = buffer.toString();
        writer.write(rawData);
//        char[] characters = rawData.toCharArray();
//
//        int indents = 0;
//        for (int i = 0; i < characters.length; i++) {
//            char c = characters[i];
//
//            boolean newLine = false;
//            if (c == '[') {
//                indents++;
//                newLine = true;
//            } else if (c == ']') {
//                indents--;
//                writer.write("\n" + "\t".repeat(indents));
//                newLine = i < characters.length - 1 && characters[i + 1] != ',';
//            } else if (c == ',') {
//                newLine = true;
//            } else if (c == ' ' && characters[i - 1] == ',') continue;
//            writer.write(c);
//            if (newLine) {
//                writer.write("\n" + "\t".repeat(indents));
//            }
//        }
        writer.close();
    }
}
