package mx.com.ciecas.app.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserComments {
	private User user;
	private Comments comments;
}
