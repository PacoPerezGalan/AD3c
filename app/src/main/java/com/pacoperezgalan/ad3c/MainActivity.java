package com.pacoperezgalan.ad3c;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient apiClient;
    EditText fitxer;
    EditText text;
    Button drive;
    String nom;
    Boolean va;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        fitxer=(EditText) findViewById(R.id.et_fitxer);
        text=(EditText) findViewById(R.id.et_text);
        drive=(Button) findViewById(R.id.btn_enviar);
        va=true;
        drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(va==true){
                    va=false;

                    nom=fitxer.getText().toString();
                    crearFitxer(nom);

                    Toast.makeText(getApplicationContext(),"S'ha enviat a Google Drive",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Ya s'ha enviat,reinicia per enviar altre",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error de conexio!", Toast.LENGTH_SHORT).show();

    }

    private void crearFitxer(final String fitxer) {

        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (result.getStatus().isSuccess()) {

                            escriureText(result.getDriveContents());

                            MetadataChangeSet changeSet =
                                    new MetadataChangeSet.Builder()
                                            .setTitle(fitxer)
                                            .setMimeType("text/plain")
                                            .build();


                            DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);


                            folder.createFile(apiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(DriveFolder.DriveFileResult result) {
                                            if (result.getStatus().isSuccess()) {
                                                Log.i("tag", "Fitxer creat amb ID = " + result.getDriveFile().getDriveId());
                                            } else {
                                                Log.e("tag", "Error al crear el fitxer");
                                            }
                                        }
                                    });
                        } else {
                            Log.e("tag", "Error al crear DriveContents");
                        }
                    }
                });
    }

    private void escriureText(DriveContents driveContents) {
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        try {
            writer.write(text.getText().toString());
            writer.close();
        } catch (IOException e) {
            Log.e("tag", "Error al escriure en el fitxer: " + e.getMessage());
        }
    }
}
