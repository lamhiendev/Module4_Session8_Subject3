package demo.appointmentservice.service.impl;

import demo.appointmentservice.dto.AppointmentServiceRequest;

import demo.appointmentservice.entity.Appointment;
import demo.appointmentservice.repository.AppointmentRepository;
import demo.appointmentservice.service.AppointmentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;

    @Override
    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "fallbackAppointment")
    @Retry(name = "patientRetry")
    public Appointment createAppointment(AppointmentServiceRequest request) {
        boolean isPatientValid = checkEntityExists("http://patient-service/api/v1/patients/"+request.getPatientId());
        if(!isPatientValid){
            throw new RuntimeException("Bệnh nhân với ID" + request.getPatientId() + " không tồn tại");
        }

        boolean isDoctorValid = checkEntityExists("http://doctor-service/api/v1/doctors/" + request.getDoctorId());
        if (!isDoctorValid){
            throw new RuntimeException("Bác sĩ với ID" + request.getDoctorId() + " không tồn tại");
        }

        Appointment newAppointment = Appointment.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .reason(request.getReason())
                .status(request.getStatus())
                .build();
        return newAppointment;
    }
    public Appointment fallbackAppointment(AppointmentServiceRequest request,Exception e){
        log.error("Fallback kích hoạt cho Request của Patient ID: {} và Doctor ID: {}. Lý do: {}",
                request.getPatientId(), request.getDoctorId(), e.getMessage());
        throw new RuntimeException("Hiện tại không thể kết nối đến Docter-Service");
    }

    private boolean checkEntityExists(String url){
        try {
            restTemplate.getForObject(url, Object.class);
            return true;
        }catch (HttpClientErrorException.NotFound e) {
            return false;
        }catch(Exception e){
            return false;
        }
    }
}
