// Copyright 2023 (c) WebIntoApp.com
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of
// this software and associated documentation files (the "Software"), to deal in the
// Software without restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
// Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//  InReach
//
//  Created by InReach on 03/10/2023.
//
package org.asylumconnect.app;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import android.os.Handler;
import android.text.InputType;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import java.io.IOException;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
public class JavaScriptInterface {
    private Context context;
    public JavaScriptInterface(Context context) {
        this.context = context;
    }
    @JavascriptInterface
    public void getBase64FromBlobData(final String base64Data, final String mimetype) throws IOException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("Save As");
        final EditText input = new EditText(this.context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    convertBase64StringAndStoreIt(base64Data, mimetype, input.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    public static String getBase64StringFromBlobUrl(String blobUrl, String mimetype) {
        if(blobUrl.startsWith("blob")){
            return "javascript: var xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '"+ blobUrl +"', true);" +
                "xhr.setRequestHeader('Content-type','" + mimetype + "');" +
                "xhr.responseType = 'blob';" +
                "xhr.onload = function(e) {" +
                "    if (this.status == 200) {" +
                "        var blobData = this.response;" +
                "        var reader = new FileReader();" +
                "        reader.readAsDataURL(blobData);" +
                "        reader.onloadend = function() {" +
                "            base64data = reader.result;" +
                "            Android.getBase64FromBlobData(base64data, '" + mimetype +"');" +
                "        }" +
                "    }" +
                "};" +
                "xhr.send();";
        }
        return "javascript: console.log('It is not a Blob URL');";
    }
    private void convertBase64StringAndStoreIt(String base64PDf, String mimetype, String filename) throws IOException {
        String[] parts = mimetype.split("/");
        String ext = parts[1];
        byte[] data = Base64.decode(base64PDf.replaceFirst("^data:" + mimetype + ";base64,", ""), Base64.DEFAULT);
        final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
        String text = new String(data, "UTF-8");
        byte[] dataAsBytes = text.getBytes(Charset.forName("UTF-8"));
        FileOutputStream os;
        os = new FileOutputStream(dwldsPath, false);
        os.write(dataAsBytes);
        os.flush();
        final int notificationId = 1;
        if (dwldsPath.exists()) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            Uri apkURI = FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider", dwldsPath);
            intent.setDataAndType(apkURI, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            String CHANNEL_ID = "MYCHANNEL";
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel= new NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_LOW);
                Notification notification = new Notification.Builder(context,CHANNEL_ID)
                    .setContentTitle("Download Complete")
                    .setContentText(filename)
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .build();
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationManager.notify(notificationId, notification);
                }
            } else {
                NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(android.R.drawable.sym_action_chat)
                    .setContentTitle("Download Complete")
                    .setContentText(filename);
                if (notificationManager != null) {
                    notificationManager.notify(notificationId, b.build());
                    Handler h = new Handler();
                    long delayInMilliseconds = 1000;
                    h.postDelayed(new Runnable() {
                        public void run() {
                            notificationManager.cancel(notificationId);
                        }
                    }, delayInMilliseconds);
                }
            }
        }
        Toast.makeText(context, "Download Completed.", Toast.LENGTH_SHORT).show();
    }
}
