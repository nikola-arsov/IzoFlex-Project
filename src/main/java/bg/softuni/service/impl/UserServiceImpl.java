package bg.softuni.service.impl;

import bg.softuni.model.binding.EditProfileModel;
import bg.softuni.model.binding.EditRolesModel;
import bg.softuni.model.binding.RegisterUserModel;
import bg.softuni.model.entity.Photo;
import bg.softuni.model.entity.Role;
import bg.softuni.model.entity.User;
import bg.softuni.model.service.UserServiceModel;
import bg.softuni.model.view.CollectionItem;
import bg.softuni.model.view.ProfileView;
import bg.softuni.util.core.ValidatorUtil;
import bg.softuni.util.handler.EnumValidator;
import bg.softuni.model.binding.MoneyTransactionModel;
import bg.softuni.model.enumeration.Gender;
import bg.softuni.model.service.NotificationServiceModel;
import bg.softuni.repository.UserRepository;
import bg.softuni.service.interf.NotificationService;
import bg.softuni.service.interf.RoleService;
import bg.softuni.service.interf.UserService;
import bg.softuni.util.core.MultipartFileHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final String AMOUNT_ERROR = "Сумата трябва да е между 0.01 и 5 000 000 лева!";
    private final String USER_ERROR = "Потребителя с това име не може да бъде намерен!";
    private final String FIELD_ERRORS = "Some fields have errors, check them and resubmit later.";
    private final String MALE_AVATAR = "/img/male-avatar.svg";
    private final String FEMALE_AVATAR = "/img/female-avatar.svg";

    private final NotificationService notificationService;
    private final MultipartFileHandler fileHandler;
    private final UserRepository repository;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    private final ValidatorUtil validator;
    private final PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(RoleService roleService, MultipartFileHandler fileHandler, NotificationService notificationService, UserRepository repository, ModelMapper modelMapper, ValidatorUtil validator, PasswordEncoder encoder) {
        this.roleService = roleService;
        this.fileHandler = fileHandler;
        this.notificationService = notificationService;
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<User> temp = this.repository.getByUsername(s);

        return temp.map(this::detailsMapper)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with username: %s , not found!", s)));
    }

    @Override
    public List<String> getDefaultUsers() {
        return this.repository.getAllByUsernameOrUsername("admin", "user").stream().map(User::getUsername).collect(Collectors.toList());
    }

    @Override
    public UserServiceModel registerUser(RegisterUserModel userModel) {
        this.validateEntity(userModel);

        User toBeRegistered = this.modelMapper.map(userModel, User.class);
        toBeRegistered.setRoles(Set.copyOf(this.roleService.getRolesByNames(List.of("ROLE_USER"))));
        toBeRegistered.setPassword(this.encoder.encode(userModel.getPassword()));
        toBeRegistered.setGender(Gender.valueOf(userModel.getGender()));
        toBeRegistered.setProfilePicture("MALE".equals(userModel.getGender()) ? MALE_AVATAR : FEMALE_AVATAR);

        User saved = this.repository.saveAndFlush(toBeRegistered);
        this.newNotification(saved, String.format("Здравейте %s %s, добре дошли в нашия сайт!", saved.getFirstName(), saved.getLastName()));

        return this.modelMapper.map(saved, UserServiceModel.class);
    }

    @Override
    public String getFullName(String username) {
        User current = this.repository.getByUsername(username).orElse(null);

        if (current == null) {
            return null;
        }
        return current.getFirstName() + " " + current.getLastName();
    }

    @Override
    public String getIdByUsername(String username) {
        return this.repository.getByUsername(username).orElseThrow(() -> new UsernameNotFoundException(USER_ERROR)).getId();
    }

    @Override
    public BigDecimal getBalance(String username) {
        return this.repository.getByUsername(username).orElseThrow(() -> new UsernameNotFoundException(USER_ERROR)).getBalance();
    }

    @Override
    @Transactional(rollbackFor = UsernameNotFoundException.class)
    public void deposit(String username, MoneyTransactionModel model) {
        User user = this.validateUserAndModel(username, model, AMOUNT_ERROR);
        user.setBalance(user.getBalance().add(model.getAmount()));

        this.repository.saveAndFlush(user);
    }

    @Override
    @Transactional(rollbackFor = {UsernameNotFoundException.class, IllegalStateException.class})
    public void withdraw(String username, MoneyTransactionModel model) {
        User user = this.validateUserAndModel(username, model, AMOUNT_ERROR);

        if (BigDecimal.ZERO.compareTo(user.getBalance().subtract(model.getAmount())) > 0) {
            throw new IllegalStateException(String.format("Потребителя няма достатъчно пари, за да прехвърли %s euro - баланс: %s €"
                    , model.getAmount(), user.getBalance()));
        }
        user.setBalance(user.getBalance().subtract(model.getAmount()));

        this.repository.saveAndFlush(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionItem> getCollection(String username) {
        User user = this.repository.getByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_ERROR));

        return user.getCollection().stream()
                .map(i -> {
                    CollectionItem item = this.modelMapper.map(i, CollectionItem.class);
                    item.setLocations(i.getPhotos().stream().map(Photo::getLocation).collect(Collectors.toList()));

                    return item;
                })
                .sorted().collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getCollectionCount(String username) {
        return this.repository.findByUsername(username).getCollection().size();
    }

    @Override
    public ProfileView getProfileView(String username) {
        User temp = this.getUserByUsername(username);
        ProfileView view = this.modelMapper.map(temp, ProfileView.class);

        view.setModifiedOn(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(temp.getModifiedOn()));

        return view;
    }

    @Override
    public UserServiceModel updateProfileInfo(String username, EditProfileModel model) {
        User user = this.validateUserAndModel(username, model, FIELD_ERRORS);
        String newLocation = this.validatePicture(username, model);

        user.setFirstName(model.getFirstName());
        user.setLastName(model.getLastName());
        this.removePicture(newLocation, user.getProfilePicture());
        user.setProfilePicture("unchanged".equals(newLocation) ? user.getProfilePicture() : newLocation);

        return this.modelMapper.map(this.repository.saveAndFlush(user), UserServiceModel.class);
    }

    @Override
    public List<String> getAllUsernames() {
        return this.repository.findAll().stream().map(User::getUsername).collect(Collectors.toList());
    }

    @Override
    public UserServiceModel changeRoles(EditRolesModel model) {
        User user = this.validateUserAndModel(model.getUsername(), model, FIELD_ERRORS);
        List<Role> roles = this.roleService.getRolesByNames(model.getRoles());
        user.setRoles(Set.copyOf(roles));

        User saved = this.repository.saveAndFlush(user);
        this.newNotification(saved
                , "Ролите бяха сменени от админа - роли: "
                        + saved.getRoles().toString());

        return this.modelMapper.map(saved, UserServiceModel.class);
    }

    @Override
    public User getUserByUsername(String username) {
        return this.repository.getByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User with that username doesn't exist!"));
    }

    @Override
    public NotificationServiceModel newNotification(User user, String text) {
        return this.notificationService.createNotification(user, text);
    }

    private void validateEntity(RegisterUserModel userModel) throws IllegalStateException {
        if (!this.validator.isValid(userModel)) {
            throw new IllegalStateException(String.format
                    ("User with email: %s has invalid values set. Check your input and resubmit!"
                            , userModel.getEmail()));
        }//Validate fields
        if (this.repository.getByUsername(userModel.getUsername()).isPresent()) {
            throw new IllegalStateException("User with that username already exists!");
        }//Check if username is taken
        if (this.repository.getByEmail(userModel.getEmail()).isPresent()) {
            throw new IllegalStateException("User with that email already exists!");
        }//Check if email is taken
        EnumValidator.validateEnum(userModel.getGender(), Gender.class, "");//Check if enum value is valid
    }

    private <T> User validateUserAndModel(String username, T model, String message) {
        User user = this.repository.getByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_ERROR));

        if (!this.validator.isValid(model)) {
            throw new IllegalStateException(message);
        }
        return user;
    }

    private UserDetails detailsMapper(User temp) {
        return new org.springframework.security.core.userdetails.User(
                temp.getUsername(),
                temp.getPassword(),
                temp.getRoles());
    }

    private String validatePicture(String username, EditProfileModel model) {
        MultipartFile file = model.getImages();

        if (("").equals(file.getOriginalFilename())) {
            return "unchanged";
        }
        return this.fileHandler.saveFile(username, file);
    }

    private boolean removePicture(String location, String oldLocation) {
        if (!"unchanged".equals(location)) {
            if (!FEMALE_AVATAR.equals(oldLocation) && !MALE_AVATAR.equals(oldLocation)) {
                File file = new File(oldLocation.substring(1));

                return file.delete();
            }
        }
        return false;
    }
}