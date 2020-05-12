package com.integrals.inlens.Activities;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.integrals.inlens.R;

import static android.view.View.VISIBLE;

public class ReportActivity extends AppCompatActivity {
    private TextInputEditText textInputEditText;
    private Button submitButton;
    ProgressDialog pDialog;
    private String albumName;
    private String albumId;
    private String albumCreatedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        textInputEditText=findViewById(R.id.report);
        submitButton=findViewById(R.id.submit);
        pDialog = new ProgressDialog(ReportActivity.this);

        albumId=getIntent().getStringExtra("Album_ID");
        albumCreatedBy=getIntent().getStringExtra("Album_CreatedBy");
        albumName=getIntent().getStringExtra("Album_Name");


        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.toString().isEmpty()){
                       submitButton.setVisibility(View.INVISIBLE);
                    }else{

                       submitButton.setVisibility(VISIBLE);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    displayAlert();
            }
        });
    }
    public void displayAlert(){
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(ReportActivity.this)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setTitle("Album will be deleted")
                .setIcon(R.drawable.ic_report_red)
                .setMessage("If you report this Cloud-Album it will be deleted permanently. This action cannot be reverted and it may take 7 working days to complete the action.")
                .setCancelable(false)
                .addButton("OK , Report",
                        getResources().getColor(R.color.red_600),
                        getResources().getColor(R.color.white),
                        CFAlertDialog.CFAlertActionStyle.DEFAULT,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pDialog.setMessage("Reporting.....");
                                pDialog.show();
                                FirebaseDatabase.getInstance().getReference().child("Abuse_Reports")
                                        .child(System.currentTimeMillis()+"")
                                        .child("Abuse")
                                        .setValue("on Album "+albumName+" ID " +albumId +" createdby "+ albumCreatedBy +" with message "+textInputEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            pDialog.dismiss();
                                            finish();
                                        }else{
                                            pDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),"Attempt failed , please try again",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addButton("CANCEL",
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.white),
                CFAlertDialog.CFAlertActionStyle.DEFAULT,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        finish();
                    }
                });

        builder.show();
    }
}
