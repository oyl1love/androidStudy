package example.home.contact

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import com.style.base.BaseRecyclerViewAdapter
import com.style.framework.R
import com.style.framework.databinding.AdapterFriendBinding

import java.util.ArrayList

class FriendAdapter : BaseRecyclerViewAdapter<Int> {

    constructor(context: Context?, dataList: ArrayList<Int>) : super(context, dataList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val bd: AdapterFriendBinding = getBinding(R.layout.adapter_friend, parent)
        return ViewHolder(bd)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val f = getData(position)
        val s = "数据$f"
        holder.bd.viewMark.text = s
        //holder.bd.viewNick.setText(f.getUser().getUserName());
        super.setOnItemClickListener(holder.itemView, position)
        holder.bd.executePendingBindings()
    }

    class ViewHolder : RecyclerView.ViewHolder {
        var bd: AdapterFriendBinding

        constructor(bd: AdapterFriendBinding) : super(bd.root) {
            this.bd = bd
        }
    }
}
