<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="notifications-panel">
    <div class="notifications-header">
        <h3><i class="fas fa-bell"></i> Notifications</h3>
        <button onclick="markAllNotificationsRead()" class="text-btn">Mark all read</button>
    </div>
    <div id="notificationList" class="notification-list">
        <div class="notification-empty">Loading notifications...</div>
    </div>
</div>
