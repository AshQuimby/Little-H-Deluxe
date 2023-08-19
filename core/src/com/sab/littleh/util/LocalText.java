package com.sab.littleh.util;

import com.sab.littleh.util.dialogue.Dialogues;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LocalText {
    private final String[] text;
    private final String wholeText;
    private final String languageKey;

    LocalText(String path) {
        this.languageKey = Localization.languageKey;
        Scanner scanner;
        InputStream s = Dialogues.class.getResourceAsStream(path);
        try {
            scanner = new Scanner(s);
        } catch (Exception e) {
            System.out.println("Localized text \"" + path + "\" not found");
            text = new String[0];
            wholeText = "";
            return;
        }

        List<String> textList = new ArrayList<>();

        scanner.useDelimiter(Pattern.compile("(\\v%split%\\v)"));

        StringBuilder wholeText = new StringBuilder("");

        while (scanner.hasNext()) {
            String next = scanner.next();
            wholeText.append(next);
            textList.add(next);
        }

        this.wholeText = wholeText.toString();
        text = textList.toArray(new String[0]);
    }

    public String[] getText() {
        return text;
    }

    public String getWholeText() {
        return wholeText;
    }

    @Override
    public String toString() {
        return getWholeText();
    }
}
