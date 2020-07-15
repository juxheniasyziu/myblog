package it.course.myblog.payload.response;

import it.course.myblog.entity.PostViewed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostViewedUniquePostResponse {
	
	private Long postId;
	
	private String postTitle;
	
	private Long totalView;

	public static PostViewedUniquePostResponse createFromPostViewedUniqueResponse(PostViewedUniqueResponse pvu) {
			
			return new PostViewedUniquePostResponse(
					pvu.getPostId(),
					pvu.getPostTitle(),
					pvu.getTotalView()		
			);
	}
	
	
	
}
