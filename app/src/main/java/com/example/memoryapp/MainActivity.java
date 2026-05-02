package com.example.memoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView timerText, wordsText, resultText, historyText;
    private EditText inputWords;
    private Button startButton, checkButton, clearHistoryButton;

    private final String[] dictionaryWords = {
            "яблоко", "река", "дом", "солнце", "книга",
            "стол", "окно", "машина", "город", "цветок",
            "лес", "море", "лампа", "ручка", "телефон",
            "часы", "птица", "облако", "дорога", "кофе",
            "молоко", "хлеб", "гора", "зима", "лето",
            "дождь", "ветер", "звезда", "музыка", "картина",
            "дверь", "школа", "поезд", "мост", "трава",
            "собака", "кошка", "апельсин", "банан", "стул",
            "карандаш", "бумага", "озеро", "парк", "снег",
            "песок", "луна", "чай", "ключ", "рюкзак",
            "врач", "память", "мозг", "здоровье", "сон",
            "сердце", "спорт", "вода", "энергия", "улыбка",
            "планета", "экран", "комната", "кровать", "зеркало",
            "пальто", "ноутбук", "письмо", "журнал", "свеча",
            "площадь", "автобус", "метро", "сумка", "билет",
            "дерево", "лист", "ягода", "рыба", "остров"
    };

    private String[] testWords = new String[5];

    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "MemoryTrackerPrefs";
    private static final String KEY_HISTORY = "history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.white));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }

        timerText = findViewById(R.id.timerText);
        wordsText = findViewById(R.id.wordsText);
        resultText = findViewById(R.id.resultText);
        historyText = findViewById(R.id.historyText);
        inputWords = findViewById(R.id.inputWords);
        startButton = findViewById(R.id.startButton);
        checkButton = findViewById(R.id.checkButton);
        clearHistoryButton = findViewById(R.id.clearHistoryButton);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        loadHistory();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMemoryTest();
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkResult();
            }
        });

        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearHistory();
            }
        });
    }

    private void generateRandomWords() {
        ArrayList<String> wordsList = new ArrayList<>();

        for (String word : dictionaryWords) {
            wordsList.add(word);
        }

        Collections.shuffle(wordsList);

        for (int i = 0; i < testWords.length; i++) {
            testWords[i] = wordsList.get(i);
        }
    }

    private void startMemoryTest() {
        generateRandomWords();

        inputWords.setText("");
        inputWords.setEnabled(false);
        checkButton.setEnabled(false);
        startButton.setEnabled(false);

        resultText.setText("Запоминайте слова...");

        StringBuilder wordsBuilder = new StringBuilder();

        for (String word : testWords) {
            wordsBuilder.append(word).append("   ");
        }

        wordsText.setText(wordsBuilder.toString());

        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                timerText.setText("Время: " + seconds);
            }

            @Override
            public void onFinish() {
                timerText.setText("Время вышло");
                wordsText.setText("Теперь введите слова, которые запомнили");
                inputWords.setEnabled(true);
                checkButton.setEnabled(true);
                startButton.setEnabled(true);
                resultText.setText("Введите слова и нажмите «Проверить результат»");
            }
        }.start();
    }

    private void checkResult() {
        String userInput = inputWords.getText().toString().toLowerCase().trim();

        if (userInput.isEmpty()) {
            Toast.makeText(this, "Введите хотя бы одно слово", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userWords = userInput.split("\\s+");

        HashSet<String> correctWords = new HashSet<>();

        for (String word : testWords) {
            correctWords.add(word.toLowerCase());
        }

        int score = 0;

        for (String userWord : userWords) {
            if (correctWords.contains(userWord)) {
                score++;
                correctWords.remove(userWord);
            }
        }

        String interpretation;

        if (score == 5) {
            interpretation = "Отличная память";
        } else if (score >= 3) {
            interpretation = "Хороший результат";
        } else if (score >= 1) {
            interpretation = "Средний результат, можно потренироваться";
        } else {
            interpretation = "Низкий результат, попробуйте ещё раз";
        }

        String result = "Правильно: " + score + " из " + testWords.length
                + "\nОценка: " + interpretation;

        resultText.setText(result);

        saveResult(score, interpretation);
        loadHistory();
    }

    private void saveResult(int score, String interpretation) {
        String oldHistory = sharedPreferences.getString(KEY_HISTORY, "");

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        StringBuilder wordsBuilder = new StringBuilder();

        for (String word : testWords) {
            wordsBuilder.append(word).append(" ");
        }

        String newRecord = date
                + " — " + score + "/5"
                + " — " + interpretation
                + "\nСлова: " + wordsBuilder.toString().trim()
                + "\n\n";

        String updatedHistory = newRecord + oldHistory;

        sharedPreferences.edit()
                .putString(KEY_HISTORY, updatedHistory)
                .apply();
    }

    private void loadHistory() {
        String history = sharedPreferences.getString(KEY_HISTORY, "");

        if (history.isEmpty()) {
            historyText.setText("История пока пустая");
        } else {
            historyText.setText(history);
        }
    }

    private void clearHistory() {
        sharedPreferences.edit()
                .remove(KEY_HISTORY)
                .apply();

        historyText.setText("История пока пустая");

        Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show();
    }
}