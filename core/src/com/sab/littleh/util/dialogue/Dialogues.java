package com.sab.littleh.util.dialogue;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class Dialogues {
   public static String languageKey = "en/";
   private static Set<String> dialogueKeys = new HashSet<>();
   
   public static Dialogue getDialogue(String fileName) {
      return getDialogue(fileName, true);
   }
   
   public static Dialogue getDialogue(String fileName, boolean spendable) {
      if (spendable) {
         if (dialogueKeys.contains(fileName)) return null;
         dialogueKeys.add(fileName);
      }

      Scanner scanner;
      InputStream s = Dialogues.class.getResourceAsStream("/dialogues/" + languageKey + fileName);
      try {
         scanner = new Scanner(s);
      } catch (Exception e) {
         System.out.println("Dialogue \"/dialogues/" + languageKey + fileName + "\" not found");
         e.printStackTrace();
         return null;
      }

      ArrayList<String> text = new ArrayList<String>();
      
      String next = "";
      boolean dontRead = false;
      
      String prevSpeaker = "";
      
      List<String> characterNames = new ArrayList<>();
      List<String> fileNames = new ArrayList<>();
      
      while (scanner.hasNext()) {
         if (scanner.hasNext("@script")) break;
         scanner.next();
         fileNames.add(scanner.next());
         characterNames.add(scanner.nextLine().substring(1));
      }
      scanner.next();
      while (scanner.hasNext()) {
         if (!dontRead) next = scanner.next();
         dontRead = false;
         if (next.startsWith("@")) {
            String toAdd = next.substring(1);
            if (!prevSpeaker.equals(toAdd)) next = "\\cN(" + (Integer.parseInt(toAdd) - 1) + ")";
            else next = "";
            next += scanner.next();
            prevSpeaker = toAdd;
            while (!next.startsWith("@")) {
               if (!scanner.hasNext()) {
                  toAdd += next;
                  break;
               }
               toAdd += next + " ";
               next = scanner.next();
               dontRead = true;
            }
            text.add(toAdd);
         } else {
            malformedDialogue(fileName, next);
         }
      }
            
      String[] dialogue = new String[text.size()];
      for (int i = 0; i < text.size(); i++) {
         dialogue[i] = text.get(i);
      }
      String[] characters = new String[characterNames.size()];
      characterNames.toArray(characters);
      String[] files = new String[fileNames.size()];
      fileNames.toArray(files);
      return new Dialogue(dialogue, characters, files);
   }
   
   public static boolean hasUnspentDialogue(String fileName) {
      return !dialogueKeys.contains(fileName);
   }
   
   public static void resetDialogues() {
      dialogueKeys.clear();
   }
   
   public static void malformedDialogue(String fileName, String at) {
      throw new RuntimeException("Malformed dialogue in " + fileName + ". Line: \"" + at + "\" contains error.");
   }
}