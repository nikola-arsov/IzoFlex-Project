package bg.softuni;

import bg.softuni.model.binding.EditProfileModel;
import bg.softuni.model.binding.EditRolesModel;
import bg.softuni.model.binding.MoneyTransactionModel;
import bg.softuni.model.binding.RegisterUserModel;
import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Photo;
import bg.softuni.model.entity.Role;
import bg.softuni.model.entity.User;
import bg.softuni.model.enumeration.Gender;
import bg.softuni.model.service.NotificationServiceModel;
import bg.softuni.model.service.UserServiceModel;
import bg.softuni.model.view.CollectionItem;
import bg.softuni.model.view.ProfileView;
import bg.softuni.repository.UserRepository;
import bg.softuni.service.impl.UserServiceImpl;
import bg.softuni.service.interf.NotificationService;
import bg.softuni.service.interf.RoleService;
import bg.softuni.service.interf.UserService;
import bg.softuni.util.core.impl.ValidatorUtilImpl;
import bg.softuni.util.core.MultipartFileHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private final String NONEXISTENT_USERNAME = "NONEXISTENT";
    private final String ADMIN_USERNAME = "ADMIN";

    @Mock
    private NotificationService notificationService;
    @Mock
    private MultipartFileHandler fileHandler;
    @Mock
    private UserRepository repository;
    @Mock
    private RoleService roleService;
    private UserService userService;
    private User admin;
    private Role userRole;
    private PasswordEncoder encoder;

    @BeforeEach
    public void init() {
        this.encoder = new BCryptPasswordEncoder();

        this.userService = new UserServiceImpl(roleService, fileHandler, notificationService,
                repository, new ModelMapper(), new ValidatorUtilImpl(), encoder);

        this.admin = new User();
        this.admin.setUsername("ADMIN");
        this.admin.setPassword(encoder.encode("PASS"));
        this.admin.setEmail("admin@abv.bg");
        this.admin.setBalance(BigDecimal.ZERO);
        this.admin.setProfilePicture("/uploads/profile.jpg");

        Role adminRole = new Role("ROLE_ADMIN");
        adminRole.setId("1");
        this.userRole = new Role("ROLE_USER");
        this.userRole.setId("2");

        this.admin.setRoles(Set.of(adminRole, userRole));

        File dummy = new File("uploads/profile.jpg");
        if (!dummy.exists()) {
            if (dummy.mkdirs()) {
                new File("uploads/profile.jpg");
            }
        }
    }

    @Test
    public void testLoadUserByUsernameWithInvalidUsername() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> this.userService.loadUserByUsername(NONEXISTENT_USERNAME));
    }

    @Test
    public void testLoadUserByUsername() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        UserDetails details = this.userService.loadUserByUsername(ADMIN_USERNAME);

        assertEquals(ADMIN_USERNAME, details.getUsername());
        assertEquals(this.admin.getPassword(), details.getPassword());
        assertEquals(this.admin.getRoles().size(), details.getAuthorities().size());
        assertTrue(details.getAuthorities().containsAll(this.admin.getRoles()));
    }

    @Test
    public void testGetDefaultUsers() {
        User user = new User();
        user.setUsername("USER");

        when(repository.getAllByUsernameOrUsername(ADMIN_USERNAME.toLowerCase(), "user")).thenReturn(List.of(this.admin, user));
        List<String> result = this.userService.getDefaultUsers();

        assertEquals(ADMIN_USERNAME, result.get(0));
        assertEquals("USER", result.get(1));
    }

    @Test
    public void testGetFullNameWithMissingUser() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertNull(userService.getFullName(NONEXISTENT_USERNAME));
    }

    @Test
    public void testGetFullName() {
        this.admin.setFirstName("Nikola");
        this.admin.setLastName("Arsov");

        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertEquals("Nikola Arsov", userService.getFullName(ADMIN_USERNAME));
    }

    @Test
    public void testGetIdByUsernameWithWrongUsername() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getIdByUsername(NONEXISTENT_USERNAME));
    }

    @Test
    public void testGetIdByUsername() {
        this.admin.setId("TEST");
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertEquals("TEST", userService.getIdByUsername(ADMIN_USERNAME));
    }

    @Test
    public void testGetBalanceWithWrongUsername() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getBalance(NONEXISTENT_USERNAME));
    }

    @Test
    public void testGetBalance() {
        this.admin.setBalance(BigDecimal.TEN);
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertEquals(BigDecimal.TEN, userService.getBalance(ADMIN_USERNAME));
    }

    @Test
    public void testGetCollectionCount() {
        this.admin.setCollection(Set.of(new Item()));
        when(repository.findByUsername(ADMIN_USERNAME)).thenReturn(this.admin);
        assertEquals(1, this.userService.getCollectionCount(ADMIN_USERNAME));
    }

    @Test
    public void testGetAllUsernamesWithEmptyRepo() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        assertEquals(0, userService.getAllUsernames().size());
    }

    @Test
    public void testGetAllUsernames() {
        when(repository.findAll()).thenReturn(List.of(this.admin));
        List<String> out = userService.getAllUsernames();

        assertEquals(1, out.size());
        assertEquals("ADMIN", out.get(0));
    }

    @Test
    public void testGetUserByUsernameWithInvalidUsername() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.userService.getUserByUsername(NONEXISTENT_USERNAME));
    }

    @Test
    public void testGetUserByUsername() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        User out = this.userService.getUserByUsername(ADMIN_USERNAME);

        assertEquals(ADMIN_USERNAME, out.getUsername());
        assertEquals(this.admin.getPassword(), out.getPassword());
        assertEquals(this.admin.getRoles().size(), out.getRoles().size());
        assertTrue(out.getRoles().containsAll(this.admin.getRoles()));
    }

    @Test
    public void testNewNotification() {
        NotificationServiceModel notification = new NotificationServiceModel();
        notification.setText("TEXT");
        notification.setUserUsername("ADMIN");

        when(notificationService.createNotification(this.admin, "TEXT")).thenReturn(notification);
        NotificationServiceModel out = userService.newNotification(this.admin, "TEXT");

        assertEquals(out.getText(), notification.getText());
        assertEquals(out.getUserUsername(), notification.getUserUsername());
    }

    @Test
    public void testRegisterUserWithBindingErrors() {
        RegisterUserModel model = new RegisterUserModel(
                "t", "t", "x", "t", "t", "e", "");
        assertThrows(IllegalStateException.class, () -> this.userService.registerUser(model));
    }

    @Test
    public void testRegisterUserWithExistingUsername() {
        RegisterUserModel model = new RegisterUserModel(
                ADMIN_USERNAME, "ADMINADMIN", "ADMINADMIN", ADMIN_USERNAME, ADMIN_USERNAME, "ADMIN@dsad.bg", "MALE");

        when(this.repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.registerUser(model));
    }

    @Test
    public void testRegisterUserWithExistingEmail() {
        RegisterUserModel model = new RegisterUserModel(
                "USER", "USERUSER", "USERUSER", ADMIN_USERNAME, ADMIN_USERNAME, "admin@abv.bg", "MALE");

        when(this.repository.getByEmail("admin@abv.bg")).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.registerUser(model));
    }

    @Test
    public void testRegisterUserWithWrongGender() {
        RegisterUserModel model = new RegisterUserModel(
                "USER", "USERUSER", "USERUSER", ADMIN_USERNAME, ADMIN_USERNAME, "user@abv.bg", "SEXY");
        assertThrows(IllegalStateException.class, () -> this.userService.registerUser(model));
    }

    @Test
    public void testRegisterUser() {
        RegisterUserModel model = new RegisterUserModel("USER", "USERUSER", "USERUSER", ADMIN_USERNAME, ADMIN_USERNAME, "user@abv.bg", "МЪЖ");
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(roleService.getRolesByNames(List.of("ROLE_USER"))).thenReturn(List.of(this.userRole));
        when(repository.saveAndFlush(any())).thenAnswer((Answer<User>) mock -> {
            User input = mock.getArgument(0);
            input.setId("USER_ID");
            return input;
        });
        UserServiceModel output = userService.registerUser(model);
        verify(notificationService).createNotification(userCaptor.capture(), stringCaptor.capture());

        assertEquals("USER_ID", output.getId());
        assertEquals("USER", output.getUsername());
        assertEquals("user@abv.bg", output.getEmail());
        Assertions.assertEquals(Gender.МЪЖ, output.getGender());
        assertTrue(this.encoder.matches("USERUSER", output.getPassword()));
        assertEquals("USER_ID", userCaptor.getValue().getId());
        assertEquals("USER", userCaptor.getValue().getUsername());
        assertEquals("Здравейте ADMIN ADMIN, добре дошли в нашия сайт!", stringCaptor.getValue());
    }

    @Test
    public void testDepositWithInvalidUser() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> this.userService.deposit(NONEXISTENT_USERNAME, new MoneyTransactionModel()));
    }

    @Test
    public void testDepositWithInvalidModel() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.deposit(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.ZERO)));
        assertThrows(IllegalStateException.class, () -> this.userService.deposit(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.valueOf(6000000))));
    }

    @Test
    public void testDeposit() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));

        userService.deposit(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.valueOf(500)));
        verify(this.repository).saveAndFlush(captor.capture());

        assertEquals(ADMIN_USERNAME, captor.getValue().getUsername());
        assertEquals(BigDecimal.valueOf(500), captor.getValue().getBalance());
    }

    @Test
    public void testWithdrawWithInvalidUser() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> this.userService.withdraw(NONEXISTENT_USERNAME, new MoneyTransactionModel()));
    }

    @Test
    public void testWithdrawWithInvalidModel() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.withdraw(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.ZERO)));
        assertThrows(IllegalStateException.class, () -> this.userService.withdraw(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.valueOf(6000000))));
    }

    @Test
    public void testWithdrawWithTooBigValue() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.withdraw(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.TEN)));
    }

    @Test
    public void testWithdraw() {
        this.admin.setBalance(BigDecimal.TEN);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));

        userService.withdraw(ADMIN_USERNAME, new MoneyTransactionModel(BigDecimal.valueOf(9)));
        verify(this.repository).saveAndFlush(captor.capture());

        assertEquals(ADMIN_USERNAME, captor.getValue().getUsername());
        assertEquals(BigDecimal.ONE, captor.getValue().getBalance());
    }

    @Test
    public void testGetCollectionWithInvalidUser() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> this.userService.getCollection(NONEXISTENT_USERNAME));
    }

    @Test
    public void testGetCollection() {
        Item item1 = new Item();
        item1.setForSale(true);
        item1.setId("1");
        item1.setName("ONE");
        item1.setOwner(this.admin);
        item1.setPhotos(Set.of(new Photo("PHOTO_ONE", item1)));

        Item item2 = new Item();
        item2.setName("TWO");
        item2.setId("2");
        item2.setOwner(this.admin);
        item2.setPhotos(Set.of(new Photo("PHOTO_TWO", item2)));

        this.admin.setCollection(Set.of(item1, item2));

        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));

        List<CollectionItem> out = this.userService.getCollection(ADMIN_USERNAME);
        assertEquals("TWO", out.get(0).getName());
        assertEquals("ONE", out.get(1).getName());
        assertEquals("PHOTO_TWO", out.get(0).getLocations().get(0));
        assertEquals("PHOTO_ONE", out.get(1).getLocations().get(0));
    }

    @Test
    public void getProfileViewWithInvalidUsername() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.userService.getProfileView(NONEXISTENT_USERNAME));
    }

    @Test
    public void getProfileView() {
        LocalDateTime time = LocalDateTime.now();
        String actualFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(time);
        this.admin.setModifiedOn(time);

        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        ProfileView out = this.userService.getProfileView(ADMIN_USERNAME);

        assertEquals(actualFormat, out.getModifiedOn());
        assertEquals(ADMIN_USERNAME, out.getUsername());
    }

    @Test
    public void testUpdateProfileInfoWithWrongUser() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> this.userService.updateProfileInfo(NONEXISTENT_USERNAME, new EditProfileModel()));
    }

    @Test
    public void testUpdateProfileInfoWithInvalidModel() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.updateProfileInfo(ADMIN_USERNAME, new EditProfileModel("1", "2")));
        assertThrows(IllegalStateException.class, () -> this.userService.updateProfileInfo(ADMIN_USERNAME, new EditProfileModel("ssssssssssssssssssssssssssssssssssssssss", "ssssssssssssssssssssssssssssssssssssssss")));
    }

    @Test
    public void testUpdateProfileInfoWithEmptyFile() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        when(this.repository.saveAndFlush(any())).thenAnswer((a) -> {
            User temp = a.getArgument(0);
            temp.setId("SAVED");
            return temp;
        });

        EditProfileModel edit = new EditProfileModel("EDITED", "EDITED TOO");
        edit.setImages(new MockMultipartFile("name", "", "image/png", new byte[3]));
        UserServiceModel out = this.userService.updateProfileInfo(ADMIN_USERNAME, edit);

        assertEquals("SAVED", out.getId());
        assertEquals("EDITED", out.getFirstName());
        assertEquals("EDITED TOO", out.getLastName());
        assertEquals("/uploads/profile.jpg", out.getProfilePicture());
    }

    @Test
    public void testUpdateProfileInfoWithNewPicture() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        when(this.fileHandler.saveFile(anyString(), any())).thenReturn("/dick-pic.jpg");
        when(this.repository.saveAndFlush(any())).thenAnswer((a) -> {
            User temp = a.getArgument(0);
            temp.setId("SAVED");
            return temp;
        });

        EditProfileModel edit = new EditProfileModel("EDITED", "EDITED TOO");
        edit.setImages(new MockMultipartFile("name", "dick-pic", "image/png", new byte[3]));
        UserServiceModel out = this.userService.updateProfileInfo(ADMIN_USERNAME, edit);

        assertEquals("SAVED", out.getId());
        assertEquals("EDITED", out.getFirstName());
        assertEquals("EDITED TOO", out.getLastName());
        assertEquals("/dick-pic.jpg", out.getProfilePicture());
        assertFalse(Files.exists(Path.of("uploads/profile.jpg")));
    }

    @Test
    public void testChangeRolesWithInvalidUser() {
        when(repository.getByUsername(NONEXISTENT_USERNAME)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> this.userService.changeRoles(new EditRolesModel(NONEXISTENT_USERNAME)));
    }

    @Test
    public void testChangeRolesWithInvalidModel() {
        EditRolesModel model=new EditRolesModel(ADMIN_USERNAME);
        model.getRoles().add("SEX");
        model.getRoles().add("TEACHER");
        model.getRoles().add("BATE");

        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        assertThrows(IllegalStateException.class, () -> this.userService.changeRoles(new EditRolesModel(ADMIN_USERNAME)));
        assertThrows(IllegalStateException.class, () -> this.userService.changeRoles(model));
    }

    @Test
    public void testChangeRoles() {
        when(repository.getByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(this.admin));
        when(this.roleService.getRolesByNames(any())).thenReturn(List.of(this.userRole));
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(this.repository.saveAndFlush(any())).thenAnswer((a) -> {
            User temp = a.getArgument(0);
            temp.setId("ROLES_CHANGED");
            return temp;
        });

        EditRolesModel model = new EditRolesModel(ADMIN_USERNAME);
        model.getRoles().add("USER");

        UserServiceModel out = this.userService.changeRoles(model);
        verify(this.notificationService).createNotification(userCaptor.capture(), textCaptor.capture());

        assertEquals("ROLES_CHANGED", out.getId());
        assertEquals("ADMIN", out.getUsername());
        assertEquals("ROLES_CHANGED", userCaptor.getValue().getId());
        assertEquals("Ролите бяха сменени от админа - роли: [USER]", textCaptor.getValue());
    }
}