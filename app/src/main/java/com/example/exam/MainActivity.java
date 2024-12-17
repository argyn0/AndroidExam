package com.example.exam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_CODE = 1;
    private RelativeLayout activity_main;
    private FirebaseListAdapter<Message> adapter;
    private FloatingActionButton SendBtn;
    private static final String PREFS_NAME = "ChatPrefs";
    private static final String THEME_KEY = "theme";

    // Константы тем
    private static final int THEME_LIGHT = 1;
    private static final int THEME_DARK = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "Вы успешно авторизованы", Snackbar.LENGTH_LONG).show();
                displayAllMessages();
            } else {
                Snackbar.make(activity_main, "Ошибка авторизации", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void loadTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int theme = prefs.getInt(THEME_KEY, THEME_LIGHT);
        switch (theme) {
            case THEME_DARK:
                activity_main.setBackgroundColor(getResources().getColor(R.color.black));
                break;
            case THEME_LIGHT:
            default:
                activity_main.setBackgroundColor(getResources().getColor(R.color.light_gray));
                break;
        }
    }

    // Метод для изменения темы
    private void changeTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int currentTheme = prefs.getInt(THEME_KEY, THEME_LIGHT);
        if (currentTheme == THEME_LIGHT) {
            editor.putInt(THEME_KEY, THEME_DARK);
        } else {
            editor.putInt(THEME_KEY, THEME_LIGHT);
        }
        editor.apply();
        loadTheme(); // Обновить фон
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);

        Button btnChangeTheme = findViewById(R.id.btnChangeTheme);
        btnChangeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTheme();
            }
        });

        SendBtn = findViewById(R.id.fab);
        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText textField = findViewById(R.id.messageField);
                if (textField.getText().toString().isEmpty()) {
                    return;
                }
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Snackbar.make(activity_main, "Ошибка: Пользователь не авторизован", Snackbar.LENGTH_LONG).show();
                    return;
                }
                FirebaseDatabase.getInstance().getReference().push().setValue(
                        new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                textField.getText().toString())
                ).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        textField.setText(""); // Очищаем поле после успешной отправки
                    } else {
                        Snackbar.make(activity_main, "Ошибка при отправке сообщения", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        } else {
            Snackbar.make(activity_main, "Вы успешно авторизованы", Snackbar.LENGTH_LONG).show();
            displayAllMessages();
        }
    }

    private void displayAllMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://chat-90560-default-rtdb.firebaseio.com/");
        databaseReference.child("appData/messages").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String textMessage = snapshot.child("textMessage").getValue(String.class);
                    Log.d("FirebaseData", "Message: " + textMessage);
                }
            } else {
                Log.e("FirebaseError", "Error: ", task.getException());
            }
        });
        Query query = databaseReference;

        FirebaseListOptions<Message> options = new FirebaseListOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLayout(R.layout.list_item)
                .build();

        // Адаптер сообщений
        adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView Mess_User, Mess_time, Mess_text;
                ImageView deleteButton; // Иконка для удаления

                Mess_User = v.findViewById(R.id.message_user);
                Mess_time = v.findViewById(R.id.message_time);
                Mess_text = v.findViewById(R.id.message_text);
                deleteButton = v.findViewById(R.id.delete_message); // Получаем иконку удаления

                Mess_User.setText(model.getUserName());
                Mess_text.setText(model.getTextMessage());
                Mess_time.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", model.getMessageTime()));

                // Обработчик клика по кнопке удаления
                deleteButton.setOnClickListener(view -> {
                    String messageId = getRef(position).getKey(); // Получаем ключ сообщения
                    if (messageId != null) {
                        FirebaseDatabase.getInstance().getReference("appData/messages").child(messageId).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Удаляем только это сообщение из списка адаптера
                                        getRef(position).removeValue(); // Удаляем сообщение из базы данных
                                        // Адаптер автоматически обновит список, так как Firebase синхронизирует данные
                                        Snackbar.make(view, "Сообщение удалено", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(view, "Ошибка при удалении", Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        };
        adapter.startListening();
        listOfMessages.setAdapter(adapter);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening(); // Начало прослушивания изменений Firebase
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening(); // Остановка прослушивания при остановке Activity
        }
    }
}



