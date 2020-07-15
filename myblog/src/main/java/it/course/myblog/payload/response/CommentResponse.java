package it.course.myblog.payload.response;

import java.util.Date;

import it.course.myblog.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CommentResponse {
	
	private Long commentId;
	
	private String comment;
	
	private Date createdAt;
	
	private String author;
	
	private Long referenceComment;
	
	public static CommentResponse createFromEntity(Comment c) {
		
		
		return new CommentResponse(
			c.getId(),
			c.getVisible() == true ? c.getComment() : "********* CENSORED *********",
			c.getCreatedAt(),
			c.getCommentAuthor().getUsername(),
			c.getReferenceComment()	== null ? 0L : c.getReferenceComment().getId()			
			);
		
	}

}
