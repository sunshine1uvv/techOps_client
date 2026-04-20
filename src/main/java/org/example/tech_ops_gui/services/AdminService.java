package org.example.tech_ops_gui.services;

import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.RequestResponseDto;
import org.example.tech_ops_gui.dto.ReviewRequestDto;

import java.util.Arrays;
import java.util.List;

public class AdminService {
    private final ApiClient apiClient = ApiClient.getInstance();

    public List<RequestResponseDto> getAllRequests() {
        try {
            RequestResponseDto[] arr = apiClient.get("/requests", RequestResponseDto[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<RequestResponseDto> getReviewedRequests() {
        try {
            RequestResponseDto[] arr = apiClient.get("/requests/reviewed", RequestResponseDto[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<RequestResponseDto> getRequestsByStatus(String status) {
        try {
            RequestResponseDto[] arr = apiClient.get("/requests/status/" + status, RequestResponseDto[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reviewRequest(Long requestId, String status, String role){
        try {
            ReviewRequestDto dto = new ReviewRequestDto();
            dto.setStatus(status);
            dto.setRole(role);
            apiClient.postVoid("/requests/" + requestId + "/review", dto);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void restoreRequest(Long requestId)  {
        try {
            apiClient.postVoid("/requests/" + requestId + "/restore", null);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}