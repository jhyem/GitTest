package redcube.android.capstone;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatMain extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatMsgAdapter adapter;
    private Button btnSend;
    private EditText etMsg;
    private ProgressBar progressBar;
    private List<ChatMsg> chatMsgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavNextActivity.setupBottomNavigation(this, bottomNavigationView);

        recyclerView = findViewById(R.id.recyclerView);
        btnSend = findViewById(R.id.btn_send);
        etMsg = findViewById(R.id.et_msg);
        progressBar = findViewById(R.id.progressBar);

        chatMsgList = new ArrayList<>();
        adapter = new ChatMsgAdapter();
        adapter.setDataList(chatMsgList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnFAQClickListener(question -> {
            chatMsgList.add(new ChatMsg(ChatMsg.ROLE_USER, question));
            adapter.notifyItemInserted(chatMsgList.size() - 1);
            sendQuestion(question);
        });

        etMsg.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                btnSend.setEnabled(s.length() > 0);
            }
        });

        btnSend.setOnClickListener(v -> {
            String msg = etMsg.getText().toString();
            chatMsgList.add(new ChatMsg(ChatMsg.ROLE_USER, msg));
            adapter.notifyItemInserted(chatMsgList.size() - 1);
            etMsg.setText(null);

            InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            sendQuestion(msg);
        });

        showFAQButtons();
    }

    private void showFAQButtons() {
        String[] faqQuestions = {
                "대형폐기물의 기준은 무엇인가요?",
                "대형폐기물 어떻게 신청하나요?",
                "납부필증 출력이 어려운데 어떻게 하나요?",

        };
        for (String question : faqQuestions) {
            chatMsgList.add(new ChatMsg(ChatMsg.ROLE_FAQ, question));
        }
        adapter.notifyDataSetChanged();
    }

    private String getPredefinedAnswer(String question) {
        switch (question.trim()) {
            case "대형폐기물의 기준은 무엇인가요?":
                return "가정 등에서 배출되는 가구, 가전제품, 생활용품 등 개별계량과 품명식별이 가능한 폐기물과 쓰레기 봉투에 담기 어려운 폐기물을 말합니다.";
            case "대형폐기물 어떻게 신청하나요?":
                return "온라인 신청하기 → 배출 신청서 작성 → 수수료 결제 → 납부필증 출력 → 배출품목에 납부필증 부착 → 배출 순서로 진행됩니다.";
            case "납부필증 출력이 어려운데 어떻게 하나요?":
                return "출력이 어려운 경우 빈 종이에 납부필증번호(바코드번호), 폐기물명, 금액, 배출장소 등을 기재하여 폐기물에 부착한 후 배출하시면 됩니다.";
            default:
                return null;
        }
    }

    private void sendQuestion(String question) {
        String predefined = getPredefinedAnswer(question);
        if (predefined != null) {
            chatMsgList.add(new ChatMsg(ChatMsg.ROLE_BOT, predefined));
            adapter.notifyItemInserted(chatMsgList.size() - 1);
            progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            sendMsgToChatGPT(question);
        }
    }

    private void sendMsgToChatGPT(String message) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ChatGPTApi api = retrofit.create(ChatGPTApi.class);

        List<ChatGPTRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatGPTRequest.Message("user", message));

        ChatGPTRequest request = new ChatGPTRequest("ft:gpt-3.5-turbo-0125:personal:faq-bot:BXsik4PS", messages);

        api.sendMessage(request).enqueue(new Callback<ChatGPTResponse>() {
            @Override
            public void onResponse(Call<ChatGPTResponse> call, Response<ChatGPTResponse> response) {
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();
                    chatMsgList.add(new ChatMsg(ChatMsg.ROLE_BOT, reply));
                    adapter.notifyItemInserted(chatMsgList.size() - 1);
                } else {
                    chatMsgList.add(new ChatMsg(ChatMsg.ROLE_BOT, "응답 실패"));
                    adapter.notifyItemInserted(chatMsgList.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<ChatGPTResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                chatMsgList.add(new ChatMsg(ChatMsg.ROLE_BOT, "오류: " + t.getMessage()));
                adapter.notifyItemInserted(chatMsgList.size() - 1);
            }
        });
    }
}
