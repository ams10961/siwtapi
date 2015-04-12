package ams10961.siwt.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormatting {

	public static String ISO8601_TIMEZONE = "UTC";
	public static String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";
	public static String ANNOTATOR_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
	
	// http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
	//	
//			  "created": "2011-05-24T18:52:08.036814",   # created datetime in iso8601 format (added by backend)
//				
//			G   Era designator       Text               AD
//			y   Year                 Year               1996; 96
//			M   Month in year        Month              July; Jul; 07
//			w   Week in year         Number             27
//			W   Week in month        Number             2
//			D   Day in year          Number             189
//			d   Day in month         Number             10
//			F   Day of week in month Number             2
//			E   Day in week          Text               Tuesday; Tue
//			u   Day number of week   Number             1
//			a   Am/pm marker         Text               PM
//			H   Hour in day (0-23)   Number             0
//			k   Hour in day (1-24)   Number             24
//			K   Hour in am/pm (0-11) Number             0
//			h   Hour in am/pm (1-12) Number             12
//			m   Minute in hour       Number             30
//			s   Second in minute     Number             55
//			S   Millisecond          Number             978
//			z   Time zone            General time zone  Pacific Standard Time; PST; GMT-08:00
//			Z   Time zone            RFC 822 time zone  -0800
//			X   Time zone            ISO 8601 time zone -08; -0800; -08:00
			
	
	static TimeZone tz = TimeZone.getTimeZone(ISO8601_TIMEZONE);

	/*
	 * new objects for thread-safety
	 */
	public static String iso8601(Date date) {
		if (date != null) {
			DateFormat df = new SimpleDateFormat(ISO8601_FORMAT);
			df.setTimeZone(tz);
			return df.format(date);
		} else {
			return "";
		}
	}
	
	public static String annotatorDate (Date date) {
		if (date != null) {
			DateFormat df = new SimpleDateFormat(ANNOTATOR_FORMAT);
			df.setTimeZone(tz);
			return df.format(date);
		} else {
			return "";
		}
	}
}
