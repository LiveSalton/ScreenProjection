package com.salton123.biz_record.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.salton123.app.BaseApplication
import com.salton123.biz_record.bean.RecordItem

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/31 9:09 PM
 * ModifyTime: 9:09 PM
 * Description:
 */
@Database(entities = [RecordItem::class], version = 1)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun getRecordDao(): RecordItemDao

    companion object {
        val mInstance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            Room.databaseBuilder(BaseApplication.sInstance, RecordDatabase::class.java, "record.db").build()
        }
    }
}