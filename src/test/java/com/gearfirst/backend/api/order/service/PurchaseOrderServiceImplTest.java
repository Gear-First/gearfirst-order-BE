package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.OrderItemRequest;
import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.fixture.PurchaseOrderFixture;
import com.gearfirst.backend.api.order.infra.client.InventoryClient;
import com.gearfirst.backend.api.order.infra.client.RepairClient;
import com.gearfirst.backend.api.order.infra.client.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.client.dto.OutboundRequest;
import com.gearfirst.backend.api.order.infra.client.dto.ReceiptCarResponse;
import com.gearfirst.backend.api.order.repository.OrderItemRepository;
import com.gearfirst.backend.api.order.repository.PurchaseOrderRepository;
import com.gearfirst.backend.common.enums.OrderStatus;
import jdk.dynalink.Operation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static com.gearfirst.backend.api.order.fixture.ReceiptCarFixture.createFakeRepairs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;


class PurchaseOrderServiceImplTest {

    private final PurchaseOrderRepository purchaseOrderRepository = mock(PurchaseOrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final InventoryClient inventoryClient = mock(InventoryClient.class);
    private final RepairClient repairClient = mock(RepairClient.class);

    private final PurchaseOrderService service = new PurchaseOrderServiceImpl(
           purchaseOrderRepository, orderItemRepository, inventoryClient, repairClient
    );

    @Test
    @DisplayName("발주 요청 시 엔지니어가 접수,수리중인 상태의 차량 전체 리스트만 조회한다.")
    public void getReceiptsByEngineer_success() throws Exception {
        //given
        List<ReceiptCarResponse> fakeRepairs = createFakeRepairs();

        given(repairClient.getAllRepairsByEngineer(10L))
                .willReturn(fakeRepairs);
        //when
        List<ReceiptCarResponse> result = service.findReceiptsByEngineer(10L);
        //then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getReceiptNumber()).isEqualTo("RO-123");
        verify(repairClient, times(1)).getAllRepairsByEngineer(10L);
    }

    @Test
    @DisplayName("엔지니어가 차량 검색 시 접수, 수리중인 상태의 차량 리스트만 조회한다.")
    public void searchReceiptsByEngineer_success() throws Exception {
        //given
        List<ReceiptCarResponse> fakeRepairs = createFakeRepairs();
        given(repairClient.searchRepairsByEngineer(10L,"98가")).willReturn(List.of(fakeRepairs.get(0),fakeRepairs.get(1)));
        given(repairClient.searchRepairsByEngineer(10L,"98")).willReturn(List.of(fakeRepairs.get(0),fakeRepairs.get(1),fakeRepairs.get(2)));
        given(repairClient.searchRepairsByEngineer(10L,"1234")).willReturn(List.of(fakeRepairs.get(0),fakeRepairs.get(2),fakeRepairs.get(3)));
        given(repairClient.searchRepairsByEngineer(10L,"가1234")).willReturn(List.of(fakeRepairs.get(0),fakeRepairs.get(3)));
        //when
        List<ReceiptCarResponse> result1 = service.searchReceiptsByEngineer(10L,"98가");
        List<ReceiptCarResponse> result2 = service.searchReceiptsByEngineer(10L,"98");
        List<ReceiptCarResponse> result3 = service.searchReceiptsByEngineer(10L,"1234");
        List<ReceiptCarResponse> result4 = service.searchReceiptsByEngineer(10L,"가1234");
        //then
        //갯수 검증
        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(3);
        assertThat(result3).hasSize(3);
        assertThat(result4).hasSize(2);

        //차량번호 검증
        assertThat(result1).extracting("vehicleNumber").containsExactlyInAnyOrder("98가1234","98가5421");
        assertThat(result2).extracting("vehicleNumber").containsExactlyInAnyOrder("98가1234","98가5421","98타1234");
        assertThat(result3).extracting("vehicleNumber").containsExactlyInAnyOrder("98가1234","98타1234","86가1234");
        assertThat(result4).extracting("vehicleNumber").containsExactlyInAnyOrder("98가1234","86가1234");

        //상태가 접수, 수리중 이여야함
        assertThat(result1).extracting("status").allMatch(s->s.equals("RECEIPT")|| s.equals("REPAIRING"));
        assertThat(result2).extracting("status").allMatch(s->s.equals("RECEIPT")|| s.equals("REPAIRING"));
        assertThat(result3).extracting("status").allMatch(s->s.equals("RECEIPT")|| s.equals("REPAIRING"));
        assertThat(result4).extracting("status").allMatch(s->s.equals("RECEIPT")|| s.equals("REPAIRING"));

        verify(repairClient, times(4)).searchRepairsByEngineer(eq(10L),anyString());
    }

    @Test
    @DisplayName("차종에 맞는 부품리스트가 검색된다.")
    public void findInventoriesByCarModel_success () throws Exception {
        //given
        List<InventoryResponse> fakeInventories = List.of(
                new InventoryResponse(1L,"A엔진 오일1","AB-EGO-1",10000),
                new InventoryResponse(1L,"A엔진 오일2","AB-EGO-2",10400),
                new InventoryResponse(1L,"A엔진 오일3","AB-EGO-3",24000),
                new InventoryResponse(1L,"A엔진 오일4","AB-EGO-4",13000)
        );
        given(inventoryClient.getInventoriesByCarModel(130L,"엔진오일")).willReturn(fakeInventories);
        //when
        List<InventoryResponse> result = service.findInventoriesByCarModel(130L,"엔진오일");
        //then
        assertThat(result).hasSize(4);
        assertThat(result).extracting("inventoryName").containsExactlyInAnyOrder("A엔진 오일1","A엔진 오일2","A엔진 오일3","A엔진 오일4");
        verify(inventoryClient, times(1)).getInventoriesByCarModel(130L,"엔진오일");
    }

    @Test
    @DisplayName("엔지니어가 발주 요청을 생성한다.") //역할 3가지
    public void createPurchaseOrderByEngineer() throws Exception {
        //given
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setInventoryId(100L);
        item1.setInventoryName("브레이크 패드2");
        item1.setInventoryCode("BP-02");
        item1.setPrice(23000);
        item1.setQuantity(2);
        OrderItemRequest item2 = new OrderItemRequest();
        item2.setInventoryId(20L);
        item2.setInventoryName("엔진오일1");
        item2.setInventoryCode("EO-05");
        item2.setPrice(12000);
        item2.setQuantity(1);
        List<OrderItemRequest> items = List.of(item1,item2);

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setVehicleNumber("12가3456");
        request.setEngineerId(10L);
        request.setBranchId(1L);
        request.setItems(items);


        given(inventoryClient.getInventoryById(100L)).willReturn(new InventoryResponse(100L,"브레이크패드2","BP-02",23000));
        given(inventoryClient.getInventoryById(20L)).willReturn(new InventoryResponse(20L,"엔진오일1","EO-05",12000));
        //when
        service.createPurchaseOrder(request);
        //then
        verify(inventoryClient, times(1)).getInventoryById(100L);
        verify(inventoryClient, times(1)).getInventoryById(20L);

        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));

