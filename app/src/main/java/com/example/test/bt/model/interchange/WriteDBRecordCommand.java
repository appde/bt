package com.example.test.bt.model.interchange;

import org.json.JSONException;
import org.json.JSONObject;

public class WriteDBRecordCommand extends Command {
    
    public WriteDBRecordCommand(int tableId, int recordId, String data) {
        try {
            JSONObject root = new JSONObject();
            root.put("writeDBRecord", getId());
            root.put("tableId", tableId);
            root.put("recordId", recordId);
            root.put("data", data);
            setData(root.toString().getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
