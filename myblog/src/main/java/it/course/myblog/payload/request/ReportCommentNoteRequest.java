package it.course.myblog.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReportCommentNoteRequest {
	
	@NotNull
	private Long commentId;
	
	@NotNull
	private Long reporterId;
	
	@NotBlank
	private String reportNote;
	
	private boolean valid;

}
