import { useEffect, useRef, useState, useCallback } from 'react';

interface UsePinchZoomProps {
    minScale?: number;
    maxScale?: number;
}

export function usePinchZoom({ minScale = 1, maxScale = 4 }: UsePinchZoomProps = {}) {
    const [wrapperNode, setWrapperNode] = useState<HTMLDivElement | null>(null);
    const contentRef = useRef<HTMLDivElement>(null);

    const scaleRef = useRef(minScale);
    const unresistedScale = useRef(minScale);
    const offsetRef = useRef({ x: 0, y: 0 });
    const transformPending = useRef(false);

    const [scale, setScale] = useState(minScale);
    const [isDragging, setIsDragging] = useState(false);

    const touchStartX = useRef(0);
    const touchStartY = useRef(0);
    const lastPinchDistance = useRef(0);

    const applyTransforms = () => {
        if (!transformPending.current) return;
        if (contentRef.current) {
            contentRef.current.style.transform = `translate3d(${offsetRef.current.x}px, ${offsetRef.current.y}px, 0) scale(${scaleRef.current})`;
        }
        transformPending.current = false;
    };

    const requestTransform = () => {
        if (!transformPending.current) {
            transformPending.current = true;
            requestAnimationFrame(applyTransforms);
        }
    };

    const handleTouchStart = useCallback((e: TouchEvent) => {
        if (contentRef.current) {
            // Remove transition when user touches screen
            contentRef.current.style.transition = 'none';
        }

        if (e.touches.length === 2) {
            const dist = Math.hypot(
                e.touches[0].pageX - e.touches[1].pageX,
                e.touches[0].pageY - e.touches[1].pageY
            );
            lastPinchDistance.current = dist;
            unresistedScale.current = scaleRef.current;
            setIsDragging(true);
        } else if (e.touches.length === 1 && scaleRef.current > minScale + 0.01) {
            touchStartX.current = e.touches[0].clientX;
            touchStartY.current = e.touches[0].clientY;
            setIsDragging(true);
        }
    }, [minScale]);

    const handleTouchMove = useCallback((e: TouchEvent) => {
        if (e.touches.length === 2) {
            if (e.cancelable) e.preventDefault();
            const dist = Math.hypot(
                e.touches[0].pageX - e.touches[1].pageX,
                e.touches[0].pageY - e.touches[1].pageY
            );
            if (lastPinchDistance.current > 0) {
                const delta = dist / lastPinchDistance.current;
                unresistedScale.current = unresistedScale.current * delta;
                
                let nextScale = unresistedScale.current;

                // Add minimal resistance when zooming below minScale
                if (nextScale < minScale) {
                    const overflow = minScale - nextScale;
                    // Let it shrink at 80% speed (very loose, natural feel) instead of 40%
                    nextScale = minScale - (overflow * 0.8); 
                }

                // Lower the hard limit so they can pinch it down to 20% size
                nextScale = Math.min(Math.max(nextScale, minScale * 0.2), maxScale);
                
                scaleRef.current = nextScale;

                // Smoothly pull back to center if zooming out
                if (nextScale <= minScale + 0.01) {
                    offsetRef.current.x *= 0.8;
                    offsetRef.current.y *= 0.8;
                }

                requestTransform();
            }
            lastPinchDistance.current = dist;
            return;
        }

        if (!isDragging) return;

        if (scaleRef.current > minScale + 0.01) {
            if (e.cancelable) e.preventDefault();
            const currentX = e.touches[0].clientX;
            const currentY = e.touches[0].clientY;
            const dx = currentX - touchStartX.current;
            const dy = currentY - touchStartY.current;

            offsetRef.current.x += dx;
            offsetRef.current.y += dy;
            touchStartX.current = currentX;
            touchStartY.current = currentY;
            requestTransform();
        }
    }, [isDragging, minScale, maxScale]);

    const handleTouchEnd = useCallback((e: TouchEvent) => {
        if (e.touches.length === 0) {
            lastPinchDistance.current = 0;
            setIsDragging(false);

            let needsTransition = false;

            // Snap back to minScale (Spring effect)
            if (scaleRef.current < minScale) {
                scaleRef.current = minScale;
                unresistedScale.current = minScale;
                offsetRef.current = { x: 0, y: 0 };
                needsTransition = true;
            } else if (scaleRef.current <= minScale + 0.05) {
                // If extremely close to 1, just snap back
                scaleRef.current = minScale;
                unresistedScale.current = minScale;
                offsetRef.current = { x: 0, y: 0 };
                needsTransition = true;
            } else if (contentRef.current && wrapperNode) {
                // Out of bounds check (Pan snapping)
                const content = contentRef.current.getBoundingClientRect();
                const wrapper = wrapperNode.getBoundingClientRect();
                
                let fixX = 0;
                let fixY = 0;

                // X bounds
                if (content.width <= wrapper.width) {
                    // If scaled content is narrower than wrapper, just center it (offset X = 0)
                    offsetRef.current.x = 0;
                    needsTransition = true;
                } else {
                    if (content.left > wrapper.left) fixX = wrapper.left - content.left;
                    if (content.right < wrapper.right) fixX = wrapper.right - content.right;
                }

                // Y bounds
                const viewportHeight = window.innerHeight;
                const topBound = Math.max(0, wrapper.top);
                
                if (content.height <= viewportHeight) {
                    offsetRef.current.y = 0;
                    needsTransition = true;
                } else {
                    if (content.top > topBound) fixY = topBound - content.top;
                    if (content.bottom < viewportHeight) fixY = viewportHeight - content.bottom;
                }

                if (fixX !== 0 || fixY !== 0) {
                    // convert screen pixels to unscaled offset
                    offsetRef.current.x += fixX / scaleRef.current;
                    offsetRef.current.y += fixY / scaleRef.current;
                    needsTransition = true;
                }
            }

            if (needsTransition && contentRef.current) {
                contentRef.current.style.transition = 'transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)'; // Bouncy spring
                requestTransform();
            }
            
            setScale(scaleRef.current);
        } else if (e.touches.length === 1) {
            lastPinchDistance.current = 0;
            touchStartX.current = e.touches[0].clientX;
            touchStartY.current = e.touches[0].clientY;
        }
    }, [minScale]);

    useEffect(() => {
        if (!wrapperNode) return;

        wrapperNode.addEventListener('touchstart', handleTouchStart, { passive: false });
        wrapperNode.addEventListener('touchmove', handleTouchMove, { passive: false });
        wrapperNode.addEventListener('touchend', handleTouchEnd, { passive: false });
        wrapperNode.addEventListener('touchcancel', handleTouchEnd, { passive: false });

        return () => {
            wrapperNode.removeEventListener('touchstart', handleTouchStart);
            wrapperNode.removeEventListener('touchmove', handleTouchMove);
            wrapperNode.removeEventListener('touchend', handleTouchEnd);
            wrapperNode.removeEventListener('touchcancel', handleTouchEnd);
        };
    }, [wrapperNode, handleTouchStart, handleTouchMove, handleTouchEnd]);

    return { wrapperRef: setWrapperNode, contentRef, scale };
}
