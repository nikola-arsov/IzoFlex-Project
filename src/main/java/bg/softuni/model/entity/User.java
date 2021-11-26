package bg.softuni.model.entity;

import bg.softuni.model.enumeration.Gender;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity{
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal balance;
    private Gender gender;
    private Set<Role> roles;
    private String profilePicture;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private Set<Item> collection;

    public User() {
        this.setCreatedOn(LocalDateTime.now());
        this.setModifiedOn(LocalDateTime.now());
        this.setBalance(BigDecimal.ZERO);
        this.collection = new LinkedHashSet<>();
        this.roles = new LinkedHashSet<>();
    }


    @Column(nullable = false, unique = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "first_name", nullable = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "last_name", nullable = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(nullable = false, unique = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Enumerated
    @Column(nullable = false)
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "profile_picture")
    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "crated_on", nullable = false)
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
        this.setModifiedOn(LocalDateTime.now());
    }

    @ManyToMany(fetch = FetchType.EAGER)
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> role) {
        this.roles = role;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(nullable = false)
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        this.setModifiedOn(LocalDateTime.now());
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    public Set<Item> getCollection() {
        return collection;
    }

    public void setCollection(Set<Item> collection) {
        this.collection = collection;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "modified_on", nullable = false)
    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
}
