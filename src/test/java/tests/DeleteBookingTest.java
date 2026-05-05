package tests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");
    }

    @Test
    public void testDeleteBooking() throws Exception{
        Response response = apiClient.getBooking();

        String responseBody = response.getBody().asString();
        List<Booking> initBookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {});

        Booking randomBooking = initBookings.get(new Random().nextInt(initBookings.size()));
        int bookingId = randomBooking.getBookingid();

        Response deleteResponse = apiClient.deleteBooking(bookingId);

        Response updatedListResponse = apiClient.getBooking();

        String updatedResponseBody = updatedListResponse.getBody().asString();
        List<Booking> updatedBookings = objectMapper.readValue(updatedResponseBody, new TypeReference<List<Booking>>() {});

        for (Booking booking : updatedBookings) {
            assertThat(booking.getBookingid()).as("Test failed as deleted id is found: " + bookingId).isNotEqualTo(bookingId);
        }
    }

}
