/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This file is based on the KnockKnockClient example
 * from Oracle's "All About Sockets" tutorial. Copyright notice included above.
 */

package com.gruups;

/**
 * Created by dnsullivan on 3/20/15.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class GruupsClient {

    private BufferedReader in;
    private PrintWriter out;
    private GruupsClientProtocol gruupsClientProtocol;
    private Socket gruupsSocket;
    private String gruupsServerIPAddress;
    private int gruupsServerPort;
    private boolean isConnected;

    //For debugging
    private static final String TAG = GruupsClient.class.getSimpleName();

    /**
     * Creates and initializes a GruupsClient object.
     * @param ip
     * @param port
     */
    public GruupsClient (String ip, int port) {
        Log.d(TAG, "GruupsClient constructor is running");
        gruupsServerIPAddress = ip;
        gruupsServerPort = port;
        isConnected = false;
    }

    /**
     * Opens connection with GruupsServer and creates objects for sending/receiving strings.
     */
    public boolean connect() {
        try {
            //Attempt to establish connection with GruupsServer.
            Log.d(TAG, "GruupsClient.connect() is running");
            InetSocketAddress gruupsInet = new InetSocketAddress(gruupsServerIPAddress, gruupsServerPort);
            gruupsSocket = new Socket();
            gruupsSocket.connect(gruupsInet, 5000);
            out = new PrintWriter(gruupsSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(gruupsSocket.getInputStream()));

            //Connection successful. Create GruupsClientProtocol object
            //and mark GruupsClient object as successfully connected.
            Log.d(TAG, "GruupsClient.connect() was successful. Socket info: "
                    + gruupsSocket.toString());
            gruupsClientProtocol = new GruupsClientProtocol(gruupsSocket.toString());
            isConnected = true;
        } catch (UnknownHostException e) {
            Log.d(TAG, "GruupsClient.connect() failed. UnknownHostException info: "
                    + e.getMessage());
            isConnected = false;
            return false;
        } catch (IOException e) {
            Log.d(TAG, "GruupsClient.connect() failed. IOException info: "
                    + e.getMessage());
            isConnected = false;
            return false;
        }
        return true;
    }

    public boolean AudienceSubmitQuestion(String audienceQuestion) {
        Log.d(TAG, "GruupsClient.AudienceSubmitQuestion() is running");
        String gruupsClientQuestion = gruupsClientProtocol.AudienceSubmitQuestion(audienceQuestion);
        Log.d(TAG, "Question string " + gruupsClientQuestion + " will be sent to server");
        try {
            out.println(gruupsClientQuestion);
            if (!gruupsClientProtocol.AudienceSubmitQuestionServerResponse(in.readLine())) {
                return false;
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "GruupsClient.AudienceSubmitQuestion() failed. IOException info: "
                    + e.getMessage());
            return false;
        }
    }

    public String AudienceGetPoll(){
        String poll = null;
        Log.d(TAG, "GruupsClient.AudienceGetPoll() is running");
        out.println(gruupsClientProtocol.AUDIENCE_GET_POLL);
        try{
            poll = in.readLine();
            Log.d(TAG, poll);
            return poll;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return poll;
    }

    public boolean PresenterPostPoll(String s){
        String pollToServer = gruupsClientProtocol.PRESENTER_START_POLL + s;
        out.println(pollToServer);
        return true;
    }

    public boolean AudienceAnswerPoll(String s){
        String answerToServer = gruupsClientProtocol.AUDIENCE_ANSWER_POLL + s;
        out.println(answerToServer);
        return true;
    }

    public ArrayList<String> PresenterPullQuestions() {
        Log.d(TAG, "GruupsClient.PresenterPullQuestions() is running");
        String unitDelimiter = Character.toString(
                GruupsClientProtocol.GRUUPS_UNIT_DELIMITER);
        Scanner presenterPulledQuestions;
        ArrayList<String> audienceQuestions = new ArrayList<String>();

        //Send presenter question pull protocol string request to server
        out.println(gruupsClientProtocol.PresenterQuestionPullRequest());

        try {
            presenterPulledQuestions = new Scanner(
                    //Get Presenter questions from server as one big string
                    gruupsClientProtocol.PresenterQuestionPull(in.readLine()));
        } catch (IOException e) {
            Log.d(TAG, "GruupsClient.PresenterPullQuestions() failed. IOException info: "
                    + e.getMessage());
            return null;
        }

        //Configure scanner to use GruupsProtocol standard question delimiter
        presenterPulledQuestions.useDelimiter(unitDelimiter);
        Log.d(TAG, "GruupsClient.PresenterPullQuestions() has set question delimiter for Scanner");

        /* Split GruupsServer presenter question pull
         * into individual questions,
         * then fill ArrayList with them.
         */
        while (presenterPulledQuestions.hasNext()) {
            audienceQuestions.add(presenterPulledQuestions.next());
        }
        Log.d(TAG, "GruupsClient.PresenterPullQuestions() has finished loop for adding audience" +
                " questions");

        return audienceQuestions;
    }

    public String presenterPullPollResults() {
        String pollResults = null;
        Log.d(TAG, "GruupsClient.PresenterPullPollResults() is running");
        out.println(GruupsClientProtocol.PRESENTER_PULL_POLL_RESULTS);
        try{
            pollResults = in.readLine();
            Log.d(TAG, pollResults);
            return pollResults;
        } catch (IOException e) {
            Log.d(TAG, "GruupsClient.PresenterPullPollResults() failed. Exception info: " +
                    e.getMessage());
        }
        return pollResults;
    }

    public void disconnect() {
        Log.d(TAG, "GruupsClient.disconnect() is running");
        out.println(GruupsClientProtocol.BYE);
        try {
            in.close();
            out.close();
            gruupsSocket.close();
        } catch (Exception e) {
            Log.d(TAG, "GruupsClient.disconnect() failed. Exception info: " + e.getMessage());
            //TODO: Do more stuff here if stuff breaks
        }
    }
}