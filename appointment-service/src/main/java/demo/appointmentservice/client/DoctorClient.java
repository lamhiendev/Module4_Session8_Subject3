package demo.appointmentservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DoctorClient {

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "getDoctorFallback")
    public boolean checkDoctorExists(Long doctorId) {
        String url = "http://doctor-service/api/v1/doctors/" + doctorId;
        restTemplate.getForObject(url, Object.class);
        return true;
    }


    public boolean getDoctorFallback(Long doctorId, Throwable t) {
        log.error("Fallback kích hoạt cho Doctor ID: {}. Lý do: {}", doctorId, t.getMessage());
        return false; // Trả về false khi service sập hoặc ngắt mạch
    }
}