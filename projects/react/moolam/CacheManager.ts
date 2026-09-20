export async function withCache<T>(
    path: string, 
    fetchFn: () => Promise<T>, 
    onFreshData?: (data: T) => void
): Promise<T> {
    const cacheKey = `elvan_cache_${path.replace(/\//g, '_')}`;
    
    // 1. Start network fetch immediately
    const networkPromise = fetchFn().then((freshData) => {
        try {
            const newStr = JSON.stringify(freshData);
            const oldStr = localStorage.getItem(cacheKey);
            
            // If data changed (or it's the first time), update cache and notify
            if (newStr !== oldStr) {
                try {
                    localStorage.setItem(cacheKey, newStr);
                } catch (e) {
                    console.warn('[CacheManager] Error saving to cache', e);
                    // Silently ignore if quota is exceeded, just don't cache
                }
                if (onFreshData && oldStr !== null) {
                    // Only call onFreshData if we already returned the stale cache
                    // If oldStr is null, we are returning freshData immediately anyway.
                    onFreshData(freshData);
                }
            }
        } catch (e) {
            console.warn('[CacheManager] JSON stringify error', e);
        }
        return freshData;
    });

    // 2. Check cache
    try {
        const cachedStr = localStorage.getItem(cacheKey);
        if (cachedStr) {
            const cachedData = JSON.parse(cachedStr) as T;
            
            // Prevent uncaught in promise if network fails while we return cache
            networkPromise.catch(() => {});
            
            return cachedData;
        }
    } catch (e) {
        console.warn('[CacheManager] Error reading from cache', e);
    }

    // 3. If no cache, wait for network
    return networkPromise;
}

export function clearCache(path: string) {
    localStorage.removeItem(`elvan_cache_${path.replace(/\//g, '_')}`);
}

export function clearAllCache() {
    const keysToRemove: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('elvan_cache_')) {
            keysToRemove.push(key);
        }
    }
    keysToRemove.forEach(k => localStorage.removeItem(k));
}
