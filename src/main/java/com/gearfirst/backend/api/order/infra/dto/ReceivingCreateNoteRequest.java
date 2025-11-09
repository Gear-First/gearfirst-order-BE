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
public class ReceivingCreateNoteRequest {
    private String requesterName;   //발주 요청자
    private String warehouseCode;   //발주 요청 창고
    private String requestedAt;      //발주 요청일
    private String expectedShipDate; //발주 승인, 출고 지정일
    private String remark;              //비고
    private List<WarehouseLineRequest> lines;      //부품 리스트

    public static ReceivingCreateNoteRequest from(PurchaseOrder order, List<OrderItem> items) {
        List<WarehouseLineRequest> lines = items.stream()
                .map(item -> new WarehouseLineRequest(
                        item.getPartId(),  // productId
                        item.getQuantity(), // orderedQty
                        null                // lineRemark (필요하면 order.note로 대체 가능)
                ))
                .toList();

        return new ReceivingCreateNoteRequest(
                order.getRequesterName(),
                order.getDestinationCode(),       // warehouseCode
                order.getRequestDate().atOffset(ZoneOffset.ofHours(9)).toString(),   // requestedAt
                order.getProcessedDate().atOffset(ZoneOffset.ofHours(9)).toString(),  // expectedShipDate
                order.getNote(),            // remark
                lines
        );
    }
}


