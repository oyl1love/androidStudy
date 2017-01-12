package test.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.style.base.BaseRecyclerViewAdapter;
import com.style.bean.Friend;
import com.style.bean.IMsg;
import com.style.bean.User;
import com.style.framework.R;
import com.style.manager.AccountManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatAdapter extends BaseRecyclerViewAdapter {
    private static final int MSG_TYPE_TEXT_LEFT = 0;
    private static final int MSG_TYPE_TEXT_RIGHT = 1;


    private User curUser;

    public ChatAdapter(Context context, List list) {
        super(context, list);
        curUser = AccountManager.getInstance().getCurrentUser();
    }

    @Override
    public int getItemViewType(int position) {
        IMsg msg = (IMsg) list.get(position);
        if (msg.getSenderId() == curUser.getUserId())
            return MSG_TYPE_TEXT_RIGHT;
        else
            return MSG_TYPE_TEXT_LEFT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItem(ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_TEXT_LEFT)
            return new ViewHolderTextLeft(mInflater.inflate(R.layout.adapter_msg_text_left, parent, false));
        else
            return new ViewHolderTextRight(mInflater.inflate(R.layout.adapter_msg_text_right, parent, false));
    }

    @Override
    public void onBindItem(RecyclerView.ViewHolder viewHolder, int position, Object data) {
        IMsg msg = (IMsg) data;
        if (getItemViewType(position) == MSG_TYPE_TEXT_LEFT) {
            ViewHolderTextLeft holder = (ViewHolderTextLeft) viewHolder;
            setText(holder.viewContent, msg.getContent());
        }else {
            ViewHolderTextRight holder = (ViewHolderTextRight) viewHolder;
            setText(holder.viewContent, msg.getContent());
        }

    }

    public class ViewHolderTextLeft extends RecyclerView.ViewHolder {
        @Bind(R.id.view_avatar)
        ImageView viewAvatar;
        @Bind(R.id.view_content)
        TextView viewContent;

        ViewHolderTextLeft(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class ViewHolderTextRight extends RecyclerView.ViewHolder {
        @Bind(R.id.view_avatar)
        ImageView viewAvatar;
        @Bind(R.id.view_content)
        TextView viewContent;

        ViewHolderTextRight(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
