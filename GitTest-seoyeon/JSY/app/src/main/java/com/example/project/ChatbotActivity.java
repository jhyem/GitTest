package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView, cardMenu;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList = new ArrayList<>();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        cardMenu = findViewById(R.id.cardMenu);

        chatAdapter = new ChatAdapter(chatList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        findViewById(R.id.channelMenuText).setOnClickListener(v -> {
            cardMenu.setVisibility(cardMenu.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        List<CardMenuAdapter.CardMenuItem> cardItems = Arrays.asList(
                new CardMenuAdapter.CardMenuItem("신청방법"),
                new CardMenuAdapter.CardMenuItem("장소"),
                new CardMenuAdapter.CardMenuItem("스티커 가격")
        );

        cardMenu.setLayoutManager(new GridLayoutManager(this, 3));
        cardMenu.setAdapter(new CardMenuAdapter(cardItems, selected -> {
            cardMenu.setVisibility(View.GONE);

            // 오른쪽 말풍선
            addMessage(new ChatMessage(selected, true));


            handler.postDelayed(() -> {
                String reply;
                switch (selected) {
                    case "신청방법":
                        reply = "네, 방법은 두가지 입니다.\n 인터넷 신청: 접수 → 결제 → 필증 인쇄/부착 → 배출 → 수거" +
                                "동주민센터 방문신청: 방문신고서에 의거 배출 신고 및 수수료 납부 → 신고서 접수 후 수수료 징수 및 스티커 발부 → 스티커 부착하여 배출 ";
                        break;
                    case "장소":
                        reply = "주민센터 또는 지정 판매소에서 구매 가능합니다." + "\n 현재 서울구청, 인천 미추홀구/ 서구 있습니다. ";
                        break;
                    case "스티커 가격":
                        reply = "스티커 가격은 용량 및 지역에 따라 다릅니다.";
                        break;
                    default:
                        reply = "알 수 없는 항목입니다.";
                }
                addMessage(new ChatMessage(reply, false));
            }, 2000);
        }));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_chatbot);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ChatbotActivity.this, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(ChatbotActivity.this, GoogleMapActivity.class));
                return true;
            }
            return false;
        });
    }

    private void addMessage(ChatMessage message) {
        chatList.add(message);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        chatRecyclerView.scrollToPosition(chatList.size() - 1);
    }
}
