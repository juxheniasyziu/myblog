package it.course.myblog.payload.request;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostViewedRequest {
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date from;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date to;

}
