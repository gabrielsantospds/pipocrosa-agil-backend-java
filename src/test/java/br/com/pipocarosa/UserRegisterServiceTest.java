package br.com.pipocarosa;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.ExistingEmailException;
import br.com.pipocarosa.models.UserModel;
import static org.hamcrest.Matchers.hasSize;
import br.com.pipocarosa.repositories.UserRepository;
import br.com.pipocarosa.services.UserRegisterService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRegisterServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16.2-alpine"
    );

    @BeforeAll
    static void startContainers() {
        postgres.start();
    }

    @AfterAll
    static void stopContainers() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRegisterService userRegisterService;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        userRepository.deleteAll();
    }

    @Test
    void shouldVerifyIfUserIsNotUnderage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate currentDate = LocalDate.now();
        LocalDate eighteenYearsAgoAndOneDayLaterDate = currentDate.minusYears(18).plusDays(1);
        LocalDate eighteenYearsAgoDate = currentDate.minusYears(18);

        String eighteenYearsAgoAndOneDayLater = eighteenYearsAgoAndOneDayLaterDate.format(formatter);
        String eighteenYearsAgo = eighteenYearsAgoDate.format(formatter);

        assertTrue(userRegisterService.checkAge(eighteenYearsAgo));
        assertFalse(userRegisterService.checkAge(eighteenYearsAgoAndOneDayLater));
    }

    @Test
    void shouldVerifyIfEmailAlreadyExists(){
        userRepository.save(new UserModel(
                1L,
                "Junior Souza",
                "junior@gmail.com",
                "17/08/2001",
                "123456")
        );
        boolean existingEmail = userRepository.existsByEmail("junior@gmail.com");
        boolean notExistingEmail = userRepository.existsByEmail("junior1@gmail.com");
        assertTrue(existingEmail);
        assertFalse(notExistingEmail);
    }

    @Test
    void shouldGetAllUsers() {
        List<UserModel> users = List.of(
                new UserModel(
                        1L,
                        "Junior Souza",
                        "junior@gmail.com",
                        "17/08/2001",
                        "123456"
                ),
                new UserModel(
                        2L,
                        "Bruno Silva",
                        "bruno@gmail.com",
                        "17/08/2001",
                        "123456"
                )
        );
        userRepository.saveAll(users);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body(".", hasSize(2));
    }

//    @Test
//    void shouldVerifyIfExceptionIsTypeExistingEmail() {
//        assertThrows(ExistingEmailException.class, () -> {
//            userRepository.save(new UserModel(
//                    2L,
//                    "Junior Silva",
//                    "junior@gmail.com",
//                    "17/08/2001",
//                    "123456")
//            );
//        });
//    }
}
