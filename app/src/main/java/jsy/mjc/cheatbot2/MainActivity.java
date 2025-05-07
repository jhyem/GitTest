package jsy.mjc.cheatbot2;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout chatLayout;
    private EditText inputMessage;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatLayout = findViewById(R.id.chatLayout);
        inputMessage = findViewById(R.id.inputMessage);
        scrollView = findViewById(R.id.scrollView);
        Button sendButton = findViewById(R.id.sendButton);
        final EditText inputMessage = findViewById(R.id.inputMessage); //한국어 키보드 기능
        inputMessage.setPrivateImeOptions("defaultInputmode=korean;");


        addMessage("폐기봇", "반갑습니다. 무엇을 도와드릴까요?", false);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = inputMessage.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    addMessage("나", userInput, true);
                    inputMessage.setText("");

                    new Handler().postDelayed(() -> {
                        String response = getAnswer(userInput);
                        addMessage("폐기봇", response, false);
                    }, 600);
                }
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void addMessage(String sender, String message, boolean isUser) {
        TextView textView = new TextView(this);
        textView.setText(sender + ": " + message);
        textView.setPadding(16, 10, 16, 10);
        textView.setBackgroundColor(isUser ? 0xFFE0F7FA : 0xFFF8E1F4);
        textView.setTextColor(0xFF000000);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        params.gravity = isUser ? Gravity.END : Gravity.START;
        textView.setLayoutParams(params);

        chatLayout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    private String getAnswer(String input) {
        input = input.toLowerCase();
        if (input.contains("티비 스티커 가격 알려줘")) {
            return "\n네 TV의 경우\n 42인치 이상은 6000원\n25인치 이상은 3600원\n25인치 미만은 3000원입니다.";
        } else if (input.contains("공기청정기 가격 알려줘")) {
            return "높이 1m 이상일 경우 3000원 입니다.";
        } else if (input.contains("식기건조기 가격 알려줘")) {
            return "높이 1m이상 3500원 입니다.";
        } else if (input.contains("식기세척기 가격 알려줘")) {
            return "높이 1m 이상 3500원 입니다.";
        } else if (input.contains("에어컨 실외기 가격 알려줘")) {
            return "높이 1m 이상 2000원 입니다..";
        }else if (input.contains("정수기 가격 알려줘")) {
            return "높이 1m 이상 5000원 입니다.";
        }else if (input.contains("티비 받침대 가격 알려줘")) {
            return "길이 120cm 이상 5000원\n" +
                    "\n 길이 120cm 미만 3000원 입니다.";
        }else if (input.contains("거울 가격 알려줘")) {
            return "1m^2당 2000원\n" +
                    "\n1m^2미만 1000원 입니다.\n";
        }else if (input.contains("찬장 가격 알려줘")) {
            return "\n폭 130cm 이상 10000원\n" +
                    "\n폭 120cm 이상 7000원\n" +
                    "\n폭 90cm 이상 4000원\n" +
                    "\n폭 90cm 미만 3500원 입니다.";
        }else if (input.contains("책상 가격 알려줘")) {
            return "\n길이 120cm이상 5000원\n" +
                    "\n길이 120cm미만 3000원 입니다.\n";
        }else if (input.contains("책장 가격 알려줘")) {
            return "\n90cm X 180cm이상 9000원\n" +
                    "\n90cm X 180cm미만 5000원\n" +
                    "\n3단 이하(길이, 높이 100cm 이하) 2000원 입니다.\n";
        }else if (input.contains("싱크대 가격 알려줘")) {
            return "\n상판길이 120cm 이상 6000\n" +
                    "\n상판길이 120cm 미만 4000\n" +
                    "\n상판(대리석) 길이 120cm이상 12000\n" +
                    "\n상판(대리석) 길이 120cm 미만 10000원 입니다.\n";
        }else if (input.contains("유리 가격 알려줘")) {
            return "1m^2당\t2000원 입니다.";
        }else if (input.contains("장롱 가격 알려줘")) {
            return "\n가로 120cm 이상 17900\n" +
                    "\n가로 90cm 이상 11900\n" +
                    "\n가로 90cm 미만 7000원 입니다.\n";
        }else if (input.contains("장식장 가격 알려줘")) {
            return "높이 180cm 이상 10000\n" +
                    "\n높이 120cm 이상 5500\n" +
                    "\n높이 120cm 미만 4000원 입니다.";
        }else if (input.contains("게시판, 화이트보드 가격 알려줘")) {
            return "\n가로 1m이상 2000원\n" +
                    "\n가로 1m 미만 1000원 입니다.";
        }else if (input.contains("고무통 가격 알려줘")) {
            return "지름 1m 이상 5000\n" +
                    "\n지름 50cm 이상 3000원\n" +
                    "\n지름 50cm 미만 2000원 입니다.\n";
        }else if (input.contains("벽시계 가격 알려줘")) {
            return "\n길이 1m 이상 2500원\n" +
                    "\n길이 1m 미만 1000원 입니다.";
        }else if (input.contains("빨래건조대 가격 알려줘")) {
            return "\n길이 1m 이상 2000원\n" +
                    "\n길이 1m 미만 1000원 입니다.";
        }else if (input.contains("액자 가격 알려줘")) {
            return "\n높이 1m 이상 3000\n" +
                    "\n높이 50cm 이상~ 1m 미만 2000원\n" +
                    "\n높이 50cm 미만 1000원 입니다.";
        }else if (input.contains("어항 가격 알려줘")) {
            return "\n가로1m x 높이 60cm 초과 6000원\n" +
                    "\n가로1m x 높이 60cm 이하 4000원 입니다.";
        }else if (input.contains("조명기구 가격 알려줘")) {
            return "\n폭*높이 1m 이상 2000원\n" +
                    "\n폭*높이 1m 미만 1000원 입니다.";
        }else if (input.contains("캣타워 가격 알려줘")) {
            return "\n높이 1m 이상 5000원\n" +
                    "\n높이 1m 미만 3000원 입니다.\n";
        }else if (input.contains("항아리 가격 알려줘")) {
            return "\n높이 50cm 이상 2000원\n" +
                    "\n높이 50cm 미만 1000원 입니다.";
        }else if (input.contains("화분 가격 알려줘")) {
            return "\n높이 50cm 이상 1500원\n" +
                    "\n높이 50cm 미만 1000원 입니다.";
        }
        return "죄송합니다, 해당 문장은 지원하지 않습니다.";
    }
}