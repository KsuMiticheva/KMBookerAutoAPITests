package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import core.clients.APIClient;
import core.models.Booking;
import core.models.Bookingdates;
import core.models.CreatedBooking;
import core.models.SingleBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CheckBookingListTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private SingleBooking myBooking;
    private CreatedBooking createdBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        myBooking = step("Create new booking object for use in the test", () -> {
            SingleBooking booking = new SingleBooking();
            booking.setFirstname("Lily");
            booking.setLastname("Valley");
            booking.setTotalprice(180);
            booking.setDepositpaid(true);
            booking.setBookingdates(new Bookingdates("2026-06-12", "2026-06-15"));
            booking.setAdditionalneeds("Non-smoking room");
            return booking;
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("ksenia miticheva")
    public void testCheckBookingList() throws Exception {
        Response response = step("Send POST request to create new booking", () -> {
            String requestBody = objectMapper.writeValueAsString(myBooking);
            return apiClient.createBooking(requestBody);
        });

        createdBooking = objectMapper.readValue(response.getBody().asString(), CreatedBooking.class);

        step("Confirm that request was sent and accepted successfully", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        List<Booking> bookings = step("Obtain list of bookings", () -> {
            String responseBody = apiClient.getBooking().getBody().asString();
            assertThat(apiClient.getBooking().getStatusCode()).isEqualTo(200);
            return objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {});
        });

        step("Check that list is not empty", () ->
                assertThat(bookings).isNotEmpty());

        step("Assure that all bookings have id", () -> {
            for (Booking booking : bookings) {
                assertThat(booking.getBookingid()).isGreaterThan(0);
            }
        });

        step("Assure that list contains new added booking", () ->
            assertTrue(isBookingPresent(bookings, createdBooking.getBookingid()), "New booking not found in the list"
        ));

    }

    @AfterEach
    @Step("Remove added booking after test to clean the DB")
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }

    public boolean isBookingPresent(List<Booking> bookings, int bookingid) {
        for (Booking booking : bookings) {
            if (booking.getBookingid() == bookingid) {
                return true;
            }
        }
        return false;
    }
}
