package com.example.myapplication.util

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.LocalDate

/**
 * 农历工具类
 * 用于将公历日期转换为农历日期显示
 * 
 * 使用 lunar-java 库 (cn.6tail:lunar:1.7.7)
 * 官方文档：https://6tail.cn/calendar/api.html
 */
object LunarUtils {

    /**
     * 获取指定日期的农历显示文本
     * 格式：农历X月X日 或 节气名称
     * 
     * @param date 公历日期
     * @return 农历显示文本，例如："农历十月十五" 或 "立春"
     */
    fun getLunarText(date: LocalDate): String {
        return try {
            // 创建公历日期对象
            val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
            // 转换为农历
            val lunar = solar.lunar
            
            // 优先显示节气（如果有）
            val solarTerm = lunar.jieQi
            if (solarTerm.isNotEmpty()) {
                return solarTerm
            }
            
            // 显示农历日期
            val monthName = lunar.monthInChinese
            val dayName = lunar.dayInChinese
            
            // 如果是初一，只显示月份
            if (dayName == "初一") {
                return "农历$monthName"
            }
            
            // 普通日期显示月和日
            "农历$monthName$dayName"
        } catch (e: Exception) {
            // 如果转换失败，返回空字符串
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取农历完整信息（用于详细显示）
     * 格式：农历XXXX年X月X日
     */
    fun getLunarFullText(date: LocalDate): String {
        return try {
            val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
            val lunar = solar.lunar
            
            val solarTerm = lunar.jieQi
            if (solarTerm.isNotEmpty()) {
                return solarTerm
            }
            
            val yearName = lunar.yearInGanZhi
            val monthName = lunar.monthInChinese
            val dayName = lunar.dayInChinese
            
            "农历${yearName}年$monthName$dayName"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取农历月份和日期（简化显示）
     * 格式：X月X日 或 节气
     */
    fun getLunarMonthDay(date: LocalDate): String {
        return try {
            val solar = Solar(date.year, date.monthValue, date.dayOfMonth)
            val lunar = solar.lunar
            
            val solarTerm = lunar.jieQi
            if (solarTerm.isNotEmpty()) {
                return solarTerm
            }
            
            val monthName = lunar.monthInChinese
            val dayName = lunar.dayInChinese
            
            if (dayName == "初一") {
                monthName
            } else {
                "$monthName$dayName"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

