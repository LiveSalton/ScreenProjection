package net.yrom.screenrecorder.callback

import com.salton123.biz_record.bean.ProjectionProp

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/7/17 18:34
 * ModifyTime: 18:34
 * Description:
 */
interface IRecordAction {
    fun updateProp(prop: ProjectionProp)
    fun startRecorder()
    fun stopRecorder()
    fun cancelRecorder()
    fun isRecording(): Boolean
}