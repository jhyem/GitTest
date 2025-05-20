package redcube.android.capstone;

import android.content.Context;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavNextActivity {
    public static void setupBottomNavigation(Context context, BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_sticker) {
                context.startActivity(new Intent(context, GoogleMapActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                context.startActivity(new Intent(context, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_chatbot) {
                context.startActivity(new Intent(context, ChatMain.class));
                return true;
            }

            return false;
        });
    }
}