        ArgumentCaptor<List<OrderItem>> orderItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(orderItemsCaptor.capture());

        List<OrderItem> saveItems = orderItemsCaptor.getValue();
        assertThat(saveItems).hasSize(2);
        assertThat(saveItems.get(0).getInventoryName()).isEqualTo("브레이크패드2");
        assertThat(saveItems.get(1).getInventoryCode()).isEqualTo("EO-05");
        assertThat(saveItems.get(1).getPrice()).isEqualTo(12000);

        int totalPrice = saveItems.stream()
                .mapToInt(i-> i.getPrice() * i.getQuantity())
                .sum();
        assertThat(totalPrice).isEqualTo(2*23000+12000);
    }

    @Test
    @DisplayName("엔지니어가 발주 전체 목록을 조회한다.")
    public void getBranchPurchaseOrders_success() throws Exception {
        //given
        Long branchId = 1L;
        Long engineerId = 10L;

        PurchaseOrder order1 = PurchaseOrderFixture.createApprovedOrder(1L,branchId,engineerId);
        PurchaseOrder order2 = PurchaseOrderFixture.createCompletedOrder(1L,branchId,engineerId);

        List<PurchaseOrder> fakeOrders = List.of(order1, order2);
        given(purchaseOrderRepository.findByBranchIdAndEngineerIdOrderByRequestDateDesc(eq(branchId),eq(engineerId)))
                .willReturn(fakeOrders);
        given(orderItemRepository.findByPurchaseOrder_Id(anyLong()))
                .willReturn(List.of(mock(OrderItem.class)));
        //when
        List<PurchaseOrderResponse> result = service.getBranchPurchaseOrders(branchId,engineerId);
        //then
        assertThat(result).hasSize(2);
        verify(purchaseOrderRepository, times(1))
                .findByBranchIdAndEngineerIdOrderByRequestDateDesc(eq(branchId), eq(engineerId));
        verify(orderItemRepository, times(2)).findByPurchaseOrder_Id(any());
    }

    @Test
    @DisplayName("엔지니어가 ready 상태의 발주 그룹 조회 시 올바른 상태 목록으로 발주 리스트를 조회한다.")
    public void getBranchPurchaseOrdersByFilter_ready() throws Exception {
        //
        Long branchId = 1L;
        Long engineerId = 10L;
        String filterType = "ready";
        PurchaseOrder order1 = PurchaseOrderFixture.createApprovedOrder(1L,branchId,engineerId);
        PurchaseOrder order2 = PurchaseOrderFixture.createShippedOrder(1L,branchId,engineerId);

        List<PurchaseOrder> fakeOrders = List.of(order1, order2);
        given(purchaseOrderRepository.findByBranchIdAndEngineerIdAndStatusInOrderByRequestDateDesc(eq(branchId),eq(engineerId),anyList()))
                .willReturn(fakeOrders);
        given(orderItemRepository.findByPurchaseOrder_Id(anyLong()))
                .willReturn(List.of(mock(OrderItem.class)));
        //when
        List<PurchaseOrderResponse> result = service.getBranchPurchaseOrdersByFilter(branchId,engineerId,filterType);
        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isIn("PENDING","APPROVED", "SHIPPED");
        verify(orderItemRepository, times(2)).findByPurchaseOrder_Id(any());
    }

    @Test
    @DisplayName("엔지니어가 completed 상태의 발주 그룹 조회 시 올바른 상태 목록으로 발주 리스트를 조회한다.")
    public void getBranchPurchaseOrdersByFilter_completed() throws Exception {
        //
        Long branchId = 1L;
        Long engineerId = 10L;
        String filterType = "completed";
        PurchaseOrder order1 = PurchaseOrderFixture.createCompletedOrder(1L,branchId,engineerId);
        PurchaseOrder order2 = PurchaseOrderFixture.createCompletedOrder(1L,branchId,engineerId);

        List<PurchaseOrder> fakeOrders = List.of(order1, order2);
        given(purchaseOrderRepository.findByBranchIdAndEngineerIdAndStatusInOrderByRequestDateDesc(eq(branchId),eq(engineerId),anyList()))
                .willReturn(fakeOrders);
        given(orderItemRepository.findByPurchaseOrder_Id(anyLong()))
                .willReturn(List.of(mock(OrderItem.class)));
        //when
        List<PurchaseOrderResponse> result = service.getBranchPurchaseOrdersByFilter(branchId,engineerId,filterType);
        //then
        assertThat(result).hasSize(2);
        assertThat(OrderStatus.valueOf(result.get(0).getStatus())).isEqualTo(OrderStatus.COMPLETED);
        verify(orderItemRepository, times(2)).findByPurchaseOrder_Id(any());
    }

    @Test
    @DisplayName("엔지니어가 cancelled 상태의 발주 그룹 조회 시 올바른 상태 목록으로 발주 리스트를 조회한다.")
    public void getBranchPurchaseOrdersByFilter_cancelled() throws Exception {
        //
        Long branchId = 1L;
        Long engineerId = 10L;
        String filterType = "cancelled";
        PurchaseOrder order1 = PurchaseOrderFixture.createRejectedOrder(1L,branchId,engineerId);
        PurchaseOrder order2 = PurchaseOrderFixture.createCancelledOrder(1L,branchId,engineerId);


        List<PurchaseOrder> fakeOrders = List.of(order1, order2);
        given(purchaseOrderRepository.findByBranchIdAndEngineerIdAndStatusInOrderByRequestDateDesc(eq(branchId),eq(engineerId),anyList()))
                .willReturn(fakeOrders);
        given(orderItemRepository.findByPurchaseOrder_Id(anyLong()))
                .willReturn(List.of(mock(OrderItem.class)));
        //when
        List<PurchaseOrderResponse> result = service.getBranchPurchaseOrdersByFilter(branchId,engineerId,filterType);
        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isIn("REJECTED","CANCELLED");
        verify(orderItemRepository, times(2)).findByPurchaseOrder_Id(any());
    }

    @Test
    @DisplayName("엔지니어가 발주 상세 조회 성공 시 주문과 주문과 품목 목록이 함께 반환된다.")
    public void getPurchaseOrderDetail_success() throws Exception {
        //given
        PurchaseOrder order = PurchaseOrderFixture.createPendingOrder(1L,1L,10L);
        given(purchaseOrderRepository.findByIdAndBranchIdAndEngineerId(1L,1L,10L))
                .willReturn(Optional.of(order));

        OrderItem item1 = new OrderItem(order, 1L, "브레이크 패드", "B001", 10000, 2);
        OrderItem item2 = new OrderItem(order, 2L, "오일필터", "O001", 5000, 1);
        given(orderItemRepository.findByPurchaseOrder_Id(1L))
                .willReturn(List.of(item1,item2));
        //when
        PurchaseOrderResponse result = service.getPurchaseOrderDetail(1L,1L,10L);
        //then
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("본사에서 발주를 승인한다.")
    public void approveOrder_success() throws Exception {
        //given
        Long orderId = 20L;
        Long warehouseId = 100L;
        PurchaseOrder order = PurchaseOrderFixture.createPendingOrder(1L,1L,10L);
        given(purchaseOrderRepository.findById(orderId)).willReturn(Optional.of(order));

        OrderItem item1 = new OrderItem(order, 1L, "브레이크 패드", "B001", 10000, 2);
        OrderItem item2 = new OrderItem(order, 2L, "오일필터", "O001", 5000, 1);
        given(orderItemRepository.findByPurchaseOrder_Id(orderId))
                .willReturn(List.of(item1,item2));

        //when
        service.approveOrder(orderId,warehouseId);
        //then
        verify(purchaseOrderRepository,times(1)).findById(orderId);
        verify(orderItemRepository, times(1)).findByPurchaseOrder_Id(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);

        ArgumentCaptor<OutboundRequest> outboundCaptor = ArgumentCaptor.forClass(OutboundRequest.class);
        verify(inventoryClient, times(1)).createOutboundOrder(outboundCaptor.capture());

        OutboundRequest outboundRequest = outboundCaptor.getValue();

        assertThat(outboundRequest.getWarehouseId()).isEqualTo(warehouseId);
        assertThat(outboundRequest.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("본사에서 발주를 반한다.")
    public void rejectOrder_success() throws Exception {
        // given
        Long orderId = 1L;
        PurchaseOrder order = PurchaseOrderFixture.createPendingOrder(orderId, 1L, 10L);
        given(purchaseOrderRepository.findById(orderId))
                .willReturn(Optional.of(order));

        // when
        service.rejectOrder(orderId);

        // then
        // 상태가 REJECTED로 바뀌었는지 확인
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);

        // 발주 조회 메서드가 호출되었는지 검증
        verify(purchaseOrderRepository, times(1)).findById(orderId);
    }

    /**
     * TODO: 실패 케이스 테스트
     *  - 존재하지 않는 주문 id
     *  - 완료 발주 내역이 없을 경우
     *  - 접수 내역 없음
     *  - 접수하지 않은 차량 번호 입력 시
     *  - 부품이름 없음
     *  - 존재하지 않는 발주 id로 승인 또는 반려 시 예외(notfound)
     *  - 이미 승인된 주문을 다시 승인 또는 반려 시 예외
     *  - 4XX, 5XX에러
     */

}