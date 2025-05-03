package onlinebookstore.repository.user;

import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.USER_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.createTestUser;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.repository.RepositoryTestDataUtil.INVALID_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import onlinebookstore.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/users/add-test-role-to-roles-table.sql",
        "classpath:database/users/add-test-users-to-users-table.sql",
        "classpath:database/users/add-test-dependencies-to-users-roles-table.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/users/remove-test-dependencies-from-users-roles-table.sql",
        "classpath:database/users/remove-test-users-from-users-table.sql",
        "classpath:database/users/remove-test-role-from-roles-table.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find a user by email with their associated role entities.")
    void findByEmail_GivenValidUser_ReturnsUserWithRelatedEntities() {
        // Given
        User expectedUser = createTestUser(EXISTING_USER_ID);

        // When
        Optional<User> actualUserOpt = userRepository.findByEmail(expectedUser.getEmail());

        // Then
        assertThat(actualUserOpt).isNotEmpty();
        assertObjectsAreEqualIgnoringFields(
                actualUserOpt.get(),
                expectedUser,
                USER_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("No finds user by email.")
    void findByEmail_NoExistingUserByEmail_ReturnsEmptyOptional() {
        // Given
        User expectedUser = createTestUser(NOT_EXISTING_USER_ID);

        // When
        Optional<User> actualUserOpt = userRepository.findByEmail(expectedUser.getEmail());

        // Then
        assertThat(actualUserOpt).isEmpty();
    }

    @Test
    @DisplayName("No finds user by invalid email.")
    void findByEmail_InvalidUserId_ReturnsEmptyOptional() {
        // When
        Optional<User> actualUserOpt = userRepository.findByEmail(INVALID_EMAIL);

        // Then
        assertThat(actualUserOpt).isEmpty();
    }
}
