package com.sab.littleh.util;

public class Localization {
    public static String languageKey = "en/";
    public static LocalText getLocalizedTextFile(String path) {
        return new LocalText("/local/texts/" + languageKey + path);
    }

    public static String[] getWholeTexts(LocalText[] array) {
        String[] wholeTexts = new String[array.length];
        for (int i = 0; i < wholeTexts.length; i++) {
            wholeTexts[i] = array[i].getWholeText();
        }
        return wholeTexts;
    }

    public static String[] getTexts(int index, LocalText[] array) {
        String[] texts = new String[array.length];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = array[i].getText()[index];
        }
        return texts;
    }

    public static String[][] getTexts(LocalText[] array) {
        String[][] texts = new String[array.length][];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = array[i].getText();
        }
        return texts;
    }
}
