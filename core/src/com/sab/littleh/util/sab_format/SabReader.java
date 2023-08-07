package com.sab.littleh.util.sab_format;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * For all your SAB file reading needs.
 */
public class SabReader {

    /**
     * Creates a com.sab_format.SabData object from a File containing all the properties in the specified file.
     * @param file
     * The stream to be scanned and read
     * @return
     * The created com.sab_format.SabData object
     */
    public static SabData read(File file) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new SabParsingException("File does not exist");
        }
        return read(stream);
    }

    /**
     * Creates a com.sab_format.SabData object from an InputStream containing all the properties in the specified stream.
     * @param stream
     * The stream to be scanned and read
     * @return
     * The created com.sab_format.SabData object
     */
    public static SabData read(InputStream stream) {
        Scanner scanner = new Scanner(stream);
        scanner.useDelimiter(""); // Consume one character at a time

        List<SabToken> tokens = new ArrayList<>();
        while (scanner.hasNext()) {
            try {
                char c = scanner.next().charAt(0);
                if (c == '@') {
                    scanner.useDelimiter(" ");
                    String identifier = scanner.next();
                    tokens.add(new SabToken(SabTokenType.Ident, identifier));
                    parseValue(scanner, tokens);
                } else if (c != '\n' && c != ' ' && c != 13) {
                    break;
                }
            } catch (NoSuchElementException | StringIndexOutOfBoundsException e) {
                scanner.close();
                throw new SabParsingException("Reached end of file while parsing");
            }
        }

        SabData data = new SabData();
        for (int i = 0; i < tokens.size(); i++) {
            SabToken token = tokens.get(i);
            switch (token.getType()) {
                case Ident -> {
                    SabToken next = tokens.get(i + 1);
                    switch (next.getType()) {
                        case Ident, CloseParen -> {
                            scanner.close();
                            throw new SabParsingException(String.format("Unexpected token type %s", token.getType()));
                        }
                        case Val -> data.insertValue(token.getValue(), new SabValue(next.getValue()));
                        case OpenParen -> {
                            int parens = 1;
                            List<String> elements = new ArrayList<>();
                            elements.add("[");
                            i++;
                            while (i < tokens.size()) {
                                i++;
                                if (i >= tokens.size()) {
                                    scanner.close();
                                    throw new SabParsingException("Reached end of file while parsing");
                                }
                                SabToken item = tokens.get(i);
                                switch(item.getType()) {
                                    case Ident -> {
                                        scanner.close();
                                        throw new SabParsingException(String.format("Unexpected token type: %s", item.getType()));
                                    }
                                    case OpenParen -> {
                                        parens++;
                                        elements.add("[");
                                    }
                                    case CloseParen -> {
                                        parens--;
                                        elements.add("]");
                                    }
                                    case Val -> elements.add(item.getValue());
                                }

                                if (parens == 0) {
                                    StringBuilder builder = new StringBuilder();
                                    for (int j = 0; j < elements.size(); j++) {
                                        String element = elements.get(j);
                                        String nextElement = j < elements.size() - 1 ? elements.get(j + 1) : null;

                                        builder.append(element);
                                        if (!element.equals("[") && nextElement != null && !nextElement.equals("]")) {
                                            builder.append(", ");
                                        }
                                    }
                                    data.insertValue(token.getValue(), new SabValue(builder.toString()));
                                    break;
                                }
                            }
                        }
                    }
                }
                case OpenParen -> {

                }
            }
        }
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public static Scanner skipSabPreface(Scanner scanner) {
        Pattern delimiter = scanner.delimiter();
        scanner.useDelimiter("");
        while (scanner.hasNext("@")) {
            scanner.nextLine();
        }
        scanner.useDelimiter(delimiter);
        return scanner;
    }

    /**
     * Reads a file and returns the first instance of a property.
     * @param file
     * The file to parse
     * @param property
     * The property to get
     * @return
     * The string value associated with the property. If none found or file does not exist, return null.
     */
    public static SabValue getProperty(File file, String property) {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                if (scanner.next().substring(1).equals(property)) {
                    return new SabValue(scanner.nextLine().substring(1));
                }
            }
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    private static void parseValue(Scanner scanner, List<SabToken> tokens) {
        scanner.useDelimiter("");
        scanner.next();
        char c = scanner.next().charAt(0);
        if (c == '[') {
            parseArray(scanner, tokens);
        } else {
            tokens.add(new SabToken(SabTokenType.Val, c + scanner.nextLine()));
        }
    }

    private static void parseArray(Scanner scanner, List<SabToken> tokens) {
        scanner.useDelimiter("");

        StringBuilder buffer = new StringBuilder();
        boolean readingValue = false;
        char c = scanner.next().charAt(0);
        tokens.add(new SabToken(SabTokenType.OpenParen, null));
        while (true) {
            if (Character.isWhitespace(c)) {
                c = scanner.next().charAt(0);
                continue;
            }

            if (c == '[') {
                if (readingValue) {
                    throw new SabParsingException("Unexpected token: " + c);
                }
                else parseArray(scanner, tokens);
            } else if (c == ']') {
                if (!buffer.toString().isBlank())
                tokens.add(new SabToken(SabTokenType.Val, buffer.toString().strip()));
                break;
            } else if (c == ',') {
                if (!buffer.isEmpty()) {
                    readingValue = false;
                    tokens.add(new SabToken(SabTokenType.Val, buffer.toString().strip()));
                    buffer = new StringBuilder();
                }
            } else {
                readingValue = true;
                buffer.append(c);
            }
            c = scanner.next().charAt(0);
        }
        tokens.add(new SabToken(SabTokenType.CloseParen, null));
    }
}
