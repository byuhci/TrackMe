package cs497.byu.trackme;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HikingHubService extends Service {

    static String TAG = HikingHubService.class.getName();

    private boolean _isStarted;

    @Override
    public IBinder onBind(Intent intent)
    {
        //this is a started service so we return null here...
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        System.out.println("-=-=-=-=-=-=-=Beginning of onStartCommand!!-==-=-=-=-=-=-=-=");
        if (intent.getAction().equals(Constants.ACTION_START_SERVICE))
        {
            if (_isStarted)
            {
                Log.i(TAG, "OnStartCommand: Service already started.");
            }
            else
            {
                Log.i(TAG, "OnStartCommand: Service starting.");
                StartServer();
                RegisterForegroundService();
                _isStarted = true;
            }

        }
        else if (intent.getAction().equals(Constants.ACTION_STOP_SERVICE))
        {
            Log.i(TAG, "OnStartCommand: Service stopping.");
            _isStarted = false;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private void StartServer()
    {
        try
        {
            MultithreadedSocketServer server = new MultithreadedSocketServer();
            server.start();
        }
        catch(Exception e)
        {
            System.out.print(e);
        }
    }

    void RegisterForegroundService()
    {
        NotificationManager man = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        String channelName = "Dude!! Notifications!!";
        String channelDescription = "My notification channel";
        NotificationChannel channel = new NotificationChannel("some_notifications", channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(channelDescription);
        try {
            if(man != null) {
                man.createNotificationChannel(channel);
            }
        } catch(Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }


        Notification notification = new Notification.Builder(this, "some_notifications")
                .setContentTitle("Hiking Hub")
                .setContentText("The hiking hub service is running")
                .setContentIntent(BuildIntentToShowMainActivity())
                .setOngoing(true)
                .build();


        // Enlist this instance of the service as a foreground service
        startForeground(Constants.SERVICE_RUNNING_NOTIFICATION_ID, notification);
    }

    PendingIntent BuildIntentToShowMainActivity()
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION_MAIN_ACTIVITY);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra(Constants.SERVICE_STARTED_KEY, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}

class MultithreadedSocketServer extends Thread {
    public void run() {
        try{
            ServerSocket server=new ServerSocket(5000);
            int counter=0;
            System.out.println("Server Started ....");
            while(true){
                counter++;
                Socket serverClient=server.accept();  //server accept the client connection request
                System.out.println(" >> " + "Client No:" + counter + " started!");
                ServerClientThread sct = new ServerClientThread(serverClient,counter); //send  the request to a separate thread
                sct.start();
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
}

class ServerClientThread extends Thread {
    Socket serverClient;
    int clientNo;
    int squre;
    ServerClientThread(Socket inSocket,int counter){
        serverClient = inSocket;
        clientNo=counter;
    }
    public void run(){
        try{
            DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
            DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
            String clientMessage="", serverMessage="";
            byte[] buffer = new byte[1024];
            //clientMessage = inStream.readUTF();
            int howMany = inStream.read(buffer);
            System.out.println("Read this many bytes: " + howMany);
            clientMessage = new String(buffer, StandardCharsets.UTF_8);
            clientMessage = clientMessage.trim();
            //System.out.println("From Client-" +clientNo+ ": Number is :"+clientMessage);
            //squre = Integer.parseInt(clientMessage) * Integer.parseInt(clientMessage);
            serverMessage="Hey there!!"; // + clientNo + " Square of " + clientMessage + " is " +squre;
            Globals g = Globals.getInstance();
            if(clientMessage.equals("timeanddistance"))
            {
                //process...
                double distInMeters = g.getDistance();
                double distInMiles = (distInMeters * 3.28084) / 5280.0;
                Date currentTime = Calendar.getInstance().getTime();
                DateFormat df = new SimpleDateFormat("h:mma");
                String response = df.format(currentTime) + " " + String.format("%.2f", distInMiles) + "MI";
                //GET TIME AND MAKE STRING...
                outStream.writeUTF(response);
                outStream.flush();
            }
            else if(clientMessage.equals("sendphoto"))
            {
                //got a photo incoming, get ready to write a file!!!
                //outStream.writeUTF("go");
                File newImage = MapsFragment.createImageFileExternal(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                System.out.println("Just finished createImageFile");
                System.out.println(newImage.getAbsolutePath());
                if(newImage != null) {
                    System.out.println("In the newImage if");
                    FileOutputStream fos = new FileOutputStream(newImage);
                    System.out.println("Opened filestream");
                    int count;
                    byte[] fileBuf = new byte[4096]; // or 4096, or more
                    while ((count = inStream.read(fileBuf)) > 0) {
                        //System.out.println("Saving some bits");
                        fos.write(fileBuf, 0, count);
                    }
                    System.out.println("All done");
                    fos.flush();
                    fos.close();
                    g.setImageToDisplay(newImage);
                    MapsFragment mapsFragment = g.getMapsFragment();
                    Handler handler = g.getHandler();
                    handler.post(mapsFragment);
                }
            }
            //outStream.writeUTF(serverMessage);
            //outStream.flush();
            inStream.close();
            outStream.close();
            serverClient.close();
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            System.out.println("Client -" + clientNo + " exit!! ");
        }
    }
}
