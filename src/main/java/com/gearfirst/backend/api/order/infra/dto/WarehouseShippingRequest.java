package com.gearfirst.backend.api.order.infra.dto;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseShippingRequest {
    private String branchName;
    private String warehouseCode;
    private String requestedAt;      //발주 요청일
    private String expectedShipDate; //발주 승인, 출고 지정일
    private String remark;              //비고
    private List<WarehouseShippingLineRequest> lines;      //부품 리스트
    private String shippingNo;              //TODO: 출고번호 뺄예정

    public static WarehouseShippingRequest from(PurchaseOrder order,List<OrderItem> items) {
        List<WarehouseShippingLineRequest> lines = items.stream()
                .map(item -> new WarehouseShippingLineRequest(
                        item.getPartId(),  // productId
                        item.getQuantity(), // orderedQty
                        null                // lineRemark (필요하면 order.note로 대체 가능)
                ))
                .toList();

        return new WarehouseShippingRequest(
                order.getRequesterCode(),          // branchName
                order.getDestinationCode(),       // warehouseCode
                order.getRequestDate().atOffset(ZoneOffset.ofHours(9)).toString(),   // requestedAt
                order.getProcessedDate().atOffset(ZoneOffset.ofHours(9)).toString(),  // expectedShipDate
                order.getNote(),            // remark
                lines,
                null
        );
    }
}
