package it.course.myblog.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ReportCommentRequest {
	
	@NotNull
	private Long commentId;
	
	@NotBlank
	private String reportReason;

}