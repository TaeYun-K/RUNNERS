"use client";

import { BarChart3, Users, Zap, Activity } from "lucide-react";

const features = [
  {
    icon: Activity,
    title: "러닝 기록 자동 연동",
    description:
      "헬스 앱과 연동해 거리, 시간, 페이스를 자동으로 기록합니다. 수동 입력 없이 러닝에만 집중하세요.",
    color: "text-purple-500",
    bgColor: "bg-purple-500/10",
  },
  {
    icon: Zap,
    title: "누적 러닝 지표",
    description:
      "지금까지 달린 총 거리와 소모 칼로리를 한눈에 확인하세요. 숫자로 보는 나의 러닝 여정.",
    color: "text-purple-500",
    bgColor: "bg-purple-500/10",
  },
  {
    icon: BarChart3,
    title: "자동 대시보드",
    description:
      "러닝 기록을 한눈에. 자동으로 생성되는 대시보드로 나의 성장과 패턴을 분석하세요.",
    color: "text-blue-500",
    bgColor: "bg-blue-500/10",
  },
  {
    icon: Users,
    title: "러너스 커뮤니티",
    description:
      "혼자가 아닌 함께. 전국의 러너들과 기록을 공유하고 응원을 주고받으세요.",
    color: "text-green-500",
    bgColor: "bg-green-500/10",
  },
];

export function FeaturesSection() {
  return (
    <section
      id="features"
      className="relative overflow-hidden bg-secondary/30 py-24 lg:py-32"
    >
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-[0.02]">
        <div
          className="absolute inset-0"
          style={{
            backgroundImage: `radial-gradient(circle at 1px 1px, currentColor 1px, transparent 1px)`,
            backgroundSize: "40px 40px",
          }}
        />
      </div>

      <div className="relative mx-auto max-w-7xl px-6 lg:px-8">
        {/* Section Header */}
        <div className="mx-auto max-w-3xl text-center">
          <p className="mt-4 text-balance text-3xl font-black leading-tight md:text-4xl lg:text-5xl">
            왜 러너들은{" "}
            <span className="inline-block">
            <span className="bg-gradient-to-r from-blue-600 via-blue-500 to-cyan-500 bg-clip-text text-transparent">
              RUNNERS
            </span>
            {" "} 를 선택할까요?
            </span>
          </p>
          <p className="mt-6 text-lg leading-relaxed text-muted-foreground">
            단순한 기록 앱을 넘어, 진짜 성장을 경험할 수 있는 러닝 플랫폼
          </p>
        </div>

        {/* Features Grid */}
        <div className="mt-16 grid grid-cols-1 gap-6 justify-items-stretch sm:grid-cols-2 lg:grid-cols-2">
          {features.map((feature) => (
            <div
              key={feature.title}
              className="group relative w-full min-w-0 rounded-3xl border border-border bg-card/50 p-8 backdrop-blur-sm transition-all duration-300 hover:border-blue-500/30 hover:bg-card/80 hover:shadow-lg hover:shadow-blue-500/5"
            >
              {/* Icon */}
              <div
                className={`inline-flex h-14 w-14 items-center justify-center rounded-2xl ${feature.bgColor}`}
              >
                <feature.icon className={`h-7 w-7 ${feature.color}`} />
              </div>

              {/* Content */}
              <div>
                <h3 className="mt-6 text-xl font-bold text-foreground">
                  {feature.title}
                </h3>
                <p className="mt-3 leading-relaxed text-muted-foreground">
                  {feature.description}
                </p>
              </div>

              {/* Hover Gradient */}
              <div className="absolute inset-0 -z-10 rounded-3xl bg-gradient-to-br from-blue-500/5 via-transparent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
            </div>
          ))}       
        </div>
      </div>
    </section>
  );
}
