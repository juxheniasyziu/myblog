package it.course.myblog.payload.response;

import it.course.myblog.entity.PostViewed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostViewedUniqueResponse {
	
	private Long postId;
	
	private String postTitle;
	
	private String ip;
	
	private String viewer;
	
	private Long totalView;

	public static PostViewedUniqueResponse createFromEntity(PostViewed pv) {
			
			return new PostViewedUniqueResponse(
					pv.getPost().getId(),
					pv.getPost().getTitle(),
					pv.getIp(),
					pv.getViewer(),
					pv.getTotalView()		
			);
	}
	
	
	
}
