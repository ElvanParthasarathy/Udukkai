// @ts-nocheck
// Print-bundle stand-in for moolam/hooks/usePinchZoom.ts. React's custom pinch handler is replaced
// by the host WebView's native zoom (smooth, focal-point, momentum like a photo gallery).
export function usePinchZoom() {
  return { wrapperRef: () => {}, contentRef: { current: null }, scale: 1 };
}
