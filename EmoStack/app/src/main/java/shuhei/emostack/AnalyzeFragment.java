package shuhei.emostack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AnalyzeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AnalyzeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalyzeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUserId;
    private LineChart lineChart;
    private long currentMS;
    private long monthMS;
    private List<Map<String,Object>> dataList;
    private final LineChart[] charts = new LineChart[5];
    private Typeface mTf;
    private int[] colors;
    private String[] emotions;
    private List<String> xLabel;
    private Map<String,Object> emoList;
    private TextView stressText;
    private Button stressReport;
    private double stressScore;
    private double aveanger;
    private double avefear;
    private double avejoy;
    private double aveconfident;
    private double avesadness;
    private RadarChart radarChart;
    private Typeface tfLight;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AnalyzeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnalyzeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalyzeFragment newInstance(String param1, String param2) {
        AnalyzeFragment fragment = new AnalyzeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_analyze, container, false);
        currentMS = System.currentTimeMillis();
        monthMS = currentMS - (1000L * 60L * 60L * 24L * 30L);
        dataList = new ArrayList<>();

        stressReport = (Button)view.findViewById(R.id.stress_report);

        stressText = (TextView)view.findViewById(R.id.stress);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        emoList = new HashMap<>();

        mTf = Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Light.ttf");

        charts[0] = view.findViewById(R.id.chart1);
        charts[1] = view.findViewById(R.id.chart2);
        charts[2] = view.findViewById(R.id.chart3);
        charts[3] = view.findViewById(R.id.chart4);
        charts[4] = view.findViewById(R.id.chart5);

        xLabel = new ArrayList<>();

        colors = new int[] {
                Color.RED,
                Color.rgb(128,0,128),
                Color.BLUE,
                Color.YELLOW,
                Color.GREEN
        };

        emotions = new String[]{
                "anger",
                "fear",
                "sadness",
                "joy",
                "confident"
        };

        if(mFirebaseUser == null){
            loadLoginActivity();
        }else{
            mUserId = mFirebaseUser.getUid();
        }

        radarChart = (RadarChart)view.findViewById(R.id.radar_chart);

        radarChart.getDescription().setEnabled(false);

        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.LTGRAY);
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(Color.LTGRAY);
        radarChart.setWebAlpha(100);

        tfLight = Typeface.createFromAsset(getContext().getAssets(),"OpenSans-Light.ttf");

        radarChart.animateXY(1400,1400, Easing.EaseInOutQuad);

        XAxis xAxis = radarChart.getXAxis();
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

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setTypeface(tfLight);
        yAxis.setLabelCount(5,false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(0.8f);
        yAxis.setDrawLabels(false);

        Legend l = radarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setTypeface(tfLight);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.BLACK);

        db = FirebaseFirestore.getInstance();

        if(mUserId != null) {
            db.collection("users")
                    .document(mUserId)
                    .collection("subcollection")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> data = document.getData();
                                    Log.e("data", data.toString());
                                    long diaryDate = (long)data.get("date");
                                    if(diaryDate >= monthMS){
                                        dataList.add(data);
                                    }
                                    Log.e("dataList",dataList.toString());
                                    Date labelDate = new Date(diaryDate);
                                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                                    String label = sdf.format(labelDate);
                                    xLabel.add(label);
                                }

                                for (int i=0;i<charts.length;i++){
                                    LineData data = getData(i,dataList);
                                    data.setValueTypeface(mTf);

                                    setupChart(charts[i],data,colors[i % colors.length]);

                                }

                                aveanger = (double)emoList.get("anger");
                                avefear = (double)emoList.get("fear");
                                aveconfident = (double)emoList.get("confident");
                                avejoy = (double)emoList.get("joy");
                                avesadness = (double)emoList.get("sadness");

                                setData(aveanger,avefear,avejoy,aveconfident,avesadness);

                                double negave = (avefear + aveanger + avesadness)/3;

                                double posave = (avejoy + aveconfident)/2;

                                double stress = posave - negave;

                                stress *= -1;
                                stress += 1;
                                stress *= 50;

                                stressText.setText("Your stress score: "+ String.format("%.0f",stress) + "%");

                                stressScore = stress;

                            } else {
                                Log.e("Error", "The task is unsuccessfully done");
                            }
                        }
                    });
        }

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

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void loadLoginActivity(){
        Intent intent = new Intent(this.getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void setupChart(LineChart chart, LineData data, int color){

        ((LineDataSet) data.getDataSetByIndex(0)).setCircleHoleColor(color);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        //chart.setBackgroundColor(color);
        chart.setViewPortOffsets(60, 60, 60, 60);
        chart.setData(data);

        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setSpaceTop(40);
        chart.getAxisLeft().setSpaceBottom(40);
        chart.getAxisRight().setEnabled(false);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTypeface(mTf);
        yAxis.setAxisMaximum(1.0f);
        yAxis.setAxisMinimum(0.0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabel.get((int)value);
            }
        });


        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawAxisLine(false);

        chart.animateX(2500);

    }

    public LineData getData(int index, List<Map<String,Object>> dataList){

        ArrayList<Entry> values = new ArrayList<>();
        double emo=0;

        int i=0;
        for(Map<String,Object> map: dataList){
            Double doubleVal = (double)map.get(emotions[index]);
            float val = doubleVal.floatValue();
            emo+=val;
            //float val = (float)map.get(emotions[index]);
            values.add(new Entry(i,val));
            i++;
        }
        emo /= i;

        emoList.put(emotions[index],emo);

        Log.e("emoList",emoList.toString());

        LineDataSet set1 = new LineDataSet(values,"DataSet 1");

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(2f);
        set1.setCircleHoleRadius(1f);
        set1.setColor(colors[index]);
        set1.setCircleColor(colors[index]);
        set1.setHighLightColor(colors[index]);
        set1.setDrawValues(true);
        set1.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f",value);
            }
        });

        return new LineData(set1);
    }

    public void loadNoStressActivity(double score){
        Intent intent = new Intent(this.getContext(),NoStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    public void loadMildStressActivity(double score){
        Intent intent = new Intent(this.getContext(),MildStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    public void loadIntermediateStressActivity(double score){
        Intent intent = new Intent(this.getContext(),IntermediateStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    public void loadHighStressActivity(double score){
        Intent intent = new Intent(this.getContext(),HighStressActivity.class);
        intent.putExtra("score",String.valueOf(score));
        startActivity(intent);
    }

    private void setData(double anger, double fear, double joy, double confident, double sadness){
        float mul = 80;
        float min = 20;
        int cnt = 5;
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry((float)anger));
        entries.add(new RadarEntry((float)fear));
        entries.add(new RadarEntry((float)joy));
        entries.add(new RadarEntry((float)confident));
        entries.add(new RadarEntry((float)sadness));

        RadarDataSet set = new RadarDataSet(entries, "Your average emotion");
        set.setColor(Color.rgb(103,110,129));
        set.setFillColor(Color.rgb(103,110,129));
        set.setDrawFilled(true);
        set.setFillAlpha(180);
        set.setLineWidth(2f);
        set.setDrawHighlightCircleEnabled(true);
        set.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set);

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

        radarChart.setData(data);
        radarChart.invalidate();

    }
}
