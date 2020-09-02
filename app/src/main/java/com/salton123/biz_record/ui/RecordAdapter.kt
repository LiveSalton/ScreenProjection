package com.salton123.biz_record.ui

import android.graphics.Bitmap
import android.widget.ImageView
import com.blankj.utilcode.util.FileUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hjq.widget.layout.RationCardView
import com.salton123.biz_record.bean.RecordItem
import com.salton123.biz_record.db.RecordDatabase
import com.salton123.log.XLog
import com.salton123.soulove.biz_record.R
import com.salton123.util.RxUtils
import com.salton123.utils.BitmapUtil
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import java.util.concurrent.TimeUnit

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/31 8:44 PM
 * ModifyTime: 8:44 PM
 * Description:
 */
const val TAG = "RecordAdapter"

class RecordAdapter : BaseQuickAdapter<RecordItem, BaseViewHolder>(R.layout.adapter_record, null) {
    override fun convert(helper: BaseViewHolder, item: RecordItem?) {
        val ivPreview = helper.getView<ImageView>(R.id.ivPreview)
        val ivRoot = helper.getView<RationCardView>(R.id.ivRoot)
        item?.let {
            if (!FileUtils.isFileExists(item.previewUrl)) {
                Observable.create(ObservableOnSubscribe<Bitmap> {
                    val bitmapFuture = Glide.with(mContext)
                            .asBitmap()
                            .load(item.fileUrl)
                            .submit()
                    val bitmap = bitmapFuture.get()
                    val videoWidth = bitmap.width
                    val videoHeight = bitmap.height
                    BitmapUtil.saveImage(item.fileUrl.replace("mp4", "jpg"),
                            bitmap, Bitmap.CompressFormat.JPEG, 60)
                    item.width = videoWidth
                    item.height = videoHeight
                    RecordDatabase.mInstance.getRecordDao().insert(item)
                    XLog.i(TAG, "videoWidth:$videoWidth,videoHeight:$videoHeight")
                    it.onNext(bitmap)
                    it.onComplete()
                }).delay(1000, TimeUnit.MICROSECONDS)
                        .compose(RxUtils.schedulersTransformer())
                        .subscribe({
                            XLog.i(TAG, "save record:$it")
                            Glide.with(mContext).load(it).into(ivPreview)
                        }, {
                            XLog.e(TAG, it.message)
                        }, {})
            } else {
                Glide.with(mContext)
                        .load(item.previewUrl)
                        .fitCenter()
                        .into(ivPreview)
                ivRoot.updateRatio(item.width.toFloat() / item.height)
            }
        }
        helper.addOnClickListener(R.id.ivPreview)
        helper.addOnLongClickListener(R.id.ivPreview)
    }
}