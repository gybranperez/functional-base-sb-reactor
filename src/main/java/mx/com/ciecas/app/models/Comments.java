package mx.com.ciecas.app.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Comments {
	private List<String> comments;

	public Comments() {
		this.comments = new ArrayList<>();	
	}
	
	public void addComment(String comment) {
		this.comments.add(comment);
	}
	
	
	
}
