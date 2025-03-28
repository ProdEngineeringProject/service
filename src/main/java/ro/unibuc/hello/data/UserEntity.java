package ro.unibuc.hello.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserEntity {

    @Id
    private String id;
    private String name;
    private String password;

    @Indexed(unique = true)    
    private String username;

    public UserEntity() {}

    public UserEntity(String name, String password, String username) {
        this.name = name;
        this.password = password;
        this.username = username;
    }

    public UserEntity(String id, String name, String password, String username) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.username = username;
    }

    public UserEntity(UserEntity other) {
        this.id = other.id;
        this.name = other.name;
        this.password = other.password;
        this.username = other.username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public final String getId() {
        return id;
    }

    public void setId(String Id) {
        this.id = Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder print = new StringBuilder("Users: " + "Name: " + name + " Username: " + username);
        return print.toString();
    }
}
