package com.example.penderiko.speechrecognizertestbed;

import android.content.Intent;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by penderiko on 31.03.16.
 */
public class SRListener implements RecognitionListener {

    public class Result {
        public Result(String msg,double certainty) {
            this.msg=msg;
            this.certainty=certainty;
        }
        public String msg;
        public double certainty;
        public String toString() {
            return msg + " (" + (int)(certainty*100) + "%)";
        }
    }


    private ConditionVariable resultAvaliable;

    private Result[] lastResult = { new Result("",0F) };
    private SpeechRecognizerTestMain main;

    public String what() {
        return lastResult[0].msg;
    }

    /*public String listen() {
        Log.i("Listener","listening...");
        resultAvaliable.close();
        main.sro.startListening(main.SRIntent);
        resultAvaliable.block();
        main.sro.stopListening();
        return what();
    }*/

    public Result[] getResults() {
        return lastResult;
    }

    public SRListener(SpeechRecognizerTestMain main) {
        this.main=main;
        this.resultAvaliable = new ConditionVariable();
        main.sro.setRecognitionListener(this);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i("Listener","ReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i("Listener", "BeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i("Listener","BufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i("Listener","EndOfSpeech");
    }

    @Override
    public void onError(int error) {
        lastResult= new Result[]{new Result("Error : " + error, 1.0)};
        resultAvaliable.open();
        Log.e("Listener","Error");
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> msgs = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        float[] certs = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        Result[] res = new Result[msgs.size()];
        for(int i=0;i<msgs.size();++i) {
            res[i] = new Result(msgs.get(i), certs[i]);
        }
        lastResult=res;
        //resultAvaliable.open();
        ((TextView)main.findViewById(R.id.textOut)).append(what() + "\n");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i("Listener","partialResult");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i("Listener","Event: " + eventType);
    }
}
