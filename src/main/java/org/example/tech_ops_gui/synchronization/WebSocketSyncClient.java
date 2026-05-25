package org.example.tech_ops_gui.synchronization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WebSocketSyncClient {

    private static WebSocketSyncClient instance;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private final String serverUrl = "http://localhost:8080/ws-tech-ops";

    // Планировщик для реконнектов
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean isConnecting = false;

    // Хранилище подписок для их восстановления после переподключения
    private final List<Consumer<UserSyncMessage>> userSubscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<RequestResponseSyncMessage>> requestSubscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<EquipmentSyncMessage>> equipmentSubscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<EquipmentTypeSyncMessage>> equipmentTypeSubscribers = new CopyOnWriteArrayList<>();
    private final List<Consumer<DepartmentSyncMessage>> departmentSubscribers = new CopyOnWriteArrayList<>();
    // Приватный конструктор (Singleton)
    public WebSocketSyncClient() {
        initStompClient();
    }

    private void initStompClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);

        stompClient.setInboundMessageSizeLimit(1024 * 1024);
        stompClient.setTaskScheduler(new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(messageConverter);
    }

    public synchronized void connect() {
        if (isConnecting || (session != null && session.isConnected())) {
            return;
        }
        isConnecting = true;
        System.out.println("Попытка подключения к WebSocket...");

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession s, StompHeaders connectedHeaders) {
                System.out.println("WebSocket подключен!");
                session = s;
                isConnecting = false;

                // Восстанавливаем все подписки после успешного (пере)подключения
                restoreSubscriptions();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("--- ТРАНСПОРТНАЯ ОШИБКА ---");
                exception.printStackTrace(); // Это покажет полный StackTrace в консоли

                if (exception.getMessage() != null && exception.getMessage().contains("limit")) {
                    System.err.println("ПОДТВЕРЖДЕНО: Превышен лимит размера сообщения!");
                }

                isConnecting = false;
                scheduleReconnect();
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("--- STOMP ИСКЛЮЧЕНИЕ ---");
                System.err.println("Команда: " + command);
                System.err.println("Заголовки: " + headers);
                if (payload != null) {
                    System.err.println("Payload length: " + payload.length);
                }
                exception.printStackTrace();
            }
        };

        try {
            stompClient.connect(serverUrl, sessionHandler);
        } catch (Exception e) {
            System.err.println("Не удалось инициировать подключение: " + e.getMessage());
            isConnecting = false;
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    // --- Методы подписки (теперь возвращают Consumer, чтобы контроллер мог отписаться) ---

    public Consumer<EquipmentSyncMessage> subscribeEquipment(Consumer<EquipmentSyncMessage> onMessage) {
        equipmentSubscribers.add(onMessage);
        if (session != null && session.isConnected()) {
            doSubscribeEquipment(onMessage);
        }
        return onMessage; // Возвращаем ссылку на коллбек для отписки
    }

    public Consumer<UserSyncMessage> subscribeUsers(Consumer<UserSyncMessage> onMessage) {
        userSubscribers.add(onMessage);
        if (session != null && session.isConnected()) {
            doSubscribeUsers(onMessage);
        }
        return onMessage;
    }

    public Consumer<RequestResponseSyncMessage> subscribeRequests(Consumer<RequestResponseSyncMessage> onMessage) {
        requestSubscribers.add(onMessage);
        if (session != null && session.isConnected()) {
            doSubscribeRequests(onMessage);
        }
        return onMessage;
    }

    public Consumer<EquipmentTypeSyncMessage> subscribeEquipmentTypes(Consumer<EquipmentTypeSyncMessage> onMessage) {
        equipmentTypeSubscribers.add(onMessage);
        if (session != null && session.isConnected()) {
            doSubscribeEquipmentTypes(onMessage);
        }
        return onMessage;
    }

    public Consumer<DepartmentSyncMessage> subscribeDepartments(Consumer<DepartmentSyncMessage> onMessage) {
        departmentSubscribers.add(onMessage);
        if (session != null && session.isConnected()) {
            doSubscribeDepartments(onMessage);
        }
        return onMessage;
    }

    // --- Методы отписки (Вызываются при закрытии окон) ---

    public void unsubscribeEquipment(Consumer<EquipmentSyncMessage> onMessage) {
        equipmentSubscribers.remove(onMessage);
    }

    public void unsubscribeUsers(Consumer<UserSyncMessage> onMessage) {
        userSubscribers.remove(onMessage);
    }

    public void unsubscribeRequests(Consumer<RequestResponseSyncMessage> onMessage) {
        requestSubscribers.remove(onMessage);
    }

    public void unsubscribeEquipmentTypes(Consumer<EquipmentTypeSyncMessage> onMessage) {
        equipmentTypeSubscribers.remove(onMessage);
    }

    public void unsubscribeDepartments(Consumer<DepartmentSyncMessage> onMessage) {
        departmentSubscribers.remove(onMessage);
    }

    // --- Внутренняя логика STOMP подписок ---

    private void restoreSubscriptions() {
        equipmentSubscribers.forEach(this::doSubscribeEquipment);
        userSubscribers.forEach(this::doSubscribeUsers);
        requestSubscribers.forEach(this::doSubscribeRequests);
        equipmentTypeSubscribers.forEach(this::doSubscribeEquipmentTypes);
        departmentSubscribers.forEach(this::doSubscribeDepartments);
    }

    private void doSubscribeEquipment(Consumer<EquipmentSyncMessage> onMessage) {
        session.subscribe("/topic/equipment-updates", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return EquipmentSyncMessage.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { onMessage.accept((EquipmentSyncMessage) payload); }
        });
    }

    private void doSubscribeUsers(Consumer<UserSyncMessage> onMessage) {
        session.subscribe("/topic/users", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return UserSyncMessage.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { onMessage.accept((UserSyncMessage) payload); }
        });
    }

    private void doSubscribeRequests(Consumer<RequestResponseSyncMessage> onMessage) {
        session.subscribe("/topic/requests", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return RequestResponseSyncMessage.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { onMessage.accept((RequestResponseSyncMessage) payload); }
        });
    }

    private void doSubscribeEquipmentTypes(Consumer<EquipmentTypeSyncMessage> onMessage) {
        session.subscribe("/topic/equipment-types", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return EquipmentTypeSyncMessage.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { onMessage.accept((EquipmentTypeSyncMessage) payload); }
        });
    }

    private void doSubscribeDepartments(Consumer<DepartmentSyncMessage> onMessage) {
        session.subscribe("/topic/departments", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return DepartmentSyncMessage.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { onMessage.accept((DepartmentSyncMessage) payload); }
        });
    }

    // Вызывать при полном закрытии приложения (например, в Application.stop())
    public void shutdown() {
        scheduler.shutdownNow();
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }
}