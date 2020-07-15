package it.course.myblog.payload.response;

import java.util.Date;
import java.util.List;
import java.util.Set;

import it.course.myblog.entity.DBFile;
import it.course.myblog.entity.Post;
import it.course.myblog.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PostDetail {
	
	private Long id;
	private String title;
	private String content;
	private Date updatedAt;
	private String author;
	private DBFile image;
	private Double avgRating;
	private Set<Tag> tags;
	private List<CommentResponse> comments;
	
	
	public static PostDetail createFromEntity(Post p, List<CommentResponse> comments) {
		
		return new PostDetail(
			p.getId(),
			p.getTitle(),
			p.getContent(),
			p.getUpdatedAt(),
			p.getAuthor().getUsername(),
			p.getDbFile(),
			p.getAvgRating(),
			p.getTags(),
			comments
			);
	}

}
