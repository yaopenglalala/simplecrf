import java.util.ArrayList;

public class MyUtil {
    public static int correctNum(Character[] example, Character[] predict){
        int len = predict.length;
        int res = 0;
        for (int i = 0; i < len; i++){
            if (example[i].equals(predict[i])) res ++;
        }
        return res;
    }

    public static String getStringFromList(ArrayList<Character> list){
        return getStringFromList(list.toArray(new Character[0]));
    }

    public static String getStringFromList(Character[] list){
        StringBuilder res = new StringBuilder();
        for (Character aList : list) {
            res.append(aList);
        }
        return res.toString();
    }

    /**
     * To choose the max location of int array
     * @param origin array
     * @return the location of max value
     */
    public static int chooseMax(int[] origin){
        int loc = 0;
        int max = origin[loc];

        for (int i = 0; i < origin.length; i ++){
            if (origin[i] > max) loc = i;
        }
        return loc;
    }

    public static int chooseMax(Integer[] origin){
        int loc = 0;
        int max = origin[loc];

        for (int i = 0; i < origin.length; i ++){
            if (origin[i] > max) loc = i;
        }
        return loc;
    }
}
