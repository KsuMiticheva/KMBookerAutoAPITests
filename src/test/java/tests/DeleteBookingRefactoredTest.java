package tests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookingRefactoredTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");
    }

    @Test
    public void testDeleteBookingRefactored() throws Exception{
        List<Booking> initBookings = objectMapper.readValue(apiClient.getBooking().getBody().asString(), new TypeReference<List<Booking>>() {});

        Booking randomBooking = initBookings.get(new Random().nextInt(initBookings.size()));
        int bookingId = randomBooking.getBookingid();

        apiClient.deleteBooking(bookingId);

        List<Booking> updatedBookings = objectMapper.readValue(apiClient.getBooking().getBody().asString(), new TypeReference<List<Booking>>() {});

        for (Booking booking : updatedBookings) {
            assertThat(booking.getBookingid()).as("Test failed as deleted id is found: " + bookingId).isNotEqualTo(bookingId);
        }
    }

}