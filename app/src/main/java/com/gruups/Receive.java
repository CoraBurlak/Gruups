package com.gruups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Receive extends Activity {
    Button bRefresh;
    public List<String> user_message = new ArrayList<String>();
    public List<String> server_messages = new ArrayList<String>();

    ArrayList<EditText> etNew = new ArrayList<EditText>();
    public static final char GRUUPS_QUESTION_DELIMTER = (char) 31;
    public static final char GRUUPS_RECORD_DELIMETER = (char) 30;
    public static final String TAG = Receive.class.getSimpleName();
    int c = 0;
    String ip;
    String resultsFromServer = "";
    ArrayList<String> pollResults = new ArrayList<String>();
    int port;

    LinearLayout ll;
    TextView a1, f1, p1, a2, f2, p2, a3, f3, p3, a4, f4, p4, tQuestion;

    EditText eQuestion,et0,et1,et2,et3;

    //For testing; should not be used in final product
    public static final String DOPEFISH_NET = "64.91.229.185";
    public static final int DOPEFISH_NET_PORT = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present);

        bRefresh = (Button)findViewById(R.id.button_refresh);
        bRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateQuestions();
            }
        });

        Intent ipIntent = getIntent();
        ip = ipIntent.getStringExtra(IPEnter.EXTRAS_IP_ADDRESS);
        port = ipIntent.getIntExtra(IPEnter.EXTRAS_PORT_NUMBER, 5555);

        ll = (LinearLayout) findViewById(R.id.tvLayout);

        final TabHost tabHost = (TabHost) findViewById(R.id.tabHost2);

        tabHost.setup();

        TabHost.TabSpec questionSpec = tabHost.newTabSpec("Question");
        questionSpec.setContent(R.id.Question);
        questionSpec.setIndicator("Question", getResources().getDrawable(R.drawable.ic_launcher));
        tabHost.addTab(questionSpec);

        TabHost.TabSpec pollingSpec = tabHost.newTabSpec("Polling");
        pollingSpec.setContent(R.id.Polling);
        pollingSpec.setIndicator("Polling", getResources().getDrawable(R.drawable.ic_launcher));
        tabHost.addTab(pollingSpec);

        TabHost.TabSpec settingsSpec = tabHost.newTabSpec("Poll Results");

        settingsSpec.setIndicator("Poll Results", getResources().getDrawable(R.drawable.ic_launcher));
        settingsSpec.setContent(R.id.Results);
        tabHost.addTab(settingsSpec);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId == "Poll Results"){
                    pullResults();
                }
            }
        });

        eQuestion = (EditText) findViewById(R.id.eQuestion);

        et0 = (EditText) findViewById(R.id.et0);
        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);

        tQuestion = (TextView) findViewById(R.id.tvPollQ);
        tQuestion.setTextSize(25);

        a1 = (TextView) findViewById(R.id.row1A);
        f1 = (TextView) findViewById(R.id.row1F);
        p1 = (TextView) findViewById(R.id.row1P);

        a2 = (TextView) findViewById(R.id.row2A);
        f2 = (TextView) findViewById(R.id.row2F);
        p2 = (TextView) findViewById(R.id.row2P);

        a3 = (TextView) findViewById(R.id.row3A);
        f3 = (TextView) findViewById(R.id.row3F);
        p3 = (TextView) findViewById(R.id.row3P);

        a4 = (TextView) findViewById(R.id.row4A);
        f4 = (TextView) findViewById(R.id.row4F);
        p4 = (TextView) findViewById(R.id.row4P);


        //TODO: Write code for pulling questions from GruupsServer
        populateQuestions();
    }

    private void populateQuestions() {
        //Run AsyncTask to pull questions from server
        PullQuestionsTask pullQuestionsTask
                = new PullQuestionsTask(DOPEFISH_NET, DOPEFISH_NET_PORT,
                                        getApplicationContext(), server_messages);
        pullQuestionsTask.execute();

        QuestionAdapter adapter = new QuestionAdapter(this,
                server_messages);                   //where to get the items

        ListView list = (ListView) findViewById(R.id.listViewReceive);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }




    protected class PullQuestionsTask extends AsyncTask<Void, Void, String> {

        private String ip = null;
        private int port = -1;
        private Context context;
        private List<String> server_msgs = new ArrayList<String>();


        PullQuestionsTask (String ipAdr, int portNum, Context context, List<String> serv_msg) {
            this.ip = ipAdr;
            this.port = portNum;
            this.context = context;
            this.server_msgs = serv_msg;
        }

        public final static String GRUUPS_SERVER_CONNECT_FAIL
                = "Failed to connect to Gruups Server";
        public final static String GRUUPS_QUESTION_PULL_TASK_FINISHED
                = "PullQuestionsTask finished. Returning executing to main UI thread.";

        //For debugging
        private final String TAG = PullQuestionsTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            //Ignore for now
        }

        @Override
        protected String doInBackground(Void... strings) {
            Log.d(TAG, "PullQuestionsTask doInBackground is running");
            GruupsClient gruupsClient = new GruupsClient(ip, port);
            Log.d(TAG, "PullQuestionsTask's gruupsClient has been created");
            if (!gruupsClient.connect()) {
                Log.d(TAG, "GruupsClient.connect() has failed");
                return GRUUPS_SERVER_CONNECT_FAIL;
            }

            Log.d(TAG, "Sending question ArrayList<String> to MainActivity " +
                    "via PresenterQuestionsAsArrayList");
            ArrayList<String> toSend = new ArrayList<String>();
            toSend = gruupsClient.PresenterPullQuestions();
            Collections.reverse(toSend);
            final ArrayList<String> finalToSend = toSend;
            Receive.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Receive.this,
                            R.layout.layout_receive,      //layout to use
                            finalToSend);                   //where to get the items

                    ListView list = (ListView) findViewById(R.id.listViewReceive);
                    list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            });
            return GRUUPS_QUESTION_PULL_TASK_FINISHED;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals(GRUUPS_SERVER_CONNECT_FAIL)) {
                Toast.makeText(context, GRUUPS_SERVER_CONNECT_FAIL, Toast.LENGTH_LONG);
            }
            Log.d(TAG, result);
        }
    }

        public void pullResults() {
            ip = DOPEFISH_NET;
            port = DOPEFISH_NET_PORT;
            pollResults.clear();
            PollResultsTask myResults = new PollResultsTask(ip, port, getApplicationContext());
            myResults.execute();

        }

        public void returnString(String s){
            if (s != ""){
                String recordDelim = Character.toString(GRUUPS_RECORD_DELIMETER);
                String unitDelim = Character.toString(GRUUPS_QUESTION_DELIMTER);
                String firstDelim = "";
                String questionText = "";
                Scanner recordScan = new Scanner(s);
                recordScan.useDelimiter(recordDelim);
                if (recordScan.hasNext())
                    questionText = recordScan.next();

                while (recordScan.hasNext()) {
                    firstDelim += recordScan.next() + unitDelim;
                }

                Scanner scan2 = new Scanner(firstDelim);
                scan2.useDelimiter(unitDelim);

                while (scan2.hasNext()) {
                    pollResults.add(scan2.next());
                }
                Log.d(TAG, pollResults.toString());

                ArrayList<String> resultsText = new ArrayList<String>();
                for (int i = 0; i < pollResults.size(); i++) {
                    resultsText.add(pollResults.get(i).toString());
                    i++;
                    resultsText.add(pollResults.get(i).toString());
                    i++;
                    resultsText.add(pollResults.get(i).toString() + "%");
                }

                TextView[] a = {a1, a2, a3, a4};
                TextView[] f = {f1, f2, f3, f4};
                TextView[] p = {p1, p2, p3, p4};
                int q = 0;

                for (int j = 0; j < resultsText.size() / 3; j++) {
                    a[j].setText(resultsText.get(q).toString());
                    q++;
                    f[j].setText(resultsText.get(q).toString());
                    q++;
                    p[j].setText(resultsText.get(q).toString());
                    q++;
                }
                tQuestion.setText(questionText);
                resultsText.clear();
            }
        }

        public void startPoll(View view){
            View focusView = null;
            ip = DOPEFISH_NET;
            port = DOPEFISH_NET_PORT;
            if(TextUtils.isEmpty(eQuestion.getText().toString())){
                eQuestion.setError("Field required");
                focusView = eQuestion;
                focusView.requestFocus();
                return;
            }else if(TextUtils.isEmpty(et0.getText().toString())) {
                et0.setError("Required field");
                focusView = et0;
                focusView.requestFocus();
                return;
            }else if(TextUtils.isEmpty(et1.getText().toString())){
                et1.setError("Required field");
                focusView = et1;
                focusView.requestFocus();
                return;
            }
            String IPnull = "IP is null";
            if (ip == null){
                Toast.makeText(this, IPnull, Toast.LENGTH_LONG).show();
                finish();
            }

            String pollToServer="";
            String recordDelim = Character.toString(GRUUPS_RECORD_DELIMETER);
            String unitDelim = Character.toString(GRUUPS_QUESTION_DELIMTER);
            pollToServer+= eQuestion.getText();

            pollToServer+= recordDelim + 0 + unitDelim + et0.getText().toString() +
                    recordDelim + 1 + unitDelim + et1.getText().toString();

            if(!TextUtils.isEmpty(et2.getText().toString()))
            {
                pollToServer+= recordDelim+2+unitDelim+et2.getText().toString();
            }
            if(!TextUtils.isEmpty(et3.getText().toString()))
            {
                pollToServer+= recordDelim+3+unitDelim+et3.getText().toString();
            }

            StartPollTask myPoll = new StartPollTask(ip, port, getApplicationContext());
            myPoll.execute(pollToServer);
            etNew.clear();
        }

        public class StartPollTask extends AsyncTask<String, Void, String>{

            String ip = null;
            int port = -1;
            Context context;

            public final static String GRUUPS_SERVER_CONNECT_FAIL
                    = "Failed to connect to Gruups Server";
            public final static String GRUUPS_QUESTION_SEND_SUCCESS = "You have submitted a question!";
            public final static String GRUUPS_QUESTION_SEND_FAIL = "Failed to send question";

            StartPollTask(String ipAdr, int portNum, Context context) {
                this.ip = ipAdr;
                this.port = portNum;
                this.context = context;
            }

            @Override
            public String doInBackground(String... params) {
                String pollToServer = params[0];
                GruupsClient gruupsClient = new GruupsClient(ip, port);
                Log.d(TAG, "SendQuestionTask 's gruupsClient has been created");
                if (!gruupsClient.connect()) {
                    Log.d(TAG, "GruupsClient.connect() has failed");
                    return GRUUPS_SERVER_CONNECT_FAIL;
                }

                if (gruupsClient.PresenterPostPoll(pollToServer)) {
                    Log.d(TAG, "GruupsClient.AudienceSubmitQuestion() has returned true");
                    gruupsClient.disconnect();
                    Log.d(TAG, "GruupsClient.disconnect() has been called");
                    return GRUUPS_QUESTION_SEND_SUCCESS;
                }
                Log.d(TAG, "GruupsClient.AudienceSubmitQuestion() has returned false");
                gruupsClient.disconnect();
                Log.d(TAG, "GruupsClient.disconnect() has been called");
                return GRUUPS_QUESTION_SEND_FAIL;
            }

            @Override
            public void onPostExecute(String result) {
                Log.d(TAG, "onPostExecute() has been called");
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Toast.makeText(result) has been called");
            }
        }

        public class PollResultsTask extends AsyncTask<Void, Void, String>{

            String ip = null;
            int port = -1;
            Context context;

            public final static String GRUUPS_SERVER_CONNECT_FAIL
                    = "Failed to connect to Gruups Server";

            PollResultsTask(String ipAdr, int portNum, Context context) {
                this.ip = ipAdr;
                this.port = portNum;
                this.context = context;
            }

            @Override
            public String doInBackground(Void... params) {
                GruupsClient gruupsClient = new GruupsClient(ip, port);
                Log.d(TAG, "SendQuestionTask 's gruupsClient has been created");
                if (!gruupsClient.connect()) {
                    Log.d(TAG, "GruupsClient.connect() has failed");
                    return GRUUPS_SERVER_CONNECT_FAIL;
                }

                resultsFromServer = gruupsClient.presenterPullPollResults();

                Log.d(TAG, "GruupsClient.AudienceSubmitQuestion() has returned false");
                gruupsClient.disconnect();
                Log.d(TAG, "GruupsClient.disconnect() has been called");
                return "Success";
            }

            @Override
            public void onPostExecute(String result) {
                Log.d(TAG, "onPostExecute() has been called");
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Toast.makeText(result) has been called");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        returnString(resultsFromServer);
                    }
                });
            }
        }

    public class QuestionAdapter extends ArrayAdapter<String> {
        public QuestionAdapter(Context context, List<String> questions) {
            super(context, R.layout.layout_receive, questions);
        }

        @Override
        public String getItem(int position) {
            return super.getItem(getCount() - position - 1);
        }
    }


}