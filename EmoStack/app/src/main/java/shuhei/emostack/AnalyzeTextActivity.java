package shuhei.emostack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCallback;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AnalyzeTextActivity extends AppCompatActivity {
    private IamAuthenticator authenticator;
    private final String apikey = "jOUhZaEYY5q1pPGWBPuV4ho8E82I9S3wu0nEUEGVDCUg";
    private final String url = "https://api.us-south.tone-analyzer.watson.cloud.ibm.com/instances/0ba9f067-ffc6-4154-9b1b-e124cdd08c8c";
    private Button analyzeButton;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUserId;
    private FirebaseFirestore db;
    private EditText userInput;
    private String textToAnalyze;
    private double score_anger;
    private double score_fear;
    private double score_joy;
    private double score_sadness;
    private double score_analytical;
    private double score_confident;
    private double score_tentative;
    private Intent intent;
    private Map<String, Object> diary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_text);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView yearMonth = (TextView)findViewById(R.id.yearMonth);

        intent = this.getIntent();
        String date = intent.getStringExtra("date");
        yearMonth.setText(date);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser != null){
            mUserId = mFirebaseUser.getUid();
        }

        IamAuthenticator authenticator = new IamAuthenticator(apikey);
        final ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2020-02-22",authenticator);
        toneAnalyzer.setServiceUrl(url);

        analyzeButton = (Button)findViewById(R.id.analyze_button);

        db = FirebaseFirestore.getInstance();

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput = (EditText)findViewById(R.id.user_input);
                textToAnalyze = userInput.getText().toString();

                ToneOptions toneOptions = new ToneOptions.Builder()
                        .text(textToAnalyze)
                        .build();

                toneAnalyzer.tone(toneOptions).enqueue(new ServiceCallback<ToneAnalysis>() {
                    @Override
                    public void onResponse(Response<ToneAnalysis> response) {
                        ToneAnalysis toneAnalysis = response.getResult();
                        JSONObject result = null;
                        try{
                            result = new JSONObject(toneAnalysis.toString());
                            JSONObject documentTone = result.getJSONObject("document_tone");
                            JSONArray sentence_tones = result.getJSONArray("sentences_tone");

                            double sum_anger =0;
                            double sum_fear =0;
                            double sum_joy =0;
                            double sum_sadness =0;
                            double sum_analytical =0;
                            double sum_confident =0;
                            double sum_tentative =0;
                            int num_anger = 0;
                            int num_fear = 0;
                            int num_joy = 0;
                            int num_sadness = 0;
                            int num_analytical = 0;
                            int num_confident = 0;
                            int num_tentative = 0;
                            for(int index = 0; sentence_tones.length() > index; index++){
                                JSONObject sentence_tone = sentence_tones.getJSONObject(index);
                                JSONArray tones = sentence_tone.getJSONArray("tones");
                                if(tones.isNull(0)) {
                                }
                                else{
                                    for(int i = 0; i < tones.length(); i++){
                                        JSONObject tone = tones.getJSONObject(i);
                                        String tone_id = tone.getString("tone_id");
                                        double tone_score = tone.getDouble("score");
                                        switch (tone_id){
                                            case "anger":{
                                                sum_anger += tone_score;
                                                num_anger++;
                                                break;
                                            }
                                            case "fear":{
                                                sum_fear += tone_score;
                                                num_fear++;
                                                break;
                                            }
                                            case "joy":{
                                                sum_joy += tone_score;
                                                num_joy++;
                                                break;
                                            }
                                            case "sadness":{
                                                sum_sadness += tone_score;
                                                num_sadness++;
                                                break;
                                            }
                                            case "analytical":{
                                                sum_analytical += tone_score;
                                                num_analytical++;
                                                break;
                                            }
                                            case "confident":{
                                                sum_confident += tone_score;
                                                num_confident++;
                                                break;
                                            }
                                            case "tentative":{
                                                sum_tentative += tone_score;
                                                num_tentative++;
                                                break;
                                            }
                                            default: {
                                                Log.e("Error", "Error in detecting type of tone");
                                                break;
                                            }

                                        }



                                    }
                                }
                            }

                            if(num_anger == 0){
                                score_anger = 0;
                            }else{
                                score_anger = sum_anger/num_anger;
                            }
                            if(num_fear == 0){
                                score_fear = 0;
                            }else{
                                score_fear = sum_fear/num_fear;
                            }
                            if(num_joy == 0){
                                score_joy = 0;
                            }else{
                                score_joy = sum_joy/num_joy;
                            }
                            if(num_sadness == 0){
                                score_sadness = 0;
                            }else{
                                score_sadness = sum_sadness/num_sadness;
                            }
                            if(num_analytical == 0){
                                score_analytical = 0;
                            }else{
                                score_analytical = sum_analytical/num_analytical;
                            }
                            if(num_confident == 0){
                                score_confident = 0;
                            }else{
                                score_confident = sum_confident/num_confident;
                            }
                            if(num_tentative == 0){
                                score_tentative = 0;
                            }else{
                                score_tentative = sum_tentative/num_tentative;
                            }

                        }catch(JSONException e){
                            e.printStackTrace();
                            Log.e("JSON error",e.getMessage());
                        }

                        diary = new HashMap<>();

                        String unixTime = intent.getStringExtra("unixTime");

                        diary.put("text",userInput.getText().toString());
                        diary.put("sadness",score_sadness);
                        diary.put("anger",score_anger);
                        diary.put("fear",score_fear);
                        diary.put("joy",score_joy);
                        diary.put("confident",score_confident);
                        diary.put("tentative",score_tentative);
                        diary.put("analytical",score_analytical);
                        Log.e("diary",diary.toString());
                        diary.put("date",Long.parseLong(unixTime));

                        db.collection("users").document(mUserId)
                                .collection("subcollection").document(diary.get("date").toString())
                                .set(diary).addOnSuccessListener(new OnSuccessListener<Void>(){

                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("success","DocumentSnapshot added successfully");
                                loadShowDiaryActivity((long)diary.get("date"));
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("error","Error adding document");
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }

        });

    }

    public void loadShowDiaryActivity(long date){
        Intent showIntent = new Intent(this,ShowDiaryActivity.class);
        showIntent.putExtra("date",String.valueOf(date));
        startActivity(showIntent);
    }
}
