package onlinebookstore.controller.user;

import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NEW_USER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.TEST_LONG_DATA;
import static onlinebookstore.util.TestDataUtil.USER_TEST_DATA_MAP;
import static onlinebookstore.util.TestDataUtil.createTestUserLoginRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestUserRegistrationRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestUserResponseDto;
import static onlinebookstore.util.TestDataUtil.fillRoleCache;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationError;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationErrorList;
import static onlinebookstore.util.controller.ControllerTestDataUtil.CONFLICT;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_USER_LOGIN_BLANK_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_USER_LOGIN_FORMAT_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_USER_REGISTRATION_FORMAT_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_USER_REGISTRATION_SIZE_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.UNAUTHORIZED;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_LOGIN;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_REGISTRATION;
import static onlinebookstore.util.controller.ControllerTestUtil.parseResponseToObject;
import static onlinebookstore.util.controller.DatabaseTestUtil.executeSqlScript;
import static onlinebookstore.util.controller.MockMvcUtil.buildMockMvc;
import static onlinebookstore.util.controller.MvcTestHelper.createJsonMvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.user.UserLoginRequestDto;
import onlinebookstore.dto.user.UserLoginResponseDto;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.repository.role.RoleRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext,
                          @Autowired RoleRepository roleRepository) {
        mockMvc = buildMockMvc(applicationContext);

        teardown(dataSource);
        executeSqlScript(dataSource,
                "database/users/add-test-users-to-users-table.sql",
                "database/users/add-test-role-to-roles-table.sql",
                "database/users/add-test-dependencies-to-users-roles-table.sql");

        fillRoleCache(roleRepository);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        executeSqlScript(dataSource,
                "database/users/remove-test-dependencies-from-users-roles-table.sql",
                "database/users/remove-test-role-from-roles-table.sql",
                "database/users/remove-test-users-from-users-table.sql");
    }

    @Test
    @DisplayName("Authorization of the current user.")
    void login_ValidUserLoginResponseDto_Success() throws Exception {
        //Given
        UserLoginRequestDto userLoginRequestDto = createTestUserLoginRequestDto(EXISTING_USER_ID);
        String jsonRequest = objectMapper.writeValueAsString(userLoginRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_LOGIN),
                status().isOk(),
                jsonRequest
        );

        //Then
        UserLoginResponseDto actual = parseResponseToObject(
                result,
                objectMapper,
                UserLoginResponseDto.class
        );
        assertThat(actual.token()).isNotBlank();
        String[] tokenParts = actual.token().split("\\.");
        assertThat(tokenParts).hasSize(3);
    }

    @Test
    @DisplayName("Verify that an exception is trow when an email doesn't exists.")
    void login_InvalidEmail_ShouldReturnUnauthorized() throws Exception {
        //Given
        UserLoginRequestDto userLoginRequestDto =
                createTestUserLoginRequestDto(NOT_EXISTING_USER_ID);
        String jsonRequest = objectMapper.writeValueAsString(userLoginRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_LOGIN),
                status().isUnauthorized(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                UNAUTHORIZED,
                "Email or password invalid"
        );
    }

    @Test
    @DisplayName("Verify that an exception is trow when the request fields are blank.")
    void login_BlankRequestFields_ShouldReturnBadRequest() throws Exception {
        //Given
        UserLoginRequestDto userLoginRequestDto = new UserLoginRequestDto("", "");
        String jsonRequest = objectMapper.writeValueAsString(userLoginRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_LOGIN),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_USER_LOGIN_BLANK_ERRORS
        );
    }

    @Test
    @DisplayName("Verify that an exception is trow when the request fields format are not valid.")
    void login_InvalidFormatRequestFields_ShouldReturnBadRequest() throws Exception {
        //Given
        UserLoginRequestDto userLoginRequestDto = new UserLoginRequestDto(
                TEST_LONG_DATA + USER_TEST_DATA_MAP.get("email"),
                TEST_LONG_DATA
        );
        String jsonRequest = objectMapper.writeValueAsString(userLoginRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_LOGIN),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_USER_LOGIN_FORMAT_ERRORS
        );
    }

    @Test
    @Sql(scripts = "classpath:database/users/remove-new-test-user-from-user-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Registration a new user.")
    void registerUser_ValidUserRegistrationRequestDto_Success() throws Exception {
        //Given
        UserResponseDto expected = createTestUserResponseDto(NEW_USER_ID);
        UserRegistrationRequestDto userRegistrationRequestDto =
                createTestUserRegistrationRequestDto(expected);
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_REGISTRATION),
                status().isOk(),
                jsonRequest
        );

        //Then
        UserResponseDto actual = parseResponseToObject(
                result,
                objectMapper,
                UserResponseDto.class
        );
        assertThat(actual.email()).isEqualTo(expected.email());
    }

    @Test
    @DisplayName("Verify that an exception is trow when an email already exists.")
    void registerUser_EmailAlreadyExists_ShouldReturnBadRequest() throws Exception {
        //Given
        UserRegistrationRequestDto userRegistrationRequestDto =
                createTestUserRegistrationRequestDto(EXISTING_USER_ID);
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_REGISTRATION),
                status().isConflict(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                CONFLICT,
                "Can't register user with this email: " + userRegistrationRequestDto.email()
        );
    }

    @Test
    @DisplayName("""
            Verify that an exception is trow when the size of the request fields is incorrect.
            """)
    void registerUser_IncorrectSizeOfRequestFields_ShouldReturnBadRequest() throws Exception {
        //Given
        UserRegistrationRequestDto userRegistrationRequestDto =
                new UserRegistrationRequestDto(
                        TEST_LONG_DATA + USER_TEST_DATA_MAP.get("email"),
                        "",
                        "",
                        "",
                        "",
                        TEST_LONG_DATA
                );
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_REGISTRATION),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_USER_REGISTRATION_SIZE_ERRORS
        );
    }

    @Test
    @DisplayName("""
            Verify that an exception is trow when the format of the request fields is invalid.
            """)
    void registerUser_InvalidFormatOfRequestFields_ShouldReturnBadRequest() throws Exception {
        //Given
        String invalidFormatOfValue = "INVALID FORMAT";
        UserRegistrationRequestDto userRegistrationRequestDto =
                new UserRegistrationRequestDto(
                        invalidFormatOfValue,
                        invalidFormatOfValue,
                        invalidFormatOfValue,
                        invalidFormatOfValue,
                        invalidFormatOfValue,
                        invalidFormatOfValue
                );
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_REGISTRATION),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_USER_REGISTRATION_FORMAT_ERRORS
        );
    }

    @Test
    @DisplayName("Verify that an exception is trow when the passwords do not match.")
    void registerUser_PasswordsDoNotMatch_ShouldReturnBadRequest() throws Exception {
        //Given
        UserRegistrationRequestDto userRegistrationRequestDto =
                new UserRegistrationRequestDto(
                        NEW_USER_ID + USER_TEST_DATA_MAP.get("email"),
                        USER_TEST_DATA_MAP.get("password"),
                        "",
                        USER_TEST_DATA_MAP.get("firstName"),
                        USER_TEST_DATA_MAP.get("lastName"),
                        USER_TEST_DATA_MAP.get("shippingAddress")
                );
        String jsonRequest = objectMapper.writeValueAsString(userRegistrationRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_REGISTRATION),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                List.of("password The passwords do not match.")
        );
    }
}
