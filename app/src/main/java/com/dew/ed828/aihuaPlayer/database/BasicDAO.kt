package com.dew.ed828.aihuaPlayer.database

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

@Dao
interface BasicDAO<Entity> {

    /* Searches */
//    fun getAll(): Flowable<List<Entity>>
    val all: Flowable<List<Entity>>
    fun listByService(serviceId: Int): Flowable<List<Entity>>

    /* Inserts */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(entity: Entity): Long

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertAll(vararg entities: Entity): List<Long>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertAll(entities: Collection<Entity>): List<Long>



    /* Deletes */
    @Delete
    fun delete(entity: Entity)

    @Delete
    fun delete(entities: Collection<Entity>): Int

    fun deleteAll(): Int

    /* Updates */
    @Update
    fun update(entity: Entity): Int

    @Update
    fun update(entities: Collection<Entity>)
}
