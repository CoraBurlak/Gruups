package com.gruups;

/**
 * Created by Matt on 3/8/2015.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends Activity{

    private static final String TAG = MainActivity.class.getSimpleName();

    private String ip;
    private int port;
    private GruupsClient gruupsClient;
    ArrayList<String> pollResults = new ArrayList<String>();
    Socket client;
    public static final String EXTRAS_MSG_TOSEND = "msg";
    public static final String UNKNOWN_HOST = "Unknown Host";
    public static final String IO_EXCEPTION = "IO Exception";
    public static final String MESSAGE_SENT = "Message Sent";
    public static final String EXTRA_POLL_STRING = "poll string";
    public static final char GRUUPS_QUESTION_DELIMTER = (char) 31;
    Button btnSend;
    TextView a1, f1, p1, a2, f2, p2, a3, f3, p3, a4, f4, p4, tResultsQuestion;
    String resultsFromServer = "";

    protected BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(UNKNOWN_HOST)) {
                Toast.makeText(getApplicationContext(), "Unknown Host Error", Toast.LENGTH_LONG).show();
                finish();
            }
            if (intent.getAction().equals(IO_EXCEPTION)){
                Toast.makeText(getApplicationContext(), "IO Exception", Toast.LENGTH_LONG).show();
                finish();
            }
            if (intent.getAction().equals(MESSAGE_SENT))
                Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSend = (Button) (findViewById(R.id.btnSend));


        Intent ipIntent = getIntent();
        ip = ipIntent.getStringExtra(IPEnter.EXTRAS_IP_ADDRESS);
        port = ipIntent.getIntExtra(IPEnter.EXTRAS_PORT_NUMBER, 5555);

        tQuestion = (TextView) findViewById(R.id.question);
        tError = (TextView) findViewById(R.id.tError);

        rGroup = (RadioGroup) findViewById(R.id.rGroup);

        rb0 = (RadioButton) findViewById(R.id.rb0);
        rb1 = (RadioButton) findViewById(R.id.rb1);
        rb2 = (RadioButton) findViewById(R.id.rb2);
        rb3 = (RadioButton) findViewById(R.id.rb3);

        rb.add(rb0);
        rb.add(rb1);
        rb.add(rb2);
        rb.add(rb3);

        tResultsQuestion = (TextView) findViewById(R.id.tvPollQ);
        tResultsQuestion.setTextSize(25);

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

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(UNKNOWN_HOST);
        iFilter.addAction(IO_EXCEPTION);
        iFilter.addAction(MESSAGE_SENT);
        bManager.registerReceiver(intentReceiver, iFilter);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

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
                }else if (tabId == "Polling"){
                    populatePoll();
                }
            }
        });


        EditText etMessage = (EditText) findViewById(R.id.edit_message);
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

    }

    //This is replacing Send.java
    public final static String NEW_MESSAGE = "SEND-RECEIVE-TEST";
    public final static String toast_sent = "You have sent a question!";
    public final List<String> user_message = new ArrayList<String>();


    public void sendMessage(View view) {
        View focusView = null;

        Log.d(TAG, "Running sendMessage");

        //Get question text
        EditText editText = (EditText)findViewById(R.id.edit_message);
        Log.d(TAG, "Message from text field should now be captured in editText");
        String message = editText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            Log.d(TAG, "message: " + message);

            //Attempt to submit question to server
            SendQuestionTask newSend = new SendQuestionTask(ip, port, message, getApplicationContext());
            Log.d(TAG, "SendQuestionTask object newQuestion has been created");
            try {
                newSend.execute();
                Log.d(TAG, "newQuestion.execute() has been called");
            } catch (Exception e) {
                Log.d(TAG, "newQuestion execution failed. Reason: " + e.getMessage());
            }
            editText.setText("");

            user_message.add(message);
            Global.getInstance().global_message.add(message);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.layout_questions,      //layout to use
                    Global.getInstance().global_message);                   //where to get the items

            ListView list = (ListView) findViewById(R.id.listViewMain);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            editText.setError("Required field");
            focusView = editText;
            focusView.requestFocus();
        }
    }

    public void sendMessage() {
        View focusView = null;

        //Let me know this function is actually executing
        Log.d(TAG, "Running sendMessage");

        //Get question text
        EditText editText = (EditText)findViewById(R.id.edit_message);
        editText.setError(null);
        Log.d(TAG, "Message from text field should now be captured in editText");
        String message = editText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            Log.d(TAG, "message: " + message);

            //Attempt to submit question to server
            SendQuestionTask newSend = new SendQuestionTask(ip, port, message, getApplicationContext());
            Log.d(TAG, "SendQuestionTask object newQuestion has been created");
            try {
                newSend.execute();
                Log.d(TAG, "newQuestion.execute() has been called");
            } catch (Exception e) {
                Log.d(TAG, "newQuestion execution failed. Reason: " + e.getMessage());
            }
            editText.setText("");

            user_message.add(message);

            Global.getInstance().global_message.add(message);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.layout_questions,      //layout to use
                    Global.getInstance().global_message);                   //where to get the items

            ListView list = (ListView) findViewById(R.id.listViewMain);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            editText.setError("Required field");
            focusView = editText;
            focusView.requestFocus();
        }
    }

    protected class SendQuestionTask extends AsyncTask<Void, Void, String> {

        String ip = null;
        int port = -1;
        String msg = null;
        Context context;

        SendQuestionTask(String ipAdr, int portNum, String msg, Context context) {
            this.ip = ipAdr;
            this.port = portNum;
            this.msg = msg;
            this.context = context;
        }

        public final static String GRUUPS_SERVER_CONNECT_FAIL
                = "Failed to connect to Gruups Server";
        public final static String GRUUPS_QUESTION_SEND_SUCCESS = "You have submitted a question!";
        public final static String GRUUPS_QUESTION_SEND_FAIL = "Failed to send question";

        //For debugging
        private final String TAG = SendQuestionTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            //Ignore for now
        }

        @Override
        protected String doInBackground(Void... strings) {
            Log.d(TAG, "SendQuestionTask doInBackground is running");
            GruupsClient gruupsClient = new GruupsClient(ip, port);
            Log.d(TAG, "SendQuestionTask 's gruupsClient has been created");
            if (!gruupsClient.connect()) {
                Log.d(TAG, "GruupsClient.connect() has failed");
                return GRUUPS_SERVER_CONNECT_FAIL;
            }
            if (gruupsClient.AudienceSubmitQuestion(msg)) {
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
        protected void onProgressUpdate(Void... nothing) {
            //Ignore for now
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute() has been called");
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Toast.makeText(result) has been called");
        }
    }


        TextView tQuestion, tError;
        RadioGroup rGroup;
        RadioButton rb0, rb1, rb2, rb3;
        ArrayList<RadioButton> rb = new ArrayList<RadioButton>();
        String pollFromServer = "No poll found";

        public static final char GRUUPS_UNIT_DELIMTER = (char) 31;
        public static final char GRUUPS_RECORD_DELIMETER = (char) 30;

        public void answerPoll(View view) {
            String s = "";
            tError.setVisibility(View.GONE);
            if (rGroup.getCheckedRadioButtonId() == -1){
                tError.setText("Must select an answer");
                tError.setVisibility(View.VISIBLE);
                return;
            }
            if(rGroup.getCheckedRadioButtonId()==R.id.rb0){
                s = "0";
            }else if (rGroup.getCheckedRadioButtonId()==R.id.rb1){
                s = "1";
            }else if (rGroup.getCheckedRadioButtonId()==R.id.rb2){
                s = "2";
            }else if (rGroup.getCheckedRadioButtonId()==R.id.rb3){
                s = "3";
            }
            AnswerPollTask myAnswer = new AnswerPollTask(ip, port, getApplicationContext(), s);
            myAnswer.execute();

            rGroup.setVisibility(View.GONE);
        }

        public boolean populatePoll(){

            rGroup.setVisibility(View.VISIBLE);

            PullPollTask newPoll = new PullPollTask(ip, port, getApplicationContext());
            Log.d(TAG, "SendQuestionTask object newQuestion has been created");
            try {
                newPoll.execute();
                Log.d(TAG, "newPoll.execute() has been called");

                return true;
            } catch (Exception e) {
                Log.d(TAG, "newPoll execution failed. Reason: " + e.getMessage());
                return false;
            }
        }

        protected void returnString(String s){
            String questionText;
            String firstDelim = "";
            Scanner scan = new Scanner(s);
            Log.d(TAG, s);
            ArrayList<String> pollList = new ArrayList<String>();
            String recordDelim = Character.toString(GRUUPS_RECORD_DELIMETER);
            String unitDelim = Character.toString(GRUUPS_UNIT_DELIMTER);
            scan.useDelimiter(recordDelim);

            questionText = scan.next();

            while(scan.hasNext())
            {
                firstDelim += scan.next() + unitDelim;
            }

            Scanner scan2 = new Scanner(firstDelim);
            scan2.useDelimiter(unitDelim);

            while(scan2.hasNext())
            {
                pollList.add(scan2.next());
            }


            tQuestion.setText(questionText);
            Log.d(TAG, pollList.toString());

            for (int i = 0; i < pollList.size(); i++){
                if(i%2==1){
                    rb.get((int)((Math.ceil(i/2)))).setText(pollList.get(i));
                }else{
                    rb.get(i/2).setVisibility(View.VISIBLE);
                }
            }
        }


        protected class PullPollTask extends AsyncTask<Void, Void, String>{

            String ip = null;
            int port = -1;
            Context context;

            public final static String GRUUPS_SERVER_CONNECT_FAIL
                    = "Failed to connect to Gruups Server";
            public final static String GRUUPS_QUESTION_SEND_SUCCESS = "You have submitted a question!";
            public final static String GRUUPS_QUESTION_SEND_FAIL = "Failed to send question";

            PullPollTask(String ipAdr, int portNum, Context context) {
                this.ip = ipAdr;
                this.port = portNum;
                this.context = context;
            }

            @Override
            protected String doInBackground(Void... params) {
                GruupsClient gruupsClient = new GruupsClient(ip, port);
                Log.d(TAG, "SendQuestionTask 's gruupsClient has been created");
                if (!gruupsClient.connect()) {
                    Log.d(TAG, "GruupsClient.connect() has failed");
                    return GRUUPS_SERVER_CONNECT_FAIL;
                }
                pollFromServer = gruupsClient.AudienceGetPoll();

                if (pollFromServer != null) {
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
            protected void onPostExecute(String result) {
                Log.d(TAG, "onPostExecute() has been called");
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Toast.makeText(result) has been called");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        returnString(pollFromServer);
                    }
                });
            }


        }

        protected class AnswerPollTask extends AsyncTask<Void, Void, String>{
            String ip = null;
            int port = -1;
            String answer;
            Context context;

            public final static String GRUUPS_SERVER_CONNECT_FAIL
                    = "Failed to connect to Gruups Server";
            public final static String GRUUPS_QUESTION_SEND_SUCCESS = "You have submitted a question!";
            public final static String GRUUPS_QUESTION_SEND_FAIL = "Failed to send question";

            AnswerPollTask(String ipAdr, int portNum, Context context, String s) {
                this.ip = ipAdr;
                this.port = portNum;
                this.context = context;
                this.answer = s;
            }

            @Override
            protected String doInBackground(Void... params) {
                GruupsClient gruupsClient = new GruupsClient(ip, port);
                Log.d(TAG, "SendQuestionTask 's gruupsClient has been created");
                if (!gruupsClient.connect()) {
                    Log.d(TAG, "GruupsClient.connect() has failed");
                    return GRUUPS_SERVER_CONNECT_FAIL;
                }

                if (gruupsClient.AudienceAnswerPoll(this.answer)) {
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
            protected void onPostExecute(String result) {
                Log.d(TAG, "onPostExecute() has been called");
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Toast.makeText(result) has been called");
            }
        }

    public void pullResults() {
        pollResults.clear();
        PollResultsTask myResults = new PollResultsTask(ip, port, getApplicationContext());
        myResults.execute();

    }

    public void returnStringResults(String s){
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
            tResultsQuestion.setText(questionText);
            resultsText.clear();
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
                    returnStringResults(resultsFromServer);
                }
            });
        }
    }

    }
