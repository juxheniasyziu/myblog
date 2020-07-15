package it.course.myblog.payload.response;

import it.course.myblog.entity.ReportComment;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CountReportsCommentByCommentResponse {
	
	private Long idComment;
	private String comment;
	private Long commentCount;	
	
	public static CountReportsCommentByCommentResponse createFromReport(ReportComment r) {
		
		return new CountReportsCommentByCommentResponse(
			r.getReportCommentCompositeKey().getReportedComment().getId(),
			r.getReportCommentCompositeKey().getReportedComment().getComment(),
			Long.valueOf(0) //0L
			);
	}

}
