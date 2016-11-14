package edu.cmu.privacy.privacyfirewall;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple AsyncTask to load the list of applications and display them
 */
public class traceIPAddr extends AsyncTask<Void, Void, String> {
    private String ipaddr;

    public traceIPAddr(String _ipaddr) {
        super();
        ipaddr = _ipaddr;
    }

    @Override
    protected String doInBackground(Void... params) {
        String recipient = "";
        String path = "http://www.whoisxmlapi.com/whoisserver/WhoisService?domainName=" +
                ipaddr + "&username=daiker0330&password=infosec2016&outputFormat=JSON";
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;

        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15 * 1000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                byte[] data = readStream(is);
                String json = new String(data);
                JSONObject jsonObject = new JSONObject(json);
                jsonObject = jsonObject.getJSONObject("WhoisRecord");
                jsonObject = jsonObject.getJSONObject("registrant");
                recipient = jsonObject.getString("organization");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recipient;
    }

    @Override
    protected void onPostExecute(String result) {
        Cursor cur = Monitor.db.getRuleCursorByAdd(ipaddr);
        if (cur.getCount() >= 1) {
            cur.moveToFirst();
            int ruleId = cur.getInt(cur.getColumnIndex(RuleDatabase.FIELD_ID));
            Monitor.db.updateRegistrant(ruleId, result);
            Log.i("Trace", "Trace " + ipaddr + " to " + result + "Success!");
        } else {
            Log.i("Trace", "No such rule");
        }

    }

    private static byte[] readStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }
        bout.close();
        inputStream.close();
        return bout.toByteArray();
    }
}
