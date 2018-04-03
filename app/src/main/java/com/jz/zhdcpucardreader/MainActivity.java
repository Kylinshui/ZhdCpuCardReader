package com.jz.zhdcpucardreader;

import android.os.Handler;
import android.os.Message;
import android.serialport.api.SerialPort;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream  mInputStream;
    private String DEVICE="/dev/ttymxc3";
    private int BAUDRATE=57600;
    private Handler handler;

    byte[] readData;

    private static final int readLength = 512;
    private readThread read_thread;
    private writeThread write_thread;
    private int iavailable = 0;
    private String Hexdata="";
    private EditText edRecv;

    private Button btRead;
    private Button btClear;
    private boolean bReadThreadGoing = true;

    byte[] resets = {(byte)0xAA,(byte)0xBB, 0x06, 0x00, 0x00, 0x00, 0x10, 0x02, 0x52, 0x40 };
    byte[] selectfile = { (byte)0xAA, (byte)0xBB, 0x0A, 0x00, 0x00, 0x00, 0x11, 0x02, 0x00, (byte)0xA4, 0x00, 0x00, 0x00, (byte)0xB7};
    byte[] waiburenzheng ={ (byte)0xAA, (byte)0xBB, 0x17, 0x00, 0x00, 0x00, 0x14, 0x02, 0x16, 0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, 0x00};
    byte[] xuanzheyonghu = { (byte)0xAA, (byte)0xBB, 0x0C, 0x00, 0x00, 0x00, 0x11, 0x02, 0x00, (byte)0xA4, 0x00, 0x00, 0x02, 0x00, 0x15, (byte)0xA0 };
    byte[] reader_infor = { (byte)0xAA, (byte)0xBB, 0x0A, 0x00, 0x00, 0x00, 0x11, 0x02, 0x00, (byte)0xB0, 0x00, 0x00, 0x78, (byte)0xDB };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btRead = (Button)findViewById(R.id.btRead);
        btClear = (Button)findViewById(R.id.btClear);
        edRecv = (EditText)findViewById(R.id.edRecv);

        readData = new byte[readLength];


        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edRecv.setText("");
            }
        });

        btRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                write_thread = new writeThread();
                write_thread.start();

            }
        });

        try{
          mSerialPort = new SerialPort(new File(DEVICE),BAUDRATE);
          mOutputStream = mSerialPort.getOutputStream();
          mInputStream  = mSerialPort.getInputStream();

        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"打开设备失败",
                    Toast.LENGTH_LONG).show();
        }

        read_thread = new readThread();
        read_thread.start();

        //主线程通进Handler显示接收到的数据
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(iavailable > 0){
                    edRecv.setText(msg.obj.toString());
               //     Log.i("TAG","recv:"+msg.obj.toString());
                }
            }
        };


    }

    private class writeThread extends Thread{
        @Override
        public void run() {
            super.run();

            try{
                if(mOutputStream !=null){
                    //发送复位卡片指令
                    mOutputStream.write(resets);
                    try{
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    //选择文件
                    mOutputStream.write(selectfile);
                    try{
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    //外部认证
                    mOutputStream.write(waiburenzheng);
                    try{
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    for(int i=0;i<3;i++) {

                  //  Log.i("TAG","write i:"+i);

                    //选择用户信息
                    mOutputStream.write(xuanzheyonghu);
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    }
                    //读文件
                    mOutputStream.write(reader_infor);
                    try{
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                Log.i("TAG","Send.........");

                }else{
                    Log.i("TAG","Send..error.......");
                    return ;
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
    private class readThread extends Thread{
        @Override
        public void run() {
            super.run();
            int i;
            while(bReadThreadGoing==true){

               try{
                   if(mInputStream == null)
                       return;
                    readData = new byte[readLength];
                   iavailable = mInputStream.read(readData);


                   if(iavailable > 0){
                       Hexdata = "";
                       Hexdata = Common.BytestoHexString(readData,iavailable);
                       Log.i("TAG","len:"+iavailable+" HexData:"+Hexdata);
                       if (readData[9] == 'S' && readData[10] == 'C')
                       {
                           //字符串显示
                           byte data[] = new byte[readLength];
                           System.arraycopy(readData,9,data,0,readData.length-9);

                           String info = new String(data,"GBK");

                           Message msg = handler.obtainMessage();

                           msg.obj = info;
                           handler.sendMessage(msg);
                       }
                       else if (readData[9] == 'R' && readData[10] == 'L')
                       {
                           //字符串显示
                           byte data[] = new byte[readLength];
                           System.arraycopy(readData,9,data,0,readData.length-9);

                           String info = new String(data,"GBK");

                           Message msg = handler.obtainMessage();

                           msg.obj = info;
                           handler.sendMessage(msg);
                       }
                   }
               }catch (IOException e){
                   e.printStackTrace();
                   return ;
               }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bReadThreadGoing=false;
        if(mSerialPort!=null)
            mSerialPort.close();
    }
}
