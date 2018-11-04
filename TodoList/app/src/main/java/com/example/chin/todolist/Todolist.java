package com.example.chin.todolist;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;

public class Todolist extends AppCompatActivity implements AdapterView.OnItemClickListener {
    final private ArrayList todo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);
        final Button button_add = (Button) findViewById(R.id.add_button);
        ListView listtodo = (ListView) findViewById(R.id.list);
        listtodo.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Todolist.this, android.R.layout.simple_list_item_1, todo);
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listtodo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Todolist.this);
                alertDialog.setTitle("删除文本");
                alertDialog.setMessage("删除后将无法恢复，确认删除？");
                alertDialog.setCancelable(true);
                alertDialog.setNegativeButton("取消", null);
                alertDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        todo.remove(position);
                        ListView listView = (ListView) findViewById(R.id.list);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Todolist.this, android.R.layout.simple_list_item_1, todo);
                        listView.setAdapter(adapter);
                    }
                });
                alertDialog.show();
                return true;
            }
        });
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText add = new EditText(Todolist.this);
                add.setMaxLines(1);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Todolist.this);
                alertDialog.setTitle("添加文本:");
                alertDialog.setCancelable(true);
                alertDialog.setView(add);
                alertDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = add.getText().toString();
                        todo.add(str);
                        listView.setAdapter(adapter);
                    }
                });
                alertDialog.setNegativeButton("取消", null);
                alertDialog.show();
            }
        });
    }

    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final EditText edit = new EditText(Todolist.this);
        edit.setMaxLines(1);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Todolist.this);
        alertDialog.setTitle("编辑文本");
        alertDialog.setView(edit);
        alertDialog.setCancelable(true);
        String toedit=todo.get(position).toString();
        edit.setText(toedit);
        edit.setSelection(toedit.length());
        alertDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                todo.set(position,edit.getText());
                ListView listView = (ListView) findViewById(R.id.list);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Todolist.this, android.R.layout.simple_list_item_1, todo);
                listView.setAdapter(adapter);
            }
        });
        alertDialog.setNegativeButton("取消", null);
        alertDialog.show();
    }
}