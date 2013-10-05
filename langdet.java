import java.util.*;
import java.io.*;
import com.google.common.collect.*;
import java.lang.*;

public class langdet {
   
   public static void main(String[] args) throws IOException {
      
      // reads the name of the language from the language-models folder
      String languageName = "";
      
      // gathers all language word count information in one table here
      Table<String, String, Integer> languageModel = HashBasedTable.create(); 
      
      // tally of all the words in each language, used to calculate probabilities of each word.
      Map<String, Integer> allLanguages = new HashMap<String, Integer>();  
      
      // the probability sum of each word in the train.txt (15 languages).
      Map<String, Double> languageResults = new TreeMap<String, Double>();
      
      // stores mean values and standard deviations learned from the training dataset for 15 languages
      Table<String, Double, Double> normalFactors = TreeBasedTable.create();
      
      // the final results
      Map<String, Double> finalOutput = new TreeMap<String, Double>();
      
      // read all language models files
      File pathName = new File("language-models/");
      int totalCount = 0;
      for (File language : pathName.listFiles()) {
         if (!language.isDirectory()) {
            if (language.getAbsolutePath().contains("unigram")) {
               languageName = language.getName().substring(0, 3);
               Scanner inputLanguage = new Scanner(language);
               while (inputLanguage.hasNextLine()) {
                  String oneLine = inputLanguage.nextLine();
                  Scanner token = new Scanner(oneLine);
                  while (token.hasNext()) {
                     String words = token.next();
                     int tally = Integer.parseInt(token.next());
                     languageModel.put(languageName, words, tally);
                     totalCount += tally;
                  }
               }
               allLanguages.put(languageName, totalCount); 
            }
         }
      }
      
      // learns the train dataset
      train(languageModel, allLanguages, languageResults, normalFactors);
      
      // tests the test dataset
      predict(normalFactors, finalOutput, allLanguages, languageModel);
      
   }
   
   
   public static void train(Table<String, String, Integer> languageModel, Map<String, Integer> allLanguages, Map<String, Double> languageResults, Table<String, Double, Double> normalFactors) throws FileNotFoundException {
      // learns the training dataset 
      File trainingData = new File("train.txt");
      Scanner input = new Scanner(trainingData);
      String training = "";
      while (input.hasNextLine()) {
            training = input.nextLine();
            training = training.replaceAll("[.,!Á´$£?À;:()/ÑÐ'ÇÈ\"]","");
            Scanner trainSentence = new Scanner(training);
            int lang = 0;
            String tokenLang = "";
            String tokenTrain = "";
            double trainCalculation = 0;
            double notFound = 0;
            int v = 0;
            while (trainSentence.hasNext()) {
               v++;  // total number of tokens read
               if (lang == 0) { // first token is the language name
                  tokenLang = trainSentence.next();
                  lang++;
               } else if (trainSentence.hasNext() && lang > 0) {
                  tokenTrain = trainSentence.next();
               }
               if (languageModel.contains(tokenLang, tokenTrain)) { // the probability sum of the train-text.
                  trainCalculation += Math.log((((double) languageModel.get(tokenLang, tokenTrain)) + 1 / ((double) allLanguages.get(tokenLang)) + v));
                  languageResults.put(tokenLang ,trainCalculation);
               } else { // smoothing goes here
                  notFound = Math.log((1 / ((double) allLanguages.get(tokenLang)) + v));
                  if (languageResults.get(tokenLang) != null) {
                     notFound += languageResults.get(tokenLang); // add the smoothing to the previous probability value.
                  }
                  languageResults.put(tokenLang, notFound);
               }
         }
         normalFactors = wordCount(training, normalFactors); // take each line and the normalFactors map.
      } 
   }
   
