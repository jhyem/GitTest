
package redcube.android.capstone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMsg> dataList;

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_FAQ = 2;

    public void setDataList(List<ChatMsg> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void addChatMsg(ChatMsg chatMsg) {
        dataList.add(chatMsg);
        notifyItemInserted(dataList.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMsg msg = dataList.get(position);
        if (msg.role.equals(ChatMsg.ROLE_USER)) {
            return TYPE_USER;
        } else if (msg.role.equals(ChatMsg.ROLE_FAQ)) {
            return TYPE_FAQ;
        } else {
            return TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.my_msg, parent, false); // 사용자 → 오른쪽
            return new MyChatViewHolder(view);
        } else if (viewType == TYPE_FAQ) {
            View view = inflater.inflate(R.layout.faq_msg, parent, false);
            return new FAQViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.chatbot_msg, parent, false); // GPT → 왼쪽
            return new BotChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMsg chatMsg = dataList.get(position);
        if (holder instanceof MyChatViewHolder) {
            ((MyChatViewHolder) holder).setMsg(chatMsg);
        } else if (holder instanceof BotChatViewHolder) {
            ((BotChatViewHolder) holder).setMsg(chatMsg);
        } else if (holder instanceof FAQViewHolder) {
            ((FAQViewHolder) holder).bind(chatMsg);
        }
    }

    @Override
    public int getItemCount() {
        return (dataList == null) ? 0 : dataList.size();
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMsg;
        public MyChatViewHolder(View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tv_msg);
        }
        public void setMsg(ChatMsg msg) {
            tvMsg.setText(msg.content);
        }
    }

    public class BotChatViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMsg;
        public BotChatViewHolder(View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tv_msg);
        }
        public void setMsg(ChatMsg msg) {
            tvMsg.setText(msg.content);
        }
    }

    public class FAQViewHolder extends RecyclerView.ViewHolder {
        private Button btnOption;
        public FAQViewHolder(View itemView) {
            super(itemView);
            btnOption = itemView.findViewById(R.id.btn_faq);
        }
        public void bind(ChatMsg msg) {
            btnOption.setText(msg.content);
            btnOption.setOnClickListener(v -> {
                if (onFAQClickListener != null) {
                    onFAQClickListener.onFAQClicked(msg.content);
                }
            });
        }
    }

    public interface OnFAQClickListener {
        void onFAQClicked(String question);
    }

    private OnFAQClickListener onFAQClickListener;

    public void setOnFAQClickListener(OnFAQClickListener listener) {
        this.onFAQClickListener = listener;
    }
}
