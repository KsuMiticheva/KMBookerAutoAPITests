package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import core.models.Bookingdates;
import core.models.CreatedBooking;
import core.models.SingleBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetBookingByIdTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private SingleBooking newBooking;
    private SingleBooking returnedBooking;
    private int createdBookingId;

    @BeforeEach
    public void setUp() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        newBooking = step("Create new booking to use in the test", () -> {
            SingleBooking newBooking = new SingleBooking();
            newBooking.setFirstname("Annie");
            newBooking.setLastname("Smith");
            newBooking.setTotalprice(800);
            newBooking.setDepositpaid(false);
            newBooking.setBookingdates(new Bookingdates("2027-01-01", "2027-01-05"));
            newBooking.setAdditionalneeds("Sea view room");
            return newBooking;
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("ksenia miticheva")
    public void testGetBookingById() throws Exception {
        Response response = step("Send POST request to create new booking", () -> {
            String requestBody = objectMapper.writeValueAsString(newBooking);
            return apiClient.createBooking(requestBody);
        });

        step("Make sure that request was sent successfully", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        createdBookingId = step("Obtain id of created booking",
                () -> response.jsonPath().getInt("bookingid"));

        returnedBooking = step("Send GET request and obtain new booking from server by id", () -> {
            Response getResponse = apiClient.getBookingById(createdBookingId);
            assertThat(getResponse.getStatusCode()).isEqualTo(200);
            return objectMapper.readValue(getResponse.getBody().asString(), SingleBooking.class);
        });

        step("Assure that all fields of booking received by id match data sent as new booking", () -> {
            assertEquals(newBooking.getFirstname(), returnedBooking.getFirstname());
            assertEquals(newBooking.getLastname(), returnedBooking.getLastname());
            assertEquals(newBooking.getTotalprice(), returnedBooking.getTotalprice());
            assertEquals(newBooking.isDepositpaid(), returnedBooking.isDepositpaid());
            assertEquals(newBooking.getBookingdates().getCheckin(), returnedBooking.getBookingdates().getCheckin());
            assertEquals(newBooking.getBookingdates().getCheckout(), returnedBooking.getBookingdates().getCheckout());
            assertEquals(newBooking.getAdditionalneeds(), returnedBooking.getAdditionalneeds());
        });
    }

    @AfterEach
    @Step("Remove created booking after test to clean the DB")
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBookingId);

        assertThat(apiClient.getBookingById(createdBookingId).getStatusCode()).isEqualTo(404);
    }
}

