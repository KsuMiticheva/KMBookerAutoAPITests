package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Bookingdates;
import core.models.SingleBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartialUpdateBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private SingleBooking newBooking;
    private SingleBooking expectedBooking;
    private SingleBooking updatedBooking;
    private int createdBookingId;
    private String patch;

    @BeforeEach
    public void setUp(){
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        newBooking = step("Create new booking object to use in the test", () -> {
            SingleBooking booking = new SingleBooking();
            booking.setFirstname("Sally");
            booking.setLastname("O'Neal");
            booking.setTotalprice(200);
            booking.setDepositpaid(true);
            booking.setBookingdates(new Bookingdates("2026-08-01", "2026-08-04"));
            booking.setAdditionalneeds("Ground floor only");
            return booking;
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.NORMAL)
    @Owner("ksenia miticheva")
    public void testPartialUpdateBooking(){
        Response response = step("Send POST request to create new booking", () -> {
            String requestBody = objectMapper.writeValueAsString(newBooking);
            return apiClient.createBooking(requestBody);
        });

        createdBookingId = step("Obtain id of created booking",
                () -> response.jsonPath().getInt("bookingid"));

        patch = step("Create patch body for PATCH request", () -> {
            Map<String, Object> patch = new HashMap<>();
            patch.put("firstname", "Nancy");
            Map<String, Object> dates = new HashMap<>();
            dates.put("checkin", "2026-08-07");
            dates.put("checkout", "2026-08-11");
            patch.put("bookingdates", dates);
            return objectMapper.writeValueAsString(patch);
        });

        expectedBooking = step("Prepare expected booking object for comparison", ()-> {
            SingleBooking booking = new SingleBooking(newBooking);
            booking.setFirstname("Nancy");
            booking.setBookingdates(new Bookingdates("2026-08-07", "2026-08-11"));
            return booking;
        });

        updatedBooking = step("Send PATCH request to update previously created booking and obtain response", () -> {
            Response receivedResponse = apiClient.partialUpdateBooking(patch, createdBookingId);
            assertThat(receivedResponse.getStatusCode()).isEqualTo(200);
            assertThat(receivedResponse).isNotNull();
            return objectMapper.readValue(receivedResponse.getBody().asString(), SingleBooking.class);
        });

        step("Compare updated booking with expected object to assure that update was made correctly", () -> {
            assertEquals(updatedBooking.getFirstname(), expectedBooking.getFirstname());
            assertEquals(updatedBooking.getLastname(), expectedBooking.getLastname());
            assertEquals(updatedBooking.getTotalprice(), expectedBooking.getTotalprice());
            assertEquals(updatedBooking.getAdditionalneeds(), expectedBooking.getAdditionalneeds());
            assertEquals(updatedBooking.getBookingdates().getCheckin(), expectedBooking.getBookingdates().getCheckin());
            assertEquals(updatedBooking.getBookingdates().getCheckout(), expectedBooking.getBookingdates().getCheckout());
        });

    }

    @AfterEach
    @Step("Remove created booking after test to clean the DB")
    public void tearDown() {
        apiClient.deleteBooking(createdBookingId);
        assertThat(apiClient.getBookingById(createdBookingId).getStatusCode()).isEqualTo(404);
    }
}
