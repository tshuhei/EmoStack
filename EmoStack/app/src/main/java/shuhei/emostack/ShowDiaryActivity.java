package shuhei.emostack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ShowDiaryActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUserId;
    private TextView dateText;
    private TextView textText;
    private TextView angerText;
    private TextView fearText;
    private TextView joyText;
    private TextView confidentText;
    private TextView sadnessText;
    private RadarChart chart;
    private Typeface tfLight;
    private Button doneButton;
    private TextView dailyStress;
    private double stress;
    private Button stressReport;
    private double stressScore;
    private double aveanger;
    private double avefear;
    private double avesadness;
    private double aveconfident;
    private double avejoy;
    private double dayanger;
    private double dayfear;
    private double daysadness;
    private double dayconfident;
    private double dayjoy;
    private List<Map<String,Object>> dataList;
    private long monthMS;
    private String[] emotions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_diary);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dateText = (TextView)findViewById(R.id.dateText);
        textText = (TextView)findViewById(R.id.textText);
        angerText = (TextView)findViewById(R.id.angerText);
        fearText = (TextView)findViewById(R.id.fearText);
        joyText = (TextView)findViewById(R.id.joyText);
        confidentText = (TextView)findViewById(R.id.confidentText);
        sadnessText = (TextView)findViewById(R.id.sadnessText);
        doneButton = (Button)findViewById(R.id.done);
        dailyStress = (TextView)findViewById(R.id.dailyStress);
        stressReport = (Button)findViewById(R.id.stress_report);

        monthMS = System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 30L);
        dataList = new ArrayList<>();

        chart = (RadarChart)findViewById(R.id.radar_chart);

        chart.getDescription().setEnabled(false);

        chart.setWebLineWidth(1f);
        chart.setWebColor(Color.LTGRAY);
        chart.setWebLineWidthInner(1f);
        chart.setWebColorInner(Color.LTGRAY);
        chart.setWebAlpha(100);

        emotions = new String[]{
                "anger",
                "fear",
                "sadness",
                "joy",
                "confident"
        };

        tfLight = Typeface.createFromAsset(this.getAssets(),"OpenSans-Light.ttf");

        chart.animateXY(1400,1400, Easing.EaseInOutQuad);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTypeface(tfLight);
        xAxis.setTextSize(9f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setValueFormatter(new ValueFormatter() {

            private final String[] mActivities = new String[]{"Anger","Fear","Joy","Confident","Sadness"};

            @Override
            public String getFormattedValue(float value) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        xAxis.setTextColor(Color.BLACK);

        YAxis yAxis = chart.getYAxis();
        yAxis.setTypeface(tfLight);
        yAxis.setLabelCount(5,false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(0.8f);
        yAxis.setDrawLabels(false);
        yAxis.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f",value);
            }
        });

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setTypeface(tfLight);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.BLACK);

        Intent intent = this.getIntent();
        String date = intent.getStringExtra("date");

        long longDate = Long.parseLong(date);
        Date dateDate = new Date(longDate);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        dateText.setText(sdf.format(dateDate));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser != null){
            mUserId = mFirebaseUser.getUid();
        }

        db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(mUserId)
                .collection("subcollection")
                .document(date)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                Map<String,Object> diaryData = document.getData();
                                textText.setText(diaryData.get("text").toString());
                                angerText.setText("Anger: " + String.format("%.2f",Double.parseDouble(diaryData.get("anger").toString())));
                                fearText.setText("Fear: " + String.format("%.2f",Double.parseDouble(diaryData.get("fear").toString())));
                                joyText.setText("Joy: " + String.format("%.2f",Double.parseDouble(diaryData.get("joy").toString())));
                                confidentText.setText("Confident: " + String.format("%.2f",Double.parseDouble(diaryData.get("confident").toString())));
                                sadnessText.setText("Sadness: " + String.format("%.2f",Double.parseDouble(diaryData.get("sadness").toString())));

                                dayanger =Double.parseDouble(String.format("%.2f",(double)diaryData.get("anger")));
                                dayfear = Double.parseDouble(String.format("%.2f",(double)diaryData.get("fear")));
                                dayjoy = Double.parseDouble(String.format("%.2f",(double)diaryData.get("joy")));
                                dayconfident = Double.parseDouble(String.format("%.2f",(double)diaryData.get("confident")));
                                daysadness = Double.parseDouble(String.format("%.2f",(double)diaryData.get("sadness")));

                                //aveanger = 0.8;
                                //avefear = 0.5;
                                //avejoy = 0.3;
                                //aveconfident = 0.1;
                                //avesadness = 0.65;

                                //setData();

                                double negave = (dayanger + dayfear + daysadness)/3;
                                double posave = (dayjoy + dayconfident)/2;

                                stress = posave - negave;
                                stress *= -1;
                                stress += 1;
                                stress*= 50;

                                dailyStress.setText("Your stress score: "+ String.format("%.0f",stress)+"%");
                                stressScore = stress;
                            }else{
                                Log.e("error","No such document");
                            }
                        }else{
                            Log.e("error","get failed");
                        }
                    }
                });

        if(mUserId!=null){
            db.collection("users")
                    .document(mUserId)
                    .collection("subcollection")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document: task.getResult()){
                                    Map<String, Object> data = document.getData();
                                    long diaryDate = (long)data.get("date");
                                    if(diaryDate >= monthMS){
                                        dataList.add(data);
                                    }
                                }

                                for(int i=0;i<5;i++) {
                                    double emo=0;
                                    int j = 0;
                                    for (Map<String, Object> map : dataList) {
                                        Double doubleval = (double) map.get(emotions[i]);
                                        float val = doubleval.floatValue();
                                        emo+=val;
                                        j++;
                                    }
                                    emo /= j;
                                    if(emotions[i]=="anger"){
                                        aveanger = Double.parseDouble(String.format("%.2f",emo));
                                    }else if(emotions[i]=="fear"){
                                        avefear =  Double.parseDouble(String.format("%.2f",emo));
                                    }else if(emotions[i]=="sadness"){
                                        avesadness =  Double.parseDouble(String.format("%.2f",emo));
                                    }else if(emotions[i]=="confident"){
                                        aveconfident =  Double.parseDouble(String.format("%.2f",emo));
                                    }else if(emotions[i]=="joy"){
                                        avejoy =  Double.parseDouble(String.format("%.2f",emo));
                                    }
                                }

                                setData();

                            }else{
                                Log.e("Error","The task is unsuccessfully done");
                            }
                        }
                    });
        }

        doneButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                loadMainActivity();
            }
        });

        stressReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(0<= stressScore && stressScore <= 25.0){
                    loadNoStressActivity(stressScore);
                }else if(25.0 < stressScore && stressScore <= 50.0){
                    loadMildStressActivity(stressScore);
                }else if(50.0 < stressScore && stressScore <= 75.0){
                    loadIntermediateStressActivity(stressScore);
                }else{
                    loadHighStressActivity(stressScore);
                }
            }
        });
    }

    private void setData(){
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry((float)dayanger));
        entries.add(new RadarEntry((float)dayfear));
        entries.add(new RadarEntry((float)dayjoy));
        entries.add(new RadarEntry((float)dayconfident));
        entries.add(new RadarEntry((float)daysadness));

        ArrayList<RadarEntry> aveEntries = new ArrayList<>();
        aveEntries.add(new RadarEntry((float)aveanger));
        aveEntries.add(new RadarEntry((float)avefear));
        aveEntries.add(new RadarEntry((float)avejoy));
        aveEntries.add(new RadarEntry((float)aveconfident));
        aveEntries.add(new RadarEntry((float)avesadness));

        RadarDataSet set = new RadarDataSet(entries, "Emotion Today");
        set.setColor(Color.rgb(103,110,129));
        set.setFillColor(Color.rgb(103,110,129));
        set.setDrawFilled(true);
        set.setFillAlpha(180);
        set.setLineWidth(2f);
        set.setDrawHighlightCircleEnabled(true);
        set.setDrawHighlightIndicators(false);

        RadarDataSet aveSet = new RadarDataSet(aveEntries, "Average Emotion");
        aveSet.setColor(Color.BLUE);
        aveSet.setFillColor(Color.BLUE);
        aveSet.setDrawFilled(true);
        aveSet.setFillAlpha(180);
        aveSet.setLineWidth(2f);
        aveSet.setDrawHighlightCircleEnabled(true);
        aveSet.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set);
        sets.add(aveSet);

        RadarData data = new RadarData(sets);
        data.setValueTypeface(tfLight);
        data.setValueTextSize(8f);
        data.setDrawValues(true);
        data.setValueTextColor(Color.BLACK);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f",value);
            }
        });

        chart.setData(data);
        chart.invalidate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }
        return false;
    }

    public void loadMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void loadNoStressActivity(double score){
        Intent intent = new Intent(this,NoStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    public void loadMildStressActivity(double score){
        Intent intent = new Intent(this,MildStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    public void loadIntermediateStressActivity(double score){
        Intent intent = new Intent(this,IntermediateStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    public void loadHighStressActivity(double score){
        Intent intent = new Intent(this,HighStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }
}
