package it.course.myblog.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import it.course.myblog.entity.DBFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PostRequest {
	
	@NotBlank
	@Size(min=1, max=80)
	private String title;
	
	private String overview;
	
	@NotBlank
	private String content;
	
	private DBFile dbFile;
	
	
	
}
