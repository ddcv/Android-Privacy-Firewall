package edu.cmu.infosec.privacyfirewall;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple AsyncTask to load the list of applications and display them
 */
public class traceIPAddr extends AsyncTask<Void, Void, Void> {
    private String ipaddr;

    public traceIPAddr(String _ipaddr) {
        super();
        ipaddr = _ipaddr;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String recipient = "Not Known";
        String country = "Not Known";
        String path = "http://www.whoisxmlapi.com/whoisserver/WhoisService?domainName=" +
                ipaddr + "&username=daiker0330&password=infosec2016&outputFormat=JSON";

        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                byte[] data = readStream(is);
                String json = new String(data);
                JSONObject jsonObject = new JSONObject(json);
                if (!jsonObject.isNull("WhoisRecord")) {
                    jsonObject = jsonObject.getJSONObject("WhoisRecord");
                    if (!jsonObject.isNull("registrant")) {
                        jsonObject = jsonObject.getJSONObject("registrant");
                        if (!jsonObject.isNull("name")) {
                            recipient = jsonObject.getString("name");
                        } else if (!jsonObject.isNull("organization")) {
                            recipient = jsonObject.getString("organization");
                        }

                        if (!jsonObject.isNull("country")) {
                            country = jsonObject.getString("country");
                        }
                    }
                }

                Cursor cur = Monitor.db.getRuleCursorByAdd(ipaddr);
                if (cur.getCount() >= 1) {
                    cur.moveToFirst();
                    int ruleId = cur.getInt(cur.getColumnIndex(RuleDatabase.FIELD_ID));
                    Monitor.db.updateRegistrant(ruleId, recipient, country);
                    Log.i("Trace", "Trace " + ipaddr + " to " + recipient + "Success!");
                } else {
                    Log.i("Trace", "No such rule");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
