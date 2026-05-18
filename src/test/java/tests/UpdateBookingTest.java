package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Bookingdates;
import core.models.CreatedBooking;
import core.models.SingleBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private SingleBooking initBooking;
    private SingleBooking updBooking;
    private int createdBookingId;
    private SingleBooking receivedBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        initBooking = step("Create new booking object to use in the test", () -> {
            SingleBooking booking = new SingleBooking();
            booking.setFirstname("Murka");
            booking.setLastname("Myaukova");
            booking.setTotalprice(750);
            booking.setDepositpaid(false);
            booking.setBookingdates(new Bookingdates("2029-07-01", "2029-07-15"));
            booking.setAdditionalneeds("Put fresh catnip to the room");
            return booking;
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.NORMAL)
    @Owner("ksenia miticheva")
    public void testUpdateBooking() {
        Response response = step("Send POST request to create new booking", () -> {
            String requestBody = objectMapper.writeValueAsString(initBooking);
            return apiClient.createBooking(requestBody);
        });

        step("Make sure that request was sent successfully", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        createdBookingId = step("Obtain id of created booking",
                () -> response.jsonPath().getInt("bookingid"));

        updBooking = step("Create updated booking to send", () -> {
            SingleBooking booking = new SingleBooking(initBooking);
            booking.setFirstname("Pushok");
            booking.setLastname("Murkov");
            booking.setTotalprice(850);
            return booking;
        });

        receivedBooking = step("Send PUT request to update previously created booking and obtain response", () -> {
            String requestBody = objectMapper.writeValueAsString(updBooking);
            Response receivedResponse = apiClient.updateBooking(requestBody, createdBookingId);
            assertThat(receivedResponse.getStatusCode()).isEqualTo(200);
            assertThat(receivedResponse).isNotNull();
            return objectMapper.readValue(receivedResponse.getBody().asString(), SingleBooking.class);
        });

        step("Review received booking to assure fields match", () -> {
            assertEquals(receivedBooking.getFirstname(), updBooking.getFirstname());
            assertEquals(receivedBooking.getLastname(), updBooking.getLastname());
            assertEquals(receivedBooking.getTotalprice(), updBooking.getTotalprice());
            assertEquals(receivedBooking.getAdditionalneeds(), updBooking.getAdditionalneeds());
            assertEquals(receivedBooking.getBookingdates().getCheckin(), updBooking.getBookingdates().getCheckin());
            assertEquals(receivedBooking.getBookingdates().getCheckout(), updBooking.getBookingdates().getCheckout());
        });
    }

    @AfterEach
    @Step("Remove created booking after test to clean the DB")
    public void tearDown() {
        apiClient.deleteBooking(createdBookingId);

        assertThat(apiClient.getBookingById(createdBookingId).getStatusCode()).isEqualTo(404);
    }

}
