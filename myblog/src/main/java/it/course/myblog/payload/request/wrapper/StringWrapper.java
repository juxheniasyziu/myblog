package it.course.myblog.payload.request.wrapper;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class StringWrapper {

	
	Set<String> strings;
	
}
