"use client";

import { Link } from "react-router-dom";
import {
  ChevronRight,
  Star,
  Play,
  TrendingUp,
  Flame,
  MapPin,
} from "lucide-react";

interface HeroSectionProps {
  isLoggedIn: boolean;
}

export function HeroSection({ isLoggedIn }: HeroSectionProps) {
  const PLAY_STORE_URL =
    "https://github.com/TaeYun-K/RUNNERS_release/releases/tag/v1.0.1";

  return (
    <section className="relative min-h-screen overflow-hidden bg-background">
      {/* Animated Background Elements */}
      <div className="absolute inset-0">
        <div className="absolute top-0 left-1/4 h-[600px] w-[600px] rounded-full bg-blue-500/10 blur-[120px]" />
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
                Google Play에서 다운로드
              </a>

              <Link
                to={isLoggedIn ? "/dashboard" : "/login"}
                className="group inline-flex w-full items-center justify-center gap-2 rounded-full border border-border bg-secondary/50 px-8 py-4 text-base font-semibold text-foreground backdrop-blur-sm transition-all hover:border-accent/50 hover:bg-secondary sm:w-auto"
              >
                {isLoggedIn ? "대시보드" : "시작하기"}
                <ChevronRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
              </Link>
            </div>

            {/* Stats Row */}
            <div className="mt-12 flex items-center justify-center gap-8 lg:justify-start">
              <div className="flex items-center gap-2">
                <div className="flex -space-x-2">
                  {[1, 2, 3, 4].map((i) => (
                    <div
                      key={i}
                      className="h-8 w-8 rounded-full border-2 border-background bg-gradient-to-br from-secondary to-muted"
                    />
                  ))}
                </div>
                <span className="text-sm text-muted-foreground">
                  +10K 러너
                </span>
              </div>

              <div className="h-6 w-px bg-border" />

              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1">
                  {[1, 2, 3, 4, 5].map((i) => (
                    <Star
                      key={i}
                      className="h-4 w-4 fill-blue-500 text-blue-500"
                    />
                  ))}
                </div>
                <span className="text-sm text-muted-foreground">4.8 평점</span>
              </div>
            </div>
          </div>

          {/* Right Content - Phone Mockup */}
          <div className="relative flex justify-center lg:justify-end">
            <div className="relative">
              {/* Glow Effect */}
              <div className="absolute -inset-4 rounded-[3rem] bg-gradient-to-b from-blue-500/20 via-transparent to-transparent blur-2xl" />

              {/* Phone Frame */}
              <div className="relative rounded-[2.5rem] border border-border/50 bg-secondary/30 p-3 shadow-2xl backdrop-blur-sm">
                <div className="relative overflow-hidden rounded-[2rem] bg-card">
                  {/* Phone Notch */}
                  <div className="absolute left-1/2 top-2 z-10 h-6 w-24 -translate-x-1/2 rounded-full bg-background" />

                  {/* App Screen Content */}
                  <div className="h-[580px] w-[280px] overflow-hidden bg-gradient-to-b from-card to-secondary/50 lg:h-[640px] lg:w-[300px]">
                    {/* Status Bar */}
                    <div className="flex items-center justify-between px-6 pt-10 text-xs text-muted-foreground">
                      <span>9:41</span>
                      <div className="flex items-center gap-1">
                        <div className="h-2 w-4 rounded-sm bg-muted-foreground/50" />
                        <div className="h-2 w-2 rounded-full bg-muted-foreground/50" />
                      </div>
                    </div>

                    {/* App Header */}
                    <div className="px-6 pt-8">
                      <p className="text-xs font-medium uppercase tracking-widest text-blue-600">
                        RUNNERS
                      </p>
                      <h3 className="mt-2 text-2xl font-bold text-foreground">
                        안녕하세요, 러너!
                      </h3>
                    </div>

                    {/* Stats Cards */}
                    <div className="mt-6 space-y-4 px-6">
                      <div className="rounded-2xl border border-border bg-secondary/50 p-4">
                        <div className="flex items-center justify-between">
                          <span className="text-sm text-muted-foreground">
                            이번 주 달린 거리
                          </span>
                          <TrendingUp className="h-4 w-4 text-blue-500" />
                        </div>
                        <p className="mt-2 text-3xl font-black text-foreground">
                          24.8
                          <span className="ml-1 text-lg font-normal text-muted-foreground">
                            km
                          </span>
                        </p>
                        <div className="mt-3 h-2 overflow-hidden rounded-full bg-secondary">
                          <div
                            className="h-full rounded-full bg-gradient-to-r from-blue-600 to-cyan-500"
                            style={{ width: "75%" }}
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3">
                        <div className="rounded-2xl border border-border bg-secondary/50 p-4">
                          <Flame className="h-5 w-5 text-blue-500" />
                          <p className="mt-2 text-2xl font-bold text-foreground">
                            12
                          </p>
                          <p className="text-xs text-muted-foreground">
                            연속 러닝
                          </p>
                        </div>
                        <div className="rounded-2xl border border-border bg-secondary/50 p-4">
                          <Star className="h-5 w-5 text-blue-500" />
                          <p className="mt-2 text-2xl font-bold text-foreground">
                            Lv.8
                          </p>
                          <p className="text-xs text-muted-foreground">
                            골드 러너
                          </p>
                        </div>
                      </div>

                      {/* Recent Activity */}
                      <div className="rounded-2xl border border-border bg-secondary/50 p-4">
                        <div className="flex items-center gap-3">
                          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-500/20">
                            <MapPin className="h-5 w-5 text-blue-500" />
                          </div>
                          <div className="flex-1">
                            <p className="font-semibold text-foreground">
                              한강 러닝 코스
                            </p>
                            <p className="text-xs text-muted-foreground">
                              오늘 오전 6:30
                            </p>
                          </div>
                          <span className="rounded-full bg-blue-500/20 px-3 py-1 text-sm font-bold text-blue-600">
                            5.2km
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Floating Badge - Top Right */}
              <div className="absolute -right-4 -top-4 rounded-2xl border border-border bg-card/90 p-4 shadow-xl backdrop-blur-sm lg:-right-8">
                <div className="flex items-center gap-3">
<div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-500/20">
                  <TrendingUp className="h-5 w-5 text-blue-500" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">이번 달</p>
                    <p className="text-lg font-bold text-foreground">
                      +156.8 km
                    </p>
                  </div>
                </div>
              </div>

              {/* Floating Badge - Bottom Left */}
              <div className="absolute -bottom-4 -left-4 rounded-2xl border border-border bg-card/90 p-4 shadow-xl backdrop-blur-sm lg:-left-8">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-500/20">
                    <Flame className="h-5 w-5 text-green-500" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">연속 기록</p>
                    <p className="text-lg font-bold text-foreground">12일</p>
                  </div>
                </div>
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
            <div className="flex h-10 w-6 items-start justify-center rounded-full border border-border p-1">
              <div className="h-2 w-1 animate-bounce rounded-full bg-muted-foreground" />
            </div>
          </button>
        </div>
      </div>
    </section>
  );
}
