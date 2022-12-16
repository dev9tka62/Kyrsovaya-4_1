package com.example.test;


import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    Cursor userCursor2;
    SimpleCursorAdapter userAdapter;
    SimpleCursorAdapter userAdapter2;
    ListView userList;
    EditText userFilter;
    ListView userList2;
    EditText userFilter2;
    Button calculate;
    TextView result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userList = (ListView) findViewById(R.id.userList);
        userFilter = (EditText) findViewById(R.id.userFilter);
        userList2 = (ListView) findViewById(R.id.userList2);
        userFilter2 = (EditText) findViewById(R.id.userFilter2);
        calculate = (Button) findViewById(R.id.calculate);
        result = (TextView) findViewById(R.id.resault);

        sqlHelper = new DatabaseHelper(getApplicationContext());
        // создаем базу данных
        sqlHelper.create_db();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            double firstFood = 0.0;
            double secondFood = 0.0;
            db = sqlHelper.open();
            userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
            userCursor2 = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
            String[] headers = new String[]{
                    DatabaseHelper.COLUMN_NAME,
                    DatabaseHelper.COLUMN_kKAL,
                    DatabaseHelper.COLUMN_BELKI,
                    DatabaseHelper.COLUMN_ZHURY,
                    DatabaseHelper.COLUMN_UGLEVODY};

            userAdapter = new SimpleCursorAdapter(this, R.layout.item,
                    userCursor, headers, new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5}, 0);

            userAdapter2 = new SimpleCursorAdapter(this, R.layout.item,
                    userCursor2, headers, new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5}, 0);


            // если в текстовом поле есть текст, выполняем фильтрацию
            // данная проверка нужна при переходе от одной ориентации экрана к другой

            if (!userFilter.getText().toString().isEmpty())
                userAdapter.getFilter().filter(userFilter.getText().toString());

            if (!userFilter2.getText().toString().isEmpty())
                userAdapter2.getFilter().filter(userFilter2.getText().toString());

            // установка слушателя изменения текста
            userFilter.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                // при изменении текста выполняем фильтрацию
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    userAdapter.getFilter().filter(s.toString());
                }
            });
            userFilter2.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                // при изменении текста выполняем фильтрацию
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    userAdapter2.getFilter().filter(s.toString());
                }
            });
            // устанавливаем провайдер фильтрации
            userAdapter.setFilterQueryProvider(constraint -> {

                if (constraint == null || constraint.length() == 0) {

                    return db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
                } else {
                    return db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                            DatabaseHelper.COLUMN_NAME + " like ?", new String[]{"%" + constraint.toString() + "%"});
                }
            });
            userAdapter2.setFilterQueryProvider(constraint -> {

                if (constraint == null || constraint.length() == 0) {

                    return db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
                } else {
                    return db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                            DatabaseHelper.COLUMN_NAME + " like ?", new String[]{"%" + constraint.toString() + "%"});
                }
            });


            userList.setAdapter(userAdapter);
            userList2.setAdapter(userAdapter2);

            userList.setClickable(true);
            userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                            DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(arg3)});

                    userCursor.moveToFirst();
                    userFilter.setText(String.format("%s %s кКал %s белки %s жиры %s углеводы",
                            userCursor.getString(1),
                            userCursor.getString(2),
                            userCursor.getString(3),
                            userCursor.getString(4),
                            userCursor.getString(5)));

                }
            });

            userList2.setClickable(true);
            userList2.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.e("ite", String.valueOf(l));
                    userCursor2 = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                            DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(l)});

                    userCursor2.moveToFirst();
                    userFilter2.setText(String.format("%s %s кКал %s белки %s жиры %s углеводы",
                            userCursor2.getString(1),
                            userCursor2.getString(2),
                            userCursor2.getString(3),
                            userCursor2.getString(4),
                            userCursor2.getString(5)));


                }
            });
            calculate.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onClick(View view) {
                    EditText weight1et = findViewById(R.id.weight1);
                    EditText weight2et = findViewById(R.id.weight2);
                    if (weight1et.getText().toString().equals("") || weight2et.getText().toString().equals("") || userFilter.getText().toString().equals("") || userFilter2.getText().toString().equals("")) {

                        Toast.makeText(getApplicationContext(), "Введите массу и продукты", Toast.LENGTH_SHORT).show();
                    } else {
                        int weight1 = Integer.parseInt(weight1et.getText().toString());
                        int weight2 = Integer.parseInt(weight2et.getText().toString());
                        result.setText(String.format(" %.1f кКал %.1fг. белков %.1fг. жиров %.1fг. углеводов",
                                (Double.parseDouble(userCursor2.getString(2))) / 100 * weight2 + (Double.parseDouble(userCursor.getString(2))) / 100 * weight1,
                                (Double.parseDouble(userCursor2.getString(3))) / 100 * weight2 + (Double.parseDouble(userCursor.getString(3))) / 100 * weight1,
                                (Double.parseDouble(userCursor2.getString(4))) / 100 * weight2 + (Double.parseDouble(userCursor.getString(4))) / 100 * weight1,
                                (Double.parseDouble(userCursor2.getString(5))) / 100 * weight2 + (Double.parseDouble(userCursor.getString(5))) / 100 * weight1
                        ));
                        findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                    }
                }
            });

        } catch (SQLException ex) {
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        userCursor2.close();
        // Закрываем подключение и курсор
        db.close();
        userCursor.close();
    }
}

