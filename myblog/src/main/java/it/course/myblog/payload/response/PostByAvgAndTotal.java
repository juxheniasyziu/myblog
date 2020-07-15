package it.course.myblog.payload.response;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.PostViewed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostByAvgAndTotal {
	
	private Long postId;
	
	private String postTitle;
	
	private Long totalVote;
	
	private double avgRating;

	public static PostByAvgAndTotal createFromEntity(Post post) {
			
			return new PostByAvgAndTotal(
					post.getId(),
					post.getTitle(),
					0L,
					post.getAvgRating()
			);
	}
	
	
	
}
