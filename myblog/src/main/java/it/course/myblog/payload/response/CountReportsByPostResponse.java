package it.course.myblog.payload.response;

import it.course.myblog.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CountReportsByPostResponse {
	
	private Long idPost;
	private String postTitle;
	private Long postCount;	
	
	public static CountReportsByPostResponse createFromReport(Report r) {
		
		return new CountReportsByPostResponse(
			r.getReportCompositeKey().getReportedPost().getId(),
			r.getReportCompositeKey().getReportedPost().getTitle(),
			Long.valueOf(0) //0L
			);
	}

}
