package com.czj.student.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtils {
    
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 获取当前日期
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }
    
    /**
     * 获取当前时间
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }
    
    /**
     * 获取当前日期时间
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 日期格式化
     */
    public static String format(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
    
    /**
     * 时间格式化
     */
    public static String format(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }
    
    /**
     * 日期时间格式化
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }
    
    /**
     * 字符串转日期
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
    
    /**
     * 字符串转时间
     */
    public static LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(TIME_FORMAT));
    }
    
    /**
     * 字符串转日期时间
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }
    
    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    
    /**
     * LocalDateTime转Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 计算两个日期之间的天数
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * 获取月初日期
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }
    
    /**
     * 获取月末日期
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    
    /**
     * 获取指定日期的开始时间
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
    
    /**
     * 获取指定日期的结束时间
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
} 