package istio.example.master.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface MinionClient {

    @GetExchange(value = "/api/application")
    String application();
}
