<<<<<<< HEAD:langdet_word
=======
============
>>>>>>> e17bc47dfbce12708a9a5c028e13420d6836ae6e:README.md

Language Detector by Word Frequencies

August 31, 2013

Language: Java

Approach:

1. Use the guava to create a table to gather all the word counts for 15 languages together.

2. Create the train method for learn the language model using the train dataset:

1) Read the training dataset (lang-label, sample sentence), we use the word count (given language) files to get the probability of each word (one HashMap), and put the probability into the equation for the Naîve Bayes assumption to get the total probability for the probability of the language given the text,

2) As we've got the probability of the language given the text, and we assume that this Naîve Bayes classifier for these 15 languages follows the normal distribution, so there are two more factors we need, the mean value of the probability and the standard deviation of the probabilities of each word. So we get the mean value, μ = word count in the train sentence / length of the train sentence, and the standard deviation, σ, as well.

3) For those word in the learning process that does not appear in the language-model, we apply the smoothing, that to assign it the same probability as a singleton (a word that occurred once in the model) for the language under consideration.

4) In conclusion, till now, we've finished the learning process, and we get the values of the probabilities for each language, and the mean values and standard deviations for each language. Thus, now for each language we have {P(lang|text), μ_lang, σ_lang}.

3. Create the predict method for the final language identification using the test dataset:

1)  In the test process, we read the test dataset, only the sentences without the indicated language. We process each line of the sentence word by word.

2) During the processing, we need to get the value for the log-probaility of each word and add them up to get a value for each language. We read each word for the test sentence, and run from language by language. First, we get the probability of the given word in the test data found on the language-models file (We use the test sentence run on all the 15 languages file by file). Then we use the μ and σ of the checked language, to get the probability of the language given the text.

3) The probability of each word is actually not we expect. We will need to log each probability we get from each word, and add them up to get the total probability of each language.

4) Finally, we report 15 values of the probabilities of each language for each sentence in the test dataset. The biggest value is the result for our predicted language.

Results:

1	eng
2	gla
3	pol
4	ita
5	spa
6	fra
7	por
8	dut
9	deu
10	nob
11	swe
12	dan
13	swh
14	tgl
15	fin
16	swh
17	por
18	dut
19	spa
20	gla
21	fra
22	swe
23	tgl
24	eng
25	ita
26	dan
27	pol
28	deu
29	fin
30	nob
