package com.tugrankenger.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView= findViewById(R.id.listView);
        nameArray= new ArrayList<String>();
        idArray= new ArrayList<Integer>();
        arrayAdapter= new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                intent.putExtra("artId",idArray.get(i));
                intent.putExtra("info","old"); // eski eklenen verilere gitmek icin
                startActivity(intent);
            }
        });

        getData();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int it, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("DELETE");
                alert.setMessage("Are you sure want to delete?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            int idd= idArray.get(it);
                            MainActivity2.database.execSQL("DELETE FROM arts WHERE id=?", new String[] {String.valueOf(idd)});
                            nameArray.remove(it);
                            idArray.remove(it);
                            arrayAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(),"Not Changed !", Toast.LENGTH_LONG).show();
                    }
                });
                alert.show();
                return false;
            }
        });

    }
    
   

    //verileri alma
    public void getData(){
   try {
       SQLiteDatabase database= this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

       Cursor cursor= database.rawQuery("SELECT*FROM arts",null);
       int nameIx=cursor.getColumnIndex("artname");
       int idIx= cursor.getColumnIndex("id");
       while (cursor.moveToNext()){
           nameArray.add(cursor.getString(nameIx));
           idArray.add(cursor.getInt(idIx));
       }

       arrayAdapter.notifyDataSetChanged(); // yeni eklenen verileri goster

       cursor.close();
   }catch(Exception e){
       e.printStackTrace();
   }
    }

    //menu cagirildi
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== R.id.add_art_item){
            Intent intent = new Intent(MainActivity.this,MainActivity2.class);
            intent.putExtra("info","new");  // yeni eklenecek veriler oldugunda
            startActivity(intent);
        }else if(item.getItemId()==R.id.about_menu){
            Toast.makeText(getApplicationContext(),"Coded By Tugrankenger",Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

}