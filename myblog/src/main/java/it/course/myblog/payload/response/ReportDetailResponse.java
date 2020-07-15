package it.course.myblog.payload.response;

import java.util.Date;

import it.course.myblog.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ReportDetailResponse {
	
	private String reportReason;
	private String reporterUsername;
	private String postTitle;
	private Date createdAt;

	public static ReportDetailResponse createFromReport(Report r) {
		
		return new ReportDetailResponse(
			r.getReportReason(), 
			r.getReportCompositeKey().getReporter().getUsername(),
			r.getReportCompositeKey().getReportedPost().getTitle(),
			r.getCreatedAt()
			);
	}

}
