import { useEffect } from 'react';

/** Re-run `callback` whenever any form dispatches the global data-changed event. */
export function useDataChanged(callback) {
  useEffect(() => {
    window.addEventListener('data-changed', callback);
    return () => window.removeEventListener('data-changed', callback);
  }, [callback]);
}
