package com.huayi.cme;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.security.MessageDigest;

public class APPIDActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appid);

        TelephonyManager TelephonyMgr = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String szImei = TelephonyMgr.getDeviceId();

        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length()%10 +
                Build.BRAND.length()%10 +
                1 +
                Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 +
                Build.HOST.length()%10 +
                Build.ID.length()%10 +
                Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 +
                Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 +
                Build.TYPE.length()%10 +
                Build.USER.length()%10 ; //13 digits

        String m_szAndroidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        String m_szWLANMAC = Installation.getTestVal(this);

        BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = m_BluetoothAdapter.getAddress();

        String m_szLongID = szImei + m_szDevIDShort
                + m_szAndroidID+ m_szWLANMAC + m_szBTMAC;
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(),0,m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID+="0";
            // add number to string
            m_szUniqueID+=Integer.toHexString(b);
        }   // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();

        setText(R.id.txt1,"1:"+szImei); //szImei
        setText(R.id.txt2,"2:"+Build.BOARD.length()%10); //Build.BOARD.length()%10
        setText(R.id.txt3,"3:"+Build.BRAND.length()%10); //Build.BRAND.length()%10
        setText(R.id.txt4,"4:"+1); //Build.CPU_ABI.length()%10
        setText(R.id.txt5,"5:"+Build.DEVICE.length()%10); //Build.DEVICE.length()%10
        setText(R.id.txt6,"6:"+Build.DISPLAY.length()%10); //Build.DISPLAY.length()%10
        setText(R.id.txt7,"7:"+Build.HOST.length()%10); //Build.HOST.length()%10
        setText(R.id.txt8,"8:"+Build.ID.length()%10); //Build.ID.length()%10
        setText(R.id.txt9,"9:"+Build.MANUFACTURER.length()%10); //Build.MANUFACTURER.length()%10
        setText(R.id.txt10,"10:"+Build.MODEL.length()%10); //Build.MODEL.length()%10
        setText(R.id.txt11,"11:"+Build.PRODUCT.length()%10); //Build.PRODUCT.length()%10
        setText(R.id.txt12,"12:"+Build.TAGS.length()%10); //Build.TAGS.length()%10
        setText(R.id.txt13,"13:"+Build.TYPE.length()%10); //Build.TYPE.length()%10
        setText(R.id.txt14,"14:"+Build.USER.length()%10); //Build.USER.length()%10
        setText(R.id.txt15,"15:"+m_szDevIDShort); //m_szDevIDShort
        setText(R.id.txt16,"16:"+m_szAndroidID); //m_szAndroidID
        setText(R.id.txt17,"17:"+m_szWLANMAC);//m_szWLANMAC
        setText(R.id.txt18,"18:"+m_szBTMAC); //m_szBTMAC
        setText(R.id.txt19,"19:"+m_szLongID); //m_szLongID
        setText(R.id.txt20,"20:"+m_szUniqueID); //m_szUniqueID
    }

    private void setText(int id,String txt){
        TextView t = (TextView)findViewById(id);
        t.setText(txt);

    }
}
