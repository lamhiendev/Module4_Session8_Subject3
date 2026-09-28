package demo.appointmentservice.service.impl;

import demo.appointmentservice.client.DoctorClient;
import demo.appointmentservice.client.PatientClient;
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
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;
    @Override
    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "fallbackAppointment")
    @Retry(name = "patientRetry")
    public Appointment createAppointment(AppointmentServiceRequest request) {
        boolean isDoctorValid = doctorClient.checkDoctorExists(request.getDoctorId());
        if (!isDoctorValid) {
            return null;
        }

        boolean isPatientValid = patientClient.checkPatientExists(request.getPatientId());
        if (!isPatientValid){
            return null;
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


}
