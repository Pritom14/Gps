package com.example.shaloin.gps2;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.shaloin.gps2.R.id.fab;

public class DetailsActivity extends AppCompatActivity {

    private DatabaseManager manager;
    private ListView listView;
    private SimpleCursorAdapter adapter;

    final String[] from=new String[] {UserDatabase.NAME,UserDatabase.NUMBER};

    final int[] to=new int[] {R.id.nameDisplay,R.id.phoneDisplay};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        manager = new DatabaseManager(getApplicationContext());
        manager.open();
        //List<User> myList=manager.fetch();
        Cursor cursor=manager.fetch();

        listView = (ListView) findViewById(R.id.listViewId);
        listView.setEmptyView(findViewById(R.id.empty));

        adapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.row_item, cursor, from, to, 0);
        /*adapter = new SimpleCursorAdapter(getApplicationContext(),
        R.layout.row_item, (Cursor)myList, from, to, 0);*/
        adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DetailsActivity.this);
                LayoutInflater inflater = DetailsActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                dialogBuilder.setView(dialogView);
                final EditText name = (EditText) dialogView.findViewById(R.id.dialogEditNmID);
                final EditText phone = (EditText) dialogView.findViewById(R.id.dialogEditPhID);

                dialogBuilder.setTitle("Add Details");
                dialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(name.getText().toString()) &&
                                !TextUtils.isEmpty(phone.getText().toString())) {
                            insertData(name.getText().toString(),phone.getText().toString());
                            /*myList.add(new User(name.getText().toString(),phone.getText.toString())
                            * adapter.notifyDatasetChanged();*/
                            Cursor cursor=manager.fetch();
                            adapter = new SimpleCursorAdapter(getApplicationContext(),
                                    R.layout.row_item, cursor, from, to, 0);
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Empty field(s)", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog b = dialogBuilder.create();
                b.show();
                //  listView.setAdapter(adapter);
                //adapter.notifyDataSetChanged();
            }
        });
    }

    public void insertData(String fname,String phnumber){
        manager.insert(fname,phnumber);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
/*public boolean isDuplicate(List<String> col,String value){
        boolean isDuplicate=false;
        for(String s:col){
            if(s.equals(value)){
                isDuplicate=true;
                break;
            }
        }
        return isDuplicate;
    }*/