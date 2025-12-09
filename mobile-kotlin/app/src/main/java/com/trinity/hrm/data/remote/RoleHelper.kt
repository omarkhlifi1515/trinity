package com.trinity.hrm.data.remote

/**
 * Role-based access control helpers
 */
object RoleHelper {
    fun isAdmin(user: JsonBinClient.User?): Boolean {
        return user?.role == JsonBinClient.UserRole.ADMIN || 
               user?.email?.lowercase() == "admin@gmail.com"
    }
    
    fun isDepartmentHead(user: JsonBinClient.User?): Boolean {
        return user?.role == JsonBinClient.UserRole.DEPARTMENT_HEAD
    }
    
    fun isEmployee(user: JsonBinClient.User?): Boolean {
        return !isAdmin(user) && !isDepartmentHead(user)
    }
    
    fun canAddEmployees(user: JsonBinClient.User?): Boolean {
        return isAdmin(user)
    }
    
    fun canAddTasks(user: JsonBinClient.User?): Boolean {
        return isAdmin(user) || isDepartmentHead(user)
    }
    
    fun canManageDepartment(user: JsonBinClient.User?): Boolean {
        return isAdmin(user) || isDepartmentHead(user)
    }
    
    fun getUserRole(user: JsonBinClient.User?): JsonBinClient.UserRole {
        if (user == null) return JsonBinClient.UserRole.EMPLOYEE
        if (isAdmin(user)) return JsonBinClient.UserRole.ADMIN
        if (isDepartmentHead(user)) return JsonBinClient.UserRole.DEPARTMENT_HEAD
        return JsonBinClient.UserRole.EMPLOYEE
    }
}

