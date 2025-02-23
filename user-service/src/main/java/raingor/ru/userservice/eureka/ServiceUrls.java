package raingor.ru.userservice.eureka;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceUrls {
    private final EurekaDiscoveryClient discoveryClient;

    public String getAuthServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
        if (instances.isEmpty()) {
            throw new RuntimeException("User service not found");
        }
        return instances.get(0).getUri().toString();
    }
}
