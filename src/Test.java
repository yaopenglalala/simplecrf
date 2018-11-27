public class Test {
    public static void main(String[] args) {
        TrainSet trainSet = new TrainSet();
        trainSet.loadFile("train.utf8");
        CRF crf = new CRF();
        crf.loadTemplate("template.utf8");
        crf.loadLabels("labels.utf8");

        int all = 0;
        for (int j = 0 ; j < 5; j ++){
            int cor = 0;
            for (int i = 0; i < trainSet.getSize() - 500 ; i ++){
                Character[][] data = trainSet.getData(i);
                //   System.out.println(TrainSet.getStringFromList(data[0]));
                cor += crf.train(data[0], data[1]);
                if (j == 0) all += data[1].length;
            }
            System.out.println((double) cor / all);
        }
        System.out.println("ok");

        all = 0;
        int cor = 0;
        for (int  i = trainSet.getSize() - 500; i < trainSet.getSize(); i ++){
            Character[][] data = trainSet.getData(i);
            //   System.out.println(TrainSet.getStringFromList(data[0]));
            cor += MyUtil.correctNum(crf.predict(data[0]), data[1]);
            all += data[1].length;
        }
        System.out.println((double) cor / all);
    }
}
