package edu.cmu.pocketsphinx;

/**
 * Created by luo on 2015/12/14.
 */
public class WordModel {
    private String word;
    private boolean correct;

    public WordModel(String w){
        word=w;
        correct=true;
    }

    public WordModel(String w, boolean correct){
        word=w;
        this.correct=correct;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
