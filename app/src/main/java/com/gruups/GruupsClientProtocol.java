/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * This file is based on the KnockKnockProtocol example
 * from Oracle's "All About Sockets" tutorial. Copyright notice included above.
 */

package com.gruups;

/**
 * Created by dnsullivan on 3/21/15.
 */

import android.util.Log;

/**
 * Takes various String inputs from GruupsClient object and GruupsServer,
 * then converts them to forms that are compatible for communication between the two.
 */
public class GruupsClientProtocol {

    //Used to define Gruups client operation
    public static final String AUDIENCE_SUBMIT_QUESTION = "a";
    public static final String AUDIENCE_GET_POLL = "b";
    public static final String AUDIENCE_ANSWER_POLL = "c";
    public static final String PRESENTER_PULL_QUESTIONS = "d";
    public static final String PRESENTER_START_POLL = "e";
    public static final String PRESENTER_PULL_POLL_RESULTS = "f";
    public static final String KILL = "k" ;
    public static final String WAITING = "w" ;

    public static final char GRUUPS_UNIT_DELIMITER = (char) 31;
    public static final char GRUUPS_RECORD_DELIMITER = (char) 30;

    public static final String OK = "OK";
    public static final String BYE = "Bye";

    //Used by debugger
    private static final String TAG = GruupsClientProtocol.class.getSimpleName();

    private String socket;

    public GruupsClientProtocol(String socket) {
        this.socket = socket;
    }

    public String AudienceSubmitQuestion(String audienceQuestion) {
        StringBuffer gruupsClientQuestion = new StringBuffer(AUDIENCE_SUBMIT_QUESTION);

        /* Replace newline characters and line separators with spaces.
         * We do this to avoid problems with PrintWriter,
         * which is the class that sends strings to the GruupsClient socket.
         */
        audienceQuestion = audienceQuestion.replace("\n", " ");
        audienceQuestion = audienceQuestion.replace(System.getProperty("line.separator"), " ");
        gruupsClientQuestion.append(audienceQuestion);
        Log.d(TAG, "gruupsClientQuestion.toString(): " + gruupsClientQuestion.toString());
        return gruupsClientQuestion.toString();
    }

    public boolean AudienceSubmitQuestionServerResponse(String serverResponse) {
        Log.d(TAG, "GruupsClientProtocol.AudienceSubmitQuestionServerResponse is running()");
        Log.d(TAG, "GruupsClientProtocol.AudienceSubmitQuestionServerResponse serverResponse: "
                + serverResponse);
        if(serverResponse.equals(OK)) {
            return true;
        }
        return false;
    }

    public String PresenterQuestionPullRequest() {
        return PRESENTER_PULL_QUESTIONS;
    }

    public String PresenterQuestionPull(String gruupsServerPresenterQuestionPull) {
        return gruupsServerPresenterQuestionPull;
    }
}