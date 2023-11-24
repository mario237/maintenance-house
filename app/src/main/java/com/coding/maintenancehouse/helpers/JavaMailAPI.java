package com.coding.maintenancehouse.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.coding.maintenancehouse.R;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@SuppressWarnings("deprecation")
@SuppressLint("StaticFieldLeak")
public class JavaMailAPI extends AsyncTask<Void, Void, Void> {

    AlertDialog loadingDialog;


    final String EMAIL = "maintenancehouse.sup@gmail.com";
    final String PASSWORD = "bola123mena00";

    final Context context;

    private final String email;
    private final String subject;
    private final String message;

    private Activity currentActivity;

    public JavaMailAPI(Context context, String email, String subject, String message) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public JavaMailAPI(Context context, String email, String subject, String message , Activity currentActivity) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.currentActivity = currentActivity;
    }



    @Override
    protected Void doInBackground(Void... voids) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(EMAIL));
            mimeMessage.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(email)));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loadingDialogMethod();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        loadingDialog.dismiss();
        Toast.makeText(context, "mail send", Toast.LENGTH_SHORT).show();

        if (currentActivity != null){
            currentActivity.finish();
        }
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }
}
