package com.salton123.biz_record.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaFormat
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.salton123.arch.mvp.BaseMvpActivity
import com.salton123.arch.view.TitleBarStyle
import com.salton123.biz_record.ScreenRecordDaemon
import com.salton123.biz_record.bean.RecordItem
import com.salton123.biz_record.db.RecordDatabase
import com.salton123.biz_record.mvp.IRecordView
import com.salton123.biz_record.mvp.RecordPresenter
import com.salton123.feature.multistatus.MultiStatusFeature
import com.salton123.soulove.biz_record.R
import com.salton123.soulove.common.Constants
import com.salton123.util.FileUtils
import com.salton123.util.RxUtils
import com.salton123.util.ScreenUtils
import com.salton123.widget.itemdecoration.RecyclerViewDivider
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.aty_record.*
import java.io.File

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/31 8:37 PM
 * ModifyTime: 8:37 PM
 * Description:
 */
@SuppressLint("CheckResult")
@Route(path = Constants.Router.Record.MAIN)
class RecordPage : BaseMvpActivity<RecordPresenter>(), IRecordView, OnRefreshLoadMoreListener {
    companion object {
        const val TAG = "RecordActivity"
    }

    override fun getRightStyle(): Int = TitleBarStyle.ICON
    override fun getLayout(): Int = R.layout.aty_record
    override fun getRightIcon(): Int = R.drawable.ic_common_sort
    override fun getPresenter(): RecordPresenter = RecordPresenter()
    override fun getLeftStyle(): Int = TitleBarStyle.HIDDEN
    override fun getTitleText(): String = "屏幕录制"
    override fun onStartRecord() {
        updateUi()
    }

    override fun onStopRecord() {
        updateUi()
    }

    override fun onRightClick(v: View?) {
        ARouter.getInstance()
                .build(Constants.Router.Record.SETTING)
                .navigation(activity())
    }

    private val mAdapter by lazy { RecordAdapter() }
    override fun initViewAndData() {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = GridLayoutManager(activity(), 2)
        RecyclerViewDivider.grid()
                .asSpace()
                .dividerSize(ScreenUtils.dp2px(10f))
                .includeEdge()
                .build()
                .addTo(recyclerView)
        updateUi()
        ivController.setOnClickListener {
            if (mPresenter.hasProjection()) {
                switchProjection()
            } else {
                mPresenter.requestProjection()
            }
        }
        refreshLayout.setOnRefreshLoadMoreListener(this)
        mAdapter.setOnItemChildLongClickListener { _, _, position ->
            Observable.create(ObservableOnSubscribe<Boolean> {
                val fileUrl = (mAdapter.getItem(position) as RecordItem).fileUrl
                val previewUrl = (mAdapter.getItem(position) as RecordItem).previewUrl
                RecordDatabase.mInstance.getRecordDao().delete(fileUrl)
                FileUtils.deleteFile(fileUrl)
                FileUtils.deleteFile(previewUrl)
            }).compose(RxUtils.schedulersTransformer()).subscribe {
                mAdapter.remove(position)
            }
            true
        }
        mAdapter.setOnItemChildClickListener { _, _, position ->
            val output = (mAdapter.getItem(position) as RecordItem).fileUrl
            val file = File(output)
            val vmPolicy = StrictMode.getVmPolicy()
            try {
                // disable detecting FileUriExposure on public file
                StrictMode.setVmPolicy(VmPolicy.Builder().build())
                viewResult(file)
            } finally {
                StrictMode.setVmPolicy(vmPolicy)
            }
        }
        getData()
        ScreenRecordDaemon.findVideoEncoders().observe(this, Observer {
            ivController?.isEnabled = it.isNotEmpty()
        })
    }

    private fun viewResult(file: File) {
        val view = Intent(Intent.ACTION_VIEW)
        view.addCategory(Intent.CATEGORY_DEFAULT)
        view.setDataAndType(Uri.fromFile(file), MediaFormat.MIMETYPE_VIDEO_AVC)
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(view)
        } catch (e: Exception) {
            // no activity can open this video
        }
    }

    private fun updateUi() {
        if (mPresenter.isRecording()) {
            ivController.setImageResource(R.drawable.ic_stop_record)
        } else {
            ivController.setImageResource(R.drawable.ic_start_record)
        }
    }

    private fun switchProjection() {
        if (!mPresenter.isRecording()) {
            mPresenter.startRecorder()
            ivController.setImageResource(R.drawable.ic_stop_record)
        } else {
            mPresenter.stopRecorder()
            ivController.setImageResource(R.drawable.ic_start_record)
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        refreshLayout.finishLoadMoreWithNoMoreData()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refreshLayout.finishRefresh(true)
        getData()
    }

    override fun onReload(v: View?) {
        super.onReload(v)
        if (mAdapter.data.isEmpty()) {
            longToast("请点击右下角录制按钮")
        }
    }

    override fun isOpenMultiStatus(): Boolean {
        return true
    }

    override fun fetchMultiStatusFeature(): MultiStatusFeature? {
        if (mMultiStatusFeature == null) {
            mMultiStatusFeature = MultiStatusFeature(this, R.id.refreshLayout)
        }
        return mMultiStatusFeature
    }

    private fun getData() {
        RecordDatabase.mInstance.getRecordDao()
                .getAllRecord()
                ?.compose(RxUtils.schedulersTransformer())
                ?.subscribe {
                    if (it.isEmpty()) {
                        showNoDataView()
                    } else {
                        mAdapter.setNewData(it)
                    }
                }
    }
}