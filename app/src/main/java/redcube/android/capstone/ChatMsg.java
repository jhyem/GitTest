package redcube.android.capstone;

public class ChatMsg {

    public static final String ROLE_USER = "user";
    public static final String ROLE_BOT = "bot";
    public static final String ROLE_FAQ = "faq";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final int TYPE_MY_CHAT = 0;
    public static final int TYPE_BOT_CHAT = 1;

    public String role;
    public String content;

    public ChatMsg(int type, String content) {
        this.role = (type == TYPE_MY_CHAT) ? ROLE_USER : ROLE_BOT;
        this.content = content;
    }

    public ChatMsg(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
