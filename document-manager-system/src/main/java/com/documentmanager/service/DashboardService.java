package com.documentmanager.service;

import com.documentmanager.dao.ActivityLogDAO;
import com.documentmanager.dao.DashboardDAO;
import com.documentmanager.model.ActivityLog;
import com.documentmanager.model.DashboardStats;
import com.documentmanager.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public DashboardStats stats() throws SQLException {
        return dashboardDAO.getStats(SessionManager.userId());
    }

    public List<ActivityLog> recentActivities() throws SQLException {
        return activityLogDAO.findRecentByUser(SessionManager.userId(), 20);
    }
}
