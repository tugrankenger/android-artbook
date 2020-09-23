package com.tugrankenger.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {
    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameText,painterNameText,yearText;
    Button button;
    static SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView= findViewById(R.id.imageView);
        artNameText = findViewById(R.id.artNameText);
        painterNameText= findViewById(R.id.painterNameText);
        yearText=findViewById(R.id.yearText);
        button= findViewById(R.id.button);

        database= this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        // menuden gelindiginde veya item' a tiklandiginda ne olcak:

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        // menuden mi geldi item'dan mı geldi kontrol:

        if(info.matches("new")){
            artNameText.setText("");
            painterNameText.setText("");
            yearText.setText("");
            button.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            imageView.setImageBitmap(selectImage);
        }else{
            int artId= intent.getIntExtra("artId",1);
            button.setVisibility(View.INVISIBLE);

            // item'a tiklandiginda ilgili veriyi Main2Activity'de gosterme

            try {

                Cursor cursor = database.rawQuery("SELECT*FROM arts WHERE id=?",new String[]{String.valueOf(artId)});

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx= cursor.getColumnIndex("paintername");
                int yearIx= cursor.getColumnIndex("year");
                int imageIx= cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    artNameText.setText(cursor.getString(artNameIx));
                    painterNameText.setText(cursor.getString(painterNameIx));
                    yearText.setText(cursor.getString(yearIx));
                    //resimleri alma:
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
                cursor.close();

            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    public void selectImage(View view){
    if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
        //izin isteme
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }else{
    //izin daha once zaten verilmisse galeriye git
        Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentToGallery,2);
    }
    }
    // izin istendiginde ne olacak:
    //grantResults: verilen degerleri soruyor
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }else if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_DENIED){
                Toast.makeText(getApplicationContext(),"You must allow to add image",Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2 && resultCode==RESULT_OK && data != null){
            Uri imageData= data.getData();
            try {
                //yeni versionlar icin:
                if(Build.VERSION.SDK_INT>=28){
                    ImageDecoder.Source source= ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage=ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }
                    //eski versionlar icin:
                selectedImage= MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){
    String artName= artNameText.getText().toString();
    String painterName= painterNameText.getText().toString();
    String year= yearText.getText().toString();
    // gorseli veriye cevirme

        Bitmap smallImage= makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        // veritabani olusturma ve kaydetme
        try{

            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artname VARCHAR,paintername VARCHAR, year VARCHAR,image BLOB)");

            String sqlString = "INSERT INTO arts (artname,paintername,year,image) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();
        }catch(Exception e){

        }
       // finish();  // bu kullanimda MainActivity acilmaz, intent yaparsak OnCreate cagrilir
        Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_SHORT).show();
        Intent intent =new  Intent(MainActivity2.this,MainActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // daha onceki calisan aktiviteleri kapatir
        startActivity(intent);

    }
    // kucuk gorsel olusturma metodu
    public  Bitmap makeSmallerImage(Bitmap image,int maximumSize){
        int width= image.getWidth();
        int height= image.getHeight();
        float bitmapRatio = (float) width/(float) height;

        if(bitmapRatio>1){
            width= maximumSize;
            height= (int)(width/bitmapRatio);
        }else{
            height= maximumSize;
            width= (int)(height*bitmapRatio);
        }
        return  Bitmap.createScaledBitmap(image,width,height,true);
    }
}