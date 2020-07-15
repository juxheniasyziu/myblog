package it.course.myblog.payload.response;

import java.util.Date;

import it.course.myblog.entity.ReportComment;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ReportCommentDetailResponse {
	
	private String reportReason;
	private String reporterUsername;
	private String comment;
	private Date createdAt;

	public static ReportCommentDetailResponse createFromReportComment(ReportComment r) {
		
		return new ReportCommentDetailResponse(
			r.getReportReason(), 
			r.getReportCommentCompositeKey().getReporter().getUsername(),
			r.getReportCommentCompositeKey().getReportedComment().getComment(),
			r.getCreatedAt()
			);
	}

}
