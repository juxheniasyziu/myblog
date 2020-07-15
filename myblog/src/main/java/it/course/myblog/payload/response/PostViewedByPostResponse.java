package it.course.myblog.payload.response;

import it.course.myblog.entity.PostViewed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostViewedByPostResponse {
	
	private Long postId;
	
	private String postTitle;
	
	//private String ip;
	
	///private Date createdAt;
	
	//private String viewer;
	
	private Long totalView;

	public static PostViewedByPostResponse createFromEntity(PostViewed pv) {
			
			return new PostViewedByPostResponse(
					pv.getPost().getId(),
					pv.getPost().getTitle(),
					//pv.getIp(),
					//pv.getCreatedAt(),
					//pv.getViewer(),
					pv.getTotalView()		
			);
	}
	
	
	
}
