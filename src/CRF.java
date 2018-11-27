import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CRF {
    private static final String NIL = "_$";
    private static final String DEFAULTBIGRAM = "$";
    private int labelNum;
    /**
     * The string of uniFeatureMap like "U00:我"
     * U00 means template
     * 我 is the word by current word through template
     * The Integer is the score of B, I, E, S and so on.
     */
    private HashMap<String, Integer[]> uniFeatureMap;

    /**
     * The string of uniFeatureMap like "B00:我"
     * B00 means template
     * The integer is the label trans score.
     * Integer[][] size is (label number + 1) * (label number + 1)
     */
    private HashMap<String, Integer[][]> biFeatureMap;

    /**
     * The string of unigramMap like "U00", which means the order.
     * The Integer[] like [-2, 0, 1] means choose last, current, next.
     * Maybe it is just one number or two.
     */
    private HashMap<String, Integer[]> unigramMap;

    /**
     * The string of unigramMap like "B00", which means the order.
     * The Integer[] like [-2, 0, 1] means choose last, current, next.
     */
    private HashMap<String, Integer[]> bigramMap;

    /**
     * Like
     * S - 0
     * B - 1 ...
     */
    private HashMap<Character, Integer> labelToIntMap;
    private ArrayList<Character> intToLabelArr;

    public CRF() {
        uniFeatureMap = new HashMap<>();
        biFeatureMap = new HashMap<>();
        unigramMap = new HashMap<>();
        bigramMap = new HashMap<>();
        labelToIntMap = new HashMap<>();
        intToLabelArr = new ArrayList<>();
    }

    /**
     * To train crf
     *
     * @param sentence The sentence to train
     * @param labels   The labels of the sentence
     */
    public int train(Character[] sentence, Character[] labels) {
        Integer[] expectInts = labelsToInts(labels);

        Character[] predictRes = predict(sentence);
        Integer[] predictInts = labelsToInts(predictRes);

        long trainstart = System.nanoTime();
        int len = sentence.length;
        for (int i = 0; i < len; i ++){
            if (! expectInts[i].equals(predictInts[i])){
                //train unigram feature score
                trainUniFeature(sentence, i, predictInts[i], expectInts[i]);

                //train bigram feature score
                trainBiFeature(sentence, expectInts, i ,predictInts[i]);
            }
        }
        long trainend = System.nanoTime();

       //System.out.println("Train time " + (trainend - trainstart));
//        for (int i = 0; i < len; i++) {//label is current label version
//        }
        long predStart = System.nanoTime();
        int num = MyUtil.correctNum(labels, predictRes);
        long predEnd = System.nanoTime();
        //System.out.println("Pred time " + (predEnd - predStart));
        return num;
    }

    /**
     * Use Viterbi Algorithm to predict labels.
     *
     * @param sentence the sentence to predict.
     * @return labels
     */
    public Character[] predict(Character[] sentence) {
        int len = sentence.length;
        //To record the score of max score
        int[][] scoreMatrix = new int[len][labelNum];
        //To record the last label of current word with a certain label.
        int[][] lastLabelMatrix = new int[len][labelNum];

        for (int i = 0; i < len; i++) {
            //get current word simple label score
            int[] tmpCurrentLabelScore = new int[labelNum];
            for (Map.Entry<String, Integer[]> unigEntry : unigramMap.entrySet()) {
                //get feature function
                String featureFunString = geneFeatureFunction(sentence, i, unigEntry);;

                //get current word score
                Integer[] scores = uniFeatureMap.get(featureFunString);
                if (scores == null) {
                    scores = new Integer[labelNum];
                    for (int index = 0; index < labelNum; index ++){
                        scores[index] = 0;
                    }
                    uniFeatureMap.put(featureFunString, scores);
                }
                for (int label = 0; label < labelNum; label++) {
                    //System.out.println(tmpCurrentLabelScore[label]);
                    tmpCurrentLabelScore[label] += scores[label];
                }
            }

            //get the sentence score til last word
            int[] lastWordLabelScore;
            if (i == 0) {
                lastWordLabelScore = new int[labelNum];
            } else {
                lastWordLabelScore = scoreMatrix[i - 1];
            }

            //get label trans score
            Integer[][] labelTransScore = biFeatureMap.get(DEFAULTBIGRAM);
            if (labelTransScore == null){
                labelTransScore = new Integer[labelNum + 1][labelNum + 1];
                for (int j = 0; j < labelNum + 1; j ++){
                    for (int k = 0; k < labelNum + 1; k ++){
                        labelTransScore[j][k] = 0;
                    }
                }
                biFeatureMap.put(DEFAULTBIGRAM, labelTransScore);
            }

            //compute the max score of current word with certain label
            for (int currentLabel = 0; currentLabel < labelNum ; currentLabel ++){
                int currentSelfScore = tmpCurrentLabelScore[currentLabel];

                int[] tmpScores = new int[labelNum];
                for (int lastLabel = 0; lastLabel < labelNum ; lastLabel ++){
                    tmpScores[lastLabel] = currentSelfScore;
                    tmpScores[lastLabel] += lastWordLabelScore[lastLabel] + labelTransScore[lastLabel + 1][currentLabel + 1];
                }
                int lastLabelBest = MyUtil.chooseMax(tmpScores);

                scoreMatrix[i][currentLabel] = tmpScores[lastLabelBest];
                lastLabelMatrix[i][currentLabel] = lastLabelBest;
            }
        }

        //get label res
        Integer[] labelRes = decode(scoreMatrix, lastLabelMatrix);

        return intsToLabels(labelRes);
    }

    private void trainUniFeature(Character[] sentence, int errorLoc, int errorLabel, int expectLabel){
        for (Map.Entry<String, Integer[]> unigEntry : unigramMap.entrySet()) {
            //get feature function
            String featureFunString = geneFeatureFunction(sentence, errorLoc, unigEntry);

            //get unigram feature score
            Integer[] uniScores = uniFeatureMap.get(featureFunString);

            //change uniScores
            uniScores[errorLabel] --;
            uniScores[expectLabel] ++;
        }
    }

    private void trainBiFeature(Character[] sentence, Integer[] expectInts ,int errorLoc, int errorLabel){
        for (Map.Entry<String, Integer[]> bigramEntry : bigramMap.entrySet()) {
            //get feature function
            String featureFunString = geneFeatureFunction(sentence, errorLoc, bigramEntry);
        }
        //by default
        //get bigram feature score
        Integer[][] transScore = biFeatureMap.get(DEFAULTBIGRAM);

        //change trans score
        int biMatchLast = 0;
        int corMatch = expectInts[errorLoc] + 1;
        int errMatch = errorLabel + 1;

        if (errorLoc > 0) biMatchLast = expectInts[errorLoc - 1] + 1;
        transScore[biMatchLast][errMatch] -= 0;
        transScore[biMatchLast][corMatch] += 0;
        //System.out.print(transScore[0][0] + " " + transScore[0][1] + transScore[0][2] + "\n");
    }

    /**
     * generate feature function
     * @param sentence the train sentence
     * @param i the loc of current letter
     * @param templateEntry feature and its scores
     * @return the feature function
     */
    private String geneFeatureFunction(Character[] sentence, int i, Map.Entry<String, Integer[]> templateEntry) {
        StringBuilder featureFun = new StringBuilder();
        int len = sentence.length;
        for (int loc : templateEntry.getValue()) {
            if (i + loc < 0) featureFun.append(NIL);
            else if (i + loc >= len) featureFun.append(NIL);
            else featureFun.append(sentence[loc + i]);
        }
        return featureFun.toString();
    }

    public void loadLabels(String fileName) {
        try {
            BufferedReader fileBuf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
            String line;
            int loc = 0;
            while ((line = fileBuf.readLine()) != null) {
                char label = line.charAt(0);
                labelToIntMap.put(label, loc);
                intToLabelArr.add(label);
                loc++;
            }
            labelNum = labelToIntMap.size();
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTemplate(String fileName) {
        try {
            BufferedReader fileBuf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
            String line;
            while ((line = fileBuf.readLine()) != null) {
                if (line.equals("") || line.charAt(0) == '\uFEFF' || line.charAt(0) == '#') continue;

                String[] splitRes = line.split(":");
                ArrayList<Integer> numbers = new ArrayList<>();

                int index = 0;
                int bound = splitRes[1].lastIndexOf(',');
                while (index < bound) {
                    int bracket = splitRes[1].indexOf('[', index);
                    index = bracket;
                    int comma = splitRes[1].indexOf(',', index);
                    index = comma;
                    String numberString = splitRes[1].substring(bracket + 1, comma);
                    numbers.add(Integer.parseInt(numberString));
                }

                if (splitRes[0].charAt(0) == 'U') {
//                    System.out.println(numbers.toArray(new Integer[0])[0]);
                    unigramMap.put(splitRes[0], numbers.toArray(new Integer[0]));
                } else if (splitRes[0].charAt(0) == 'B') {
                    bigramMap.put(splitRes[0], numbers.toArray(new Integer[0]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer[] decode(int[][] scoreMatrix, int[][] lastLabelMatrix){
        int len = scoreMatrix.length;

        Integer[] labelRes = new Integer[len];

        int maxLabel = MyUtil.chooseMax(scoreMatrix[len - 1]);
        labelRes[len - 1] = maxLabel;

        for (int index = len - 1; index > 0; index --){
            int tmp = lastLabelMatrix[index][maxLabel];
            labelRes[index - 1] = tmp;
            maxLabel = tmp;
        }

        return labelRes;
    }

    public Character[] intsToLabels(Integer[] from){
        int len = from.length;
        Character[] res = new Character[len];
        for (int i = 0; i < len; i ++){
            res[i] = intToLabelArr.get(from[i]);
        }
        return res;
    }

    public Integer[] labelsToInts(Character[] from){
        int len = from.length;
        Integer[] res = new Integer[len];
        for (int i = 0; i < len; i ++){
            res[i] = labelToInt(from[i]);
        }
        return res;
    }

    public int labelToInt(char label) {
        Integer res = labelToIntMap.get(label);
        if (res == null) return -1;
        return res;
    }
}
