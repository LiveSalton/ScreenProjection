package com.salton123.biz_record.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.salton123.biz_record.bean.RecordItem
import io.reactivex.Observable
import org.jetbrains.annotations.NotNull

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/31 9:15 PM
 * ModifyTime: 9:15 PM
 * Description:
 */
@Dao
interface RecordItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: RecordItem): Long

    @Query("DELETE FROM " + RecordItem.TABLE_NAME + " WHERE fileUrl == :fileUrl")
    fun delete(@NotNull fileUrl: String)

    @Delete
    fun delete(entity: RecordItem?)

    @Query("SELECT * FROM " + RecordItem.TABLE_NAME)
    fun getAllRecord(): Observable<List<RecordItem>>?

    @Query("SELECT * FROM " + RecordItem.TABLE_NAME)
    fun getAllRecords(): LiveData<List<RecordItem>>?
}