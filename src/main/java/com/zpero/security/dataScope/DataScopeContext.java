package com.zpero.security.dataScope;

import lombok.Data;

@Data
public class DataScopeContext {
    private boolean all;
    private Long collegeId;
    private Long counselorId;

    public boolean hasCollegeScope() {
        return collegeId!=null;
    }
    public boolean hasCounselorScope() {
        return counselorId!=null;
    }
}
