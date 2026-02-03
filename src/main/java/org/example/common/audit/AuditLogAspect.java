package org.example.common.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.security.SecurityUtil;
import org.example.system.audit.AuditLogEntity;
import org.example.system.audit.AuditLogMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;

    public AuditLogAspect(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Integer status = 1; // Success
        String errorMsg = null;

        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            status = 2; // Fail
            errorMsg = e.getMessage();
            throw e;
        } finally {
            saveLog(point, auditLog, status, errorMsg, System.currentTimeMillis() - start);
        }
    }

    private void saveLog(ProceedingJoinPoint point, AuditLog auditLog, Integer status, String errorMsg, long cost) {
        try {
            AuditLogEntity e = new AuditLogEntity();
            e.setAction(auditLog.action());
            e.setResource(auditLog.resource());
            e.setCostMs((int) cost);
            e.setResult(status);
            if (errorMsg != null) {
                e.setDetail(errorMsg.length() > 200 ? errorMsg.substring(0, 200) : errorMsg);
            }

            try {
                e.setUserId(SecurityUtil.currentUserId());
            } catch (Exception ex) {
                // Ignore if no user (e.g. login)
            }

            // Getting IP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                e.setIp(request.getRemoteAddr());
                String ua = request.getHeader("User-Agent");
                e.setUserAgent(ua != null && ua.length() > 255 ? ua.substring(0, 255) : ua);
            }

            auditLogMapper.insert(e);
        } catch (Exception ex) {
            // Log logging error?
            System.err.println("Failed to save audit log: " + ex.getMessage());
        }
    }
}
