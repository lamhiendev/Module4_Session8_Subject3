package demo.appointmentservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientClient {

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "patientServiceCB", fallbackMethod = "getPatientFallback")
    public boolean checkDoctorExists(Long patientId) {
        String url = "http://doctor-service/api/v1/doctors/" + patientId;
        restTemplate.getForObject(url, Object.class);
        return true;
    }


    public boolean getPatientFallback(Long patientId, Throwable t) {
        log.error("Fallback kích hoạt cho Doctor ID: {}. Lý do: {}", patientId, t.getMessage());
        return false; // Trả về false khi service sập hoặc ngắt mạch
    }
}