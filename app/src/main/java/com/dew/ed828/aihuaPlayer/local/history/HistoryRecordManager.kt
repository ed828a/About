package com.dew.ed828.aihuaPlayer.local.history

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager

import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.database.AppDatabase
import com.dew.ed828.aihuaPlayer.database.EdPlayerDatabase
import com.dew.ed828.aihuaPlayer.database.history.dao.SearchHistoryDAO
import com.dew.ed828.aihuaPlayer.database.history.dao.StreamHistoryDAO
import com.dew.ed828.aihuaPlayer.database.history.model.SearchHistoryEntry
import com.dew.ed828.aihuaPlayer.database.history.model.StreamHistoryEntity
import com.dew.ed828.aihuaPlayer.database.history.model.StreamHistoryEntry
import com.dew.ed828.aihuaPlayer.database.stream.dao.StreamDAO
import com.dew.ed828.aihuaPlayer.database.stream.dao.StreamStateDAO
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamEntity
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamStateEntity
import com.dew.ed828.aihuaPlayer.database.stream.model.StreamStatisticsEntry
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.util.*

/**
 *
 * Created by Edward on 12/4/2018.
 *
 */

class HistoryRecordManager(context: Context) {

    private val database: AppDatabase = EdPlayerDatabase.getInstance(context)
    private val streamTable: StreamDAO = database.streamDAO()
    private val streamHistoryTable: StreamHistoryDAO = database.streamHistoryDAO()
    private val searchHistoryTable: SearchHistoryDAO = database.searchHistoryDAO()
    private val streamStateTable: StreamStateDAO = database.streamStateDAO()
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val searchHistoryKey: String = context.getString(R.string.enable_search_history_key)
    private val streamHistoryKey: String = context.getString(R.string.enable_watch_history_key)

    val streamHistory: Flowable<List<StreamHistoryEntry>>
        get() = streamHistoryTable.history.subscribeOn(Schedulers.io())

    val streamStatistics: Flowable<List<StreamStatisticsEntry>>
        get() = streamHistoryTable.statistics.subscribeOn(Schedulers.io())

    private val isStreamHistoryEnabled: Boolean
        get() = sharedPreferences.getBoolean(streamHistoryKey, false)

    private val isSearchHistoryEnabled: Boolean
        get() = sharedPreferences.getBoolean(searchHistoryKey, false)

    ///////////////////////////////////////////////////////
    // Watch History
    ///////////////////////////////////////////////////////

    fun onViewed(info: StreamInfo): Maybe<Long> {
        if (!isStreamHistoryEnabled) return Maybe.empty()

        val currentTime = Date()
        return Maybe.fromCallable {
            database.runInTransaction<Long> {
                val streamId = streamTable.upsert(StreamEntity(info))
                val latestEntry = streamHistoryTable.getLatestEntry()

                if (latestEntry != null && latestEntry.streamUid == streamId) {
                    streamHistoryTable.delete(latestEntry)
                    latestEntry.accessDate = currentTime
                    latestEntry.repeatCount = latestEntry.repeatCount + 1
                    return@runInTransaction streamHistoryTable.insert(latestEntry)
                } else {
                    return@runInTransaction streamHistoryTable.insert(StreamHistoryEntity(streamId, currentTime))
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun deleteStreamHistory(streamId: Long): Single<Int> {
        return Single.fromCallable { streamHistoryTable.deleteStreamHistory(streamId) }
            .subscribeOn(Schedulers.io())
    }

    fun deleteWholeStreamHistory(): Single<Int> {
        return Single.fromCallable { streamHistoryTable.deleteAll() }
            .subscribeOn(Schedulers.io())
    }

    fun insertStreamHistory(entries: Collection<StreamHistoryEntry>): Single<List<Long>> {
        val entities = ArrayList<StreamHistoryEntity>(entries.size)
        for (entry in entries) {
            entities.add(entry.toStreamHistoryEntity())
        }
        return Single.fromCallable { streamHistoryTable.insertAll(entities) }
            .subscribeOn(Schedulers.io())
    }

    fun deleteStreamHistory(entries: Collection<StreamHistoryEntry>): Single<Int> {
        val entities = ArrayList<StreamHistoryEntity>(entries.size)
        for (entry in entries) {
            entities.add(entry.toStreamHistoryEntity())
        }
        return Single.fromCallable { streamHistoryTable.delete(entities) }
            .subscribeOn(Schedulers.io())
    }

    ///////////////////////////////////////////////////////
    // Search History
    ///////////////////////////////////////////////////////

    fun onSearched(serviceId: Int, search: String): Maybe<Long> {
        if (!isSearchHistoryEnabled) return Maybe.empty()

        val currentTime = Date()
        val newEntry = SearchHistoryEntry(currentTime, serviceId, search)

        return Maybe.fromCallable {
            database.runInTransaction<Long> {
                val latestEntry = searchHistoryTable.getLatestEntry()
                if (latestEntry != null && latestEntry.hasEqualValues(newEntry)) {
                    latestEntry.creationDate = currentTime
                    return@runInTransaction searchHistoryTable.update(latestEntry).toLong()
                } else {
                    return@runInTransaction searchHistoryTable.insert(newEntry)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun deleteSearchHistory(search: String): Single<Int> =
        Single.fromCallable { searchHistoryTable.deleteAllWhereQuery(search) }
            .subscribeOn(Schedulers.io())


    fun deleteWholeSearchHistory(): Single<Int> =
        Single.fromCallable { searchHistoryTable.deleteAll() }
            .subscribeOn(Schedulers.io())


    fun getRelatedSearches(
        query: String,
        similarQueryLimit: Int,
        uniqueQueryLimit: Int
    ): Flowable<List<SearchHistoryEntry>> =
        if (query.isNotEmpty())
            searchHistoryTable.getSimilarEntries(query, similarQueryLimit)
        else
            searchHistoryTable.getUniqueEntries(uniqueQueryLimit)


    ///////////////////////////////////////////////////////
    // Stream State History
    ///////////////////////////////////////////////////////

    fun loadStreamState(info: StreamInfo): Maybe<StreamStateEntity> =
        Maybe.fromCallable { streamTable.upsert(StreamEntity(info)) }
            .flatMap { streamId -> streamStateTable.getState(streamId).firstElement() }
            .flatMap { states -> if (states.isEmpty()) Maybe.empty() else Maybe.just(states[0]) }
            .subscribeOn(Schedulers.io())


    fun saveStreamState(info: StreamInfo, progressTime: Long): Maybe<Long> =
        Maybe.fromCallable {
            database.runInTransaction<Long> {
                val streamId = streamTable.upsert(StreamEntity(info))
                streamStateTable.upsert(StreamStateEntity(streamId, progressTime))
            }
        }.subscribeOn(Schedulers.io())


    ///////////////////////////////////////////////////////
    // Utility
    ///////////////////////////////////////////////////////

    fun removeOrphanedRecords(): Single<Int> =
        Single.fromCallable { streamTable.deleteOrphans() }
            .subscribeOn(Schedulers.io())

}
