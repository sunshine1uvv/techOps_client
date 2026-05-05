package org.example.tech_ops_gui.config;

import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.repository.UserRepository;
import org.example.tech_ops_gui.services.*;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;
import org.example.tech_ops_gui.utils.SessionManager;

public class AppContext {

    private static final ApiClient apiClient = new ApiClient();
    private static final WebSocketSyncClient webSocketClient = new WebSocketSyncClient();
    private static final SessionManager sessionManager = new SessionManager();

    private static final AuthService authService = new AuthService(apiClient);
    private static final UserService userService = new UserService(apiClient);
    private static final EquipmentService equipmentService = new EquipmentService(apiClient);
    private static final EquipmentTypeService equipmentTypeService = new EquipmentTypeService(apiClient);
    private static final AdminService adminService = new AdminService(apiClient);


    private static final UserRepository userRepository = new UserRepository(userService, webSocketClient);
    private static final EquipmentRepository equipmentRepository = new EquipmentRepository(equipmentService, webSocketClient);
    private static final EquipmentTypeRepository equipmentTypeRepository = new EquipmentTypeRepository(equipmentTypeService);

    private static final EquipmentBatchService equipmentBatchService = new EquipmentBatchService(equipmentRepository);

    public static void init() {
        System.out.println("Система инициализирована. Зависимости созданы.");
    }

    public static void onUserLogin() {
        webSocketClient.connect();
        equipmentTypeRepository.initData();
        userRepository.initData();
        equipmentRepository.initData();
    }

    public static ApiClient getApiClient() { return apiClient; }
    public static WebSocketSyncClient getWebSocketClient() { return webSocketClient; }
    public static SessionManager getSessionManager() { return sessionManager; }
    public static AuthService getAuthService() { return authService; }
    public static UserService getUserService() { return userService; }
    public static AdminService getAdminService() { return adminService; }
    public static EquipmentBatchService getEquipmentBatchService() { return equipmentBatchService; }
    public static EquipmentRepository getEquipmentRepository() { return equipmentRepository; }
    public static EquipmentTypeRepository getEquipmentTypeRepository() { return equipmentTypeRepository; }
    public static UserRepository getUserRepository() { return userRepository; }
    public static EquipmentService getEquipmentService() {return equipmentService;}
}