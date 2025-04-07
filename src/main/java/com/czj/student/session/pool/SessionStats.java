package com.czj.student.session.pool;

/**
 * 会话池统计信息
 */
public class SessionStats {
    private final int createdCount;    // 创建的会话总数
    private final int borrowedCount;   // 借出的会话总数
    private final int returnedCount;   // 归还的会话总数
    private final int discardedCount;  // 丢弃的会话总数
    private final int activeCount;     // 当前活跃会话数
    private final int idleCount;       // 当前空闲会话数
    
    public SessionStats(int createdCount, int borrowedCount, int returnedCount, 
                       int discardedCount, int activeCount, int idleCount) {
        this.createdCount = createdCount;
        this.borrowedCount = borrowedCount;
        this.returnedCount = returnedCount;
        this.discardedCount = discardedCount;
        this.activeCount = activeCount;
        this.idleCount = idleCount;
    }
    
    public int getCreatedCount() {
        return createdCount;
    }
    
    public int getBorrowedCount() {
        return borrowedCount;
    }
    
    public int getReturnedCount() {
        return returnedCount;
    }
    
    public int getDiscardedCount() {
        return discardedCount;
    }
    
    public int getActiveCount() {
        return activeCount;
    }
    
    public int getIdleCount() {
        return idleCount;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SessionStats{created=%d, borrowed=%d, returned=%d, discarded=%d, active=%d, idle=%d}",
            createdCount, borrowedCount, returnedCount, discardedCount, activeCount, idleCount
        );
    }
} 