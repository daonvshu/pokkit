package com.daonvshu.mikan.utils

import com.daonvshu.shared.database.schema.MikanDataRecord
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.ZoneId

object MikanDataParseUtil {

    fun getSeasonTimePointByIndex(year: Int, seasonIndex: Int): Long {
        val month = seasonIndex * 3 + 1
        val date = LocalDate.of(year, month, 1)
        val dateTime = date.atStartOfDay()
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun parseData(seasonTime: Long, data: String): List<MikanDataRecord> {
        val result = mutableListOf<MikanDataRecord>()
        try {
            val document = Jsoup.parse(data)
            val elements = document.select("[data-dayofweek]")
            elements.forEach { week ->
                val weekDay = week.attr("data-dayofweek")
                val liElement = week.select("li")
                liElement.forEach { li ->
                    val span = li.select("span")
                    var thumbnail = span.attr("data-src")
                    if (thumbnail.isNotEmpty()) {
                        thumbnail = thumbnail.split("?")[0]
                    }
                    val bangumiId = span.attr("data-bangumiid")

                    val a = li.select("a")
                    val link = a.attr("href")
                    val title = a.attr("title")

                    result.add(MikanDataRecord(
                        mikanId = bangumiId.toInt(),
                        bindBangumiId = -1,
                        link = link,
                        seasonTime = seasonTime,
                        dayOfWeek = weekDay.toInt(),
                        title = title,
                        thumbnail = thumbnail,
                        favorite = false
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}