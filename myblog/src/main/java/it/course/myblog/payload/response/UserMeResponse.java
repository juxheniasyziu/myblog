package it.course.myblog.payload.response;

import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import it.course.myblog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserMeResponse {
	
	private Long id;
	private String email;
	private String username;
	private Date createdAt;
	private Date updatedAt;
	
	public static UserMeResponse createFromEntity(User user) {
		
		return new UserMeResponse(
			user.getId(),
			user.getEmail(),
			user.getUsername(),
			user.getCreatedAt(),
			user.getUpdatedAt()
			);		
	}

}
