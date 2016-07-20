package com.example.test.bt.model.interchange;

import org.json.JSONException;
import org.json.JSONObject;

public class WriteDBRecordCommand extends Command {

    private byte[] data;
    private byte[] answer;

    public WriteDBRecordCommand(int tableId, int recordId, String data) {
        try {
            JSONObject root = new JSONObject();
            root.put("writeDBRecord", getId());
            root.put("tableId", tableId);
            root.put("recordId", recordId);
            root.put("data", data);
            this.data = root.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getAnswer() {
        return answer;
    }

    @Override
    public void setAnswer(byte[] answer) {
        this.answer = answer;
    }


}