   // total count of each word in the line
   public static Table<String, Double, Double> wordCount(String line, Table<String, Double, Double> languageModel) {
      Scanner wordCounts = new Scanner(line);
      Map<String, Integer> wordTally = new HashMap<String, Integer>();
      String temp = "";
      String lang = "";
      int first = 0;
      int v = 0;
      while (wordCounts.hasNext()) {
         if (wordCounts.hasNext()) {
            if (first == 0) {
               lang = wordCounts.next();
               first++;
            } else {
               v++;
               temp = wordCounts.next();
               if (wordTally.containsKey(temp)) {
                  wordTally.put(lang, wordTally.get(lang) + 1);
               } else {
                  wordTally.put(lang, 1);
               }
            }
         }
      }
      double muTemp = 0;
      muTemp = (double) wordTally.get(lang) / v;
      languageModel.put(lang, sigma(wordTally.get(lang), muTemp), muTemp);
      return languageModel;
   }
   
   
   public static double sigma(double word, double mu) {
      return Math.sqrt(mu * Math.pow(word - mu, 2));
   }
   
   
   /* gets the probabilities of each word in the testing dataset 
      by taking its probability in certain langauge and the factors in normal distribution */
   public static double normalDistribution(double x, double mu, double sg) {
      return Math.log(Math.pow(1/(sg * Math.sqrt(2 * Math.PI)), -(Math.pow(x - mu, 2)) / (2 * Math.pow(sg, 2))));
   }
   
   public static void predict(Table<String, Double, Double> normalFactors, Map<String, Double> finalOutput,
   Map<String, Integer> allLanguages, Table<String, String, Integer> languageModel) throws IOException {
      File testData = new File("test.txt");
      Scanner testing = new Scanner(testData);
      Map<Double, Double> quick = new TreeMap<Double, Double>();
      String temp = "";
      String highLang = "";
      int identifier = 0;
      while (testing.hasNextLine()) { // next line in the Test.txt.
         if (testing.hasNextLine()) {
            temp = testing.nextLine();
            temp = temp.replaceAll("[.,!Á´$£?À;:()/ÑÐ'ÇÈ\"]","");
            Scanner token = new Scanner(temp);
            int start = 0;
            int v = 0;
            while (token.hasNext()) {
               if (token.hasNext()) {
                  if (start == 0) { // first token is always a number.
                     start++;
                     identifier = token.nextInt();
                  } else {
                     v++;
                     String testingWord = token.next(); // get the next token.
                     double normal = 0;
                     for (String language : allLanguages.keySet()) { // for every language
                        if (languageModel.contains(language, testingWord)) { // if the word in the test.txt is in the language
                           quick = normalFactors.row(language); // get the mu and the standard deviation of that language..
                           for (Double key : quick.keySet()) { // get the mu and the standard deviation of that language.
                              normal = normalDistribution(languageModel.get(language, testingWord) / allLanguages.get(language),
                              quick.get(key), key); // get the normal distribution.
                              if (finalOutput.get(language) != null) {
                                 finalOutput.put(language, (finalOutput.get(language) + normal));
                              } else {
                                 finalOutput.put(language, normal);
                              }
                           }
                        }  else { //smoothing
                           quick = normalFactors.row(language); // get the mu and the standard deviation of that language..
                           for (Double key : quick.keySet()) { // get the mu and the standard deviation of that language.
                              normal = normalDistribution((double)1 / allLanguages.get(language),
                              quick.get(key), key); // get the normal distribution.
                              if (finalOutput.get(language) != null) {
                                 finalOutput.put(language, (finalOutput.get(language) + ((double)1/allLanguages.get(language)) ));
                              } else {
                                 finalOutput.put(language, ((double)1/allLanguages.get(language)));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
         Double high = -.0000009;
         System.out.println(temp); // print the line we are testing
         for (String p : finalOutput.keySet()) { // for every langauge in the finalOuput
            System.out.println(p + "\t" + finalOutput.get(p));    // print the language and the calculation
            if (finalOutput.get(p) < high) { // get the highest language
               high = finalOutput.get(p);
               highLang = p;
            } else if (finalOutput.get(p) == high) {
               highLang = "unknown";
            }
         }
         System.out.println("result" + "\t" + highLang);
         System.out.println();
         finalOutput.clear(); // reset
      }
   }
   
}