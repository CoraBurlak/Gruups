package com.gruups;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;


/**
 * A login screen that offers login via email/password.
 */
public class IPEnter extends Activity {

    public static final String EXTRAS_IP_ADDRESS = "host_ip";
    public static final String EXTRAS_PORT_NUMBER = "host_port";

    private static final Pattern IP_ADDRESS = Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
            + "|[1-9][0-9]|[0-9]))");

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mIPAddressView;
    private EditText mPortView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mIPAddressSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipenter);

        // Set up the login form.
        mIPAddressView = (AutoCompleteTextView) findViewById(R.id.ipAddress);

        mPortView = (EditText) findViewById(R.id.portNumber);
        mPortView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mIPAddressSignInButton = (Button) findViewById(R.id.connect_button);
        mIPAddressSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Checks to make sure everything is good for connection info, doesn't actually create and connect to socket
     */
    public void attemptLogin() {

        // Reset errors.
        mIPAddressView.setError(null);
        mPortView.setError(null);

        // Store values at the time of the login attempt.
        String ip = mIPAddressView.getText().toString();
        String sPort = mPortView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for a valid port number
        if (TextUtils.isEmpty(sPort)) {
            mPortView.setError(getString(R.string.error_field_required));
            focusView = mPortView;
            cancel = true;
        }else if (!isPortValid(Integer.parseInt(sPort))) {
            mPortView.setError(getString(R.string.error_invalid_port));
            focusView = mPortView;
            cancel = true;
        }

        // Check for a valid ip address.
        if (TextUtils.isEmpty(ip)) {
            mIPAddressView.setError(getString(R.string.error_field_required));
            focusView = mIPAddressView;
            cancel = true;
        } else if (!isIPValid(ip)) {
            mIPAddressView.setError(getString(R.string.error_invalid_ipaddress));
            focusView = mIPAddressView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt connection and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //send info to MainActivity
            int port = Integer.parseInt(sPort);
            Intent ipSend = new Intent(IPEnter.this, MainActivity.class);
            ipSend.putExtra(EXTRAS_IP_ADDRESS, ip);
            ipSend.putExtra(EXTRAS_PORT_NUMBER, port);
            startActivity(ipSend);
        }
    }

    private boolean isIPValid(String ipaddress) {
        if (IP_ADDRESS.matcher(ipaddress).matches()) {
            return true;
        }
        return false;
    }

    private boolean isPortValid(int port) {
        if (port >= 0 && port <= 65535)
            return true;
        return false;
    }

}