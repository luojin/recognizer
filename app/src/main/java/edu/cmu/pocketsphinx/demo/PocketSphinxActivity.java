/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.WordModel;
import edu.cmu.pocketsphinx.widgets.DiffColorTextView;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener, Handler.Callback, View.OnClickListener {
    private TextView resultTV;
    private TextView captionTV;
    private Handler handler;
    private Button controlBtn;
		
    private static final String SENTENCE_SEARCH = "sentence";
    private SpeechRecognizer recognizer;

    private static final int MSG_NEW_MESSAGE = 1;
    private static final int MSG_NEW_CAPTION = 2;

    private boolean start = false;

    private List<String> sentenceList = new ArrayList<String>();
    private List<WordModel> wordList = new ArrayList<>();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);

        resultTV = (TextView) findViewById(R.id.result_text);
        captionTV = (TextView) findViewById(R.id.caption_text);
        controlBtn = (Button) findViewById(R.id.controlBtn);
        controlBtn.setOnClickListener(this);
        handler = new Handler(this);

        initData();
        initRecognizer();
        DiffColorTextView tv= (DiffColorTextView) findViewById(R.id.text);
        tv.setText(wordList);
    }

    private void initData(){
        sentenceList.add("what's your name");
        sentenceList.add("how are you");
        sentenceList.add("good morning");
        sentenceList.add("how much are these apples");

        wordList.add(new WordModel("what's"));
        wordList.add(new WordModel("your",false));
        wordList.add(new WordModel("gpqtyidgfbghjklb"));
        wordList.add(new WordModel("?",false));
    }

    private String getNextString(){
        Random random = new Random();
        int i = random.nextInt(sentenceList.size());
        return sentenceList.get(i);
    }

    private void initRecognizer(){
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showCaption("preparing the recognizer");
                controlBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    showCaption("Failed to init recognizer " + result);
                } else {
                    setControlBtn(start);
                    controlBtn.setVisibility(View.VISIBLE);
                    showCaption("say a sentence, click button to get the result.");
                }
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }
    
    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
    	    return;

//        String text = hypothesis.getHypstr();
//        showLog("onPartialResult: " + text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        showLog("onResult: " + hypothesis.getHypstr());
    }

    @Override
    public void onBeginningOfSpeech() {
        showLog("onBeginningOfSpeech");
    }

    /**
     * We stop recognizer here to get a final result
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void onEndOfSpeech() {
        showLog("onEndOfSpeech");
//        stopRecognizer();
        controlBtn.callOnClick();
    }

    private void startRecognizer(){
        stopRecognizer();

//        String nextRead = getNextString();
//        recognizer.addKeyphraseSearch(SENTENCE_SEARCH,nextRead);
//        showCaption(nextRead);

        //recognizer.startListening(SENTENCE_SEARCH,10000); timeout=10s
        recognizer.startListening(SENTENCE_SEARCH); //stop manual
    }

    private void stopRecognizer(){
        recognizer.stop();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                
                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)
                
                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)
                
                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)
                
                .getRecognizer();
        recognizer.addListener(this);

        // Create language model search: N-Gram Models
        File languageModel = new File(assetsDir, "corpus.lm");
        recognizer.addNgramSearch(SENTENCE_SEARCH, languageModel);


    }

    @Override
    public void onError(Exception error) {
        showLog("onError");
        stopRecognizer();
    }

    @Override
    public void onTimeout() {
        showLog("onTimeout");
        stopRecognizer();
    }

    private void showCaption(String text){
        Message msg = Message.obtain();
        msg.what = MSG_NEW_CAPTION;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    private void showLog(String text){
        Message msg = Message.obtain();
        msg.what = MSG_NEW_MESSAGE;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    private void setControlBtn(boolean begin){
        if(!begin)
            controlBtn.setText("start");
        else
            controlBtn.setText("stop");
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what){
            case MSG_NEW_MESSAGE:
                String log = msg.obj+"\n" + resultTV.getText();
                resultTV.setText(log);
                break;
            case MSG_NEW_CAPTION:
                String caption = (String) msg.obj;
                captionTV.setText(caption);
                break;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.controlBtn:
                if(start)
                    stopRecognizer();
                else
                    startRecognizer();

                setControlBtn(!start);
                start = !start;
                break;
        }
    }
}
