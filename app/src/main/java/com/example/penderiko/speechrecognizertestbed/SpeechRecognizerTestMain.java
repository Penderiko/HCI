package com.example.penderiko.speechrecognizertestbed;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

public class SpeechRecognizerTestMain extends AppCompatActivity {

    private Intent SRIntent;
    private static final int SPEECH_2_TEXT_REQUEST = 0;
    private static final int SPEECH_2_TEXT_CONFIRMATION = 1;
    private Random rnd = new Random();
    private TextToSpeech tts;
    private String match;
    private String number;
    //private ConditionVariable ttsInitialized = new ConditionVariable(false);
   // private SpeechRecognizer sro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognizer_test_main);
        SRIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SRIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "de-AT");
        //sro = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());


        findViewById(R.id.btSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(SRIntent, SPEECH_2_TEXT_REQUEST);
            }
        });
 /*   }

    @Override
    protected void onResume() {
        Log.i("OnResume", "entered");
        super.onResume();*/
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            String[] msgs = {"Hallo? Hallo?", "Au, nicht so fest", "Tadaaa"};
            //Voice[] voices;
            //int utID = 0;

            @Override
            public void onInit(int status) {
                Log.i("TTS", "initialized");
                if (status == TextToSpeech.SUCCESS) {
                    //ttsInitialized.open();
                    //voices= (Voice[])tts.getVoices().toArray();
                    Button drck = (Button) findViewById(R.id.btDrck);
                    drck.setEnabled(true);
                    drck.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //tts.setVoice(voices[rnd.nextInt(voices.length)]);
                            tts.setPitch(rnd.nextFloat());
                            tts.setSpeechRate(rnd.nextFloat());
                            tts.speak(msgs[rnd.nextInt(3)], TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        //Log.i("OnPause", "entered");
        //ttsInitialized.close();
        //tts.shutdown();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        Log.d("onActivityResult", "entered");
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_2_TEXT_REQUEST:

                //ttsInitialized.block(15000);
                if (resultCode == RESULT_OK) { /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpeechRecognizerTestMain.this.getApplicationContext(),
                                "Spracherkennung fehlgeschlagen / resCode: "+resultCode, Toast.LENGTH_SHORT).show();
                    }
                })*/

                    List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                    TextView tv = (TextView) findViewById(R.id.textOut);
                    tv.setText("Ergebnis: \n ");

                    String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection, null, null, null, null);
                    match = null;
                    float conf = 0.0F;
                    int inam=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int inum=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    Log.i("hi",""+cursor.getCount());
                    if (cursor.getCount() > 0)
                        while (cursor.moveToNext())
                            //tv.append(cursor.getString(0));
                            for (int i = 0; i < confidence.length; ++i) {
                                if (result.get(i).toLowerCase().contains(cursor.getString(inam).toLowerCase()))
                                    /*result.get(i).toLowerCase().contains(cursor.getString(1)))*/ {
                                    match = cursor.getString(inam);
                                    number = cursor.getString(inum);
                                    conf = confidence[i];
                                    break;
                                }
                            }

                    if (match != null) {
                        tts.speak("Wollen Sie ", TextToSpeech.QUEUE_FLUSH, null);
                        //tts.playSilence(250, TextToSpeech.QUEUE_ADD, null);
                        tts.speak(match, TextToSpeech.QUEUE_ADD, null);
                        //tts.playSilence(200, TextToSpeech.QUEUE_ADD, null);
                        tts.speak(" anrufen?", TextToSpeech.QUEUE_ADD, null);
                        tv.append("Contacts match: " + match + " (" + conf * 100 + "%) \n");
                    }
                    tv.append(result.get(0) + " (" + confidence[0] * 100 + "%) \n");
                    startActivityForResult(SRIntent, SPEECH_2_TEXT_CONFIRMATION);
                }
                break;
            case SPEECH_2_TEXT_CONFIRMATION:
                if (resultCode == RESULT_OK) {
                    List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

                    float conf=0.0F;
                    for (int i=0;i<confidence.length;++i)
                        if (result.get(i).equalsIgnoreCase("ja"))
                            conf+=confidence[i];
                    ((TextView)findViewById(R.id.textOut)).append("ja: " + conf*100 + "% \n");
                    if (conf>0.4) {
                        ((TextView)findViewById(R.id.textOut)).append("rufe an: " + number + "\n");
                        Intent callInt = new Intent(Intent.ACTION_CALL);
                        callInt.setData(Uri.parse("tel:" + number));
                        if (callInt.resolveActivity(getPackageManager()) != null) {
                            startActivity(callInt);
                        }
                    }
                }

        }
    }
}




