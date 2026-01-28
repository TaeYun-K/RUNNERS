"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { ChevronLeft, ChevronRight, Play, ChevronDown } from "lucide-react";

const HERO_IMAGES = ["/main.jpg", "/main2.jpg", "/main3.jpg"];
const AUTO_ROTATE_MS = 4500;
const TRANSITION_MS = 800;

interface HeroSectionProps {
  isLoggedIn: boolean;
}

function PhoneMockup({
  src,
  isActive,
}: {
  src: string;
  isActive: boolean;
}) {
  return (
    <div className="relative">
      {/* Glow Effect */}
      <div
        className={`absolute -inset-4 rounded-[3rem] bg-gradient-to-b from-blue-500/20 via-transparent to-transparent blur-2xl transition-opacity duration-500 ${
          isActive ? "opacity-100" : "opacity-40"
        }`}
      />

      {/* Phone Frame */}
      <div className="relative rounded-[2.5rem] border border-border/50 bg-secondary/30 p-3 shadow-2xl backdrop-blur-sm">
        <div className="relative overflow-hidden rounded-[2rem] bg-card">
          <div className="relative w-[270px] max-w-full aspect-[1080/2137] overflow-hidden rounded-[2rem] bg-background lg:w-[290px]">
            {/* App Screen Content (1080 x 2137) */}
            <img
              src={src}
              alt={isActive ? "RUNNERS 앱 화면" : ""}
              aria-hidden={!isActive}
              className="absolute inset-0 h-full w-full object-contain"
              loading={isActive ? "eager" : "lazy"}
              draggable={false}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export function HeroSection({ isLoggedIn }: HeroSectionProps) {
  const PLAY_STORE_URL =
    "https://github.com/TaeYun-K/RUNNERS_release/releases/tag/v1.0.1";

  // --- Carousel state (3-up mockups: prev/current/next) ---
  const [activeImageIndex, setActiveImageIndex] = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);
  const deckRef = useRef<HTMLDivElement | null>(null);
  const [deckWidth, setDeckWidth] = useState<number | null>(null);

  const timeoutIdRef = useRef<number | null>(null);
  const isAnimatingRef = useRef(false);

  const clampIndex = useCallback(
    (i: number) => (i + HERO_IMAGES.length) % HERO_IMAGES.length,
    [],
  );

  // preload
  useEffect(() => {
    const preloaded: HTMLImageElement[] = [];
    for (const src of HERO_IMAGES) {
      const img = new Image();
      img.src = src;
      preloaded.push(img);
    }
  }, []);

  // cleanup timer
  useEffect(() => {
    return () => {
      if (timeoutIdRef.current !== null) {
        window.clearTimeout(timeoutIdRef.current);
      }
    };
  }, []);

  // keep layout responsive to avoid horizontal overflow
  useEffect(() => {
    const el = deckRef.current;
    if (!el) return;

    const update = () => setDeckWidth(el.clientWidth);
    update();

    const ro = new ResizeObserver(update);
    ro.observe(el);

    return () => ro.disconnect();
  }, []);

  const go = useCallback(
    (dir: "next" | "prev", targetIndex?: number) => {
      if (isAnimatingRef.current) return;

      const current = activeImageIndex;

      // dot click: decide direction (one-step only)
      if (typeof targetIndex === "number") {
        if (targetIndex === current) return;
        dir = targetIndex > current ? "next" : "prev";
      }

      isAnimatingRef.current = true;
      setIsAnimating(true);
      setActiveImageIndex((prev) =>
        clampIndex(dir === "next" ? prev + 1 : prev - 1),
      );

      if (timeoutIdRef.current !== null) window.clearTimeout(timeoutIdRef.current);
      timeoutIdRef.current = window.setTimeout(() => {
        isAnimatingRef.current = false;
        setIsAnimating(false);
      }, TRANSITION_MS);
    },
    [activeImageIndex, clampIndex],
  );

  // auto rotate
  useEffect(() => {
    const intervalId = window.setInterval(() => {
      go("next");
    }, AUTO_ROTATE_MS);

    return () => window.clearInterval(intervalId);
  }, [go]);

  const prevIndex = clampIndex(activeImageIndex - 1);
  const nextIndex = clampIndex(activeImageIndex + 1);
  const sideOffsetPx =
    deckWidth === null
      ? 110
      : Math.min(140, Math.max(80, Math.round(deckWidth * 0.23)));
  const sideScale = deckWidth !== null && deckWidth < 420 ? 0.7 : 0.76;

  return (
    <section className="relative min-h-screen overflow-hidden bg-background">
      {/* Animated Background Elements */}
      <div className="absolute inset-0">
        <div className="absolute left-1/4 top-0 h-[600px] w-[600px] rounded-full bg-blue-500/10 blur-[120px]" />
        <div className="absolute bottom-0 right-1/4 h-[500px] w-[500px] rounded-full bg-cyan-500/10 blur-[100px]" />
      </div>

      {/* Grid Pattern Overlay */}
      <div
        className="absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage: `linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px),
                           linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)`,
          backgroundSize: "60px 60px",
        }}
      />

      <div className="relative z-10 mx-auto max-w-7xl px-6 py-20 lg:px-8 lg:py-32">
        <div className="grid grid-cols-1 items-center gap-16 lg:grid-cols-2">
          {/* Left Content */}
          <div className="text-center lg:text-left">
            {/* Badge */}
            <div className="mb-8 inline-flex items-center gap-2 rounded-full border border-border bg-secondary/50 px-4 py-2 backdrop-blur-sm">
              <span className="flex h-2 w-2 animate-pulse rounded-full bg-blue-500" />
              <span className="text-sm font-medium text-muted-foreground">
                10,000+ 러너들의 선택
              </span>
            </div>

            {/* Main Title */}
            <h1 className="text-pretty text-4xl font-black leading-[1.1] tracking-tight md:text-5xl lg:text-6xl">
              <span className="text-foreground">누적 KM로</span>
              <br />
              <span className="bg-gradient-to-r from-blue-600 via-blue-500 to-cyan-500 bg-clip-text text-transparent">
                성장하는
              </span>
              <br />
              <span className="text-foreground">러닝 커뮤니티</span>
            </h1>

            <p className="mx-auto mt-6 max-w-lg text-lg leading-relaxed text-muted-foreground lg:mx-0">
              {isLoggedIn
                ? "오늘도 당신의 한계를 넘어설 시간입니다."
                : "달린 거리만큼 레벨업하고, 기록을 공유하고, 함께 달리는 사람들과 연결되세요."}
            </p>

            {/* CTA Buttons */}
            <div className="mt-10 flex flex-col items-center gap-4 sm:flex-row lg:items-start">
              <a
                href={PLAY_STORE_URL}
                target="_blank"
                rel="noreferrer"
                className="group inline-flex w-full items-center justify-center gap-3 rounded-full bg-blue-600 px-8 py-4 text-base font-bold text-white transition-all hover:scale-[1.02] hover:bg-blue-700 hover:shadow-lg hover:shadow-blue-500/30 sm:w-auto"
              >
                <Play className="h-5 w-5 fill-current" />
                Google Play에서 다운로드 (출시 준비 중)
              </a>

              <Link
                to={isLoggedIn ? "/dashboard" : "/login"}
                className="group inline-flex w-full items-center justify-center gap-2 rounded-full border border-border bg-secondary/50 px-8 py-4 text-base font-semibold text-foreground backdrop-blur-sm transition-all hover:border-accent/50 hover:bg-secondary sm:w-auto"
              >
                {isLoggedIn ? "대시보드" : "시작하기"}
                <ChevronRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
              </Link>
            </div>
          </div>

          {/* Right Content - Phone Mockup (SLIDE) */}
          <div className="relative flex justify-center lg:justify-end">
            <div className="relative">
              {/* 3-up mockups deck */}
              <div
                ref={deckRef}
                className="relative h-[600px] w-[420px] max-w-[calc(100vw-3rem)] overflow-hidden lg:h-[640px] lg:w-[520px]"
                style={{ perspective: "1200px" }}
              >
              {HERO_IMAGES.map((src, imageIndex) => {
                const position =
                  imageIndex === activeImageIndex
                    ? ("center" as const)
                    : imageIndex === prevIndex
                      ? ("left" as const)
                      : imageIndex === nextIndex
                        ? ("right" as const)
                        : ("hidden" as const) // length>3 확장 대비

                const isActive = position === "center"
                if (position === "hidden") return null

                const targetTransform =
                  position === "center"
                    ? "translate(-50%, -50%) translateX(0px) scale(1)"
                    : position === "left"
                      ? `translate(-50%, -50%) translateX(-${sideOffsetPx}px) scale(${sideScale})`
                      : `translate(-50%, -50%) translateX(${sideOffsetPx}px) scale(${sideScale})`

                return (
                  <button
                    key={imageIndex}
                    type="button"
                    aria-label={
                      position === "left"
                        ? "이전 화면 보기"
                        : position === "right"
                          ? "다음 화면 보기"
                          : "현재 화면"
                    }
                    onClick={() => {
                      if (position === "left") go("prev")
                      if (position === "right") go("next")
                    }}
                    disabled={isAnimating || isActive}
                    className="absolute left-1/2 top-1/2 outline-none disabled:cursor-default"
                    style={{
                      transform: targetTransform,
                      transition: `transform ${TRANSITION_MS}ms cubic-bezier(0.4, 0, 0.2, 1), opacity ${TRANSITION_MS}ms cubic-bezier(0.4, 0, 0.2, 1)`,
                      opacity: isActive ? 1 : 0.45,
                      zIndex: isActive ? 30 : 10,
                      willChange: "transform, opacity",
                    }}
                  >
                    <div className={isActive ? "" : "brightness-90"}>
                      <PhoneMockup src={src} isActive={isActive} />
                    </div>
                    {!isActive && (
                      <div className="pointer-events-none absolute inset-0 rounded-[2.5rem] bg-black/15" />
                    )}
                  </button>
                )
              })}
                {/* Prev/Next buttons */}
                <button
                  type="button"
                  aria-label="이전 화면"
                  onClick={() => go("prev")}
                  disabled={isAnimating}
                  className="absolute left-2 top-1/2 z-40 -translate-y-1/2 rounded-full border border-border bg-secondary/60 p-2 backdrop-blur-sm transition hover:bg-secondary disabled:opacity-50"
                >
                  <ChevronLeft className="h-4 w-4" />
                </button>

                <button
                  type="button"
                  aria-label="다음 화면"
                  onClick={() => go("next")}
                  disabled={isAnimating}
                  className="absolute right-2 top-1/2 z-40 -translate-y-1/2 rounded-full border border-border bg-secondary/60 p-2 backdrop-blur-sm transition hover:bg-secondary disabled:opacity-50"
                >
                  <ChevronRight className="h-4 w-4" />
                </button>
              </div>

              {/* Dot Indicator Controls */}
              <div className="absolute -bottom-12 left-1/2 flex -translate-x-1/2 items-center gap-3">
                {HERO_IMAGES.map((_, index) => (
                  <button
                    key={index}
                    type="button"
                    aria-label={`화면 ${index + 1}로 이동`}
                    onClick={() => go(index > activeImageIndex ? "next" : "prev", index)}
                    disabled={isAnimating}
                    className={`group relative transition-all duration-300 ${
                      index === activeImageIndex
                        ? "scale-100"
                        : "scale-90 hover:scale-100"
                    } disabled:opacity-60`}
                  >
                    <span
                      className={`block h-3 w-3 rounded-full border-2 transition-all duration-300 ${
                        index === activeImageIndex
                          ? "border-blue-500 bg-blue-500"
                          : "border-border bg-secondary/50 hover:border-blue-400 hover:bg-blue-400/30"
                      }`}
                    />
                    {index === activeImageIndex && (
                      <span className="absolute inset-0 animate-ping rounded-full bg-blue-500/40" />
                    )}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Scroll Indicator */}
        <div className="mt-20 flex justify-center">
          <button
            type="button"
            className="flex flex-col items-center gap-2 text-muted-foreground transition-colors hover:text-foreground"
            onClick={() => {
              const el = document.getElementById("features");
              el?.scrollIntoView({ behavior: "smooth", block: "start" });
            }}
          >
            <span className="text-sm">더 알아보기</span>
            <div className="flex h-12 w-12 items-center justify-center rounded-full border border-border bg-secondary/30 backdrop-blur-sm">
              <ChevronDown className="h-6 w-6 motion-safe:animate-bounce" />
            </div>
          </button>
        </div>
      </div>
    </section>
  );
}
