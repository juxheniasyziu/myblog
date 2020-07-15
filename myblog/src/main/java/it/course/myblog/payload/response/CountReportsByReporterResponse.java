package it.course.myblog.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class CountReportsByReporterResponse {
	
	private Long idUser;
	private String username;
	private Long reportCount;

}