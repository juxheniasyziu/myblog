package it.course.myblog.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReportNoteRequest {
	
	@NotNull
	private Long postId;
	
	@NotNull
	private Long reporterId;
	
	@NotBlank
	private String reportNote;
	
	private boolean valid;

	
}
