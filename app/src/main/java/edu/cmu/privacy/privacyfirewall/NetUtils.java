package edu.cmu.privacy.privacyfirewall;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by YunfanW on 11/3/2016.
 */

public class NetUtils {
    public static int readProcFile(int packetPort) {
        File readFile =  new File("/proc/net/tcp");
        if(!readFile.exists()){
            Log.d("NetUtils","Cannot find file");
            return -1;
        }

        int uid = -1;

        try {
            FileInputStream inputStream = new FileInputStream(readFile);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //ignore the first line
            String line = bufferedReader.readLine();


            while((line=bufferedReader.readLine())!=null){
                uid= parseLine(line,packetPort);
                if(uid!=-1){
                    break;
                }
            }
            inputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return uid;
        }
    }

    private static int  parseLine(String line,int reqPort){
        //Log.d("NetUtils", "line: " + line);

        String[] strs = line.split(" ");

        // skip space
        int localAddrIndex = 0 ;
        int i ;
        for(i = 0; i < strs.length; i++){
            if(!strs[i].equals("")){
                localAddrIndex = i+1;
                break;
            }
        }
        //Log.d("NetUtils", "strs: " + strs[localAddrIndex]);

        // get ip address
        String[] localAddr = strs[localAddrIndex].split(":");
        //Log.d("NetUtils", "localAddr: " + localAddr[0]);

        String ip = Integer.valueOf(localAddr[0].substring(6), 16) + "."
                + Integer.valueOf(localAddr[0].substring(4,6), 16) + "."
                + Integer.valueOf(localAddr[0].substring(2,4), 16) + "."
                + Integer.valueOf(localAddr[0].substring(0,2), 16);

//        if(!ip.equals(FirewallVpnService.VPN_ADDRESS)){
        if(!ip.equals(LocalVPNService.VPN_ADDRESS)){
            return -1;
        }


        int port = Integer.valueOf(localAddr[1], 16);
        //Log.d("NetUtils", "tcp_port = " + port);

        if(port!=reqPort){
            return -1;
        }

        int uidIndex = localAddrIndex+6;


//        // ignore space
//        if(strs[uidIndex].equals("")) {
//            for (int i = uidIndex; i < strs.length; i++) {
//
//                if (!strs[i].equals("")) {
//
//                    uidIndex = i;
//                    break;
//                }
//            }
//        }

        Log.d("NetUtils", "Ip:"+ip+ " Port:"+port+" Uid:"+strs[uidIndex]);

        return Integer.valueOf(strs[uidIndex]);
    }
}
