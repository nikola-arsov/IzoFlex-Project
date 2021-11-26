package bg.softuni.model.entity;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity implements GrantedAuthority {
    private String name;

    public Role() {
    }

    public Role(String role) {
        this.name = role;
    }

    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @Transient
    public String getAuthority() {
        return this.getName();
    }

    @Override
    public String toString() {
        return this.name.substring(5);
    }
}
