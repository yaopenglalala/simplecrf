import java.io.*;
import java.util.ArrayList;

public class TrainSet {
    private ArrayList<Character[]> sentences;
    private ArrayList<Character[]> labels;

    public TrainSet(){
         sentences = new ArrayList<>();
         labels = new ArrayList<>();
    }

    public int getSize(){
        return sentences.size();
    }

    /**
     * To get train data
     * @param index The index of your expected data
     * @return Array. the first is sentence , the second is labels.
     */
    public Character[][] getData(int index){
        Character[][] res = new Character[2][0];
        res[0] = sentences.get(index);
        res[1] = labels.get(index);
        return res;
    }

    public void insert(String sentence, String labels){
        ArrayList<Character> sentenceChars = new ArrayList<>();
        ArrayList<Character> labelChars = new ArrayList<>();

        int len = sentence.length();

        for (int i = 0 ; i < len; i++){
            sentenceChars.add(sentence.charAt(i));
            labelChars.add(labels.charAt(i));
        }

        this.sentences.add(sentenceChars.toArray(new Character[0]));
        this.labels.add(labelChars.toArray(new Character[0]));
    }

    public void loadFile(String fileName){
         try{
             BufferedReader fileBuf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
             String line;
             ArrayList<Character> sentenceChars = new ArrayList<>();
             ArrayList<Character> labelsChars = new ArrayList<>();
             while ( (line = fileBuf.readLine()) != null){
                 if (line.equals("")){//a new sentence
                     sentences.add(sentenceChars.toArray(new Character[0]));
                     labels.add(labelsChars.toArray(new Character[0]));
                     //System.out.println(MyUtil.getStringFromList(sentenceChars));
                     //System.out.println(MyUtil.getStringFromList(labelsChars));
                     sentenceChars = new ArrayList<>();
                     labelsChars = new ArrayList<>();
                 } else {
                     sentenceChars.add(line.charAt(0));
                     labelsChars.add(line.charAt(2));
                 }
             }
             sentences.add(sentenceChars.toArray(new Character[0]));
             labels.add(labelsChars.toArray(new Character[0]));
         } catch (FileNotFoundException e) {
             System.out.println("File doesn't exist!");
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
}
