package edu.cmu.privacy.privacyfirewall;

import java.net.InetAddress;

/**
 * Created by YunfanW on 10/27/2016.
 */

public interface AuxiliaryInterface {
    public String traceRecipient(InetAddress ipaddr);
    public String scanSensitive(char[] content);
}
