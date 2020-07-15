package it.course.myblog.service;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class PostService {
	
	
	// 2020-07-10 12:49:21 -> quando viene visto il dettaglio post
	// from -> 2020-07-10 00:00.00
	// to   -> 2020-07-10 23:59:59
	
	
	/* DJIALA ZANGUIM Albert Franck */
	public Date getStartOfDay(Date date) {
       
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(
			calendar.get(Calendar.YEAR), 
			calendar.get(Calendar.MONTH), 
			calendar.get(Calendar.DATE), 
			0, 0, 0
		);
		
		return calendar.getTime();
	}

	
	public Date getEndOfDay(Date date) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(
			calendar.get(Calendar.YEAR), 
			calendar.get(Calendar.MONTH), 
			calendar.get(Calendar.DATE), 
			23, 59, 59
		);
		
		return calendar.getTime();
		
	}
	
	
	public Date limitOfDay(Date date, String s) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(
			calendar.get(Calendar.YEAR), 
			calendar.get(Calendar.MONTH), 
			calendar.get(Calendar.DATE)
			);
		
		if(s.equals("S")) {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);			
		} 
		
		return calendar.getTime();
		
	}

}
