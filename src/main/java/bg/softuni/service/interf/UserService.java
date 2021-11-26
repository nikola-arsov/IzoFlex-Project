package bg.softuni.service.interf;

import bg.softuni.model.binding.EditProfileModel;
import bg.softuni.model.binding.EditRolesModel;
import bg.softuni.model.binding.MoneyTransactionModel;
import bg.softuni.model.binding.RegisterUserModel;
import bg.softuni.model.entity.User;
import bg.softuni.model.service.NotificationServiceModel;
import bg.softuni.model.service.UserServiceModel;
import bg.softuni.model.view.CollectionItem;
import bg.softuni.model.view.ProfileView;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;
import java.util.List;

public interface UserService extends UserDetailsService {
    List<String> getDefaultUsers();

    UserServiceModel registerUser(RegisterUserModel userModel) throws IllegalStateException;

    String getFullName(String username);

    String getIdByUsername(String username);

    BigDecimal getBalance(String username);

    void deposit(String username, MoneyTransactionModel model);

    void withdraw(String username, MoneyTransactionModel model);

    List<CollectionItem> getCollection(String username);

    long getCollectionCount(String username);

    ProfileView getProfileView(String username);

    UserServiceModel updateProfileInfo(String username, EditProfileModel model);

    List<String> getAllUsernames();

    UserServiceModel changeRoles(EditRolesModel model);

    User getUserByUsername(String username);

    NotificationServiceModel newNotification(User user, String text);
}