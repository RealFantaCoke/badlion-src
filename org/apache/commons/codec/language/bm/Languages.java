package org.apache.commons.codec.language.bm;

import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.codec.language.bm.NameType;

public class Languages {
   public static final String ANY = "any";
   private static final Map LANGUAGES = new EnumMap(NameType.class);
   private final Set languages;
   public static final Languages.LanguageSet NO_LANGUAGES;
   public static final Languages.LanguageSet ANY_LANGUAGE;

   public static Languages getInstance(NameType nameType) {
      return (Languages)LANGUAGES.get(nameType);
   }

   public static Languages getInstance(String languagesResourceName) {
      Set<String> ls = new HashSet();
      InputStream langIS = Languages.class.getClassLoader().getResourceAsStream(languagesResourceName);
      if(langIS == null) {
         throw new IllegalArgumentException("Unable to resolve required resource: " + languagesResourceName);
      } else {
         Scanner lsScanner = new Scanner(langIS, "UTF-8");

         try {
            boolean inExtendedComment = false;

            while(lsScanner.hasNextLine()) {
               String line = lsScanner.nextLine().trim();
               if(inExtendedComment) {
                  if(line.endsWith("*/")) {
                     inExtendedComment = false;
                  }
               } else if(line.startsWith("/*")) {
                  inExtendedComment = true;
               } else if(line.length() > 0) {
                  ls.add(line);
               }
            }
         } finally {
            lsScanner.close();
         }

         return new Languages(Collections.unmodifiableSet(ls));
      }
   }

   private static String langResourceName(NameType nameType) {
      return String.format("org/apache/commons/codec/language/bm/%s_languages.txt", new Object[]{nameType.getName()});
   }

   private Languages(Set languages) {
      this.languages = languages;
   }

   public Set getLanguages() {
      return this.languages;
   }

   static {
      for(NameType s : NameType.values()) {
         LANGUAGES.put(s, getInstance(langResourceName(s)));
      }

      NO_LANGUAGES = new Languages.LanguageSet() {
         public boolean contains(String language) {
            return false;
         }

         public String getAny() {
            throw new NoSuchElementException("Can\'t fetch any language from the empty language set.");
         }

         public boolean isEmpty() {
            return true;
         }

         public boolean isSingleton() {
            return false;
         }

         public Languages.LanguageSet restrictTo(Languages.LanguageSet other) {
            return this;
         }

         public String toString() {
            return "NO_LANGUAGES";
         }
      };
      ANY_LANGUAGE = new Languages.LanguageSet() {
         public boolean contains(String language) {
            return true;
         }

         public String getAny() {
            throw new NoSuchElementException("Can\'t fetch any language from the any language set.");
         }

         public boolean isEmpty() {
            return false;
         }

         public boolean isSingleton() {
            return false;
         }

         public Languages.LanguageSet restrictTo(Languages.LanguageSet other) {
            return other;
         }

         public String toString() {
            return "ANY_LANGUAGE";
         }
      };
   }

   public abstract static class LanguageSet {
      public static Languages.LanguageSet from(Set langs) {
         return (Languages.LanguageSet)(langs.isEmpty()?Languages.NO_LANGUAGES:new Languages.SomeLanguages(langs));
      }

      public abstract boolean contains(String var1);

      public abstract String getAny();

      public abstract boolean isEmpty();

      public abstract boolean isSingleton();

      public abstract Languages.LanguageSet restrictTo(Languages.LanguageSet var1);
   }

   public static final class SomeLanguages extends Languages.LanguageSet {
      private final Set languages;

      private SomeLanguages(Set languages) {
         this.languages = Collections.unmodifiableSet(languages);
      }

      public boolean contains(String language) {
         return this.languages.contains(language);
      }

      public String getAny() {
         return (String)this.languages.iterator().next();
      }

      public Set getLanguages() {
         return this.languages;
      }

      public boolean isEmpty() {
         return this.languages.isEmpty();
      }

      public boolean isSingleton() {
         return this.languages.size() == 1;
      }

      public Languages.LanguageSet restrictTo(Languages.LanguageSet other) {
         if(other == Languages.NO_LANGUAGES) {
            return other;
         } else if(other == Languages.ANY_LANGUAGE) {
            return this;
         } else {
            Languages.SomeLanguages sl = (Languages.SomeLanguages)other;
            Set<String> ls = new HashSet(Math.min(this.languages.size(), sl.languages.size()));

            for(String lang : this.languages) {
               if(sl.languages.contains(lang)) {
                  ls.add(lang);
               }
            }

            return from(ls);
         }
      }

      public String toString() {
         return "Languages(" + this.languages.toString() + ")";
      }
   }
}
