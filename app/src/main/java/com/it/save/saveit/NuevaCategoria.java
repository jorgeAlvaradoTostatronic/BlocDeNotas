package com.it.save.saveit;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.it.save.saveit.sqlite.model.CategoriaAdministradora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;

public class NuevaCategoria extends AppCompatActivity {

    private final int MY_PERMISSIONS = 100;
    private static final int CAMERA_REQUEST = 1888;
    ImageView imgSelected;
    Button agregar;
    EditText txtNombreCategoria;
    CategoriaAdministradora ca;
    boolean imagenIncertadaCcorrectamente=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_categoria);
        ca=new CategoriaAdministradora(this);
        imgSelected=(ImageView) findViewById(R.id.imgvNuevaImagen);
        agregar=(Button)findViewById(R.id.btnAgregar);
        txtNombreCategoria=(EditText)findViewById(R.id.txtNombreCategoria);
        imgSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pidePermisosEscritura();
                pidePermisosLectura();
                AlertDialog.Builder menu = new AlertDialog.Builder(NuevaCategoria.this);
                CharSequence[] opciones = {"Elegir de galeria","Tomar foto"};
                menu.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch(which)
                        {
                            case 0:
                                SeleccionaFotoDeGaleria();
                                break;
                            case 1:
                                TomarFoto();
                                break;
                        }
                    }
                });
                menu.create().show();
            }
        });
        agregar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(ca.VerificaTitulo(txtNombreCategoria.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ya existe categoria", Toast.LENGTH_SHORT).show();
                    txtNombreCategoria.setText("");
                    txtNombreCategoria.setFocusable(true);
                }
                else if(txtNombreCategoria.getText().length()==0)
                {
                    Toast.makeText(getApplicationContext(), "El campo no puede ir vacio", Toast.LENGTH_SHORT).show();
                    txtNombreCategoria.setText("");
                    txtNombreCategoria.setFocusable(true);
                }
                else if(!imagenIncertadaCcorrectamente)
                    Toast.makeText(getApplicationContext(), "Selecciones imagen", Toast.LENGTH_SHORT).show();
                else
                {
                    Bitmap bitmap = ((BitmapDrawable)imgSelected.getDrawable()).getBitmap();
                    SaveImage(bitmap);
                    ca.InsertarCategoria(new CategoriaClass(txtNombreCategoria.getText().toString(),-1,0));
                    Toast.makeText(getApplicationContext(), "Agregado", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(NuevaCategoria.this,MainActivity.class);
                    finish();
                    startActivity(i);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 20) {
                Uri imageUri = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    imgSelected.setImageBitmap(image);
                    imagenIncertadaCcorrectamente=true;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == CAMERA_REQUEST)
            {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imgSelected.setImageBitmap(photo);
                imagenIncertadaCcorrectamente=true;
            }
        }
    }
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/imagenes_categorias/";
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = txtNombreCategoria.getText().toString();
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private void pidePermisosEscritura()
    {
        if (ContextCompat.checkSelfPermission(NuevaCategoria.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(NuevaCategoria.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {

            } else {
                ActivityCompat.requestPermissions(NuevaCategoria.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
            }
        }
    }
    private void pidePermisosLectura()
    {
        if (ContextCompat.checkSelfPermission(NuevaCategoria.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(NuevaCategoria.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))
            {

            } else {
                ActivityCompat.requestPermissions(NuevaCategoria.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
            }
        }
    }
    private void SeleccionaFotoDeGaleria()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        photoPickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(photoPickerIntent, 20);
    }
    private void TomarFoto()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
}
