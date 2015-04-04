package com.gruups;

import android.app.Application;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 3/12/2015.
 */
public final class Global extends Application {

    private static final Global instance = new Global();

    public List<String> global_message = new ArrayList<String>();

    public static Global getInstance() {
        return instance;
    }
    /*
    public void setList(List<String> global_message) {
        this.global_message = global_message;
    }*/

}
