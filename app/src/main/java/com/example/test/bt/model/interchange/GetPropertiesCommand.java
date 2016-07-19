package com.example.test.bt.model.interchange;

import org.json.JSONException;
import org.json.JSONObject;

public class GetPropertiesCommand extends Command {

    private byte[] data;
    private byte[] answer;

    public GetPropertiesCommand() {
        try {
            JSONObject root = new JSONObject();
            this.data = root.put("getProperties", getId()).toString().getBytes();
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
