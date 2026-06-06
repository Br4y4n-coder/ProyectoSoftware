import { useEffect, useState } from "react";

export function useMockData<T>(data: T, delayMs = 400): { data: T | null; loading: boolean } {
  const [result, setResult] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const timer = window.setTimeout(() => {
      setResult(data);
      setLoading(false);
    }, delayMs);
    return () => window.clearTimeout(timer);
  }, [data, delayMs]);

  return { data: result, loading };
}
