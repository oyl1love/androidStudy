package example.customView

import android.os.Bundle
import com.style.base.activity.BaseDefaultTitleBarActivity
import com.style.framework.R
import com.style.framework.databinding.ActivityHeartLineBinding
import com.style.view.healthy.HeartLineChart
import java.util.*

class HeartLineActivity : BaseDefaultTitleBarActivity() {

    lateinit var bd: ActivityHeartLineBinding

    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        setContentView(R.layout.activity_heart_line)
        setToolbarTitle("心率曲线图")
        bd = getBinding()
        bd.btnRefresh.setOnClickListener { v -> refresh() }
    }

    fun refresh() {
        bd.heartLine.setData(getData())
    }

    private fun getData(): List<HeartLineChart.HeartLineItem> {
        val list = ArrayList<HeartLineChart.HeartLineItem>(100)
        val random = Random()
        for (i in 0..99) {
            val b = HeartLineChart.HeartLineItem(random.nextInt(40) + 60, "00:00")
            list.add(b)
        }
        return list
    }
}
