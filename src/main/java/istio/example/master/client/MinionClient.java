package istio.example.master.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@CloudNativeExchange(service = "minion")
@HttpExchange
public interface MinionClient {

    @GetExchange(value = "/api/application")
    String application();
}
