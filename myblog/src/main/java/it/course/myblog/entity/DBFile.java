package it.course.myblog.entity;

import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="db_file")
@Data @AllArgsConstructor @NoArgsConstructor
public class DBFile{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;	

	@NotBlank
	private String fileName;
	
	@NotBlank
	private String fileType;
	
	@Lob
	private byte[] data;

	public DBFile(String fileName, String fileType, byte[] data) {
		super();
		this.fileName = fileName;
		this.fileType = fileType;
		this.data = data;
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DBFile))
			return false;
		DBFile other = (DBFile) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}
	
	
	
}


































