package com.sab.littleh.util.dialogue;

import com.sab.littleh.util.Localization;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class Dialogues {
   private static Set<String> dialogueKeys = new HashSet<>();
   private static final Pattern validCharacterId = Pattern.compile("([0-9])");
   private static String[] dialogue;
   private static String[] characters;
   private static String[] files;
   private static Map<String, Integer> breakPoints;

   public static Dialogue getDialogue(String fileName) {
      return getDialogue(fileName, true);
   }
   public static boolean fillDialogue(String fileName) {
      Scanner scanner;
      InputStream s = Dialogues.class.getResourceAsStream("/local/dialogues/" + Localization.languageKey + fileName);
      try {
         scanner = new Scanner(s);
      } catch (Exception e) {
         System.out.println("VnDialogue \"/local/dialogues/" + Localization.languageKey + fileName + "\" not found");
         e.printStackTrace();
         return false;
      }

      List<String> text = new ArrayList<>();

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

      int line = 1;
      breakPoints = new HashMap<>();

      scanner.next();
      while (scanner.hasNext()) {
         if (!dontRead) next = scanner.next();
         dontRead = false;
         if (next.startsWith("@")) {
            String tag = next.substring(1);
            String toAdd = tag;
            if (tag.equals("force_end")) {
               toAdd = "1\\eD(true)";
               text.add(toAdd);
               continue;
            } else if (!validCharacterId.matcher(tag).matches()) {
               breakPoints.put(tag, line);
               continue;
            }
//            if (!prevSpeaker.equals(toAdd)) next = "\\cN(" + (Integer.parseInt(toAdd) - 1) + ")";
//            else next = "";
            next = "";
            scanner.useDelimiter("");
            scanner.next();
//            prevSpeaker = toAdd;
            int parens = 0;
            while (!next.equals("@")) {
               if (!scanner.hasNext()) {
                  toAdd += next;
                  break;
               }
               if (next.equals("\n")) {
                  next = scanner.next();
                 continue;
               } else if (next.equals("\\")) {
                  String escape = scanner.next();
                  if (escape.equals("\\")) {
                     next = "\\";
                  } else if (escape.equals("n")) {
                     next = "\n";
                  } else {
                     next += escape;
                  }
               }
               if (next.equals("(")) parens++;
               if (next.equals(")")) parens--;
               if (parens == 0) {
                  if (next.equals(",")) next += "\\wF(5)";
                  else if (next.equals(".")) next += "\\wF(15)";
                  else if (next.equals("?")) next += "\\wF(15)";
                  else if (next.equals(":")) next += "\\wF(5)";
                  else if (next.equals(";")) next += "\\wF(10)";
               }
               toAdd += next;
               next = scanner.next();
               dontRead = true;
            }
            text.add(toAdd);
            scanner.reset();
            line++;
            if (scanner.hasNext())
               next += scanner.next();
         } else {
            malformedDialogue(fileName, next);
         }
      }

      dialogue = new String[text.size()];
      for (int i = 0; i < text.size(); i++) {
         dialogue[i] = text.get(i);
      }
      characters = new String[characterNames.size()];
      characterNames.toArray(characters);
      files = new String[fileNames.size()];
      fileNames.toArray(files);
      return true;
   }
   
   public static Dialogue getDialogue(String fileName, boolean spendable) {
      if (spendable) {
         if (dialogueKeys.contains(fileName)) return null;
         dialogueKeys.add(fileName);
      }

      // Fills the arrays
      if (!fillDialogue(fileName))
         return null;

      return new Dialogue(dialogue, characters, files, breakPoints);
   }

//   public static VnDialogue getVnDialogue(String fileName) {
//       Fills the arrays
//      if (!fillDialogue(fileName))
//         return null;
//
//      return new VnDialogue(dialogue, characters, files, breakPoints);
//   }
   
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