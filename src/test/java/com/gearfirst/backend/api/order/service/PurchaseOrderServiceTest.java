package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.RepairResponse;
import com.gearfirst.backend.api.order.infra.client.RepairClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8082)
class PurchaseOrderServiceTest {

    private final RepairClient repairClient;

    PurchaseOrderServiceTest(RepairClient repairClient) {
        this.repairClient = repairClient;
    }

    @Test
    void 차량_검색_요청시_RepairService_가짜_응답() throws Exception {
        // given (가짜 응답 등록)
        stubFor(get(urlEqualTo("/api/v1/repairs/1/소나타"))
                .willReturn(okJson("""
                    [
                      {"repairId": 10, "vehicleNumber": "12가3456", "vehicleModel": "소나타", "customerName": "홍길동"},
                      {"repairId": 11, "vehicleNumber": "45나6789", "vehicleModel": "그랜저", "customerName": "이순신"}
                    ]
                """)));

        // when
        List<RepairResponse> result = repairClient.getRepairsByEngineer(1L, "소나타");

        // then
        assertThat(result).hasSize(2);
    }
}