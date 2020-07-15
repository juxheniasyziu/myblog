package it.course.myblog.payload.response;

import it.course.myblog.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PostToVerifyOrToPublish {
	
	private Long Id;
	private String title;
	
	public static PostToVerifyOrToPublish createFromEntity(Post p) {
		
		return new PostToVerifyOrToPublish(
			p.getId(),
			p.getTitle()
			);
	}

}
