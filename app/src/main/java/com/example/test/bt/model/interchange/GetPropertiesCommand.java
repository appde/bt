package com.example.test.bt.model.interchange;

import org.json.JSONException;
import org.json.JSONObject;

public class GetPropertiesCommand extends Command {

    public GetPropertiesCommand() {
        try {
            JSONObject root = new JSONObject();
            setData(root.put("getProperties", getId()).toString().getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
