package com.baofeng.aone.memory;

import com.baofeng.aone.AndroidCallback;

public interface MemoryCallback extends AndroidCallback{
    public void onMemoryClean(int clearProcessCount, Long clearMemory);
}
