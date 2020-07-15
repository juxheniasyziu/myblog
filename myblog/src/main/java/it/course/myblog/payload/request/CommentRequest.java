package it.course.myblog.payload.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CommentRequest {
	
	@NotBlank
	@Max(value=150)
	private String comment;
	
	@NotNull
	private Long postId;
	
	private Long referenceComment;

}
