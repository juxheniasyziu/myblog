package it.course.myblog.payload.response;

import java.util.Date;
import java.util.Set;

import it.course.myblog.entity.DBFile;
import it.course.myblog.entity.Post;
import it.course.myblog.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PostsHomeResponse {
	
	private Long id;
	private String title;
	private String overview;
	private Date updatedAt;
	private String author;
	private DBFile image;
	private Double avgRating;
	private Set<Tag> tags;
	
	
	public static PostsHomeResponse createFromEntity(Post p) {
		
		return new PostsHomeResponse(
			p.getId(),
			p.getTitle(),
			p.getOverview(),
			p.getUpdatedAt(),
			p.getAuthor().getUsername(),
			p.getDbFile(),
			p.getAvgRating(),
			p.getTags()
			);
	}

}
